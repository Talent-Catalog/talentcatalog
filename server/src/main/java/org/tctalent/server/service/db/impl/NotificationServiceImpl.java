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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.NotificationService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final AuthService authService;
    private final CandidateOpportunityService candidateOpportunityService;
    private final EmailHelper emailHelper;
    private final JobChatService jobChatService;
    private final UserService userService;

    @Override
    public void notifyUsersOfChatsWithNewPosts() {

        //Map of user id to set of chats with new posts
        Map<Long, Set<JobChat>> userNotifications = new HashMap<>();

        OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
        List<Long> chatsWithNewPosts = jobChatService.findChatsWithPostsSinceDate(yesterday);

        List<JobChat> chats = jobChatService.findByIds(chatsWithNewPosts);

        //Note that this is Candidate user notification only.
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
                    }
                }
                case CandidateRecruiting -> {
                    if (candidate != null && job != null) {
                        CandidateOpportunity aCase = candidateOpportunityService.findOpp(candidate, job);
                        if (aCase != null) {
                            //Candidates only see this chat if they are at or past the review stage
                            if (aCase.getStage().isWon() || !aCase.getStage().isClosed()
                                && CandidateOpportunityStage.cvReview.compareTo(aCase.getStage()) <= 0) {
                                notifyCandidate(candidate, chat, userNotifications);
                                notifySourcePartner(candidate, chat, userNotifications);
                                notifyDestinationPartner(chat, userNotifications);
                            }
                        }
                    }
                }
                case AllJobCandidates -> {
                    if (job != null) {
                        Set<CandidateOpportunity> cases = job.getCandidateOpportunities();
                        for (CandidateOpportunity aCase : cases) {
                            //Candidates only see this chat if they have accepted the job offer
                            if (aCase.getStage().isWon() || !aCase.getStage().isClosed()
                                && CandidateOpportunityStage.acceptance.compareTo(aCase.getStage()) <= 0) {
                                candidate = aCase.getCandidate();
                                notifyCandidate(candidate, chat, userNotifications);
                                notifySourcePartner(candidate, chat, userNotifications);
                            }
                        }
                        notifyDestinationPartner(chat, userNotifications);
                    }
                }
                //TODO JC Other types
            }
        }

        //Construct and send emails
        for (Long userId : userNotifications.keySet()) {
            final Set<JobChat> userChats = userNotifications.get(userId);

            User user = userService.getUser(userId);
            if (user != null) {

                //TODO JC Could filter out chats that the user has marked as read

                String s = userChats.stream()
                    .map(c -> c.getId().toString())
                    .collect(Collectors.joining(","));

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("notifyOfChatsWithNewPosts")
                    .message("Notifying user " + userId + " about posts to chats " + s)
                    .logInfo();

                emailHelper.sendNewChatPostsForCandidateUserEmail(user, userChats);
            }
        }
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

    private void notifySourcePartner(
        Candidate candidate, JobChat chat, Map<Long, Set<JobChat>> userNotifications) {
        Partner sourcePartner = candidate.getUser().getPartner();
        if (sourcePartner != null) {
            User sourceUser = sourcePartner.getJobContact() != null ?
                sourcePartner.getJobContact() : sourcePartner.getDefaultContact();
            if (sourceUser != null) {
                Set<JobChat> chats = userNotifications.computeIfAbsent(
                    sourceUser.getId(), k -> new HashSet<>());
                chats.add(chat);
            }
        }
    }
}
