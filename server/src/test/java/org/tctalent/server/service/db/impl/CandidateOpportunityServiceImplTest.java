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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.CandidateOpportunityTestData.createUpdateCandidateOppRequestAndExpectedOpp;
import static org.tctalent.server.data.CandidateOpportunityTestData.getCandidateOpp;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.CandidateTestData.getListOfCandidates;
import static org.tctalent.server.data.CountryTestData.UNITED_KINGDOM;
import static org.tctalent.server.data.OpportunityTestData.getOpportunityForCandidate;
import static org.tctalent.server.data.PartnerImplTestData.getDefaultPartner;
import static org.tctalent.server.data.PartnerImplTestData.getDestinationPartner;
import static org.tctalent.server.data.PartnerImplTestData.getSourcePartner;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppExtended;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppMinimal;
import static org.tctalent.server.data.UserTestData.getAdminUser;
import static org.tctalent.server.data.UserTestData.getFullUser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
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
import org.tctalent.server.data.CandidateOpportunityTestData.CreateUpdateCandidateOppTestData;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.repository.db.CandidateOpportunitySpecification;
import org.tctalent.server.request.candidate.UpdateCandidateOppsRequest;
import org.tctalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tctalent.server.request.candidate.dependant.UpdateRelocatingDependantIds;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SystemNotificationService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.SalesforceHelper;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@ExtendWith(MockitoExtension.class)
public class CandidateOpportunityServiceImplTest {

    private CandidateOpportunity candidateOpp;
    private Opportunity sfOpp;
    private List<Candidate> candidateList;
    private UpdateCandidateOppsRequest updateRequest;
    private CandidateOpportunity expectedOpp;
    private CandidateOpportunityParams params;
    private SalesforceJobOpp jobOpp;
    private User adminUser;
    private Candidate candidate;
    private List<CandidateOpportunity> candidateOppList;
    private String nextStep;

    private static final Specification<CandidateOpportunity> FAKE_SPEC = (root, query, cb) -> null;

    @Mock private SalesforceService salesforceService;
    @Mock private CandidateService candidateService;
    @Mock private SalesforceJobOppService salesforceJobOppService;
    @Mock private CandidateOpportunityRepository candidateOpportunityRepository;
    @Mock private UserService userService;
    @Mock private NextStepProcessingService nextStepProcessingService;
    @Mock private SystemNotificationService systemNotificationService;
    @Mock private AuthService authService;
    @Mock private SalesforceHelper salesforceHelper;
    @Mock private MultipartFile mockFile;
    @Mock private GoogleDriveConfig googleDriveConfig;
    @Mock private FileSystemService fileSystemService;

    @Captor ArgumentCaptor<CandidateOpportunity> oppCaptor;
    @Captor ArgumentCaptor<UpdateCandidateStatusInfo> statusInfoCaptor;
    @Captor ArgumentCaptor<Candidate> candidateCaptor;

    @InjectMocks CandidateOpportunityServiceImpl candidateOpportunityService;

    @BeforeEach
    void setUp() {
        candidateOpp = getCandidateOpp();
        sfOpp = getOpportunityForCandidate();
        candidateList = getListOfCandidates();
        CreateUpdateCandidateOppTestData data = createUpdateCandidateOppRequestAndExpectedOpp();
        updateRequest = data.request();
        expectedOpp = data.expectedOpp();
        params = data.request().getCandidateOppParams();
        nextStep = params.getNextStep();
        jobOpp = getSalesforceJobOppMinimal();
        adminUser = getAdminUser();
        candidate = getCandidate();
        candidateOppList = List.of(candidateOpp, candidateOpp);
        // Set up adminUser with a Partner
        adminUser.setPartner(getSourcePartner()); // Already done, kept for clarity

        // Ensure candidate and candidateList have a User with a Partner
        candidate.setUser(adminUser);
        for (Candidate c : candidateList) {
            c.setUser(adminUser);
        }

        // Ensure candidateOpp's Candidate has a User with a Partner
        Candidate oppCandidate = candidateOpp.getCandidate();
        if (oppCandidate == null) {
            oppCandidate = new Candidate();
            candidateOpp.setCandidate(oppCandidate);
        }
        oppCandidate.setUser(adminUser); // Set the same adminUser with Partner

        // Ensure expectedOpp's Candidate has a User with a Partner
        Candidate expectedOppCandidate = expectedOpp.getCandidate();
        if (expectedOppCandidate == null) {
            expectedOppCandidate = new Candidate();
            expectedOpp.setCandidate(expectedOppCandidate);
        }
        expectedOppCandidate.setUser(adminUser); // Set the same adminUser with Partner
    }

