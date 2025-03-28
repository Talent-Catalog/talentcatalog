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

package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.tctalent.anonymization.model.CandidateRegistration;

class CandidateMapperTest {
    private CandidateMapper candidateMapper;

    @BeforeEach
    void setUp() {
        candidateMapper = Mappers.getMapper(CandidateMapper.class);
    }

    @Test
    void shouldMapEmptyData() {
        CandidateRegistration registrationInfo = new CandidateRegistration();
        Candidate candidate = candidateMapper.candidateRegistrationToCandidate(registrationInfo);
        assertNotNull(candidate);
    }

    @Test
    void shouldMapOccupationData() {
        CandidateRegistration registrationInfo = new CandidateRegistration();
        org.tctalent.anonymization.model.CandidateOccupation publicOccupation = new org.tctalent.anonymization.model.CandidateOccupation();
        final int yearsExperience = 5;
        publicOccupation.setYearsExperience(yearsExperience);
        registrationInfo.setCandidateOccupations(Collections.singletonList(publicOccupation));
        Candidate candidate = candidateMapper.candidateRegistrationToCandidate(registrationInfo);
        assertNotNull(candidate);
        final List<CandidateOccupation> candidateOccupations = candidate.getCandidateOccupations();
        assertNotNull(candidateOccupations);
        assertEquals(1, candidateOccupations.size());
        assertEquals(yearsExperience, candidateOccupations.get(0).getYearsExperience());
    }
}
