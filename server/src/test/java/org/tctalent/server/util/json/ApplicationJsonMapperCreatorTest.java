/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.json;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.service.api.SkillName;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Guards the spring.jackson settings in application.yml which restore Jackson 2's creation of
 * classes that have no default constructor (TC-1189, decision D26). Without them Jackson 3 cannot
 * create these classes, which broke candidate search and skills extraction.
 */
class ApplicationJsonMapperCreatorTest {

    private final JsonMapper jsonMapper = ApplicationJsonMapper.create();

    @Test
    @DisplayName("the application's JsonMapper detects constructor parameter names")
    void detectsParameterNames() {
        assertTrue(jsonMapper.isEnabled(MapperFeature.DETECT_PARAMETER_NAMES));
    }

    @Test
    @DisplayName("candidate JSON built by the database is read into CandidateReadDto")
    void candidateReadDto() {
        //CandidateReadDto, CountryReadDto and UserReadDto only have Lombok's package private
        //@Builder constructor
        CandidateReadDto dto = jsonMapper.readValue("""
            {"id": 3, "candidateNumber": "5000003", "dob": "1990-05-17",
             "country": {"id": 0, "name": "Unknown", "status": "inactive"},
             "user": {"email": "candidate@example.com", "firstName": "Jo"}}
            """, CandidateReadDto.class);

        assertAll(
            () -> assertEquals(3L, dto.getId()),
            () -> assertEquals("5000003", dto.getCandidateNumber()),
            () -> assertEquals(LocalDate.of(1990, 5, 17), dto.getDob()),
            () -> assertEquals("Unknown", dto.getCountry().getName()),
            () -> assertEquals("candidate@example.com", dto.getUser().getEmail())
        );
    }

    @Test
    @DisplayName("skill names returned by the skills extraction service are read")
    void skillNames() {
        //SkillName only has a public all arguments constructor
        List<SkillName> skills = jsonMapper.readValue(
            "[{\"lang\": \"en\", \"name\": \"Java\"}, {\"lang\": \"en\", \"name\": \"SQL\"}]",
            new TypeReference<>() {});

        assertEquals(List.of(new SkillName("en", "Java"), new SkillName("en", "SQL")), skills);
    }
}
