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

package org.tctalent.server.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class CandidateSearchUtilsTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void buildOrderByClause() {
        String s;

        s = CandidateSearchUtils.buildOrderByClause(null);
        Assertions.assertEquals(" order by candidate.id DESC", s);

        s = CandidateSearchUtils.buildOrderByClause(
            Sort.by(Sort.Direction.ASC, "id"));
        Assertions.assertEquals(" order by candidate.id ASC", s);

        s = CandidateSearchUtils.buildOrderByClause(
            Sort.by(Sort.Direction.ASC, "status"));
        Assertions.assertEquals(" order by candidate.status ASC,candidate.id DESC", s);

        s = CandidateSearchUtils.buildOrderByClause(
            Sort.by(Sort.Direction.ASC, "user.firstName"));
        Assertions.assertEquals(" order by users.first_name ASC,candidate.id DESC", s);
    }

    @Test
    void buildOrderByTextMatchClause() {
        String s;

        s = CandidateSearchUtils.buildOrderByClause(
            Sort.by(Sort.Direction.ASC, "text_match"));
        Assertions.assertEquals(" order by rank ASC,candidate.id DESC", s);
    }

    @Test
    void buildNonIdFieldList() {
        String s;

        s = CandidateSearchUtils.buildNonIdFieldList(null, null);
        Assertions.assertEquals("", s);

        s = CandidateSearchUtils.buildNonIdFieldList(Sort.by("id"), null);
        Assertions.assertEquals("", s);

        s = CandidateSearchUtils.buildNonIdFieldList(
            Sort.by("user.firstName"), null);
        Assertions.assertEquals("users.first_name", s);
    }

    @Test
    void buildNonIdFieldListWithTextMatch() {
        String s;

        String textQuery = "accountant + (excel powerpoint)";
        s = CandidateSearchUtils.buildNonIdFieldList(
            Sort.by("text_match"), textQuery);
        String tsQuerySql = CandidateSearchUtils.buildTsQuerySQL(textQuery);
        Assertions.assertEquals("ts_rank("
            + CandidateSearchUtils.CANDIDATE_TS_TEXT_FIELD
            +",to_tsquery('english','" + tsQuerySql + "')) as rank", s);
    }

    @Test
    void buildTsQuerySQLNullEmpty() {
        String s;

        s = CandidateSearchUtils.buildTsQuerySQL(null);
        Assertions.assertEquals("", s);

        s = CandidateSearchUtils.buildTsQuerySQL("   ");
        Assertions.assertEquals("", s);
    }

    @Test
    void buildTsQuerySQLOr() {
        String s;

        s = CandidateSearchUtils.buildTsQuerySQL("accounting excel");
        Assertions.assertEquals("accounting | excel", s);
    }

    @Test
    void buildTsQuerySQLAnd() {
        String s;

        s = CandidateSearchUtils.buildTsQuerySQL("accounting + excel");
        Assertions.assertEquals("accounting & excel", s);
    }

    @Test
    void buildTsQuerySQLQuote() {
        String s;

        s = CandidateSearchUtils.buildTsQuerySQL("\"accounting excel\"");
        Assertions.assertEquals("accounting <-> excel", s);

        s = CandidateSearchUtils.buildTsQuerySQL("\"accounting excel powerpoint\"");
        Assertions.assertEquals("accounting <-> excel <-> powerpoint", s);
    }

    @Test
    void buildTsQuerySQLBrackets() {
        String s;

        s = CandidateSearchUtils.buildTsQuerySQL("(accounting excel) + powerpoint");
        Assertions.assertEquals("(accounting | excel) & powerpoint", s);
    }
}