    @Test
    @DisplayName("should return id of salesforce opp found")
    void fetchSalesforceId_shouldReturnIdOfSalesforceOpp_whenFound() {
        given(salesforceService.findCandidateOpportunity(
            candidateOpp.getCandidate().getCandidateNumber(), candidateOpp.getJobOpp().getSfId())
        ).willReturn(sfOpp);

        String result = candidateOpportunityService.fetchSalesforceId(candidateOpp);

        assertEquals(sfOpp.getId(), result);
    }

    @Test
    @DisplayName("should return null when salesforce opp not found")
    void fetchSalesforceId_shouldReturnNull_whenSalesforceOppNotFound() {
        given(salesforceService.findCandidateOpportunity(
            candidateOpp.getCandidate().getCandidateNumber(), candidateOpp.getJobOpp().getSfId())
        ).willReturn(null);

        assertNull(candidateOpportunityService.fetchSalesforceId(candidateOpp));
    }

    @Test
    @DisplayName("should update salesforce first")
    void createUpdateCandidateOpportunities_shouldUpdateSalesforceFirst() {
        given(candidateService.findByIds(updateRequest.getCandidateIds()))
            .willReturn(candidateList);
        given(salesforceJobOppService.getOrCreateJobOppFromId(updateRequest.getSfJobOppId()))
            .willReturn(jobOpp);
        given(salesforceJobOppService.updateJob(jobOpp)).willReturn(jobOpp);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(salesforceService.findCandidateOpportunity(eq(null), anyString()))
            .willReturn(sfOpp);

        candidateOpportunityService.createUpdateCandidateOpportunities(updateRequest);

        InOrder inOrder = inOrder(salesforceService, candidateOpportunityRepository);
        inOrder.verify(salesforceService).createOrUpdateCandidateOpportunities(candidateList,
            params, jobOpp);
        inOrder.verify(candidateOpportunityRepository).save(any(CandidateOpportunity.class));
    }

    @Test
    @DisplayName("should update opp as expected")
    void createUpdateCandidateOpportunities_shouldUpdateOppAsExpected() {
        stubUpdateCandidateOpp();

        candidateOpportunityService.createUpdateCandidateOpportunities(updateRequest);

        assertThat(oppCaptor.getValue())
            .usingRecursiveComparison()
            .ignoringFields("candidate.createdDate", "candidate.updatedDate",
                "createdBy", "createdDate", "updatedDate")
            .isEqualTo(expectedOpp);
    }

    @Test
    @DisplayName("should update opp when user partner is default source partner but not the candidate partner")
    void createUpdateCandidateOpportunities_shouldUpdateOpp_whenUserPartnerIsDefaultSourcePartnerButNotCandidatePartner() {
        // Admin user has default source partner, candidate has different partner
        adminUser.setPartner(getDefaultPartner());
        candidateList.get(0).setUser(getFullUser());

        stubUpdateCandidateOpp();

        candidateOpportunityService.createUpdateCandidateOpportunities(updateRequest);

        assertThat(oppCaptor.getValue())
            .usingRecursiveComparison()
            .ignoringFields("candidate.createdDate", "candidate.updatedDate",
                "createdBy", "createdDate", "updatedDate", "updatedBy")
            .isEqualTo(expectedOpp);
    }

    private void stubUpdateCandidateOpp() {
        given(candidateService.findByIds(updateRequest.getCandidateIds()))
            .willReturn(candidateList);
        given(salesforceJobOppService.getOrCreateJobOppFromId(updateRequest.getSfJobOppId()))
            .willReturn(jobOpp);
        given(salesforceJobOppService.updateJob(jobOpp)).willReturn(jobOpp);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(candidateOpportunityRepository.findByCandidateIdAndJobId(null, jobOpp.getId()))
            .willReturn(candidateOpp);
        given(candidateOpportunityRepository.save(oppCaptor.capture())).willReturn(candidateOpp);
        given(nextStepProcessingService.processNextStep(candidateOpp, nextStep)).willReturn(nextStep);
    }

