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
import static org.mockito.BDDMockito.given;
import static org.tctalent.server.data.CandidateOpportunityTestData.getCandidateOpp;
import static org.tctalent.server.data.OpportunityTestData.getOpportunity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.service.db.SalesforceService;

@ExtendWith(MockitoExtension.class)
public class CandidateOpportunityServiceImplTest {

    private CandidateOpportunity candidateOpp;
    private Opportunity sfOpp;

    @Mock SalesforceService salesforceService;

    @InjectMocks CandidateOpportunityServiceImpl candidateOpportunityService;

    @BeforeEach
    void setUp() {
        candidateOpp = getCandidateOpp();
        sfOpp = getOpportunity();
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

}
