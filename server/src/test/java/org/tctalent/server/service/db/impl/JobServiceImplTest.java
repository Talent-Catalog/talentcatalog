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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppExtended;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppMinimal;
import static org.tctalent.server.data.UserTestData.getAdminUser;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.OppNotificationService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    private SalesforceJobOpp shortJob;
    private SalesforceJobOpp longJob;
    private UpdateJobRequest updateJobRequest;
    private User adminUser;

    @Mock private UserService userService;
    @Mock private SalesforceJobOppRepository salesforceJobOppRepository;
    @Mock private SalesforceService sfService;
    @Mock private SavedSearchService savedSearchService;
    @Mock private SavedListService savedListService;
    @Mock private NextStepProcessingService nextStepProcessingService;
    @Mock private OppNotificationService oppNotificationService;
    @Mock private CandidateOpportunityService candidateOpportunityService;
    @Mock private AuthService authService;

    @Captor ArgumentCaptor<CandidateOpportunityParams> caseParamsCaptor;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        shortJob = getSalesforceJobOppMinimal();
        longJob = getSalesforceJobOppExtended();
        updateJobRequest = new UpdateJobRequest();
        adminUser = getAdminUser();
    }

    @Test
    @DisplayName("should update open candidate opps to closed stage when job opp closed")
    void updateJob_shouldCloseOpenCandidateOpps_whenJobOppClosed() {
        updateJobRequest.setStage(JobOpportunityStage.noJobOffer);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(1L)).willReturn(Optional.of(longJob));

        jobService.updateJob(1L, updateJobRequest);

        verify(candidateOpportunityService).createUpdateCandidateOpportunities(
            anyCollection(), eq(longJob), caseParamsCaptor.capture());
        assertTrue(caseParamsCaptor.getValue().getStage().isClosed());
    }

    @Test
    @DisplayName("should set open candidate opp stage to jobIneligible when job closed for reasons "
        + "unrelated to candidate")
    void updateJob_shouldSetOpenCandidateOppsToJobIneligible() {
        updateJobRequest.setStage(JobOpportunityStage.ineligibleEmployer);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(1L)).willReturn(Optional.of(longJob));

        jobService.updateJob(1L, updateJobRequest);

        verify(candidateOpportunityService).createUpdateCandidateOpportunities(
            anyCollection(), eq(longJob), caseParamsCaptor.capture());
        assertEquals(caseParamsCaptor.getValue().getStage(), CandidateOpportunityStage.jobIneligible);
    }

    @Test
    @DisplayName("should set open candidate opp stage to jobWithdrawn when job closed due to "
        + "employer issues with process")
    void updateJob_shouldSetOpenCandidateOppsToJobWithdrawn() {
        updateJobRequest.setStage(JobOpportunityStage.tooExpensive);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(1L)).willReturn(Optional.of(longJob));

        jobService.updateJob(1L, updateJobRequest);

        verify(candidateOpportunityService).createUpdateCandidateOpportunities(
            anyCollection(), eq(longJob), caseParamsCaptor.capture());
        assertEquals(caseParamsCaptor.getValue().getStage(), CandidateOpportunityStage.jobWithdrawn);
    }

    @Test
    @DisplayName("should set open candidate opp stage to notFitForRole when job closed due to "
        + "issues with candidate")
    void updateJob_shouldSetOpenCandidateOppsToNotFitForRole() {
        updateJobRequest.setStage(JobOpportunityStage.noVisa);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(1L)).willReturn(Optional.of(longJob));

        jobService.updateJob(1L, updateJobRequest);

        verify(candidateOpportunityService).createUpdateCandidateOpportunities(
            anyCollection(), eq(longJob), caseParamsCaptor.capture());
        assertEquals(caseParamsCaptor.getValue().getStage(), CandidateOpportunityStage.notFitForRole);
    }

    @Test
    @DisplayName("should set candidate opp at stage after twoWayReview to noJobOffer when job closed")
    void updateJob_shouldSetCandidateOppsPastTwoWayReviewToNoJobOffer() {
        updateJobRequest.setStage(JobOpportunityStage.noInterest);
        longJob.getCandidateOpportunities().iterator().next()
            .setStage(CandidateOpportunityStage.offer);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(1L)).willReturn(Optional.of(longJob));

        jobService.updateJob(1L, updateJobRequest);

        verify(candidateOpportunityService).createUpdateCandidateOpportunities(
            anyCollection(), eq(longJob), caseParamsCaptor.capture());
        assertEquals(caseParamsCaptor.getValue().getStage(), CandidateOpportunityStage.noJobOffer);
    }

    @Test
    @DisplayName("should set candidate opp at acceptance stage to CandidateRejectsOffer when job closed")
    void updateJob_shouldSetCandidateOppsAtAcceptanceStageToCandidateRejectsOffer() {
        updateJobRequest.setStage(JobOpportunityStage.noSuitableCandidates);
        longJob.getCandidateOpportunities().iterator().next()
            .setStage(CandidateOpportunityStage.acceptance);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(1L)).willReturn(Optional.of(longJob));

        jobService.updateJob(1L, updateJobRequest);

        verify(candidateOpportunityService).createUpdateCandidateOpportunities(
            anyCollection(), eq(longJob), caseParamsCaptor.capture());
        assertEquals(caseParamsCaptor.getValue().getStage(),
            CandidateOpportunityStage.candidateRejectsOffer);
    }

    @Test
    @DisplayName("updateJob sets new name on SF AND TC when provided in request")
    void updateJobSetsNewNameWhenChanged() {
        final String newName = "new name";
        final String oldName = shortJob.getName();
        updateJobRequest.setJobName(newName);

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).should().updateEmployerOpportunityName(shortJob.getSfId(), newName);
        assertEquals(shortJob.getName(), newName);
        then(savedSearchService).should().updateSuggestedSearchesNames(shortJob, oldName);
        then(savedListService).should().updateAssociatedListsNames(shortJob);
    }

    @Test
    @DisplayName("updateJob sets Stage on SF AND TC when provided in request")
    void updateJobSetsStageOnSfAndTcWhenProvided() {
        final JobOpportunityStage newStage = JobOpportunityStage.jobOffer;
        updateJobRequest.setStage(newStage);

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).should()
            .updateEmployerOpportunityStage(shortJob, newStage, null, null);
        assertEquals(shortJob.getStage(), newStage);
    }

    @Test
    @DisplayName("updateJob sets Next Step on SF AND TC when provided in request")
    void updateJobSetsNextStepOnSfAndTcWhenProvided() {
        final String newNextStep = "do something";
        updateJobRequest.setNextStep(newNextStep);

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));
        given(nextStepProcessingService.processNextStep(shortJob, newNextStep)).willReturn(newNextStep);

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).should()
            .updateEmployerOpportunityStage(shortJob, null, newNextStep, null);
        assertEquals(shortJob.getNextStep(), newNextStep);
    }

    @Test
    @DisplayName("updateJob sets Next Step Due Date on SF AND TC when provided in request")
    void updateJobSetsNextStepDueDateOnSfAndTcWhenProvided() {
        final LocalDate newNextStepDueDate = LocalDate.of(1970, 1,1);
        updateJobRequest.setNextStepDueDate(newNextStepDueDate);

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).should()
            .updateEmployerOpportunityStage(shortJob, null, null, newNextStepDueDate);
        assertEquals(shortJob.getNextStepDueDate(), newNextStepDueDate);
    }

    @Test
    @DisplayName("updateJob - when SF-pertinent request values all null, no SF update "
        + "and corresponding TC Job values unchanged")
    void updateJobNoUnnecessaryUpdate() {
        updateJobRequest.setEvergreen(true); // Minor update (not pertinent to SF)
        final String currentNextStep = shortJob.getNextStep();
        final LocalDate currentDueDate = shortJob.getNextStepDueDate();
        final String currentName = shortJob.getName();
        final JobOpportunityStage currentStage = shortJob.getStage();

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).shouldHaveNoInteractions();
        assertEquals(shortJob.getNextStep(), currentNextStep);
        assertEquals(shortJob.getNextStepDueDate(), currentDueDate);
        assertEquals(shortJob.getName(), currentName);
        assertEquals(shortJob.getStage(), currentStage);
    }

}
