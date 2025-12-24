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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
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
}