    @Test
    @DisplayName("should create opp as expected when none exists")
    void createUpdateCandidateOpportunities_shouldCreateOppAsExpected_whenNoneExists() {
        final String nextStep = expectedOpp.getNextStep();
        given(candidateService.findByIds(updateRequest.getCandidateIds()))
            .willReturn(candidateList);
        given(salesforceJobOppService.getOrCreateJobOppFromId(updateRequest.getSfJobOppId()))
            .willReturn(jobOpp);
        given(salesforceJobOppService.updateJob(jobOpp)).willReturn(jobOpp);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(candidateOpportunityRepository.findByCandidateIdAndJobId(null, jobOpp.getId()))
            .willReturn(null); // candidate opp not found - sets up create path
        given(salesforceService.generateCandidateOppName(any(Candidate.class),
            any(SalesforceJobOpp.class))).willReturn(expectedOpp.getName());
        given(candidateOpportunityRepository.save(oppCaptor.capture())).willReturn(candidateOpp);
        given(nextStepProcessingService.processNextStep(any(CandidateOpportunity.class), anyString()))
            .willReturn(nextStep);

        candidateOpportunityService.createUpdateCandidateOpportunities(updateRequest);
        CandidateOpportunity result = oppCaptor.getValue();
        assertEquals(nextStep, result.getNextStep());
        assertEquals(expectedOpp.getNextStepDueDate(), result.getNextStepDueDate());
        assertEquals(expectedOpp.getStage(), result.getStage());
        assertEquals(expectedOpp.getClosingComments(), result.getClosingComments());
        assertEquals(expectedOpp.getEmployerFeedback(), result.getEmployerFeedback());
        assertEquals(expectedOpp.getUpdatedBy(), result.getUpdatedBy());
        verify(systemNotificationService, times(3))
            .notifyNewCase(any(CandidateOpportunity.class));
    }

    @Test
    @DisplayName("should update opps as expected when candidate collection passed")
    void createUpdateCandidateOpportunities_shouldUpdateOppAsExpected_whenPassedCandidateCollection() {
        setUpUpdateCandidateOppPath();

        candidateOpportunityService.createUpdateCandidateOpportunities(candidateList, jobOpp,
            params);

        assertThat(oppCaptor.getValue())
            .usingRecursiveComparison()
            .ignoringFields("candidate.createdDate", "candidate.updatedDate",
                "createdBy", "createdDate", "updatedDate")
            .isEqualTo(expectedOpp);
    }

    @Test
    @DisplayName("should change candidate status to employed when new stage is employed")
    void createUpdateCandidateOpportunities_shouldChangeCandidateStatusToEmployed() {
        // Employed stage
        params.setStage(CandidateOpportunityStage.relocating);

        setUpUpdateCandidateOppPath();

        candidateOpportunityService.createUpdateCandidateOpportunities(candidateList, jobOpp,
            params);

        verify(candidateService, times(3)).
            updateCandidateStatus(any(Candidate.class), statusInfoCaptor.capture());
        List<UpdateCandidateStatusInfo> statusInfos = statusInfoCaptor.getAllValues();
        assertEquals(CandidateStatus.employed, statusInfos.get(0).getStatus());
    }

    @Test
    @DisplayName("should change candidate status to ineligible when stage indicates")
    void createUpdateCandidateOpportunities_shouldChangeCandidateStatusToIneligible() {
        // Stage indicating ineligibility
        updateRequest.getCandidateOppParams().setStage(CandidateOpportunityStage.notEligibleForTC);

        setUpUpdateCandidateOppPath();

        candidateOpportunityService.createUpdateCandidateOpportunities(candidateList, jobOpp,
            updateRequest.getCandidateOppParams());

        verify(candidateService, times(3)).
            updateCandidateStatus(any(Candidate.class), statusInfoCaptor.capture());
        List<UpdateCandidateStatusInfo> statusInfos = statusInfoCaptor.getAllValues();
        assertEquals(CandidateStatus.ineligible, statusInfos.get(0).getStatus());
    }

    @Test
    @DisplayName("should change candidate status to relocatedIndependently when stage indicates relocated via "
        + "no job offer pathway")
    void createUpdateCandidateOpportunities_shouldChangeCandidateStatusToWithdrawn() {
        // Stage indicating ineligibility
        updateRequest.getCandidateOppParams()
            .setStage(CandidateOpportunityStage.relocatedNoJobOfferPathway);

        setUpUpdateCandidateOppPath();

        candidateOpportunityService.createUpdateCandidateOpportunities(candidateList, jobOpp,
            updateRequest.getCandidateOppParams());

        verify(candidateService, times(3)).
            updateCandidateStatus(any(Candidate.class), statusInfoCaptor.capture());
        List<UpdateCandidateStatusInfo> statusInfos = statusInfoCaptor.getAllValues();
        assertEquals(CandidateStatus.relocatedIndependently, statusInfos.get(0).getStatus());
    }

