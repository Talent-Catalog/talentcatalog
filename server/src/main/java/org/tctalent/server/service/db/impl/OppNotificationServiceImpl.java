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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.AbstractOpportunity;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.NextStepWithDueDate;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.OppNotificationService;
import org.tctalent.server.service.db.PostService;
import org.tctalent.server.service.db.TranslationService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.TranslationHelper;

@Service
@RequiredArgsConstructor
@Slf4j
public class OppNotificationServiceImpl implements OppNotificationService {
    private final ChatPostService chatPostService;
    private final EmailHelper emailHelper;
    private final JobChatService jobChatService;
    private final NextStepProcessingService nextStepProcessingService;
    private final PostService postService;
    private final TranslationService translationService;
    private final UserService userService;

    @Override
    public void notifyCaseChanges(CandidateOpportunity opp, CandidateOpportunityParams changes) {
        if (changes != null) {
            final CandidateOpportunityStage currentStage = opp.getStage();
            final CandidateOpportunityStage newStage = changes.getStage();
            if (newStage != null) {
                // Stage is changing
                if (!newStage.equals(currentStage)) {
                    // If stage is changing to CLOSED (e.g. removed from submission list) publish posts
                    // unless the candidate was accidentally added.
                    if (newStage.isClosed()
                        && newStage != CandidateOpportunityStage.candidateMistakenProspect) {
                        publishOppClosedPosts(opp, newStage);
                    } else {
                        //New stage is not a closed stage
                        //Publish standard stage change post
                        publishStageChangePosts(opp, newStage);

                        //Make a special additional post when candidate has accepted a job offer
                        //If current stage is at or before acceptance stage and new stage is after
                        //acceptance, then the candidate has accepted the job offer.
                        if (currentStage.ordinal() <= CandidateOpportunityStage.acceptance.ordinal()
                            && newStage.ordinal() > CandidateOpportunityStage.acceptance.ordinal()) {
                            publishOppAcceptedPosts(opp);
                        }
                    }
                }
            }

            // NEXT STEP PROCESSING
            String processedNextStep =
                nextStepProcessingService.processNextStep(opp, changes.getNextStep());

            NextStepWithDueDate requested =
                new NextStepWithDueDate(processedNextStep, changes.getNextStepDueDate());

            NextStepWithDueDate current =
                new NextStepWithDueDate(opp.getNextStep(), opp.getNextStepDueDate());

            if (!requested.equals(current)) {
                // Find the relevant job chat
                JobChat jcspChat = jobChatService.getOrCreateJobChat(
                    JobChatType.JobCreatorSourcePartner,
                    opp.getJobOpp(),
                    opp.getCandidate().getUser().getPartner(),
                    null
                );

                String s =
                    "The next step details have changed for this case relating to candidate " +
                        constructCandidateNameNumber(opp.getCandidate());
                String mess = constructNextStepMessage(opp, requested, s);
                publishMessage(jcspChat, mess);
            }
        }
    }

    @Override
    public void notifyNewCase(CandidateOpportunity opp) {
        Candidate candidate = opp.getCandidate();
        String candidateNameAndNumber = constructCandidateNameNumber(opp.getCandidate());

        Post autoPostNewOpp = postService.createPost("The candidate " + candidateNameAndNumber +
            " is a prospect for the job '" + opp.getJobOpp().getName() +"'.");

        // Note that we don't post to candidates until they get past the prospect stage
        if (opp.getStage() != CandidateOpportunityStage.prospect) {
            // AUTO CHAT TO PROSPECT CHAT
            JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect,
                null,null, candidate);
            publishPost(prospectChat, autoPostNewOpp);
        }

