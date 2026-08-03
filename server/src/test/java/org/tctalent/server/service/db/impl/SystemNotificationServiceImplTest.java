/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.PostService;
import org.tctalent.server.service.db.TranslationService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class SystemNotificationServiceImplTest {

  @Mock
  private ChatPostService chatPostService;

  @Mock
  private EmailHelper emailHelper;

  @Mock
  private JobChatService jobChatService;

  @Mock
  private NextStepProcessingService nextStepProcessingService;

  @Mock
  private PostService postService;

  @Mock
  private TranslationService translationService;

  @Mock
  private UserService userService;

  private SystemNotificationServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new SystemNotificationServiceImpl(
        chatPostService,
        emailHelper,
        jobChatService,
        nextStepProcessingService,
        postService,
        translationService,
        userService
    );
  }

  @Test
  void notifyCandidateChangesCountryPublishesNotification() {
    Candidate candidate = candidate(false);

    Country originalCountry = new Country();
    originalCountry.setName("Jordan");
    candidate.setCountry(originalCountry);

    Country newCountry = new Country();
    newCountry.setName("Canada");

    service.notifyCandidateChangesCountry(candidate, newCountry);

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.CandidateProspect,
        null,
        null,
        candidate
    );

    verify(postService).createPost(
        contains(
            "John Smith (123) has relocated from Jordan to Canada"
        ),
        eq(true)
    );

    verify(chatPostService).publishChatPost(null);
  }

  @Test
  void notifyCaseChangesDoesNothingWhenChangesAreNull() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.prospect, false);

    service.notifyCaseChanges(opportunity, null);

    verifyNoInteractions(
        jobChatService,
        postService,
        chatPostService,
        nextStepProcessingService,
        translationService,
        emailHelper
    );
  }

  @Test
  void notifyCaseChangesDoesNotPublishWhenNothingChanged() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.cvReview, false);

    opportunity.setNextStep("Contact employer");
    opportunity.setNextStepDueDate(LocalDate.of(2026, 8, 1));

    CandidateOpportunityParams changes =
        new CandidateOpportunityParams();
    changes.setStage(CandidateOpportunityStage.cvReview);
    changes.setNextStep("Contact employer");
    changes.setNextStepDueDate(LocalDate.of(2026, 8, 1));

    when(nextStepProcessingService.processNextStep(
        opportunity,
        "Contact employer"
    )).thenReturn("Contact employer");

    service.notifyCaseChanges(opportunity, changes);

    verify(nextStepProcessingService).processNextStep(
        opportunity,
        "Contact employer"
    );
    verifyNoInteractions(
        jobChatService,
        postService,
        chatPostService,
        translationService,
        emailHelper
    );
  }

  @Test
  void notifyCaseChangesToMistakenProspectOnlyNotifiesPartnerChat() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.cvReview, false);

    CandidateOpportunityParams changes =
        new CandidateOpportunityParams();
    changes.setStage(
        CandidateOpportunityStage.candidateMistakenProspect
    );

    service.notifyCaseChanges(opportunity, changes);

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.JobCreatorSourcePartner,
        opportunity.getJobOpp(),
        opportunity.getCandidate().getUser().getPartner(),
        null
    );

    verify(jobChatService, never()).getOrCreateJobChat(
        JobChatType.CandidateProspect,
        null,
        null,
        opportunity.getCandidate()
    );

    verify(jobChatService, never()).getOrCreateJobChat(
        JobChatType.CandidateRecruiting,
        opportunity.getJobOpp(),
        opportunity.getCandidate().getUser().getPartner(),
        opportunity.getCandidate()
    );

    verify(postService).createPost(
        contains("Candidate was mistakenly proposed"),
        eq(true)
    );

    verify(chatPostService, times(1)).publishChatPost(null);
  }

  @Test
  void closingProspectCaseDoesNotPublishToCandidateProspectChat() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.prospect, false);

    CandidateOpportunityParams changes =
        new CandidateOpportunityParams();
    changes.setStage(CandidateOpportunityStage.noJobOffer);

    service.notifyCaseChanges(opportunity, changes);

    verify(jobChatService, never()).getOrCreateJobChat(
        JobChatType.CandidateProspect,
        null,
        null,
        opportunity.getCandidate()
    );

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.CandidateRecruiting,
        opportunity.getJobOpp(),
        opportunity.getCandidate().getUser().getPartner(),
        opportunity.getCandidate()
    );

    // Partner chat and recruiting chat.
    verify(chatPostService, times(2)).publishChatPost(null);
  }

  @Test
  void closingAdvancedCasePublishesToAllRelevantChats() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.cvReview, false);

    CandidateOpportunityParams changes =
        new CandidateOpportunityParams();
    changes.setStage(CandidateOpportunityStage.noJobOffer);

    service.notifyCaseChanges(opportunity, changes);

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.CandidateProspect,
        null,
        null,
        opportunity.getCandidate()
    );

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.CandidateRecruiting,
        opportunity.getJobOpp(),
        opportunity.getCandidate().getUser().getPartner(),
        opportunity.getCandidate()
    );

    verify(chatPostService, times(3)).publishChatPost(null);
  }

  @Test
  void candidateWithAllNotificationsReceivesEarlyStageChange() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.prospect, true);

    CandidateOpportunityParams changes =
        new CandidateOpportunityParams();
    changes.setStage(CandidateOpportunityStage.miniIntake);

    when(translationService.translateToEnglish(
        org.mockito.ArgumentMatchers.any(String[].class)
    )).thenReturn("The candidate has progressed");

    service.notifyCaseChanges(opportunity, changes);

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.CandidateProspect,
        null,
        null,
        opportunity.getCandidate()
    );

    verify(translationService).translateToEnglish(
        org.mockito.ArgumentMatchers.any(String[].class)
    );

    // Candidate prospect chat and partner chat.
    verify(chatPostService, times(2)).publishChatPost(null);
  }

  @Test
  void candidateWithoutAllNotificationsDoesNotReceiveEarlyStageChange() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.prospect, false);

    CandidateOpportunityParams changes =
        new CandidateOpportunityParams();
    changes.setStage(CandidateOpportunityStage.miniIntake);

    service.notifyCaseChanges(opportunity, changes);

    verify(jobChatService, never()).getOrCreateJobChat(
        JobChatType.CandidateProspect,
        null,
        null,
        opportunity.getCandidate()
    );

    verifyNoInteractions(translationService);

    // Only the job creator/source partner chat.
    verify(chatPostService, times(1)).publishChatPost(null);
  }

  @Test
  void candidateWithoutAllNotificationsReceivesLaterStageChange() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.fullIntake, false);

    CandidateOpportunityParams changes = new CandidateOpportunityParams();
    changes.setStage(CandidateOpportunityStage.oneWayPreparation);

    when(translationService.translateToEnglish(
        org.mockito.ArgumentMatchers.any(String[].class)
    )).thenReturn("The candidate is now preparing for a one-way interview");

    service.notifyCaseChanges(opportunity, changes);

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.CandidateProspect,
        null,
        null,
        opportunity.getCandidate()
    );

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.JobCreatorSourcePartner,
        opportunity.getJobOpp(),
        opportunity.getCandidate().getUser().getPartner(),
        null
    );

    verify(chatPostService, times(2)).publishChatPost(null);
    verifyNoInteractions(emailHelper);
  }

  @Test
  void movingToHiredStagePublishesHiredNotifications() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.offer, false);

    CandidateOpportunityParams changes =
        new CandidateOpportunityParams();
    changes.setStage(
        CandidateOpportunityStage.provincialVisaPreparation
    );

    when(translationService.translateToEnglish(
        org.mockito.ArgumentMatchers.any(String[].class)
    )).thenReturn("The candidate has progressed");

    service.notifyCaseChanges(opportunity, changes);

    verify(emailHelper).sendCandidateHiredEmail(
        opportunity.getCandidate()
    );

    verify(postService).createPost(
        contains("has been hired for the job"),
        eq(true)
    );

    /*
     * Standard stage change:
     * - candidate chat
     * - partner chat
     *
     * Hired notification:
     * - prospect chat
     * - recruiting chat
     * - partner chat
     */
    verify(chatPostService, times(5)).publishChatPost(null);
  }

  @Test
  void nextStepChangePublishesCaseMessageWithNoDueDate() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.cvReview, false);

    opportunity.setNextStep("Old step");
    opportunity.setNextStepDueDate(LocalDate.of(2026, 7, 1));

    CandidateOpportunityParams changes =
        new CandidateOpportunityParams();
    changes.setNextStep("New step");
    changes.setNextStepDueDate(null);

    when(nextStepProcessingService.processNextStep(
        opportunity,
        "New step"
    )).thenReturn("Processed new step");

    service.notifyCaseChanges(opportunity, changes);

    verify(postService).createPost(
        contains("<br><b>Due date:</b> none"),
        eq(true)
    );

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.JobCreatorSourcePartner,
        opportunity.getJobOpp(),
        opportunity.getCandidate().getUser().getPartner(),
        null
    );

    verify(chatPostService).publishChatPost(null);
  }

  @Test
  void unchangedJobNextStepDoesNotPublishMessage() {
    SalesforceJobOpp job = job();
    job.setNextStep("Contact employer");
    job.setNextStepDueDate(LocalDate.of(2026, 8, 1));

    UpdateJobRequest changes = mock(UpdateJobRequest.class);
    when(changes.getNextStep()).thenReturn("Contact employer");
    when(changes.getNextStepDueDate())
        .thenReturn(LocalDate.of(2026, 8, 1));

    when(nextStepProcessingService.processNextStep(
        job,
        "Contact employer"
    )).thenReturn("Contact employer");

    service.notifyJobOppNextStepInfoChangesIfAny(job, changes);

    verifyNoInteractions(
        jobChatService,
        postService,
        chatPostService
    );
  }

  @Test
  void changedJobNextStepPublishesMessageWithDueDate() {
    SalesforceJobOpp job = job();
    job.setNextStep("Old step");
    job.setNextStepDueDate(null);

    UpdateJobRequest changes = mock(UpdateJobRequest.class);
    when(changes.getNextStep()).thenReturn("New step");
    when(changes.getNextStepDueDate())
        .thenReturn(LocalDate.of(2026, 9, 15));

    when(nextStepProcessingService.processNextStep(
        job,
        "New step"
    )).thenReturn("Processed new step");

    service.notifyJobOppNextStepInfoChangesIfAny(job, changes);

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.JobCreatorAllSourcePartners,
        job,
        null,
        null
    );

    verify(postService).createPost(
        contains("<br><b>Due date:</b> 2026-09-15"),
        eq(true)
    );

    verify(chatPostService).publishChatPost(null);
  }

  @Test
  void notifyNewProspectCaseOnlyPublishesToPartnerChat() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.prospect, false);

    service.notifyNewCase(opportunity);

    verify(jobChatService, never()).getOrCreateJobChat(
        JobChatType.CandidateProspect,
        null,
        null,
        opportunity.getCandidate()
    );

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.JobCreatorSourcePartner,
        opportunity.getJobOpp(),
        opportunity.getCandidate().getUser().getPartner(),
        null
    );

    verify(postService).createPost(
        contains("is a prospect for the job"),
        eq(true)
    );

    verify(chatPostService, times(1)).publishChatPost(null);
  }

  @Test
  void notifyNewAdvancedCasePublishesToCandidateAndPartnerChats() {
    CandidateOpportunity opportunity =
        opportunity(CandidateOpportunityStage.cvReview, false);

    service.notifyNewCase(opportunity);

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.CandidateProspect,
        null,
        null,
        opportunity.getCandidate()
    );

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.JobCreatorSourcePartner,
        opportunity.getJobOpp(),
        opportunity.getCandidate().getUser().getPartner(),
        null
    );

    verify(chatPostService, times(2)).publishChatPost(null);
  }

  @Test
  void notifyNewJobPublishesJobDetails() {
    SalesforceJobOpp job = job();

    service.notifyNewJobOpp(job);

    verify(jobChatService).getOrCreateJobChat(
        JobChatType.JobCreatorAllSourcePartners,
        job,
        null,
        null
    );

    verify(postService).createPost(
        contains("A new job has been published"),
        eq(true)
    );
    verify(postService).createPost(
        contains("Software Engineer"),
        eq(true)
    );

    verify(chatPostService).publishChatPost(null);
  }

  private CandidateOpportunity opportunity(
      CandidateOpportunityStage stage,
      boolean allNotifications) {

    CandidateOpportunity opportunity =
        new CandidateOpportunity();

    opportunity.setName("Candidate Case");
    opportunity.setCandidate(candidate(allNotifications));
    opportunity.setJobOpp(job());
    opportunity.setStage(stage);

    return opportunity;
  }

  private Candidate candidate(boolean allNotifications) {
    PartnerImpl partner = new PartnerImpl();

    User user = new User();
    user.setFirstName("John");
    user.setLastName("Smith");
    user.setPartner(partner);

    Candidate candidate = new Candidate();
    candidate.setCandidateNumber("123");
    candidate.setUser(user);
    candidate.setAllNotifications(allNotifications);

    return candidate;
  }

  private SalesforceJobOpp job() {
    Country country = new Country();
    country.setName("Canada");

    PartnerImpl jobCreator = new PartnerImpl();
    jobCreator.setName("Destination Partner");

    SalesforceJobOpp job = new SalesforceJobOpp();
    job.setName("Software Engineer");
    job.setCountry(country);
    job.setJobCreator(jobCreator);

    return job;
  }
}