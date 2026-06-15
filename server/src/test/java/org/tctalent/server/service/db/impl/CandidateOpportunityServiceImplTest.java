/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
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
import java.util.Set;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.data.CandidateOpportunityTestData.CreateUpdateCandidateOppTestData;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
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

        verify(candidateService).save(candidateCaptor.capture());
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

    @Test
    @DisplayName("should return fully visible candidate IDs for job creator partner")
    void findFullyVisibleCandidateIds_shouldReturnIds_whenPartnerIsJobCreator() {
        PartnerImpl destinationPartner = getDestinationPartner();
        Set<Long> expectedIds = Set.of(1L, 2L, 3L);

        given(candidateOpportunityRepository.findFullyVisibleCandidateIds(destinationPartner.getId()))
            .willReturn(expectedIds);

        Set<Long> result =
            candidateOpportunityService.findFullyVisibleCandidateIds(destinationPartner);

        assertEquals(expectedIds, result);
        verify(candidateOpportunityRepository)
            .findFullyVisibleCandidateIds(destinationPartner.getId());
    }

    @Test
    @DisplayName("should return empty fully visible candidate IDs for null or non job creator partner")
    void findFullyVisibleCandidateIds_shouldReturnEmpty_whenPartnerNullOrNotJobCreator() {
        assertTrue(candidateOpportunityService.findFullyVisibleCandidateIds(null).isEmpty());
        assertTrue(candidateOpportunityService.findFullyVisibleCandidateIds(getSourcePartner()).isEmpty());

        verify(candidateOpportunityRepository, never()).findFullyVisibleCandidateIds(anyLong());
    }

    @Test
    @DisplayName("should return fully visible user IDs for job creator partner")
    void findFullyVisibleUserIds_shouldReturnIds_whenPartnerIsJobCreator() {
        PartnerImpl destinationPartner = getDestinationPartner();
        Set<Long> expectedIds = Set.of(10L, 20L, 30L);

        given(candidateOpportunityRepository.findFullyVisibleUserIds(destinationPartner.getId()))
            .willReturn(expectedIds);

        Set<Long> result =
            candidateOpportunityService.findFullyVisibleUserIds(destinationPartner);

        assertEquals(expectedIds, result);
        verify(candidateOpportunityRepository)
            .findFullyVisibleUserIds(destinationPartner.getId());
    }

    @Test
    @DisplayName("should return empty fully visible user IDs for null or non job creator partner")
    void findFullyVisibleUserIds_shouldReturnEmpty_whenPartnerNullOrNotJobCreator() {
        assertTrue(candidateOpportunityService.findFullyVisibleUserIds(null).isEmpty());
        assertTrue(candidateOpportunityService.findFullyVisibleUserIds(getSourcePartner()).isEmpty());

        verify(candidateOpportunityRepository, never()).findFullyVisibleUserIds(anyLong());
    }

    @Test
    @DisplayName("should throw when finding unread chats and user not logged in")
    void findUnreadChatsInOpps_shouldThrow_whenUserNotLoggedIn() {
        SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();

        given(userService.getLoggedInUser()).willReturn(null);

        assertThrows(InvalidSessionException.class,
            () -> candidateOpportunityService.findUnreadChatsInOpps(request));

        verify(candidateOpportunityRepository, never()).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("should throw when searching candidate opps and user not logged in")
    void searchCandidateOpportunities_shouldThrow_whenUserNotLoggedIn() {
        SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();

        given(userService.getLoggedInUser()).willReturn(null);

        assertThrows(InvalidSessionException.class,
            () -> candidateOpportunityService.searchCandidateOpportunities(request));

        verify(candidateOpportunityRepository, never())
            .findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("should throw when logged in user has no partner")
    void createUpdateCandidateOpportunities_shouldThrow_whenUserHasNoPartner() {
        adminUser.setPartner(null);
        given(userService.getLoggedInUser()).willReturn(adminUser);

        assertThrows(InvalidRequestException.class,
            () -> candidateOpportunityService.createUpdateCandidateOpportunities(updateRequest));

        verify(candidateService, never()).findByIds(anyList());
    }

    @Test
    @DisplayName("should throw when partner is not authorized for candidate")
    void createUpdateCandidateOpportunities_shouldThrow_whenPartnerUnauthorizedForCandidate() {
        User candidateUser = getFullUser();
        candidateUser.setPartner(getDestinationPartner());

        Candidate unauthorizedCandidate = getCandidate();
        unauthorizedCandidate.setUser(candidateUser);

        adminUser.setPartner(getSourcePartner());

        given(userService.getLoggedInUser()).willReturn(adminUser);
        given(candidateService.findByIds(updateRequest.getCandidateIds()))
            .willReturn(List.of(unauthorizedCandidate));
        given(salesforceJobOppService.getOrCreateJobOppFromId(updateRequest.getSfJobOppId()))
            .willReturn(jobOpp);

        assertThrows(InvalidRequestException.class,
            () -> candidateOpportunityService.createUpdateCandidateOpportunities(updateRequest));

        verify(salesforceService, never())
            .createOrUpdateCandidateOpportunities(anyList(), any(), any());
        verify(candidateOpportunityRepository, never()).save(any(CandidateOpportunity.class));
    }

    @Test
    @DisplayName("collection update should throw when logged in user has no partner")
    void createUpdateCandidateOpportunitiesCollection_shouldThrow_whenUserHasNoPartner() {
        adminUser.setPartner(null);
        given(userService.getLoggedInUser()).willReturn(adminUser);

        assertThrows(InvalidRequestException.class,
            () -> candidateOpportunityService.createUpdateCandidateOpportunities(
                candidateList, jobOpp, params));

        verify(candidateService, never()).upsertCandidatesToSf(anyList());
    }

    @Test
    @DisplayName("collection update should only upsert candidates when job opp is null")
    void createUpdateCandidateOpportunitiesCollection_shouldOnlyUpsertCandidates_whenJobOppNull() {
        given(userService.getLoggedInUser()).willReturn(adminUser);

        candidateOpportunityService.createUpdateCandidateOpportunities(List.of(), null, params);

        verify(candidateService).upsertCandidatesToSf(List.of());
        verify(salesforceService, never())
            .createOrUpdateCandidateOpportunities(anyList(), any(), any());
        verify(candidateOpportunityRepository, never()).save(any(CandidateOpportunity.class));
    }

    @Test
    @DisplayName("collection update should not refresh recently updated job")
    void createUpdateCandidateOpportunitiesCollection_shouldNotRefreshRecentlyUpdatedJob() {
        jobOpp.setUpdatedDate(OffsetDateTime.now());

        given(userService.getLoggedInUser()).willReturn(adminUser);

        candidateOpportunityService.createUpdateCandidateOpportunities(List.of(), jobOpp, null);

        verify(candidateService).upsertCandidatesToSf(List.of());
        verify(salesforceJobOppService, never()).updateJob(jobOpp);
        verify(salesforceService).createOrUpdateCandidateOpportunities(List.of(), null, jobOpp);
    }

    @Test
    @DisplayName("should load candidate opportunities in chunks of ten")
    void loadCandidateOpportunities_shouldLoadInChunksOfTen() {
        String[] inputIds = {
            "job1", "job2", "job3", "job4", "job5", "job6",
            "job7", "job8", "job9", "job10", "job11", "job12"
        };

        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(salesforceService.findCandidateOpportunitiesByJobOpps(any(String[].class)))
            .willReturn(List.of());

        candidateOpportunityService.loadCandidateOpportunities(inputIds);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(salesforceService, times(2)).findCandidateOpportunitiesByJobOpps(captor.capture());

        assertEquals(10, captor.getAllValues().get(0).length);
        assertEquals(2, captor.getAllValues().get(1).length);
    }

    @Test
    @DisplayName("should load single Salesforce candidate opportunity and create job opp when requested")
    void loadCandidateOpportunity_shouldCreateLocalOppUsingGetOrCreateJobOpp() {
        Opportunity opportunity = getOpportunityForCandidate();
        opportunity.setCreatedDate(null);
        opportunity.setLastModifiedDate(null);
        opportunity.setNextStepDueDate(null);

        given(candidateOpportunityRepository.findBySfId(opportunity.getId()))
            .willReturn(Optional.empty());
        given(salesforceJobOppService.getOrCreateJobOppFromId(opportunity.getParentOpportunityId()))
            .willReturn(jobOpp);
        given(candidateService.findByCandidateNumber(opportunity.getCandidateId()))
            .willReturn(candidate);
        given(candidateOpportunityRepository.save(oppCaptor.capture()))
            .willReturn(candidateOpp);

        CandidateOpportunity result =
            candidateOpportunityService.loadCandidateOpportunity(opportunity);

        assertEquals(candidateOpp, result);

        CandidateOpportunity savedOpp = oppCaptor.getValue();
        assertEquals(jobOpp, savedOpp.getJobOpp());
        assertEquals(candidate, savedOpp.getCandidate());
        assertEquals(opportunity.getId(), savedOpp.getSfId());

        verify(salesforceJobOppService)
            .getOrCreateJobOppFromId(opportunity.getParentOpportunityId());
    }

    @Test
    @DisplayName("should default loaded candidate opportunity stage to prospect when SF stage is invalid")
    void loadCandidateOpportunity_shouldDefaultStageToProspect_whenSalesforceStageInvalid() {
        Opportunity opportunity = getOpportunityForCandidate();
        opportunity.setStageName("Definitely not a real stage");
        opportunity.setCreatedDate("not-a-date");
        opportunity.setLastModifiedDate("not-a-date");
        opportunity.setNextStepDueDate("not-a-date");

        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(candidateOpportunityRepository.findBySfId(opportunity.getId()))
            .willReturn(Optional.empty());
        given(salesforceJobOppService.getOrCreateJobOppFromId(opportunity.getParentOpportunityId()))
            .willReturn(jobOpp);
        given(candidateService.findByCandidateNumber(opportunity.getCandidateId()))
            .willReturn(candidate);
        given(candidateOpportunityRepository.save(oppCaptor.capture()))
            .willReturn(candidateOpp);

        candidateOpportunityService.loadCandidateOpportunity(opportunity);

        CandidateOpportunity savedOpp = oppCaptor.getValue();
        assertEquals(CandidateOpportunityStage.prospect, savedOpp.getStage());
    }

    @Test
    @DisplayName("process case update batch should return zero when TC opp is missing")
    void processCaseUpdateBatch_shouldReturnZero_whenTcOppMissing() {
        given(candidateOpportunityRepository.findBySfId(sfOpp.getId()))
            .willReturn(Optional.empty());

        int updates = candidateOpportunityService.processCaseUpdateBatch(List.of(sfOpp));

        assertEquals(0, updates);
        verify(systemNotificationService, never())
            .notifyCaseChanges(any(CandidateOpportunity.class), any(CandidateOpportunityParams.class));
    }

    @Test
    @DisplayName("process case update batch should return zero when SF opp has no valid changes")
    void processCaseUpdateBatch_shouldReturnZero_whenSalesforceOppHasNoValidChanges() {
        sfOpp.setNextStep(null);
        sfOpp.setNextStepDueDate(null);
        sfOpp.setStageName("Definitely not a real stage");
        sfOpp.setClosingComments(null);
        sfOpp.setClosingCommentsForCandidate(null);
        sfOpp.setEmployerFeedback(null);

        given(candidateOpportunityRepository.findBySfId(sfOpp.getId()))
            .willReturn(Optional.of(candidateOpp));

        int updates = candidateOpportunityService.processCaseUpdateBatch(List.of(sfOpp));

        assertEquals(0, updates);
        verify(candidateOpportunityRepository, never()).save(any(CandidateOpportunity.class));
    }

    @Test
    @DisplayName("process case update batch should return number of updated cases")
    void processCaseUpdateBatch_shouldReturnNumberOfUpdatedCases() {
        given(candidateOpportunityRepository.findBySfId(sfOpp.getId()))
            .willReturn(Optional.of(candidateOpp));

        int updates = candidateOpportunityService.processCaseUpdateBatch(List.of(sfOpp));

        assertEquals(1, updates);
    }

    @Test
    @DisplayName("should stop loading last active stages when Salesforce returns no opportunities")
    void loadCandidateOpportunityLastActiveStages_shouldStop_whenNoSalesforceOppsReturned() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(salesforceService.findCandidateOpportunities(null, 10))
            .willReturn(List.of());

        candidateOpportunityService.loadCandidateOpportunityLastActiveStages();

        verify(salesforceService).findCandidateOpportunities(null, 10);
        verify(salesforceService, never()).findOpportunityHistories(anyList());
        verify(candidateOpportunityRepository, never()).save(any(CandidateOpportunity.class));
    }

    @Test
    @DisplayName("process opp history should default last active stage to prospect when history is empty")
    void processOppHistory_shouldDefaultLastActiveStageToProspect_whenHistoryEmpty() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(candidateOpportunityRepository.findBySfId("sf-opp-1"))
            .willReturn(Optional.of(candidateOpp));

        ReflectionTestUtils.invokeMethod(
            candidateOpportunityService,
            "processOppHistory",
            "sf-opp-1",
            List.of()
        );

        assertEquals(CandidateOpportunityStage.prospect, candidateOpp.getLastActiveStage());
        verify(candidateOpportunityRepository).save(candidateOpp);
    }

    @Test
    @DisplayName("process opp history should do nothing when opp id is null")
    void processOppHistory_shouldDoNothing_whenOppIdNull() {
        ReflectionTestUtils.invokeMethod(
            candidateOpportunityService,
            "processOppHistory",
            null,
            List.of()
        );

        verify(candidateOpportunityRepository, never()).findBySfId(anyString());
        verify(candidateOpportunityRepository, never()).save(any(CandidateOpportunity.class));
    }

    @Test
    @DisplayName("upload offer should propagate IOException and not save")
    void uploadOffer_shouldThrowIOExceptionAndNotSave_whenInputStreamFails() throws IOException {
        given(candidateOpportunityRepository.findById(1L))
            .willReturn(Optional.of(candidateOpp));
        given(mockFile.getOriginalFilename()).willReturn("offer.pdf");
        given(mockFile.getInputStream()).willThrow(new IOException("stream failed"));

        assertThrows(IOException.class,
            () -> candidateOpportunityService.uploadOffer(1L, mockFile));

        verify(candidateOpportunityRepository, never()).save(any(CandidateOpportunity.class));
    }

    @Test
    @DisplayName("update relocating dependants should throw when opportunity is missing")
    void updateRelocatingDependants_shouldThrow_whenOpportunityMissing() {
        UpdateRelocatingDependantIds ids = new UpdateRelocatingDependantIds();
        ids.setRelocatingDependantIds(List.of(1L, 2L));

        given(candidateOpportunityRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateOpportunityService.updateRelocatingDependants(999L, ids));
    }

    @Test
    @DisplayName("partner authorization should allow default source partner")
    void isPartnerAuthorizedForCandidate_shouldAllowDefaultSourcePartner() {
        PartnerImpl defaultSourcePartner = getDefaultPartner();

        User candidateUser = getFullUser();
        candidateUser.setPartner(getDestinationPartner());

        Candidate candidate = getCandidate();
        candidate.setUser(candidateUser);

        assertTrue(candidateOpportunityService.isPartnerAuthorizedForCandidate(
            defaultSourcePartner, candidate));
    }

    @Test
    @DisplayName("partner authorization should reject non-default source partner for another partner candidate")
    void isPartnerAuthorizedForCandidate_shouldRejectSourcePartnerForDifferentCandidatePartner() {
        PartnerImpl sourcePartner = getSourcePartner();

        User candidateUser = getFullUser();
        candidateUser.setPartner(getDestinationPartner());

        Candidate candidate = getCandidate();
        candidate.setUser(candidateUser);

        assertTrue(sourcePartner.isSourcePartner());
        assertTrue(!sourcePartner.isDefaultSourcePartner());
        assertTrue(!candidateOpportunityService.isPartnerAuthorizedForCandidate(
            sourcePartner, candidate));
    }

    @Test
    @DisplayName("partner authorization should allow non-default source partner for own candidate")
    void isPartnerAuthorizedForCandidate_shouldAllowSourcePartnerForOwnCandidate() {
        PartnerImpl sourcePartner = getSourcePartner();

        User candidateUser = getFullUser();
        candidateUser.setPartner(sourcePartner);

        Candidate candidate = getCandidate();
        candidate.setUser(candidateUser);

        assertTrue(candidateOpportunityService.isPartnerAuthorizedForCandidate(
            sourcePartner, candidate));
    }

    @Test
    @DisplayName("partner authorization should allow non-source partner")
    void isPartnerAuthorizedForCandidate_shouldAllowNonSourcePartner() {
        Partner nonSourcePartner = mock(Partner.class);
        given(nonSourcePartner.isSourcePartner()).willReturn(false);

        User candidateUser = getFullUser();
        candidateUser.setPartner(getSourcePartner());

        Candidate candidate = getCandidate();
        candidate.setUser(candidateUser);

        assertTrue(candidateOpportunityService.isPartnerAuthorizedForCandidate(
            nonSourcePartner, candidate));
    }
}
