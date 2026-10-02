/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.repository.db.read.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Verifies that {@link CandidateJobExperienceReadDto#getFullTime()} and
 * {@link CandidateJobExperienceReadDto#getPaid()} pass through the read (search/list) path as
 * booleans - the same as the entity-based endpoints - rather than being coerced to the strings
 * "true"/"false".
 */
class CandidateJobExperienceReadDtoJacksonTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    @DisplayName("Postgres-built JSON booleans deserialize and are output as JSON booleans")
    void booleansRoundTripAsBooleans() throws Exception {
        CandidateJobExperienceReadDto dto = objectMapper.readValue(
            "{\"fullTime\":true,\"paid\":false}", CandidateJobExperienceReadDto.class);

        assertEquals(Boolean.TRUE, dto.getFullTime());
        assertEquals(Boolean.FALSE, dto.getPaid());

        Map<String, Object> built = new DtoBuilder().add("fullTime").add("paid").build(dto);
        assertEquals(Boolean.TRUE, built.get("fullTime"));
        assertEquals(Boolean.FALSE, built.get("paid"));
        assertEquals("{\"fullTime\":true}",
            objectMapper.writeValueAsString(Map.of("fullTime", built.get("fullTime"))));
    }

    @Test
    @DisplayName("null (not supplied) stays null and is omitted from the built DTO")
    void nullStaysNull() throws Exception {
        CandidateJobExperienceReadDto dto = objectMapper.readValue(
            "{\"fullTime\":null}", CandidateJobExperienceReadDto.class);

        assertNull(dto.getFullTime());
        assertNull(dto.getPaid());

        Map<String, Object> built = new DtoBuilder().add("fullTime").add("paid").build(dto);
        assertFalse(built.containsKey("fullTime"));
        assertFalse(built.containsKey("paid"));
    }

    @Test
    @DisplayName("string booleans are still accepted on input (Jackson coercion), yielding booleans")
    void stringBooleansStillDeserialize() throws Exception {
        CandidateJobExperienceReadDto dto = objectMapper.readValue(
            "{\"fullTime\":\"false\",\"paid\":\"true\"}", CandidateJobExperienceReadDto.class);

        assertEquals(Boolean.FALSE, dto.getFullTime());
        assertEquals(Boolean.TRUE, dto.getPaid());
    }
}
