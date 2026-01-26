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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tctalent.server.repository.db.read.dao.CandidateReadDao;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Tag("skip-test-in-gradle-build")
@SpringBootTest
class CandidateReadDaoTest {

    @Autowired
    private CandidateReadDao candidateReadDao;

    @BeforeEach
    void setUp() {
    }

    @Test
    void findByIds() {
        assertNotNull(candidateReadDao);

        final List<CandidateReadDto> dtos = candidateReadDao.findByIds(Set.of(27673L));
        assertNotNull(dtos);

    }
}
