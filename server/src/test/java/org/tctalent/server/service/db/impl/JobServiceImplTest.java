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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.PartnerImplTestData.getDefaultPartner;
import static org.tctalent.server.data.PartnerImplTestData.getSourcePartner;
import static org.tctalent.server.data.SalesforceJobOppTestData.getEmployer;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppExtended;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppMinimal;
import static org.tctalent.server.data.SavedListTestData.getExclusionList;
import static org.tctalent.server.data.SavedListTestData.getSubmissionList;
import static org.tctalent.server.data.UserTestData.getAdminUser;

import java.time.LocalDate;
import java.util.List;
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
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.sf.Account;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.EmployerService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.OppNotificationService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.SalesforceBridgeService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    private SalesforceJobOpp shortJob;
    private SalesforceJobOpp longJob;
    private UpdateJobRequest updateJobRequest;
    private UpdateJobRequest createJobRequest;
    private User adminUser;
    private Employer employer;
    private SavedList submissionList;
    private SavedList exclusionList;
    private final String sfJobLink =
        "https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/006Uu33300BHCHlIAP/view";
    private final String newNextStep = "do something";
    private final LocalDate newNextStepDueDate = LocalDate.now().plusDays(14);
    private final String processedNextStep = "processedNextStep";

    @Mock private UserService userService;
    @Mock private SalesforceJobOppRepository salesforceJobOppRepository;
    @Mock private SavedSearchService savedSearchService;
    @Mock private SavedListService savedListService;
    @Mock private NextStepProcessingService nextStepProcessingService;
    @Mock private OppNotificationService oppNotificationService;
    @Mock private CandidateOpportunityService candidateOpportunityService;
    @Mock private AuthService authService;
    @Mock private SalesforceJobOppService salesforceJobOppService;
    @Mock private EmployerService employerService;
    @Mock private SalesforceService salesforceService;
    @Mock private SalesforceBridgeService salesforceBridgeService;
    @Mock private JobChatService jobChatService;
    @Mock private PartnerService partnerService;

    @Captor ArgumentCaptor<CandidateOpportunityParams> caseParamsCaptor;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        shortJob = getSalesforceJobOppMinimal();
        longJob = getSalesforceJobOppExtended();
        updateJobRequest = new UpdateJobRequest();
        updateJobRequest.setStage(JobOpportunityStage.cvReview);
        updateJobRequest.setNextStep(newNextStep);
        updateJobRequest.setNextStepDueDate(newNextStepDueDate);
        adminUser = getAdminUser();
        employer = getEmployer();
        createJobRequest = new UpdateJobRequest();
        submissionList = getSubmissionList();
        exclusionList = getExclusionList();
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
    @DisplayName("should throw exception if logged in user is not from job creator partner")
    void createJob_shouldThrowExceptionIfLoggedInUserIsNotFromJobCreatorPartner() {
        given(userService.getLoggedInUser()).willReturn(adminUser);

        assertThrows(UnauthorisedActionException.class,
            () -> jobService.createJob(createJobRequest));
    }

    @Test
    @DisplayName("should throw exception when logged in user is from default job creator and request "
        + "has no sfLink")
    void createJob_shouldThrowException_whenLoggedInUserIsFromDefaultJobCreatorPartnerAndNoSfLink() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        adminUser.setPartner(getDefaultPartner());

        Exception ex = assertThrows(InvalidRequestException.class,
            () -> jobService.createJob(createJobRequest));

        assertEquals(ex.getMessage(), "Missing link to Salesforce opportunity");
    }

    @Test
    @DisplayName("should throw exception for default job creator when job already exists")
    void createJob_shouldThrowExceptionForDefaultJobCreator_whenJobAlreadyExists() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        adminUser.setPartner(getDefaultPartner());
        createJobRequest.setSfJoblink(sfJobLink); // Valid request
        given(salesforceJobOppService.getJobOppById(anyString())).willReturn(longJob);

        assertThrows(EntityExistsException.class, () -> jobService.createJob(createJobRequest));
    }

    @Test
    @DisplayName("should call salesforceJobOppService to create job and update TC version with SF "
        + "details for default job creator user")
    void createJob_shouldCallSalesforceJobOppServiceToCreateJobAndThenUpdateTCVersion() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        adminUser.setPartner(getDefaultPartner());
        createJobRequest.setSfJoblink(sfJobLink); // Valid request
        given(salesforceJobOppService.createJobOpp(anyString())).willReturn(longJob);
        given(salesforceService.findAccount(anyString())).willReturn(new Account());
        given(employerService.findOrCreateEmployerFromSalesforceAccount(any(Account.class)))
            .willReturn(employer);
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);
        given(savedListService.createSavedList(any(UpdateSavedListInfoRequest.class)))
            .willReturn(submissionList);
        given(salesforceBridgeService.findSeenCandidates(anyString(), anyString()))
            .willReturn(exclusionList);
        given(partnerService.listActiveSourcePartners()).willReturn(List.of(getSourcePartner()));

        jobService.createJob(createJobRequest);

        verify(salesforceJobOppService).createJobOpp(anyString());
        assertEquals(longJob.getCountry(), employer.getCountry());
        assertEquals(longJob.getSubmissionList(), submissionList);
        assertEquals(longJob.getExclusionList(), exclusionList);
        verify(jobChatService).createJobCreatorChat(JobChatType.AllJobCandidates, longJob);
        verify(jobChatService).createJobCreatorChat(JobChatType.JobCreatorAllSourcePartners, longJob);
        jobChatService.createJobCreatorSourcePartnerChat(longJob, getSourcePartner());

        // TODO verify SF fields updated (e.g. SF ID)? - separate test for job chat creation?
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

        then(salesforceService).should().updateEmployerOpportunityName(shortJob.getSfId(), newName);
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
        given(nextStepProcessingService.processNextStep(shortJob, updateJobRequest.getNextStep()))
            .willReturn(processedNextStep);

        jobService.updateJob(99L, updateJobRequest);

        then(salesforceService).should().updateEmployerOpportunityStage(
            shortJob,
            newStage,
            processedNextStep,
            updateJobRequest.getNextStepDueDate()
        );
        assertEquals(shortJob.getStage(), newStage);
    }

    @Test
    @DisplayName("updateJob sets Next Step using processed version on SF AND TC when provided in "
        + "request")
    void updateJobSetsNextStepOnSfAndTcWhenProvided() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));
        given(nextStepProcessingService.processNextStep(shortJob, updateJobRequest.getNextStep()))
            .willReturn(processedNextStep);

        jobService.updateJob(99L, updateJobRequest);

        then(salesforceService).should().updateEmployerOpportunityStage(
            shortJob,
            updateJobRequest.getStage(),
            processedNextStep,
            updateJobRequest.getNextStepDueDate()
        );
        assertEquals(shortJob.getNextStep(), processedNextStep);
    }

    @Test
    @DisplayName("updateJob sets Next Step Due Date on SF AND TC when provided in request")
    void updateJobSetsNextStepDueDateOnSfAndTcWhenProvided() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));
        given(nextStepProcessingService.processNextStep(shortJob, updateJobRequest.getNextStep()))
            .willReturn(processedNextStep);

        jobService.updateJob(99L, updateJobRequest);

        then(salesforceService).should().updateEmployerOpportunityStage(
            shortJob,
            updateJobRequest.getStage(),
            processedNextStep,
            newNextStepDueDate
        );
        assertEquals(shortJob.getNextStepDueDate(), newNextStepDueDate);
    }

    @Test
    @DisplayName("updateJob - when SF-pertinent request values all null, no SF update "
        + "and corresponding TC Job values unchanged")
    void updateJobNoUnnecessaryUpdate() {
        UpdateJobRequest emptyRequest = new UpdateJobRequest();
        emptyRequest.setEvergreen(true); // Minor update (not pertinent to SF)
        final String currentNextStep = shortJob.getNextStep();
        final LocalDate currentDueDate = shortJob.getNextStepDueDate();
        final String currentName = shortJob.getName();
        final JobOpportunityStage currentStage = shortJob.getStage();

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));

        jobService.updateJob(99L, emptyRequest);

        then(salesforceService).shouldHaveNoInteractions();
        assertEquals(shortJob.getNextStep(), currentNextStep);
        assertEquals(shortJob.getNextStepDueDate(), currentDueDate);
        assertEquals(shortJob.getName(), currentName);
        assertEquals(shortJob.getStage(), currentStage);
    }

}