    @Test
    @DisplayName("should notify case changes when params non null")
    void createUpdateCandidateOpportunities_shouldNotifyCaseChanges_whenParamsNonNull() {
        setUpUpdateCandidateOppPath();

        candidateOpportunityService.createUpdateCandidateOpportunities(candidateList, jobOpp,
            updateRequest.getCandidateOppParams());

        verify(systemNotificationService, times(3))
            .notifyCaseChanges(any(CandidateOpportunity.class), any(CandidateOpportunityParams.class));
    }

    @Test
    @DisplayName("should set relocated address when stage indicates")
    void createUpdateCandidateOpportunities_shouldSetRelocatedAddress() {
        updateRequest.getCandidateOppParams().setStage(CandidateOpportunityStage.relocated);
        setUpUpdateCandidateOppPath();

        candidateOpportunityService.createUpdateCandidateOpportunities(candidateList, jobOpp,
            updateRequest.getCandidateOppParams());

        verify(candidateService).save(candidateCaptor.capture(), eq(false));
        assertEquals(UNITED_KINGDOM, candidateCaptor.getValue().getRelocatedCountry());
    }

    private void setUpUpdateCandidateOppPath() {
        given(salesforceJobOppService.updateJob(jobOpp)).willReturn(jobOpp);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(candidateOpportunityRepository.findByCandidateIdAndJobId(null, jobOpp.getId()))
            .willReturn(candidateOpp);
        given(candidateOpportunityRepository.save(oppCaptor.capture())).willReturn(candidateOpp);
        given(nextStepProcessingService.processNextStep(any(CandidateOpportunity.class), anyString()))
            .willReturn(nextStep);
    }

    @Test
    @DisplayName("should return opp when found")
    void findOpp_shouldReturnOpp() {
        given(candidateOpportunityRepository.findByCandidateIdAndJobId(anyLong(), anyLong()))
            .willReturn(candidateOpp);

        CandidateOpportunity result = candidateOpportunityService.findOpp(candidate, jobOpp);

        assertEquals(candidateOpp, result);
    }

    @Test
    @DisplayName("should return opps when partner is of right type and opps found")
    void findJobCreatorPartnerOpps_shouldReturnOpps() {
        PartnerImpl destinationPartner = getDestinationPartner();
        List<CandidateOpportunity> returnedOpps = List.of(candidateOpp);
        given(candidateOpportunityRepository.findPartnerOpps(anyLong()))
            .willReturn(returnedOpps);

        List<CandidateOpportunity> result =
            candidateOpportunityService.findJobCreatorPartnerOpps(destinationPartner);

        assertEquals(returnedOpps, result);
        verify(candidateOpportunityRepository).findPartnerOpps(anyLong());
    }

    @Test
    @DisplayName("should return empty list when partner is not job creator")
    void findJobCreatorPartnerOpps_shouldReturnEmptyList() {
        PartnerImpl sourcePartner = getSourcePartner();

        List<CandidateOpportunity> result =
            candidateOpportunityService.findJobCreatorPartnerOpps(sourcePartner);

        assertTrue(result.isEmpty());
        verify(candidateOpportunityRepository, never()).findPartnerOpps(anyLong());
    }

    @Test
    @DisplayName("should return CandidateOpportunity when found")
    void getCandidateOpportunity_shouldReturnOpportunity_whenFound() {
        given(candidateOpportunityRepository.findById(1L)).willReturn(Optional.of(candidateOpp));

        CandidateOpportunity result = candidateOpportunityService.getCandidateOpportunity(1L);

        assertEquals(candidateOpp, result);
        verify(candidateOpportunityRepository).findById(1L);
    }

    @Test
    @DisplayName("should throw NoSuchObjectException when not found")
    void getCandidateOpportunity_shouldThrow_whenNotFound() {
        given(candidateOpportunityRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateOpportunityService.getCandidateOpportunity(1L));
    }

