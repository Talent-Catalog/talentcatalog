/*
 * Copyright (c) 2026 Talent Catalog.
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

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Candidate;

@ExtendWith(MockitoExtension.class)
class CandidateNumberGeneratorTest {

    private Candidate testCandidate;
    private CandidateNumberGenerator candidateNumberGenerator;

    @Mock
    private TcInstanceService tcInstanceService;


    @BeforeEach
    void setUp() {
        testCandidate = new Candidate();
        testCandidate.setId(1L);

    }

    @Test
    void grnNumbersShouldBeGreaterThan5Million() {
        given(tcInstanceService.isGRN()).willReturn(true);
        candidateNumberGenerator = new CandidateNumberGenerator(tcInstanceService);
        String generatedNumber = candidateNumberGenerator.generateCandidateNumber(testCandidate);
        Assertions.assertEquals("5000001", generatedNumber);
    }

    @Test
    void tbbNumbersShouldBeSameAsId() {
        given(tcInstanceService.isGRN()).willReturn(false);
        candidateNumberGenerator = new CandidateNumberGenerator(tcInstanceService);
        String generatedNumber = candidateNumberGenerator.generateCandidateNumber(testCandidate);
        Assertions.assertEquals("0001", generatedNumber);
    }
}
