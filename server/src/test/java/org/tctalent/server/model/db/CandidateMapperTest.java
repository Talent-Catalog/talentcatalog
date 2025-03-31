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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tctalent.anonymization.model.CandidateRegistration;

@SpringBootTest
class CandidateMapperTest {
    @Autowired
    private CandidateMapper candidateMapper;

    @BeforeEach
    void setUp() {
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
        org.tctalent.anonymization.model.CandidateOccupation publicCandidateOccupation =
            new org.tctalent.anonymization.model.CandidateOccupation();

        final int yearsExperience = 5;
        publicCandidateOccupation.setYearsExperience(yearsExperience);

        org.tctalent.anonymization.model.Occupation occupation =
            new org.tctalent.anonymization.model.Occupation();
        final String occupationName = "Accountant";
        occupation.setName(occupationName);
        final String isco08Code = "123456";
        occupation.setIsco08Code(isco08Code);
        publicCandidateOccupation.setOccupation(occupation);

        registrationInfo.setCandidateOccupations(Collections.singletonList(publicCandidateOccupation));
        Candidate candidate = candidateMapper.candidateRegistrationToCandidate(registrationInfo);
        assertNotNull(candidate);
        final List<CandidateOccupation> candidateOccupations = candidate.getCandidateOccupations();
        assertNotNull(candidateOccupations);
        assertEquals(1, candidateOccupations.size());
        final CandidateOccupation candidateOccupation = candidateOccupations.get(0);
        assertEquals(yearsExperience, candidateOccupation.getYearsExperience());
        assertEquals(isco08Code, candidateOccupation.getOccupation().getIsco08Code());
        assertEquals(occupationName, candidateOccupation.getOccupation().getName());


    }


    @Test
    void shouldMapCountry() {
        CandidateRegistration registrationInfo = new CandidateRegistration();

        org.tctalent.anonymization.model.Country publicCountry =
            new org.tctalent.anonymization.model.Country();

        publicCountry.setIsoCode("AU");

        registrationInfo.setCountry(publicCountry);
        Candidate candidate = candidateMapper.candidateRegistrationToCandidate(registrationInfo);
        assertNotNull(candidate);
        final Country country = candidate.getCountry();
        assertNotNull(country);

    }

}