    @Test
    @DisplayName("should deduplicate SF IDs and copy and save each opp to TC")
    void loadCandidateOpportunities_shouldCopyAndSaveEachOppToTc() {
        String[] inputIds = {"id1", "id2", "id1", "id3", "id4"};
        String[] expectedIds = {"id1", "id2", "id3", "id4"};

        SalesforceJobOpp differentJob = getSalesforceJobOppExtended();
        Candidate differentCandidate = new Candidate();
        String inputDateTime = "2023-06-01T00:21:58.000+0000";
        sfOpp.setLastModifiedDate(inputDateTime);
        sfOpp.setCreatedDate(inputDateTime);
        OffsetDateTime convertedDateTime =
            SalesforceHelper.parseSalesforceOffsetDateTime(inputDateTime);

        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(salesforceService.findCandidateOpportunitiesByJobOpps(any(String[].class)))
            .willReturn(List.of(sfOpp, sfOpp, sfOpp, sfOpp));
        given(candidateOpportunityRepository.findBySfId(sfOpp.getId()))
            .willReturn(Optional.of(candidateOpp));
        given(salesforceJobOppService.getJobOppById(anyString())).willReturn(differentJob);
        given(candidateService.findByCandidateNumber(anyString())).willReturn(differentCandidate);

        candidateOpportunityService.loadCandidateOpportunities(inputIds); // When

        // Deduplication:
        ArgumentCaptor<String[]> idCaptor = ArgumentCaptor.forClass(String[].class);
        verify(salesforceService).findCandidateOpportunitiesByJobOpps(idCaptor.capture());
        assertArrayEquals(expectedIds, idCaptor.getValue());

        // Fields set as expected:
        assertEquals(differentJob, candidateOpp.getJobOpp());
        assertEquals(differentCandidate, candidateOpp.getCandidate());
        assertEquals(CandidateOpportunityStage.relocated, candidateOpp.getStage());
        assertEquals(sfOpp.getNextStep(), candidateOpp.getNextStep());
        assertEquals(sfOpp.getClosingCommentsForCandidate(),
            candidateOpp.getClosingCommentsForCandidate());
        assertEquals(convertedDateTime, candidateOpp.getCreatedDate());
        assertEquals(LocalDate.parse(sfOpp.getNextStepDueDate()), candidateOpp.getNextStepDueDate());
        assertEquals(convertedDateTime, candidateOpp.getUpdatedDate());

        verify(candidateOpportunityRepository, times(4)).save(candidateOpp);
    }

    @Test
    @DisplayName("should return IDs of opps with unread chats")
    void findUnreadChatsInOpps_shouldReturnOppsWithUnreadChats() {
        List<Long> unreadChatIds = List.of(1L, 2L, 3L);
        SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(candidateOpportunityRepository.findAll(any(Specification.class)))
            .willReturn(candidateOppList);
        given(candidateOpportunityRepository.findUnreadChatsInOpps(anyLong(), anyList()))
            .willReturn(unreadChatIds);

        try (MockedStatic<CandidateOpportunitySpecification> mockedStatic =
            mockStatic(CandidateOpportunitySpecification.class)) {
            mockedStatic.when(() -> CandidateOpportunitySpecification.buildSearchQuery(any(
                    SearchCandidateOpportunityRequest.class), any(User.class)))
                .thenReturn(FAKE_SPEC);

            assertEquals(unreadChatIds, candidateOpportunityService.findUnreadChatsInOpps(request));
        }
    }

