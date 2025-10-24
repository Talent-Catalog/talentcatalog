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

package org.tctalent.server.service.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
class TcSkillsExtractionServiceImplTest {

    @Autowired
    private TcSkillsExtractionService skillsExtractionService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void extractSkills() {
        ExtractSkillsRequest request = new ExtractSkillsRequest();
        request.setText("John knows Java, Python, and Algol. He also knows C++, C, and Fortran.");
        final ExtractSkillsResponse response = skillsExtractionService.extractSkills(request);
        assertNotNull(response);
    }
}
