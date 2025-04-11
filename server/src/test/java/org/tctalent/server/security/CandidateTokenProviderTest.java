/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class CandidateTokenProviderTest {

    CandidateTokenProvider ctp;

    @BeforeEach
    void setUp() {
        ctp = new CandidateTokenProvider();
        ctp.setJwtSecretBase64("l0eZJNk7LmiX0+Xgnp/aNWWnlNy41rcDtmEXyRZRPys=");
    }

    @Test
    void createToken() {

        String numIn = "12345";

        String s = ctp.generateToken(numIn, 30000);

        System.out.println(s);
        System.out.println("Length: " + s.length());

        String num = ctp.getCandidateNumberFromToken(s);
        assertEquals(numIn, num);
    }


    @Test
    void oldCandidateOnlyTokenWorksWithCvClaimDecoding() {

        String numIn = "12345";

        String s = ctp.generateToken(numIn, 30000);

        System.out.println(s);
        System.out.println("Length: " + s.length());

        CvClaims claims = ctp.getCvClaimsFromToken(s);
        assertEquals(numIn, claims.candidateNumber());
        assertNotNull(claims.candidateOccupationIds());
        assertTrue(claims.candidateOccupationIds().isEmpty());
    }

    static Stream<Arguments> generateCvClaimsRoundTripData() {
        return Stream.of(
                Arguments.of("12345", false, null ),
                Arguments.of("23456", false, Arrays.asList(new Long[] {})),
                Arguments.of("34567", true, Arrays.asList(new Long[] {})),
                Arguments.of("45678", true, Arrays.asList(new Long[] {1L})),
                Arguments.of("56789", true, Arrays.asList(new Long[] {2L,3L}))
        );
    }
    @ParameterizedTest
    @MethodSource("generateCvClaimsRoundTripData")
    void cvClaimsRoundTrip(String candidateNumber, boolean restrictCandidateOccupations, List<Long> candidateOccupations) {


        String s = ctp.generateCvToken(new CvClaims(candidateNumber, restrictCandidateOccupations, candidateOccupations), 30000);

        System.out.println(s);
        System.out.println("Length: " + s.length());

        CvClaims claims = ctp.getCvClaimsFromToken(s);
        assertEquals(candidateNumber, claims.candidateNumber());
        assertEquals(restrictCandidateOccupations, claims.restrictCandidateOccupations());

        if( restrictCandidateOccupations ) {
            assertArrayEquals(candidateOccupations.toArray(), claims.candidateOccupationIds().toArray());
        }else{
            assertNotNull(claims.candidateOccupationIds());
            assertTrue(claims.candidateOccupationIds().isEmpty());
        }
    }
}
