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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.InstanceType;

class CandidateNumberGeneratorTest {

    private Candidate testCandidate;
    private CandidateNumberGenerator candidateNumberGenerator;

    @BeforeEach
    void setUp() {
        testCandidate = new Candidate();
        testCandidate.setId(1L);
    }

    @Test
    void grnNumbersShouldBeGreaterThan5Million() {
        candidateNumberGenerator = new CandidateNumberGenerator(InstanceType.GRN);
        String generatedNumber = candidateNumberGenerator.generateCandidateNumber(testCandidate);
        Assertions.assertEquals("5000001", generatedNumber);
    }

    @Test
    void tbbNumbersShouldBeSameAsId() {
        candidateNumberGenerator = new CandidateNumberGenerator(InstanceType.TBB);
        String generatedNumber = candidateNumberGenerator.generateCandidateNumber(testCandidate);
        Assertions.assertEquals("0001", generatedNumber);
    }
}
