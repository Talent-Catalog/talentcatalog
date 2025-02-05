/*
 * Copyright (c) 2025 Talent Catalog.
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

import static org.tctalent.server.util.NextStepHelper.auditStampNextStep;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.OppNotificationService;
import org.tctalent.server.service.db.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OppNotificationServiceImpl implements OppNotificationService {
    private final ChatPostService chatPostService;
    private final JobChatService jobChatService;
    private final UserService userService;

    @Override
    public void notifyOppChanges(CandidateOpportunity opp, CandidateOpportunityParams changes) {
        if (changes != null) {
            final CandidateOpportunityStage newStage = changes.getStage();
            if (newStage != null) {
                // Stage is changing
                if (!newStage.equals(opp.getStage())) {
                    // If stage is changing to CLOSED (e.g. removed from submission list) publish posts
                    if (newStage.isClosed()) {
                        publishOppClosedPosts(opp, newStage);
                        // If a stage is changed to ACCEPTANCE (job offer is accepted)
                    } else if (newStage.equals(CandidateOpportunityStage.acceptance)) {
                        publishOppAcceptedPosts(opp);
                    } else {
                        // If non closing stage change, publish posts
                        publishStageChangePosts(opp, newStage);
                    }
                }
            }

            if (changes.getNextStep() != null) {
                // NEXT STEP PROCESSING
                User userForAttribution = userService.getLoggedInUser();
                if (userForAttribution == null) {
                    userForAttribution = userService.getSystemAdminUser();
                }
                final String processedNextStep = auditStampNextStep(
                    userForAttribution.getUsername(), LocalDate.now(),
                    opp.getNextStep(), changes.getNextStep());

                // If next step details changed, automate post to JobCreatorSourcePartner chat.
                // To compare previous next step to new one, need to ensure neither is null.
                // Cases are auto-populated with a value for next step when created, but this has
                // not always been the case.
                String currentNextStep = opp.getNextStep() == null ? "" : opp.getNextStep();

                // If only the due date has changed, we still want to send a message.
                // As above, there are some old cases with null values that need to be dealt with.
                LocalDate currentDueDate =
                    opp.getNextStepDueDate() == null ?
                        LocalDate.of(1970, 1, 1) : opp.getNextStepDueDate();

                // If the request due date is null (user deletes the existing value in the form but
                // doesn't set a new one, then submits) it will not be used (see below) — so, for
                // purpose of comparison we give it the same value as the current due date (no
                // message will be sent because they're the same).
                LocalDate requestDueDate =
                    changes.getNextStepDueDate() == null ?
                        currentDueDate : changes.getNextStepDueDate();

                if (!processedNextStep.equals(currentNextStep) || !requestDueDate.equals(currentDueDate)) {
                    // Find the relevant job chat
                    JobChat jcspChat = jobChatService.getOrCreateJobChat(
                        JobChatType.JobCreatorSourcePartner,
                        opp.getJobOpp(),
                        opp.getCandidate().getUser().getPartner(),
                        null
                    );

                    String candidateNameAndNumber = constructCandidateNameNumber(opp.getCandidate());

                    // Set the chat post content
                    Post autoPostNextStepChange = new Post();
                    autoPostNextStepChange.setContent(
                        "💼 <b>" + opp.getName()
                            + "</b> 🪜<br> The next step details have changed for this case relating to candidate "
                            + candidateNameAndNumber
                            + ".<br><b>Next step:</b> " + processedNextStep
                            + "<br><b>Due date:</b> "
                            + (changes.getNextStepDueDate() == null ?
                            opp.getNextStepDueDate() : changes.getNextStepDueDate())
                    );

                    // Create the chat post
                    ChatPost nextStepChangeChatPost = chatPostService.createPost(
                        autoPostNextStepChange, jcspChat, userService.getSystemAdminUser());

                    // Publish the chat post
                    chatPostService.publishChatPost(nextStepChangeChatPost);
                }
            }
        }
    }

    /**
     * Get candidate name and number string for automated chat posts
     * @param candidate Candidate to get details from
     */
    private String constructCandidateNameNumber(Candidate candidate) {
        // Get candidate name and number for automated chat posts
        return candidate.getUser().getFirstName() + " "
            + candidate.getUser().getLastName()
            + " (" + candidate.getCandidateNumber() + ")";
    }

    /**
     * Publish posts for a candidate opportunity that is moved to a closing stage.
     * Publish to:
     * - JobCreatorSourcePartner chat
     * - CandidateProspect chat
     * - CandidateRecruiting chat
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     * @param newStage CandidateOpportunityStage - closing stage that the opp is being changed to
     */
    private void publishOppClosedPosts(CandidateOpportunity opp, CandidateOpportunityStage newStage) {
        Candidate candidate = opp.getCandidate();
        String candidateNameAndNumber = constructCandidateNameNumber(opp.getCandidate());

        Post autoPostRemovedFromSubList = new Post();
        autoPostRemovedFromSubList.setContent("The candidate " + candidateNameAndNumber +
            " has been removed for the job '" + opp.getJobOpp().getName() +
            "' with the reason " + newStage.getSalesforceStageName() + ".");

        //Only post to candidate if they got past the prospect stage for this job
        if (opp.getStage() != CandidateOpportunityStage.prospect) {
            // AUTO CHAT TO PROSPECT CHAT
            JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect, null,
                null, candidate);
            // Create the chat post
            ChatPost prospectChatPostRemoved = chatPostService.createPost(
                autoPostRemovedFromSubList, prospectChat, userService.getSystemAdminUser());
            // Publish chat post
            chatPostService.publishChatPost(prospectChatPostRemoved);
        }

        // AUTO CHAT TO RECRUITING CHAT
        JobChat recruitingChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateRecruiting, opp.getJobOpp(),
            candidate.getUser().getPartner(), candidate);
        // Create the chat post
        ChatPost recruitingChatPostRemoved = chatPostService.createPost(
            autoPostRemovedFromSubList, recruitingChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(recruitingChatPostRemoved);

        // AUTO CHAT TO JOB CREATOR SOURCE PARTNER CHAT
        JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner, opp.getJobOpp(),
            candidate.getUser().getPartner(), null);
        // Create the chat post
        ChatPost jcspChatPostRemoved = chatPostService.createPost(
            autoPostRemovedFromSubList, jcspChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(jcspChatPostRemoved);
    }

    /**
     * Publish posts for after a Candidate Opportunity is created (e.g. Added to submission list)
     * and the stage is now prospect.
     * Publish to:
     * - CandidateProspect chat
     * - JobCreatorSourcePartner chat.
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     */
    public void notifyNewOpp(CandidateOpportunity opp) {
        Candidate candidate = opp.getCandidate();
        String candidateNameAndNumber = constructCandidateNameNumber(opp.getCandidate());

        Post autoPostAddedToSubList = new Post();
        autoPostAddedToSubList.setContent("The candidate " + candidateNameAndNumber +
            " is a prospect for the job '" + opp.getJobOpp().getName() +"'.");

        // Note that we don't post to candidates until they get past the prospect stage
        if (opp.getStage() != CandidateOpportunityStage.prospect) {
            // AUTO CHAT TO PROSPECT CHAT
            JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect,
                null,null, candidate);
            // Create the chat post
            ChatPost prospectChatPost = chatPostService.createPost(
                autoPostAddedToSubList, prospectChat, userService.getSystemAdminUser());
            //publish chat post
            chatPostService.publishChatPost(prospectChatPost);
        }

        // AUTO CHAT TO JOB CREATOR SOURCE PARTNER CHAT
        JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner, opp.getJobOpp(),
            candidate.getUser().getPartner(), null);
        // Create the chat post
        ChatPost jcspChatPost = chatPostService.createPost(
            autoPostAddedToSubList, jcspChat, userService.getSystemAdminUser());
        //publish chat post
        chatPostService.publishChatPost(jcspChatPost);
    }

    /**
     * Publish post for a candidate opportunity stage change that is not to a closing stage.
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     * @param newStage CandidateOpportunityStage - non-closing stage that the opp is being changed to
     */
    private void publishStageChangePosts(CandidateOpportunity opp, CandidateOpportunityStage newStage) {

        //This code only works for unclosed stages. Ignore if called with a closed stage.
        if (newStage.isClosed()) {
            return;
        }

        final Candidate candidate = opp.getCandidate();

        //     Candidate Related Chats

        //Candidates who have opted in to receiving all notifications are notified for all
        //stages after "prospect".
        //Candidates who have not opted in to all notifications are only notified once they get
        //past the "review" stage.
        CandidateOpportunityStage lastExcludedStage = candidate.isAllNotifications()
            ? CandidateOpportunityStage.prospect : CandidateOpportunityStage.cvReview;

        if (newStage.ordinal() > lastExcludedStage.ordinal()) {
            //TODO JC Post to candidate related chats
        }

        //     Non Candidate Related Chats

        // Find the relevant job chat
        JobChat jcspChat = jobChatService.getOrCreateJobChat(
            JobChatType.JobCreatorSourcePartner,
            opp.getJobOpp(),
            candidate.getUser().getPartner(),
            null
        );

        String candidateNameAndNumber = constructCandidateNameNumber(candidate);

        // Set the chat post content
        Post autoPostCandidateOppStageChange = new Post();
        autoPostCandidateOppStageChange.setContent(
            "💼 <b>" + opp.getName() + "</b> 🪜<br> This case for candidate "
                + candidateNameAndNumber
                + " has changed stage from '" + opp.getStage() + "' to '"
                + newStage + "'."
        );

        // Create the chat post
        ChatPost candidateOppStageChangeChatPost = chatPostService.createPost(
            autoPostCandidateOppStageChange, jcspChat, userService.getSystemAdminUser());

        // Publish the chat post
        chatPostService.publishChatPost(candidateOppStageChangeChatPost);
    }

    /**
     * Publish post for a candidate opportunity stage change to acceptance. Notify all previous chats.
     * - CandidateProspect chat
     * - CandidateRecruiting chat
     * - JobCreatorSourcePartner chat
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     */
    private void publishOppAcceptedPosts(CandidateOpportunity opp) {
        Candidate candidate = opp.getCandidate();
        String candidateNameAndNumber = constructCandidateNameNumber(opp.getCandidate());
        Post autoPostAcceptedJobOffer = new Post();
        autoPostAcceptedJobOffer.setContent("The candidate " + candidateNameAndNumber + " has accepted the job offer from '"
            + opp.getJobOpp().getName() + " and is now a member of the <a href=\"https://pathwayclub.org/about\" target=\"_blank\">Pathway Club</a>.");

        // AUTO CHAT TO PROSPECT CHAT
        JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect, null,
            null, candidate);
        // Create the chat post
        ChatPost prospectChatPostAccepted = chatPostService.createPost(
            autoPostAcceptedJobOffer, prospectChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(prospectChatPostAccepted);

        // AUTO CHAT TO RECRUITING CHAT
        JobChat recruitingChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateRecruiting, opp.getJobOpp(),
            candidate.getUser().getPartner(), candidate);
        // Create the chat post
        ChatPost recruitingChatPostAccepted = chatPostService.createPost(
            autoPostAcceptedJobOffer, recruitingChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(recruitingChatPostAccepted);

        // AUTO CHAT TO JOB CREATOR SOURCE PARTNER CHAT
        JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner, opp.getJobOpp(),
            candidate.getUser().getPartner(), null);
        // Create the chat post
        ChatPost jcspChatPostAccepted = chatPostService.createPost(
            autoPostAcceptedJobOffer, jcspChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(jcspChatPostAccepted);
    }

}
