/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
