/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.JobChatUserService;
import org.tctalent.server.service.db.NotificationService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final AuthService authService;
    private final CandidateOpportunityRepository candidateOpportunityRepository;
    private final ChatPostService chatPostService;
    private final EmailHelper emailHelper;
    private final JobChatService jobChatService;
    private final JobChatUserService jobChatUserService;
    private final PartnerService partnerService;
    private final UserService userService;

    //One minute past Midnight GMT
    @Scheduled(cron = "0 1 0 * * ?", zone = "GMT")
    @SchedulerLock(name = "NotificationService_scheduledNotifyOfChatsWithNewPosts", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
    @Transactional
    public void scheduledNotifyOfChatsWithNewPosts() {
        notifyUsersOfChatsWithNewPosts();
    }

    @Override
    public void notifyUsersOfChatsWithNewPosts() {
        Map<Long, Set<JobChat>> userNotifications = computeUserNotifications();

        sendEmailsFromNotifications(userNotifications);
    }

    /**
     * Exposed for unit testing
     */
    protected @NotNull Map<Long, Set<JobChat>> computeUserNotifications() {
        //Map of user id to set of chats with new posts
        Map<Long, Set<JobChat>> userNotifications = new HashMap<>();

        OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
        List<Long> chatsWithNewPosts = jobChatService.findChatsWithPostsSinceDate(yesterday);

        List<JobChat> chats = jobChatService.findByIds(chatsWithNewPosts);

        //Extract all users who need to be notified of chats with new posts
        for (JobChat chat : chats) {
            JobChatType chatType = chat.getType();
            Candidate candidate = chat.getCandidate();
            SalesforceJobOpp job = chat.getJobOpp();
            switch (chatType) {
                case CandidateProspect -> {
                    if (candidate != null) {
                        notifyCandidate(candidate, chat, userNotifications);
                        notifySourcePartner(candidate, chat, userNotifications);
                        notifyParticipants(chat, userNotifications);
                    }
                }
                case CandidateRecruiting -> {
                    if (candidate != null) {
                        //This chat is only visible to the candidate if the corresponding case is
                        //at or past the review stage
                        if (isChatCaseAtOrPastReviewStage(chat)) {
                            notifyCandidate(candidate, chat, userNotifications);
                        }
                        notifySourcePartner(candidate, chat, userNotifications);
                        notifyDestinationPartner(chat, userNotifications);
                        notifyParticipants(chat, userNotifications);
                    }
                }
                case AllJobCandidates -> {
                    if (job != null) {
                        Set<CandidateOpportunity> cases = job.getCandidateOpportunities();
                        for (CandidateOpportunity aCase : cases) {
                            candidate = aCase.getCandidate();
                            //Candidates only see this chat if they have accepted the job offer
                            if (aCase.getStage().isWon() || !aCase.getStage().isClosed()
                                && CandidateOpportunityStage.acceptance.compareTo(aCase.getStage()) <= 0) {
                                notifyCandidate(candidate, chat, userNotifications);
                            }
                            notifySourcePartner(candidate, chat, userNotifications);
                        }
                        notifyDestinationPartner(chat, userNotifications);
                        notifyParticipants(chat, userNotifications);
                    }
                }
                case JobCreatorSourcePartner -> {
                    Partner sourcePartner = chat.getSourcePartner();
                    notifySourcePartner(sourcePartner, chat, userNotifications);
                    notifyDestinationPartner(chat, userNotifications);
                    notifyParticipants(chat, userNotifications);
                }
                case JobCreatorAllSourcePartners -> {
                    notifyDestinationPartner(chat, userNotifications);

                    //Notify all source partners
                    final List<PartnerImpl> sourcePartners = partnerService.listSourcePartners();
                    for (PartnerImpl sourcePartner : sourcePartners) {
                        notifySourcePartner(sourcePartner, chat, userNotifications);
                    }
                    notifyParticipants(chat, userNotifications);
                }
            }
        }
        return userNotifications;
    }


    private void notifyCandidate(
        Candidate candidate, JobChat chat, Map<Long, Set<JobChat>> userNotifications) {
        User candidateUser = candidate.getUser();
        Set<JobChat> chats = userNotifications.computeIfAbsent(
            candidateUser.getId(), k -> new HashSet<>());
        chats.add(chat);
    }

    private void notifyDestinationPartner(
        JobChat chat, Map<Long, Set<JobChat>> userNotifications) {
        final SalesforceJobOpp jobOpp = chat.getJobOpp();
        if (jobOpp != null) {
            User destinationUser =
                jobOpp.getContactUser() != null ? jobOpp.getContactUser() : jobOpp.getPublishedBy();
            if (destinationUser != null) {
                Set<JobChat> chats = userNotifications.computeIfAbsent(
                    destinationUser.getId(), k -> new HashSet<>());
                chats.add(chat);
            }
        }
    }

    private void notifyParticipants(
        JobChat chat, Map<Long, Set<JobChat>> userNotifications) {
        //Get posts for chat. Extract all users who have posted
        // (excluding auto posts from Talent Catalog) and notify them
        final List<ChatPost> chatPosts = chatPostService.listChatPosts(chat.getId());

        long systemAdminUserId = userService.getSystemAdminUser().getId();

        //Get users who have posted to chat
        final Set<User> usersInChat = new HashSet<>();
        for (ChatPost chatPost : chatPosts) {
            final User user = chatPost.getCreatedBy();
            //Ignore system admin user posts
            if (user.getId() != systemAdminUserId) {
                usersInChat.add(user);
            }
        }
        //Add those users to the notifications.
        for (User user : usersInChat) {
            Set<JobChat> chats = userNotifications.computeIfAbsent(
                user.getId(), k -> new HashSet<>());
            chats.add(chat);
        }
    }

    private void notifySourcePartner(
        Candidate candidate, JobChat chat, Map<Long, Set<JobChat>> userNotifications) {
        Partner sourcePartner = candidate.getUser().getPartner();
        notifySourcePartner(sourcePartner, chat, userNotifications);
    }

    private void notifySourcePartner(
        Partner sourcePartner, JobChat chat, Map<Long, Set<JobChat>> userNotifications) {
        if (sourcePartner != null) {
            final SalesforceJobOpp jobOpp = chat.getJobOpp();
            if (jobOpp != null) {
                sourcePartner.setContextJobId(jobOpp.getId());
            }
            User sourceUser = sourcePartner.getJobContact() != null ?
                sourcePartner.getJobContact() : sourcePartner.getDefaultContact();
            if (sourceUser != null) {
                Set<JobChat> chats = userNotifications.computeIfAbsent(
                    sourceUser.getId(), k -> new HashSet<>());
                chats.add(chat);
            }
        }
    }


    private boolean isChatCaseAtOrPastReviewStage(@NonNull JobChat chat) {
        boolean result = false;
        Candidate candidate = chat.getCandidate();
        SalesforceJobOpp job = chat.getJobOpp();
        if (candidate != null && job != null) {
            CandidateOpportunity aCase = candidateOpportunityRepository
                .findByCandidateIdAndJobId(candidate.getId(), job.getId());
            if (aCase != null) {
                result = (aCase.getStage().isWon() || !aCase.getStage().isClosed()
                    && CandidateOpportunityStage.cvReview.compareTo(aCase.getStage()) <= 0);
            }
        }
        return result;
    }

    private void sendEmailsFromNotifications(Map<Long, Set<JobChat>> userNotifications) {
        //Construct and send emails
        for (Long userId : userNotifications.keySet()) {
            final Set<JobChat> userChatsWithNewPosts = userNotifications.get(userId);

            User user = userService.getUser(userId);
            if (user != null) {

                //Filter out chats that the user has marked as read
                Set<JobChat> unreadChats = userChatsWithNewPosts.stream()
                    .filter(chat -> !jobChatUserService.isChatReadByUser(chat, user))
                    .collect(Collectors.toSet());

                String s = unreadChats.stream()
                    .map(c -> c.getId().toString())
                    .collect(Collectors.joining(","));

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("notifyOfChatsWithNewPosts")
                    .message("Notifying user " + userId + " about posts to chats " + s)
                    .logInfo();

                final boolean isCandidate = userService.isCandidate(user);
                //TODO JC Construct list of topics (chat focus?) - which are either a job opp,
                // a candidate opp or a source partner / candidate (for CandidateProspect chats with no job).
                //For html can generate links (eg to job opps, candidate opps, candidates): source partner is always name
                //For text just names of opps (or candidate name or source partner name)

                //todo jc When adding these focuses - add to sets, removing duplicates.

                //TODO JC Compute list of chat focuses and pass in replacing unreadChats
                emailHelper.sendNewChatPostsForCandidateUserEmail(
                    user, userService.isCandidate(user), unreadChats);
            }
        }
    }
}
