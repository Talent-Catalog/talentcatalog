/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.request.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.Gender;

class SearchCandidateRequestTest {
    
    private SearchCandidateRequest request;

    @BeforeEach
    void setUp() {
        request = new SearchCandidateRequest();
    }

    @Test
    @DisplayName("no SQL generated from empty request")
    void extractPredicateSQLFromEmptyRequest() {
        String sql = request.extractPredicateSQL(true);
        assertEquals(0, sql.length());
    }

    @Test
    @DisplayName("SQL generated from local enumeration")
    void extractPredicateSQLFromLocalEnumerationRequest() {
        Gender gender = Gender.male;
        request.setGender(gender);
        String sql = request.extractPredicateSQL(true);
        assertEquals("candidate.gender = '" + gender.name() + "'", sql);

        String jpql = request.extractPredicateSQL(false);
        assertEquals("candidate.gender = '" + gender.name() + "'", jpql);
    }
}