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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.CountryTestData.CANADA;
import static org.tctalent.server.data.CountryTestData.UNITED_KINGDOM;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppMinimal;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.configuration.SalesforceRecordTypeConfig;
import org.tctalent.server.configuration.SalesforceTbbAccountsConfig;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.service.db.CandidateDependantService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class SalesforceServiceImplTest {

    @Mock private EmailHelper emailHelper;
    @Mock private SalesforceConfig salesforceConfig;
    @Mock private SalesforceRecordTypeConfig salesforceRecordTypeConfig;
    @Mock private SalesforceTbbAccountsConfig salesforceTbbAccountsConfig;
    @Mock private CandidateDependantService candidateDependantService;
    @Mock private NextStepProcessingService nextStepProcessingService;

    private SalesforceServiceImpl salesforceService;

    @BeforeEach
    void setUp() {
        given(salesforceConfig.getBaseClassicUrl())
            .willReturn("https://talentbeyondboundaries.my.salesforce.com/");

        salesforceService = new SalesforceServiceImpl(
            emailHelper,
            salesforceConfig,
            salesforceRecordTypeConfig,
            salesforceTbbAccountsConfig,
            candidateDependantService,
            nextStepProcessingService
        );
    }

    @Test
    @DisplayName("should make candidate opportunity external id from candidate and job ids")
    void makeExternalId_shouldJoinCandidateAndJobIds() {
        assertEquals("123-006Uu00000IGDAEIA5",
            SalesforceServiceImpl.makeExternalId("123", "006Uu00000IGDAEIA5"));
    }

    @Test
    @DisplayName("should truncate generated candidate opportunity names to Salesforce limit")
    void generateCandidateOppName_shouldTruncateLongNamesToSalesforceLimit() {
        Candidate candidate = getCandidate();
        candidate.setCandidateNumber("123456");

        SalesforceJobOpp job = getSalesforceJobOppMinimal();
        job.setName("A".repeat(130));

        String result = salesforceService.generateCandidateOppName(candidate, job);

        assertAll(
            () -> assertEquals(120, result.length()),
            () -> assertEquals("test(123456)-" + "A".repeat(104) + "...", result)
        );
    }

    @Test
    @DisplayName("should choose Canada candidate opportunity record type for Canadian jobs")
    void getCandidateOpportunityRecordType_shouldUseCanadaRecordTypeForCanadianJobs() {
        SalesforceJobOpp job = getSalesforceJobOppMinimal();
        job.setCountry(CANADA);

        String result = ReflectionTestUtils.invokeMethod(
            salesforceService, "getCandidateOpportunityRecordType", job);

        assertEquals("Candidate recruitment (CAN)", result);
    }

    @Test
    @DisplayName("should choose default candidate opportunity record type for non-Canadian jobs")
    void getCandidateOpportunityRecordType_shouldUseDefaultRecordTypeForNonCanadianJobs() {
        SalesforceJobOpp job = getSalesforceJobOppMinimal();
        job.setCountry(UNITED_KINGDOM);

        String result = ReflectionTestUtils.invokeMethod(
            salesforceService, "getCandidateOpportunityRecordType", job);

        assertEquals("Candidate recruitment", result);
    }

    @Test
    @DisplayName("should build create candidate opportunity request with required Salesforce fields")
    void candidateOpportunityRecordComposite_shouldIncludeCreateOnlyFields_whenCreating() {
        Candidate candidate = getCandidate();
        candidate.setSflink(
            "https://talentbeyondboundaries.lightning.force.com/lightning/r/Contact/003Uu00000IGDAEIA5/view");
        SalesforceJobOpp job = getSalesforceJobOppMinimal();
        job.setOwnerId("005Uu00000IGDAEIA5");

        SalesforceServiceImpl.CandidateOpportunityRecordComposite request =
            salesforceService.new CandidateOpportunityRecordComposite(
                "Candidate recruitment", candidate, job, true);

        SalesforceServiceImpl.RecordTypeField recordType =
            (SalesforceServiceImpl.RecordTypeField) request.get("RecordType");
        SalesforceServiceImpl.CompositeAttributes attributes =
            (SalesforceServiceImpl.CompositeAttributes) request.get("attributes");

        assertAll(
            () -> assertNotNull(attributes),
            () -> assertEquals("Opportunity", attributes.type),
            () -> assertEquals("Candidate recruitment", recordType.Name),
            () -> assertEquals("99-sales-force-job-opp-id",
                request.get("TBBCandidateExternalId__c")),
            () -> assertEquals("test(99)-Test Job", request.get("Name")),
            () -> assertEquals(job.getAccountId(), request.get("AccountId")),
            () -> assertEquals("003Uu00000IGDAEIA5", request.get("Candidate_Contact__c")),
            () -> assertEquals(job.getSfId(), request.get("Parent_Opportunity__c")),
            () -> assertEquals(job.getOwnerId(), request.get("OwnerId")),
            () -> assertNotNull(LocalDate.parse((String) request.get("CloseDate")))
        );
    }

    @Test
    @DisplayName("should build update candidate opportunity request without create-only fields")
    void candidateOpportunityRecordComposite_shouldOmitCreateOnlyFields_whenUpdating() {
        Candidate candidate = getCandidate();
        SalesforceJobOpp job = getSalesforceJobOppMinimal();

        SalesforceServiceImpl.CandidateOpportunityRecordComposite request =
            salesforceService.new CandidateOpportunityRecordComposite(
                "Candidate recruitment", candidate, job, false);

        assertAll(
            () -> assertEquals("99-sales-force-job-opp-id",
                request.get("TBBCandidateExternalId__c")),
            () -> assertNull(request.get("Name")),
            () -> assertFalse(request.containsKey("AccountId")),
            () -> assertFalse(request.containsKey("Candidate_Contact__c")),
            () -> assertFalse(request.containsKey("Parent_Opportunity__c")),
            () -> assertFalse(request.containsKey("CloseDate"))
        );
    }

}