        // AUTO CHAT TO JOB CREATOR SOURCE PARTNER CHAT
        JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner,
            opp.getJobOpp(), candidate.getUser().getPartner(), null);
        publishPost(jcspChat, autoPostNewOpp);
    }

    @Override
    public void notifyJobOppChanges(SalesforceJobOpp opp, UpdateJobRequest changes) {
        // NEXT STEP PROCESSING
        String processedNextStep =
            nextStepProcessingService.processNextStep(opp, changes.getNextStep());

        NextStepWithDueDate requested =
            new NextStepWithDueDate(processedNextStep, changes.getNextStepDueDate());

        NextStepWithDueDate current =
            new NextStepWithDueDate(opp.getNextStep(), opp.getNextStepDueDate());

        if (!requested.equals(current)) {
            // Find the relevant job chat
            JobChat jcspChat = jobChatService.getOrCreateJobChat(
                JobChatType.JobCreatorAllSourcePartners,
                opp,
                null,
                null
            );

            String s = "The next step details for this job opportunity have changed:";
            String mess = constructNextStepMessage(opp, requested, s);
            publishMessage(jcspChat, mess);
        }
    }

    @Override
    public void notifyNewJobOpp(SalesforceJobOpp job) {
        JobChat jobChat = jobChatService.getOrCreateJobChat(
            JobChatType.JobCreatorAllSourcePartners, job, null, null);

        String mess = "💼 <b>A new job has been published!</b></br>"
            + "<b>Job Name:</b> </br>" + job.getName() + "</br>"
            + "<b> Job Creator: </b> </br>" + job.getJobCreator() + "</br>"
            + "<b> Job Country: </b> </br>" + job.getCountry().getName() + "</br>"
            + "</br>"; // Add a newline for readability

        publishMessage(jobChat, mess);
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

    private String constructNextStepMessage(
        AbstractOpportunity opp, NextStepWithDueDate step, String keyMessage) {
        return "💼 <b>" + opp.getName() + "</b> 🪜<br> " + keyMessage
            + ".<br><b>Next step:</b> " + step.nextStep()
            + "<br><b>Due date:</b> " + (step.dueDate() == null ? "none" : step.dueDate());
    }

    /**
     * Publish posts for a candidate opportunity that is moved to a closing stage.
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     * @param newStage CandidateOpportunityStage - closing stage that the opp is being changed to
     */
    private void publishOppClosedPosts(CandidateOpportunity opp, CandidateOpportunityStage newStage) {
        Candidate candidate = opp.getCandidate();
        String candidateNameAndNumber = constructCandidateNameNumber(opp.getCandidate());

        Post autoPostClosedOpp = postService.createPost("The candidate " + candidateNameAndNumber +
            " has been removed for the job '" + opp.getJobOpp().getName() +
            "' with the reason " + newStage.getSalesforceStageName() + ".");

        //Only post to candidate if they got past the prospect stage for this job
        if (opp.getStage() != CandidateOpportunityStage.prospect) {
            // AUTO CHAT TO PROSPECT CHAT
            JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect, null,
                null, candidate);
            publishPost(prospectChat, autoPostClosedOpp);
        }

        // AUTO CHAT TO RECRUITING CHAT
        JobChat recruitingChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateRecruiting, opp.getJobOpp(),
            candidate.getUser().getPartner(), candidate);
        publishPost(recruitingChat, autoPostClosedOpp);

        // AUTO CHAT TO JOB CREATOR SOURCE PARTNER CHAT
        JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner, opp.getJobOpp(),
            candidate.getUser().getPartner(), null);
        publishPost(jcspChat, autoPostClosedOpp);
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
            //Post to candidate
            JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect, null,
                null, candidate);

            //Extract candidate friendly message relating to the new stage from the translated stage
            //description.
            String[] keys = TranslationHelper.getCaseStageTranslationKeys(newStage);
            String message = translationService.translateToEnglish(keys);

            Post autoPostStageChange = postService.createPost(message);
            publishPost(prospectChat, autoPostStageChange);

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
        Post autoPostCandidateOppStageChange = postService.createPost(
            "💼 <b>" + opp.getName() + "</b> 🪜<br> This case for candidate "
                + candidateNameAndNumber
                + " has changed stage from '" + opp.getStage() + "' to '"
                + newStage + "'."
        );

        publishPost(jcspChat, autoPostCandidateOppStageChange);
    }

    /**
     * Publish post for a candidate opportunity stage change to acceptance. Notify all previous chats.
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     */
    private void publishOppAcceptedPosts(CandidateOpportunity opp) {
        Candidate candidate = opp.getCandidate();
        String candidateNameAndNumber = constructCandidateNameNumber(opp.getCandidate());

        //Email candidate
        emailHelper.sendOfferAcceptedEmail(candidate);

        Post autoPostAcceptedJobOffer = postService.createPost(
            "The candidate " + candidateNameAndNumber + " has accepted the job offer from '"
                + opp.getJobOpp().getName() + " and is now a member of the <a href=\"https://pathwayclub.org/about\" target=\"_blank\">Pathway Club</a>."
        );

        // AUTO CHAT TO PROSPECT CHAT
        JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect, null,
            null, candidate);
        publishPost(prospectChat, autoPostAcceptedJobOffer);

        // AUTO CHAT TO RECRUITING CHAT
        JobChat recruitingChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateRecruiting, opp.getJobOpp(),
            candidate.getUser().getPartner(), candidate);
        publishPost(recruitingChat, autoPostAcceptedJobOffer);

        // AUTO CHAT TO JOB CREATOR SOURCE PARTNER CHAT
        JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner, opp.getJobOpp(),
            candidate.getUser().getPartner(), null);
        publishPost(jcspChat, autoPostAcceptedJobOffer);
    }

    private void publishMessage(JobChat chat, String mess) {

        // Set the chat post content
        Post post = postService.createPost(mess);

        publishPost(chat, post);
    }

    private void publishPost(JobChat chat, Post post) {
        // Create the chat post
        ChatPost chatPost = chatPostService.createPost(post, chat, userService.getSystemAdminUser());

        // Publish the chat post
        chatPostService.publishChatPost(chatPost);
    }

}
