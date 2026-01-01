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

package org.tctalent.server.repository.db.read.sql;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;

class SqlJsonQueryBuilderTest {

    SqlJsonQueryBuilder sqlJsonQueryBuilder;
    
    @BeforeEach
    void setUp() {
        sqlJsonQueryBuilder = new SqlJsonQueryBuilder();
    }

    @Test
    void buildByIdsQuery() {
        final String sql = sqlJsonQueryBuilder.buildByIdsQuery(CandidateReadDto.class, "ids");
        System.out.println(sql);
    }

    @Test
    void nested_one_to_many_uses_sqltable_for_table_and_correlates_to_parent_alias() {

        // when
        String sql = sqlJsonQueryBuilder.buildByIdsQuery(CandidateReadDto.class, "ids");

        // then
        // ---------------------------------------------------------------------
        // Table resolution should come from @SqlTable on each DTO.
        // ---------------------------------------------------------------------

        assertThat(sql)
            .contains("from candidate c")                 // CandidateReadDto @SqlTable
            .contains("from candidate_occupation cocc")     // CandidateOccupationReadDto @SqlTable
            .contains("from candidate_job_experience cje");// CandidateJobExperienceReadDto @SqlTable

        //Job experiences per candidate occupation are fetched
        assertThat(sql)
            .contains("cje.candidate_occupation_id = cocc.id");

        //All job experiences from a candidate are fetched
        assertThat(sql)
            .contains("cje.candidate_id = c.id");
    }

}