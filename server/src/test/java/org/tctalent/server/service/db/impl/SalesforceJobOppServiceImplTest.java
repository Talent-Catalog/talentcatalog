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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.OpportunityTestData.getOpportunityForJob;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppMinimal;
import static org.tctalent.server.util.SalesforceHelper.parseSalesforceOffsetDateTime;

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
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.service.db.SalesforceService;

@ExtendWith(MockitoExtension.class)
class SalesforceJobOppServiceImplTest {

    private SalesforceJobOpp shortJob;
    private Opportunity sfOpp;

    private static final String SF_ID = "006Uu00000IGDAEIA5";
    private static final String SF_URL =
        "https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/" + SF_ID + "/view";

    @Mock private SalesforceJobOppRepository salesforceJobOppRepository;
    @Mock private SalesforceService salesforceService;

    @Captor
    private ArgumentCaptor<SalesforceJobOpp> oppCaptor;

    @InjectMocks
    private SalesforceJobOppServiceImpl salesforceJobOppService;

    @BeforeEach
    void setUp() {
        shortJob = getSalesforceJobOppMinimal();
        sfOpp = getOpportunityForJob();
    }

    @Test
    @DisplayName("should return job opp when found")
    void getJobOpp_shouldReturnJobOpp_whenFound() {
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(shortJob));