    @Test
    @DisplayName("should return matching candidate opps")
    void searchCandidateOpps_shouldReturnOpps() {
        Page<CandidateOpportunity> oppsPage = new PageImpl<>(candidateOppList);
        SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(candidateOpportunityRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .willReturn(oppsPage);

        try (MockedStatic<CandidateOpportunitySpecification> mockedStatic =
            mockStatic(CandidateOpportunitySpecification.class)) {
            mockedStatic.when(() -> CandidateOpportunitySpecification.buildSearchQuery(any(
                    SearchCandidateOpportunityRequest.class), any(User.class)))
                .thenReturn(FAKE_SPEC);

            assertEquals(oppsPage, candidateOpportunityService.searchCandidateOpportunities(request));
        }
    }

    @Test
    @DisplayName("should update opp as expected")
    void updateCandidateOpportunity_shouldUpdateOppAsExpected() {
        given(candidateOpportunityRepository.findById(1L)).willReturn(Optional.of(candidateOpp));
        given(candidateOpportunityRepository.findByCandidateIdAndJobId(anyLong(), anyLong()))
            .willReturn(candidateOpp);
        given(salesforceJobOppService.updateJob(any(SalesforceJobOpp.class))).willReturn(jobOpp);
        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(nextStepProcessingService.processNextStep(candidateOpp, params.getNextStep())).willReturn(nextStep);
        given(candidateOpportunityRepository.save(oppCaptor.capture())).willReturn(candidateOpp);

        candidateOpportunityService.updateCandidateOpportunity(1L, params);

        assertThat(oppCaptor.getValue())
            .usingRecursiveComparison()
            .ignoringFields("candidate.createdDate", "candidate.updatedDate",
                "createdBy", "createdDate", "updatedDate")
            .isEqualTo(expectedOpp);
    }

    @Test
    @DisplayName("should throw when opp has no associated candidate")
    void uploadOffer_shouldThrow_whenOppHasNoAssociatedCandidate() {
        candidateOpp.setCandidate(null);
        given(candidateOpportunityRepository.findById(1L)).willReturn(Optional.of(candidateOpp));

        assertThrows(InvalidRequestException.class,
            () -> candidateOpportunityService.uploadOffer(1L, mockFile));
    }

    @Test
    @DisplayName("should upload offer")
    void uploadOffer_shouldUploadOffer() throws IOException {
        GoogleFileSystemFile mockGoogleFile = mock(GoogleFileSystemFile.class);
        InputStream mockStream = mock(InputStream.class);
        final String fileLink = "url";
        final String fileName = "name";

        given(candidateOpportunityRepository.findById(1L)).willReturn(Optional.of(candidateOpp));
        given(mockFile.getInputStream()).willReturn(mockStream);
        given(mockFile.getOriginalFilename()).willReturn("filename");
        given(googleDriveConfig.getCandidateDataDrive()).willReturn(mock(GoogleFileSystemDrive.class));
        given(fileSystemService.uploadFile(
            any(GoogleFileSystemDrive.class),
            any(GoogleFileSystemFolder.class),
            anyString(),
            any(File.class)
        )).willReturn(mockGoogleFile);
        given(mockGoogleFile.getUrl()).willReturn(fileLink);
        given(mockGoogleFile.getName()).willReturn(fileName);

        given(candidateOpportunityRepository.save(candidateOpp)).willReturn(candidateOpp);

        CandidateOpportunity result = candidateOpportunityService.uploadOffer(1L, mockFile);

        assertEquals(fileLink, result.getFileOfferLink());
        assertEquals(fileName, result.getFileOfferName());
    }

    @Test
    @DisplayName("should successfully update relocating dependents")
    void updateRelocatingDependents_shouldUpdateRelocatingDependents() {
        UpdateRelocatingDependantIds ids = new UpdateRelocatingDependantIds();
        ids.setRelocatingDependantIds(List.of(1L, 2L, 3L));

        given(candidateOpportunityRepository.findById(1L)).willReturn(Optional.of(candidateOpp));
        given(candidateOpportunityRepository.save(candidateOpp)).willReturn(candidateOpp);

        candidateOpportunityService.updateRelocatingDependants(1L, ids);

        assertEquals(ids.getRelocatingDependantIds(), candidateOpp.getRelocatingDependantIds());
    }

    @Test
    @DisplayName("should successfully update tc opp from sf equivalent")
    void processCaseUpdateBatch_shouldSuccessfullyUpdateBatch() {
        given(candidateOpportunityRepository.findBySfId(sfOpp.getId()))
            .willReturn(Optional.of(candidateOpp));

        candidateOpportunityService.processCaseUpdateBatch(List.of(sfOpp));

        verify(systemNotificationService).notifyCaseChanges(any(CandidateOpportunity.class),
            any(CandidateOpportunityParams.class));
    }

    @Test
    @DisplayName("should catch ex and continue when next step due date invalid")
    void processCaseUpdateBatch_shouldTCatchAndContinue_whenNextStepDueDateInvalid() {
        sfOpp.setNextStepDueDate("Tuesday");
        given(candidateOpportunityRepository.findBySfId(sfOpp.getId()))
            .willReturn(Optional.of(candidateOpp));

        candidateOpportunityService.processCaseUpdateBatch(List.of(sfOpp));
    }

    @Test
    @DisplayName("should return result of repo call")
    void findAllNonNullSfIdsByClosedFalse_shouldReturnRepoCall() {
        List<String> result = List.of("id1", "id2", "id3");
        given(candidateOpportunityRepository.findAllNonNullSfIdsByClosedFalse())
            .willReturn(result);

        assertEquals(result, candidateOpportunityService.findAllNonNullSfIdsByClosedFalse());
    }

}
