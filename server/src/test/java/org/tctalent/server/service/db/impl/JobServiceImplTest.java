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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.PartnerImplTestData.getDefaultPartner;
import static org.tctalent.server.data.PartnerImplTestData.getEmployerPartner;
import static org.tctalent.server.data.PartnerImplTestData.getSourcePartner;
import static org.tctalent.server.data.SalesforceJobOppTestData.createUpdateJobRequestAndExpectedJob;
import static org.tctalent.server.data.SalesforceJobOppTestData.getEmployer;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppExtended;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppMinimal;
import static org.tctalent.server.data.SavedListTestData.getExclusionList;
import static org.tctalent.server.data.SavedListTestData.getSavedList;
import static org.tctalent.server.data.SavedListTestData.getSubmissionList;
import static org.tctalent.server.data.UserTestData.getAdminUser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.data.SalesforceJobOppTestData.UpdateJobTestData;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.JobOppIntake;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.sf.Account;
import org.tctalent.server.repository.db.JobSpecification;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.job.JobInfoForSlackPost;
import org.tctalent.server.request.job.JobIntakeData;
import org.tctalent.server.request.job.SearchJobRequest;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.request.link.UpdateLinkRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.EmployerService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.JobOppIntakeService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.OppNotificationService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.SalesforceBridgeService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    private SalesforceJobOpp shortJob;
    private SalesforceJobOpp longJob;
    private SalesforceJobOpp emptyJob1;
    private List<SalesforceJobOpp> emptyJobsList;
    private UpdateJobRequest updateJobRequest;
    private UpdateJobRequest createJobRequest;
    private User adminUser;
    private UpdateLinkRequest updateLinkRequest;
    private SalesforceJobOpp expectedJob;
    private UpdateJobRequest updateRequestExtended;

    private static final JobOppIntake JOB_OPP_INTAKE = new JobOppIntake();
    private static final JobIntakeData JOB_INTAKE_DATA = new JobIntakeData();
    private static final SearchJobRequest SEARCH_JOB_REQUEST = new SearchJobRequest();
    private static final SavedList SUGGESTED_LIST = getSavedList();
    private static final SavedList EXCLUSION_LIST = getExclusionList();
    private static final SavedList SUBMISSION_LIST = getSubmissionList();
    private static final Employer EMPLOYER = getEmployer();
    private static final String SF_JOB_LINK =
        "https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/006Uu33300BHCHlIAP/view";
    private static final String SF_JOB_ID = "006Uu33300BHCHlIAP";
    private static final String NEXT_STEP = "do something";
    private static final LocalDate NEXT_STEP_DUE_DATE = LocalDate.parse("2025-12-01");
    private static final String PROCESSED_NEXT_STEP = "processedNextStep";
    private static final long JOB_ID = 11L;
    private static final String NAME = "name";
    private static final String URL = "www.url.com";
    private static final Specification<SalesforceJobOpp> FAKE_SPEC = (root, query, cb) -> null;

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
    @Mock private SalesforceConfig salesforceConfig;
    @Mock private CandidateSavedListService candidateSavedListService;
    @Mock private JobOppIntakeService jobOppIntakeService;
    @Mock private JobServiceHelper jobServiceHelper;
    @Mock private MultipartFile mockFile;
    @Mock private GoogleDriveConfig googleDriveConfig;
    @Mock private FileSystemService fileSystemService;

    @Captor ArgumentCaptor<CandidateOpportunityParams> caseParamsCaptor;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        shortJob = getSalesforceJobOppMinimal();
        longJob = getSalesforceJobOppExtended();
        updateJobRequest = new UpdateJobRequest();
        updateJobRequest.setStage(JobOpportunityStage.cvReview);
        updateJobRequest.setNextStep(NEXT_STEP);
        updateJobRequest.setNextStepDueDate(NEXT_STEP_DUE_DATE);
        adminUser = getAdminUser();
        createJobRequest = new UpdateJobRequest();
        emptyJob1 = new SalesforceJobOpp();
        emptyJob1.setId(1L);
        SalesforceJobOpp emptyJob2 = new SalesforceJobOpp();
        emptyJob2.setId(2L);
        emptyJobsList = new ArrayList<>(List.of(emptyJob1, emptyJob2));
        updateLinkRequest = new UpdateLinkRequest();
        updateLinkRequest.setUrl(URL);
        updateLinkRequest.setName(NAME);
        UpdateJobTestData data = createUpdateJobRequestAndExpectedJob();
        expectedJob = data.expectedJob();
        updateRequestExtended = data.request();
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
    @DisplayName("should throw when logged in user is not from job creator partner")
    void createJob_shouldThrow_whenLoggedInUserIsNotFromJobCreatorPartner() {
        given(userService.getLoggedInUser()).willReturn(adminUser);

        assertThrows(UnauthorisedActionException.class,
            () -> jobService.createJob(createJobRequest));
    }

    @Test
    @DisplayName("should throw when no logged in user")
    void createJob_shouldThrow_whenNoLoggedInUser() {
        given(userService.getLoggedInUser()).willReturn(null);

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
        createJobRequest.setSfJoblink(SF_JOB_LINK); // Default job creator must have SF equivalent
        given(salesforceJobOppService.getJobOppById(anyString())).willReturn(longJob);

        assertThrows(EntityExistsException.class, () -> jobService.createJob(createJobRequest));
    }

    @Test
    @DisplayName("should call salesforceJobOppService to create job and update TC version with SF "
        + "details for default job creator user")
    void createJob_shouldCallSalesforceJobOppServiceToCreateJobAndThenUpdateTCVersion() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        adminUser.setPartner(getDefaultPartner());
        createJobRequest.setSfJoblink(SF_JOB_LINK); // Default job creator must have SF equivalent
        given(salesforceJobOppService.createJobOpp(anyString())).willReturn(longJob);
        given(salesforceService.findAccount(anyString())).willReturn(new Account());
        given(employerService.findOrCreateEmployerFromSalesforceAccount(any(Account.class)))
            .willReturn(EMPLOYER);
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);
        given(salesforceBridgeService.findSeenCandidates(anyString(), anyString()))
            .willThrow(RuntimeException.class);
        given(savedListService.createSavedList(any(UpdateSavedListInfoRequest.class)))
            .willReturn(SUBMISSION_LIST);
        given(partnerService.listActiveSourcePartners()).willReturn(List.of(getSourcePartner()));

        jobService.createJob(createJobRequest);

        verify(salesforceJobOppService).createJobOpp(anyString());
        assertEquals(longJob.getCountry(), EMPLOYER.getCountry());
        assertEquals(longJob.getSubmissionList(), SUBMISSION_LIST);
        verify(jobChatService).createJobCreatorChat(JobChatType.AllJobCandidates, longJob);
        verify(jobChatService).createJobCreatorChat(JobChatType.JobCreatorAllSourcePartners, longJob);
        jobChatService.createJobCreatorSourcePartnerChat(longJob, getSourcePartner());
    }

    @Test
    @DisplayName("should create and set submission and exclusion lists")
    void createJob_shouldCreateAndSetSubmissionAndExclusionLists() {
        setUpAndCompleteCreateJobPath();

        jobService.createJob(createJobRequest);

        assertEquals(longJob.getSubmissionList(), SUBMISSION_LIST);
        assertEquals(longJob.getExclusionList(), EXCLUSION_LIST);
    }

    @Test
    @DisplayName("should create relevant job chats")
    void createJob_shouldCreateRelevantJobChats() {
        setUpAndCompleteCreateJobPath();

        jobService.createJob(createJobRequest);

        verify(jobChatService).createJobCreatorChat(JobChatType.AllJobCandidates, longJob);
        verify(jobChatService).createJobCreatorChat(JobChatType.JobCreatorAllSourcePartners, longJob);
    }

    @Test
    @DisplayName("should create job on SF and then update TC job with SF ID")
    void createJob_shouldCreateJobOnSfAndThenUpdateTCJobWithSfId() {
        setUpAndCompleteCreateJobPath();

        jobService.createJob(createJobRequest);

        assertEquals(longJob.getSfId(), SF_JOB_ID);
    }

    @Test
    @DisplayName("should throw exception for user of partner that is not employer or default job "
        + "creator")
    void createJob_shouldThrowExceptionForUserOfPartnerThatIsNotJobCreator() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        adminUser.getPartner().setJobCreator(true);

        Exception ex = assertThrows(InvalidRequestException.class,
            () -> jobService.createJob(createJobRequest));

        assertEquals(ex.getMessage(),
            "Unsupported type of partner: " + adminUser.getPartner().getName());
    }

    private void setUpAndCompleteCreateJobPath() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        adminUser.getPartner().setEmployer(getEmployer());
        adminUser.getPartner().setJobCreator(true);
        given(salesforceJobOppRepository.save(any(SalesforceJobOpp.class))).willReturn(longJob);
        given(salesforceService.createOrUpdateJobOpportunity(longJob))
            .willReturn(SF_JOB_ID);
        given(savedListService.createSavedList(any(UpdateSavedListInfoRequest.class)))
            .willReturn(SUBMISSION_LIST);
        given(salesforceBridgeService.findSeenCandidates(anyString(), anyString()))
            .willReturn(EXCLUSION_LIST);
        given(partnerService.listActiveSourcePartners()).willReturn(List.of(getSourcePartner()));
    }

    @Test
    @DisplayName("should set new name on SF AND TC when provided in request")
    void updateJob_setsNewName_whenChanged() {
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
    @DisplayName("should set Stage on SF AND TC when provided in request")
    void updateJob_setsStageOnSfAndTc_whenProvided() {
        final JobOpportunityStage newStage = JobOpportunityStage.jobOffer;
        updateJobRequest.setStage(newStage);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));
        given(nextStepProcessingService.processNextStep(shortJob, updateJobRequest.getNextStep()))
            .willReturn(PROCESSED_NEXT_STEP);

        jobService.updateJob(99L, updateJobRequest);

        then(salesforceService).should().updateEmployerOpportunityStage(
            shortJob,
            newStage,
            PROCESSED_NEXT_STEP,
            updateJobRequest.getNextStepDueDate()
        );
        assertEquals(shortJob.getStage(), newStage);
    }

    @Test
    @DisplayName("should set Next Step using processed version on SF AND TC when provided in "
        + "request")
    void updateJob_setsNextStepOnSfAndTc_whenProvided() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));
        given(nextStepProcessingService.processNextStep(shortJob, updateJobRequest.getNextStep()))
            .willReturn(PROCESSED_NEXT_STEP);

        jobService.updateJob(99L, updateJobRequest);

        then(salesforceService).should().updateEmployerOpportunityStage(
            shortJob,
            updateJobRequest.getStage(),
            PROCESSED_NEXT_STEP,
            updateJobRequest.getNextStepDueDate()
        );
        assertEquals(shortJob.getNextStep(), PROCESSED_NEXT_STEP);
    }

    @Test
    @DisplayName("should set Next Step Due Date on SF AND TC when provided in request")
    void updateJob_setsNextStepDueDateOnSfAndTc_whenProvided() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));
        given(nextStepProcessingService.processNextStep(shortJob, updateJobRequest.getNextStep()))
            .willReturn(PROCESSED_NEXT_STEP);

        jobService.updateJob(99L, updateJobRequest);

        then(salesforceService).should().updateEmployerOpportunityStage(
            shortJob,
            updateJobRequest.getStage(),
            PROCESSED_NEXT_STEP,
            NEXT_STEP_DUE_DATE
        );
        assertEquals(shortJob.getNextStepDueDate(), NEXT_STEP_DUE_DATE);
    }

    @Test
    @DisplayName("shouldn't update SF and corresponding TC Job values when unchanged")
    void updateJob_noUnnecessaryUpdate() {
        UpdateJobRequest emptyRequest = new UpdateJobRequest();
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

    @Test
    @DisplayName("should return job when found")
    void getJob_shouldReturnJob_whenFound() {
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));

        SalesforceJobOpp result = jobService.getJob(JOB_ID);

        assertEquals(longJob, result);
    }

    @Test
    @DisplayName("should throw when not found")
    void getJob_shouldThrow_whenNotFound() {
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class, () -> jobService.getJob(JOB_ID));
    }

    @Test
    @DisplayName("should create and set employer entity when not found")
    void checkEmployerEntity_shouldCreateAndSetEmployerEntity_whenNotFound() {
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        longJob.setEmployerEntity(null);
        given(employerService.findOrCreateEmployerFromSalesforceId(anyString()))
            .willReturn(EMPLOYER);
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.getJob(JOB_ID);

        assertEquals(result.getEmployerEntity(), EMPLOYER);
        verify(salesforceJobOppRepository).save(longJob);
    }

    @Test
    @DisplayName("should return job page and check employer entities on search")
    void searchJobs_shouldReturnPage_andCheckEmployerEntities() {
        SearchJobRequest request = mock(SearchJobRequest.class);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<SalesforceJobOpp> page = new PageImpl<>(List.of(longJob));

        given(request.getPageRequest()).willReturn(pageRequest);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findAll(any(Specification.class), eq(pageRequest)))
            .willReturn(page);

        try (MockedStatic<JobSpecification> mockedStatic = mockStatic(JobSpecification.class)) {
            mockedStatic.when(() -> JobSpecification.buildSearchQuery(any(
                    SearchJobRequest.class), any(User.class)))
                .thenReturn(FAKE_SPEC);

            assertEquals(page, jobService.searchJobs(request));
        }
    }

    @Test
    @DisplayName("should create and attach suggested search to job")
    void createSuggestedSearch_shouldAttachSuggestedSearch() {
        String suffix = "SuggestedSearch";

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setId(123L);

        SavedList exclusionList = new SavedList();
        longJob.setExclusionList(exclusionList);
        longJob.setSuggestedSearches(new HashSet<>());

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(savedSearchService.createSavedSearch(any())).willReturn(savedSearch);
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.createSuggestedSearch(JOB_ID, suffix);

        assertTrue(result.getSuggestedSearches().contains(savedSearch));
        assertEquals(adminUser, result.getUpdatedBy());
        verify(salesforceJobOppRepository).save(longJob);
    }

    @Test
    @DisplayName("should extract job info with contact and links")
    void extractJobInfoForSlack_shouldReturnSlackPost() {
        given(salesforceJobOppRepository.findById(1L)).willReturn(Optional.of(longJob));
        given(salesforceConfig.getBaseLightningUrl()).willReturn("https://salesforce.example.com");

        JobInfoForSlackPost result =
            jobService.extractJobInfoForSlack(1L, "https://tc.example.com/job");

        assertEquals(longJob.getName(), result.getJobName());
        assertEquals(longJob.getContactUser(), result.getContact());
        assertEquals(longJob.getJobSummary(), result.getJobSummary());
        assertEquals("https://tc.example.com/job", result.getTcJobLink());
        assertTrue(result.getSfJobLink().contains("salesforce.example.com"));
    }

    @Test
    @DisplayName("should throw when no logged in user")
    void publishJob_shouldThrow_whenNoLoggedInUser() {
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(UnauthorisedActionException.class, () -> jobService.publishJob(JOB_ID));
    }

    @Test
    @DisplayName("should set stage to candidateSearch when previously at earlier stage and "
        + "skipCandidateSearch set to false")
    void publishJob_shouldSetStageToCandidateSearch_whenPreviouslyAtEarlierStage() {
        longJob.setStage(JobOpportunityStage.prospect);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(candidateSavedListService.copy(any(SavedList.class), any(CopySourceContentsRequest.class)))
            .willReturn(new SavedList());
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.publishJob(JOB_ID);

        assertEquals(result.getStage(), JobOpportunityStage.candidateSearch);
    }

    @Test
    @DisplayName("should set stage to visaEligibility when previously at "
        + "earlier stage and skipCandidateSearch set to true")
    void publishJob_shouldSetStageToVisaEligibility_whenPreviouslyAtEarlierStage() {
        longJob.setStage(JobOpportunityStage.prospect);
        longJob.setSkipCandidateSearch(true);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(candidateSavedListService.copy(any(SavedList.class), any(CopySourceContentsRequest.class)))
            .willReturn(new SavedList());
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.publishJob(JOB_ID);

        assertEquals(result.getStage(), JobOpportunityStage.visaEligibility);
    }

    @Test
    @DisplayName("should set a suggested list with contents of submission list when not empty")
    void publishJob_shouldSetSuggestedListWithContentsOfSubmissionList_whenNotEmpty() {
        longJob.setStage(JobOpportunityStage.prospect);
        longJob.setSkipCandidateSearch(true);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(candidateSavedListService.copy(any(SavedList.class), any(CopySourceContentsRequest.class)))
            .willReturn(SUGGESTED_LIST);
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.publishJob(JOB_ID);

        assertEquals(result.getSuggestedList(), SUGGESTED_LIST);
    }

    @Test
    @DisplayName("should remove suggested search, delete it and set audit fields")
    void removeSuggestedSearch_shouldRemoveSuggestedSearch() {
        SavedSearch suggestedSearch = new SavedSearch();
        longJob.setSuggestedSearches(new HashSet<>(Set.of(suggestedSearch)));
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(savedSearchService.getSavedSearch(anyLong())).willReturn(suggestedSearch);
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.removeSuggestedSearch(JOB_ID, 1L);

        assertEquals(result.getSuggestedSearches(), Collections.emptySet());
    }

    @Test
    @DisplayName("should return list of unread chat IDs for matched job opps")
    void findUnreadChatsInOpps_shouldReturnUnreadChatIds_whenUserIsLoggedIn() {
        List<Long> expectedUnreadIds = List.of(1L);

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findAll(any(Specification.class)))
            .willReturn(emptyJobsList);
        given(salesforceJobOppRepository.findUnreadChatsInOpps(adminUser.getId(), List.of(1L, 2L)))
            .willReturn(expectedUnreadIds);

        try (MockedStatic<JobSpecification> mockedStatic = mockStatic(JobSpecification.class)) {
            mockedStatic.when(() -> JobSpecification.buildSearchQuery(any(
                    SearchJobRequest.class), any(User.class)))
                .thenReturn(FAKE_SPEC);

            assertEquals(expectedUnreadIds, jobService.findUnreadChatsInOpps(SEARCH_JOB_REQUEST));
            verify(salesforceJobOppRepository)
                .findUnreadChatsInOpps(adminUser.getId(), List.of(1L, 2L));
        }
    }

    @Test
    @DisplayName("should throw when no logged in user")
    void findUnreadChatsInOpps_shouldThrow_whenNoLoggedInUser() {
        given(userService.getLoggedInUser()).willReturn(null);

        assertThrows(InvalidSessionException.class,
            () -> jobService.findUnreadChatsInOpps(SEARCH_JOB_REQUEST));
    }

    @Test
    @DisplayName("should return matching job opportunities unpaged")
    void searchJobsUnpaged_shouldReturnMatchingJobs() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findAll(any(Specification.class))).willReturn(emptyJobsList);

        try (MockedStatic<JobSpecification> mockedStatic = mockStatic(JobSpecification.class)) {
            mockedStatic.when(() -> JobSpecification.buildSearchQuery(any(
                    SearchJobRequest.class), any(User.class)))
                .thenReturn(FAKE_SPEC);

            assertEquals(emptyJobsList, jobService.searchJobsUnpaged(SEARCH_JOB_REQUEST));
            verify(salesforceJobOppRepository).findAll(any(Specification.class));
        }
    }

    @Test
    @DisplayName("should set JD link and audit fields as requested")
    void updateJDLink_shouldSetAsRequested() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.updateJdLink(JOB_ID, updateLinkRequest);

        assertEquals(result.getSubmissionList().getFileJdLink(), URL);
        assertEquals(result.getSubmissionList().getFileJdName(), NAME);
        assertEquals(longJob.getUpdatedBy(), adminUser);
    }

    @Test
    @DisplayName("should throw if no submission list")
    void updateJdLink_shouldThrowIfNoSubmissionList() {
        longJob.setSubmissionList(null);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));

        Exception ex = assertThrows(InvalidRequestException.class,
            () -> jobService.updateJdLink(JOB_ID, updateLinkRequest));

        assertEquals(ex.getMessage(), "Job " + JOB_ID + " does not have submission list");
    }

    @Test
    @DisplayName("should set JOI link and audit fields as requested")
    void updateJoiLink_shouldSetAsRequested() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.updateJoiLink(JOB_ID, updateLinkRequest);

        assertEquals(result.getSubmissionList().getFileJoiLink(), URL);
        assertEquals(result.getSubmissionList().getFileJoiName(), NAME);
        assertEquals(longJob.getUpdatedBy(), adminUser);
    }

    @Test
    @DisplayName("should throw if no submission list")
    void updateJoiLink_shouldThrowIfNoSubmissionList() {
        longJob.setSubmissionList(null);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));

        Exception ex = assertThrows(InvalidRequestException.class,
            () -> jobService.updateJoiLink(JOB_ID, updateLinkRequest));

        assertEquals(ex.getMessage(), "Job " + JOB_ID + " does not have submission list");
    }

    @Test
    @DisplayName("should set interview guidance link and audit fields as requested")
    void updateInterviewGuidanceLink_shouldSetAsRequested() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.updateInterviewGuidanceLink(JOB_ID, updateLinkRequest);

        assertEquals(result.getSubmissionList().getFileInterviewGuidanceLink(), URL);
        assertEquals(result.getSubmissionList().getFileInterviewGuidanceName(), NAME);
        assertEquals(longJob.getUpdatedBy(), adminUser);
    }

    @Test
    @DisplayName("should throw if no submission list")
    void updateInterviewGuidanceLink_shouldThrowIfNoSubmissionList() {
        longJob.setSubmissionList(null);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));

        Exception ex = assertThrows(InvalidRequestException.class,
            () -> jobService.updateInterviewGuidanceLink(JOB_ID, updateLinkRequest));

        assertEquals(ex.getMessage(), "Job " + JOB_ID + " does not have submission list");
    }

    @Test
    @DisplayName("should set as requested")
    void updateMouLink_shouldSetAsRequested() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        SalesforceJobOpp result = jobService.updateMouLink(JOB_ID, updateLinkRequest);

        assertEquals(result.getSubmissionList().getFileMouLink(), URL);
        assertEquals(result.getSubmissionList().getFileMouName(), NAME);
        assertEquals(longJob.getUpdatedBy(), adminUser);
    }

    @Test
    @DisplayName("should throw if no submission list")
    void updateMouLink_shouldThrowIfNoSubmissionList() {
        longJob.setSubmissionList(null);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));

        Exception ex = assertThrows(InvalidRequestException.class,
            () -> jobService.updateMouLink(JOB_ID, updateLinkRequest));

        assertEquals(ex.getMessage(), "Job " + JOB_ID + " does not have submission list");
    }

    @Test
    @DisplayName("should create intake and save job when intake is missing")
    void updateIntakeData_shouldCreateIntakeAndSaveJob_whenIntakeIsMissing() {
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        longJob.setJobOppIntake(null);
        given(jobOppIntakeService.create(JOB_INTAKE_DATA)).willReturn(JOB_OPP_INTAKE);
        given(salesforceJobOppRepository.save(longJob)).willReturn(longJob);

        jobService.updateIntakeData(JOB_ID, JOB_INTAKE_DATA);

        assertEquals(JOB_OPP_INTAKE, longJob.getJobOppIntake());
        verify(salesforceJobOppRepository).save(longJob);
        verify(jobOppIntakeService).create(JOB_INTAKE_DATA);
        verify(jobOppIntakeService, never()).update(anyLong(), any());
    }

    @Test
    @DisplayName("should update existing intake when intake is already set")
    void updateIntakeData_shouldUpdateExistingIntake_whenIntakeIsPresent() {
        JobOppIntake existingIntake = new JobOppIntake();
        existingIntake.setId(123L);

        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        longJob.setJobOppIntake(existingIntake);

        jobService.updateIntakeData(JOB_ID, JOB_INTAKE_DATA);

        verify(jobOppIntakeService).update(123L, JOB_INTAKE_DATA);
        verify(salesforceJobOppRepository, never()).save(any());
        verify(jobOppIntakeService, never()).create(any());
    }

    @Test
    @DisplayName("should update Salesforce Job as expected")
    void updateJob_shouldSetFieldsAsExpected() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(nextStepProcessingService.processNextStep(longJob, updateRequestExtended.getNextStep()))
            .willReturn(updateRequestExtended.getNextStep());

        jobService.updateJob(JOB_ID, updateRequestExtended);

        verify(salesforceService).updateEmployerOpportunityStage(
            longJob,
            updateRequestExtended.getStage(),
            updateRequestExtended.getNextStep(),
            updateRequestExtended.getNextStepDueDate()
        );

        verify(salesforceService).updateEmployerOpportunityName(
            longJob.getSfId(),
            updateRequestExtended.getJobName()
        );
    }

    /**
     * expectedJop is essentially shortJob with the requested updates pre-set — so, if update method
     * working as expected, they will pass the comparison test at the end.
     */
    @Test
    @DisplayName("should update TC Job as expected")
    void updateJob_shouldUpdateTcJobAsExpected() {
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));
        given(nextStepProcessingService.processNextStep(shortJob, updateRequestExtended.getNextStep()))
            .willReturn(updateRequestExtended.getNextStep());
        given(userService.getUser(updateRequestExtended.getContactUserId()))
            .willReturn(adminUser);
        given(salesforceJobOppRepository.save(shortJob)).willReturn(shortJob);

        SalesforceJobOpp result = jobService.updateJob(JOB_ID, updateRequestExtended);

        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFields("createdDate", "updatedBy", "updatedDate")
            .isEqualTo(expectedJob);
    }

    @Test
    @DisplayName("should successfully copy existing job when getJobToCopyId true")
    void createJob_shouldSuccessfullyCopyExistingJob_whenGetJobToCopyIdTrue() {
        updateJobRequest.setJobToCopyId(JOB_ID);
        adminUser.setPartner(getEmployerPartner());
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.save(any(SalesforceJobOpp.class))).willReturn(shortJob);
        given(savedListService.createSavedList(any(UpdateSavedListInfoRequest.class)))
            .willReturn(SUBMISSION_LIST);
        given(salesforceBridgeService.findSeenCandidates(anyString(), anyString()))
            .willReturn(EXCLUSION_LIST);
        given(salesforceJobOppService.getJobOpp(JOB_ID)).willReturn(longJob);
        given(jobOppIntakeService.create(longJob.getJobOppIntake()))
            .willReturn(longJob.getJobOppIntake());

        SalesforceJobOpp result = jobService.createJob(updateJobRequest);

        assertEquals(longJob.getJobSummary(), result.getJobSummary());
        assertEquals(longJob.getJobOppIntake(), result.getJobOppIntake());

        SavedList master = longJob.getSubmissionList();
        SavedList copy = result.getSubmissionList();
        assertEquals(copy.getFileJdLink(), master.getFileJdLink());
        assertEquals(copy.getFileJdName(), master.getFileJdName());
        assertEquals(copy.getFileJoiLink(), master.getFileJoiLink());
        assertEquals(copy.getFileJoiName(), master.getFileJoiName());
        assertEquals(copy.getFileMouLink(), master.getFileMouLink());
        assertEquals(copy.getFileMouName(), master.getFileMouName());
        assertEquals(copy.getFileInterviewGuidanceLink(), master.getFileInterviewGuidanceLink());
        assertEquals(copy.getFileInterviewGuidanceName(), master.getFileInterviewGuidanceName());
    }

    @Test
    @DisplayName("should create evergreen child opp when evergreen and updated to employed stage")
    void updateJob_shouldCreateEvergreenChildOpp_whenEvergreenAndUpdatedToEmployedStage() {
        shortJob.setEvergreenChild(null);
        shortJob.setSubmissionList(SUBMISSION_LIST);
        updateJobRequest.setEvergreen(true);
        updateJobRequest.setStage(JobOpportunityStage.jobOffer);

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));
        given(salesforceJobOppRepository.save(any(SalesforceJobOpp.class))).willReturn(emptyJob1);
        given(salesforceJobOppRepository.save(shortJob)).willReturn(shortJob);
        given(savedListService.createSavedList(any(UpdateSavedListInfoRequest.class)))
            .willReturn(SUBMISSION_LIST);

        SalesforceJobOpp result = jobService.updateJob(JOB_ID, updateJobRequest);

        assertNotNull(result.getEvergreenChild());
    }

    @Test
    @DisplayName("should not create evergreen child opp when already set")
    void updateJob_shouldNotCreateEvergreenChildOpp_whenAlreadySet() {
        shortJob.setEvergreenChild(emptyJob1);
        updateJobRequest.setEvergreen(true);
        updateJobRequest.setStage(JobOpportunityStage.jobOffer);

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));
        given(salesforceJobOppRepository.save(shortJob)).willReturn(shortJob);

        SalesforceJobOpp result = jobService.updateJob(JOB_ID, updateJobRequest);

        assertEquals(result.getEvergreenChild(), emptyJob1);
    }

    @Test
    @DisplayName("should not create evergreen child opp when already set")
    void updateJob_shouldNotCreateEvergreenChildOpp_whenEvergreenSetToFalse() {
        shortJob.setEvergreenChild(null);
        updateJobRequest.setEvergreen(false);
        updateJobRequest.setStage(JobOpportunityStage.jobOffer);

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));
        given(salesforceJobOppRepository.save(shortJob)).willReturn(shortJob);

        SalesforceJobOpp result = jobService.updateJob(JOB_ID, updateJobRequest);

        assertNull(result.getEvergreenChild());
    }

    @Test
    @DisplayName("should update summary and set audit fields")
    void updateJobSummary_shouldUpdateSummaryAndSetAuditFields() {
        String summary = "summary";
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));
        given(salesforceJobOppRepository.save(shortJob)).willReturn(shortJob);

        SalesforceJobOpp result = jobService.updateJobSummary(JOB_ID, summary);

        assertEquals(result.getJobSummary(), summary);
        assertEquals(result.getUpdatedBy(), adminUser);
    }

    @Test
    @DisplayName("should remove starring user when starred is false")
    void updateStarred_shouldRemoveStarringUser_whenStarredIsFalse() {
        shortJob.setStarringUsers(new HashSet<>(Set.of(adminUser)));
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));
        given(salesforceJobOppRepository.save(shortJob)).willReturn(shortJob);

        SalesforceJobOpp result = jobService.updateStarred(JOB_ID, false);

        assertFalse(result.getStarringUsers().contains(adminUser));
    }

    @Test
    @DisplayName("should add starring user when starred is true")
    void updateStarred_shouldAddStarringUser_whenStarredIsTrue() {
        shortJob.setStarringUsers(new HashSet<>(Collections.emptySet()));
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));
        given(salesforceJobOppRepository.save(shortJob)).willReturn(shortJob);

        SalesforceJobOpp result = jobService.updateStarred(JOB_ID, true);

        assertTrue(result.getStarringUsers().contains(adminUser));
    }

    @Test
    @DisplayName("should upload JD")
    void uploadJd() throws IOException {
        stubFileUpload();

        SalesforceJobOpp result = jobService.uploadJd(JOB_ID, mockFile);

        assertEquals(result.getSubmissionList().getFileJdLink(), URL);
        assertEquals(result.getSubmissionList().getFileJdName(), NAME);
        verify(savedListService).saveIt(result.getSubmissionList());
    }

    @Test
    @DisplayName("should throw when no submission list")
    void uploadJd_shouldThrow_whenNoSubmissionList() {
        shortJob.setSubmissionList(null);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));

        assertThrows(InvalidRequestException.class, () -> jobService.uploadJd(JOB_ID, mockFile));
    }

    @Test
    @DisplayName("should upload JOI")
    void uploadJoi() throws IOException {
        stubFileUpload();

        SalesforceJobOpp result = jobService.uploadJoi(JOB_ID, mockFile);

        assertEquals(result.getSubmissionList().getFileJoiLink(), URL);
        assertEquals(result.getSubmissionList().getFileJoiName(), NAME);
        verify(savedListService).saveIt(result.getSubmissionList());
    }

    @Test
    @DisplayName("should throw when no submission list")
    void uploadJoi_shouldThrow_whenNoSubmissionList() {
        shortJob.setSubmissionList(null);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));

        assertThrows(InvalidRequestException.class, () -> jobService.uploadJoi(JOB_ID, mockFile));
    }

    @Test
    @DisplayName("should upload interview guidance")
    void uploadInterviewGuidance() throws IOException {
        stubFileUpload();

        SalesforceJobOpp result = jobService.uploadInterviewGuidance(JOB_ID, mockFile);

        assertEquals(result.getSubmissionList().getFileInterviewGuidanceLink(), URL);
        assertEquals(result.getSubmissionList().getFileInterviewGuidanceName(), NAME);
        verify(savedListService).saveIt(result.getSubmissionList());
    }

    @Test
    @DisplayName("should throw when no submission list")
    void uploadInterviewGuidance_shouldThrow_whenNoSubmissionList() {
        shortJob.setSubmissionList(null);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));

        assertThrows(InvalidRequestException.class,
            () -> jobService.uploadInterviewGuidance(JOB_ID, mockFile));
    }

    @Test
    @DisplayName("should upload MOU")
    void uploadMou() throws IOException {
        stubFileUpload();

        SalesforceJobOpp result = jobService.uploadMou(JOB_ID, mockFile);

        assertEquals(result.getSubmissionList().getFileMouLink(), URL);
        assertEquals(result.getSubmissionList().getFileMouName(), NAME);
        verify(savedListService).saveIt(result.getSubmissionList());
    }

    @Test
    @DisplayName("should throw when no submission list")
    void uploadMou_shouldThrow_whenNoSubmissionList() {
        shortJob.setSubmissionList(null);
        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(shortJob));

        assertThrows(InvalidRequestException.class, () -> jobService.uploadMou(JOB_ID, mockFile));
    }

    private void stubFileUpload() throws IOException {
        InputStream mockStream = mock(InputStream.class);
        GoogleFileSystemFile mockGoogleFile = mock(GoogleFileSystemFile.class);

        given(salesforceJobOppRepository.findById(JOB_ID)).willReturn(Optional.of(longJob));
        given(mockFile.getOriginalFilename()).willReturn(NAME);
        given(mockFile.getInputStream()).willReturn(mockStream);
        given(mockStream.read(any())).willReturn(-1);
        given(googleDriveConfig.getListFoldersDrive()).willReturn(mock(GoogleFileSystemDrive.class));
        given(fileSystemService.uploadFile(
            any(GoogleFileSystemDrive.class),
            any(GoogleFileSystemFolder.class),
            anyString(),
            any(File.class))
        ).willReturn(mockGoogleFile);
        given(mockGoogleFile.getName()).willReturn(NAME);
        given(mockGoogleFile.getUrl()).willReturn(URL);
    }

}