        assertEquals(shortJob, salesforceJobOppService.getJobOpp(1L));
    }

    @Test
    @DisplayName("should throw no such object exception when job opp not found")
    void getJobOpp_shouldThrowNoSuchObjectException_whenJobOppNotFound() {
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class, () -> salesforceJobOppService.getJobOpp(1L));
    }

    @Test
    @DisplayName("should return job opp when found")
    void getJobOppById_shouldReturnJobOpp_whenFound() {
        given(salesforceJobOppRepository.findBySfId(anyString())).willReturn(Optional.of(shortJob));

        assertEquals(shortJob, salesforceJobOppService.getJobOppById(SF_ID));
    }

    @Test
    @DisplayName("should return null when not found")
    void getJobOppById_shouldReturnNull_whenNotFound() {
        given(salesforceJobOppRepository.findBySfId(anyString())).willReturn(Optional.empty());

        assertNull(salesforceJobOppService.getJobOppById(SF_ID));
    }

    @Test
    @DisplayName("should return job opp when found")
    void getJobOppByUrl_shouldReturnJobOpp_whenFound() {
        given(salesforceJobOppRepository.findBySfId(anyString())).willReturn(Optional.of(shortJob));

        assertEquals(shortJob, salesforceJobOppService.getJobOppByUrl(SF_URL));
    }

    @Test
    @DisplayName("should return null when not found")
    void getJobOppByUrl_shouldReturnNull_whenNotFound() {
        given(salesforceJobOppRepository.findBySfId(anyString())).willReturn(Optional.empty());

        assertNull(salesforceJobOppService.getJobOppByUrl(SF_URL));
    }

    @Test
    @DisplayName("should create job opp with expected values")
    void createJobOpp_shouldCreateJobOppWithExpectedValues() {
        given(salesforceService.fetchJobOpportunity(SF_ID)).willReturn(sfOpp);

        salesforceJobOppService.createJobOpp(SF_ID);

        verify(salesforceJobOppRepository).save(oppCaptor.capture());
        SalesforceJobOpp tcOpp = oppCaptor.getValue();
        assertEquals(SF_ID, tcOpp.getSfId());
        compareTcAndSfOpps(sfOpp, tcOpp);
    }

    private void compareTcAndSfOpps(Opportunity sfOpp, SalesforceJobOpp tcOpp) {
        assertEquals(sfOpp.getName(), tcOpp.getName());
        assertEquals(sfOpp.getAccountId(), tcOpp.getAccountId());
        assertEquals(sfOpp.getOwnerId(), tcOpp.getOwnerId());
        assertEquals(sfOpp.isClosed(), tcOpp.isClosed());
        assertEquals(sfOpp.getClosingComments(), tcOpp.getClosingComments());
        assertEquals(sfOpp.getNextStep(), tcOpp.getNextStep());
        assertEquals(sfOpp.isWon(), tcOpp.isWon());
        assertEquals(sfOpp.getHiringCommitment(), tcOpp.getHiringCommitment());
        assertEquals(JobOpportunityStage.recruitmentProcess, tcOpp.getStage());
        assertEquals(LocalDate.parse(sfOpp.getNextStepDueDate()), tcOpp.getNextStepDueDate());
        assertEquals(parseSalesforceOffsetDateTime(sfOpp.getCreatedDate()), tcOpp.getCreatedDate());
        assertEquals(parseSalesforceOffsetDateTime(sfOpp.getLastModifiedDate()), tcOpp.getUpdatedDate());
    }

    @Test
    @DisplayName("should throw when job opp not found")
    void createJobOpp_shouldThrow_whenJobOppNotFound() {
        given(salesforceService.fetchJobOpportunity(SF_ID)).willReturn(null);

        assertThrows(InvalidRequestException.class, () -> salesforceJobOppService.createJobOpp(SF_ID));
    }

    @Test
    @DisplayName("should return null when sf id is null")
    void getOrCreateJobOppFromId_shouldReturnNull_whenSfIdIsNull() {
        assertNull(salesforceJobOppService.getOrCreateJobOppFromId(null));
    }

    @Test
    @DisplayName("should return null when sf id is empty string")
    void getOrCreateJobOppFromId_shouldReturnNull_whenSfIdIsEmptyString() {
        assertNull(salesforceJobOppService.getOrCreateJobOppFromId(""));
    }

    @Test
    @DisplayName("should create job opp when none exists")
    void getOrCreateJobOppFromId_shouldCreateJobOpp_whenNoneExists() {
        given(salesforceJobOppRepository.findBySfId(SF_ID)).willReturn(Optional.empty());
        given(salesforceService.fetchJobOpportunity(SF_ID)).willReturn(sfOpp);
        // We can return anything here because we'll use the job that's being saved to verify success:
        given(salesforceJobOppRepository.save(any(SalesforceJobOpp.class)))
            .willReturn(mock(SalesforceJobOpp.class));

        salesforceJobOppService.getOrCreateJobOppFromId(SF_ID);

        verify(salesforceJobOppRepository).save(oppCaptor.capture());
        SalesforceJobOpp tcOpp = oppCaptor.getValue();
        assertEquals(SF_ID, tcOpp.getSfId());
        compareTcAndSfOpps(sfOpp, tcOpp);
    }

    @Test
    @DisplayName("should return null when sf id is null")
    void getOrCreateJobOppFromLink_shouldReturnNull_whenSfIdIsNull() {
        assertNull(salesforceJobOppService.getOrCreateJobOppFromLink(null));
    }

    @Test
    @DisplayName("should return null when sf id is empty string")
    void getOrCreateJobOppFromLink_shouldReturnNull_whenSfIdIsEmptyString() {
        assertNull(salesforceJobOppService.getOrCreateJobOppFromLink(""));
    }

    @Test
    @DisplayName("should create job opp when none exists")
    void getOrCreateJobOppFromLink_shouldCreateJobOpp_whenNoneExists() {
        given(salesforceJobOppRepository.findBySfId(SF_ID)).willReturn(Optional.empty());
        given(salesforceService.fetchJobOpportunity(SF_ID)).willReturn(sfOpp);
        // We can return anything here because we'll use the job that's being saved to verify success:
        given(salesforceJobOppRepository.save(any(SalesforceJobOpp.class)))
            .willReturn(mock(SalesforceJobOpp.class));

        salesforceJobOppService.getOrCreateJobOppFromLink(SF_URL);

        verify(salesforceJobOppRepository).save(oppCaptor.capture());
        SalesforceJobOpp tcOpp = oppCaptor.getValue();
        compareTcAndSfOpps(sfOpp, tcOpp);
    }

    @Test
    @DisplayName("should update and save job as expected")
    void updateJob_shouldUpdateJobOppAsExpected() {
        given(salesforceService.fetchJobOpportunity(shortJob.getSfId())).willReturn(sfOpp);

        salesforceJobOppService.updateJob(shortJob);

        compareTcAndSfOpps(sfOpp, shortJob);
    }

}
