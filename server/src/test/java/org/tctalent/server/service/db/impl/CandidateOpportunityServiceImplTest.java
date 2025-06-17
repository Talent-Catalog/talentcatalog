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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.tctalent.server.data.CandidateOpportunityTestData.createUpdateCandidateOppRequestAndExpectedOpp;
import static org.tctalent.server.data.CandidateOpportunityTestData.getCandidateOpp;
import static org.tctalent.server.data.CandidateTestData.getListOfCandidates;
import static org.tctalent.server.data.OpportunityTestData.getOpportunity;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppMinimal;
import static org.tctalent.server.data.UserTestData.getAdminUser;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.data.CandidateOpportunityTestData.CreateUpdateCandidateOppTestData;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.request.candidate.UpdateCandidateOppsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.OppNotificationService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.UserService;

@ExtendWith(MockitoExtension.class)
public class CandidateOpportunityServiceImplTest {

    private CandidateOpportunity candidateOpp;
    private Opportunity sfOpp;
    private List<Candidate> candidateList;
    private final CreateUpdateCandidateOppTestData data = createUpdateCandidateOppRequestAndExpectedOpp();
    private UpdateCandidateOppsRequest updateRequest;
    private CandidateOpportunity expectedOpp;
    private SalesforceJobOpp jobOpp;
    private User adminUser;

    @Mock SalesforceService salesforceService;
    @Mock CandidateService candidateService;
    @Mock SalesforceJobOppService salesforceJobOppService;
    @Mock CandidateOpportunityRepository candidateOpportunityRepository;
    @Mock UserService userService;
    @Mock NextStepProcessingService nextStepProcessingService;
    @Mock OppNotificationService oppNotificationService;
    @Mock AuthService authService;

    @Captor ArgumentCaptor<CandidateOpportunity> oppCaptor;

    @InjectMocks CandidateOpportunityServiceImpl candidateOpportunityService;

    @BeforeEach
    void setUp() {
        candidateOpp = getCandidateOpp();
        sfOpp = getOpportunity();
        candidateList = getListOfCandidates();
        updateRequest = data.request();
        expectedOpp = data.expectedOpp();
        jobOpp = getSalesforceJobOppMinimal();
        adminUser = getAdminUser();
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
            updateRequest.getCandidateOppParams(), jobOpp);
        inOrder.verify(candidateOpportunityRepository).save(any(CandidateOpportunity.class));
    }

    @Test
    @DisplayName("should update opp as expected")
    void createUpdateCandidateOpportunities_shouldUpdateOppAsExpected() {
        final String nextStep = updateRequest.getCandidateOppParams().getNextStep();
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

        candidateOpportunityService.createUpdateCandidateOpportunities(updateRequest);

        assertThat(oppCaptor.getValue())
            .usingRecursiveComparison()
            .ignoringFields("candidate.createdDate", "candidate.updatedDate",
                "createdBy", "createdDate", "updatedDate")
            .isEqualTo(expectedOpp);
    }

    @Test
    @DisplayName("should create opp as expected when none exists")
    void createUpdateCandidateOpportunities_shouldCreateOppAsExpected_whenNoneExists() {
        final String nextStep = updateRequest.getCandidateOppParams().getNextStep();
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

        assertEquals(oppCaptor.getValue().getNextStep(), nextStep);
        assertEquals(oppCaptor.getValue().getNextStepDueDate(), expectedOpp.getNextStepDueDate());
        assertEquals(oppCaptor.getValue().getStage(), expectedOpp.getStage());
        assertEquals(oppCaptor.getValue().getClosingComments(), expectedOpp.getClosingComments());
        assertEquals(oppCaptor.getValue().getEmployerFeedback(), expectedOpp.getEmployerFeedback());
        assertEquals(oppCaptor.getValue().getUpdatedBy(), expectedOpp.getUpdatedBy());
    }

}
