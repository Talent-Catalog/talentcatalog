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

package org.tctalent.server.repository.db.read.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.tctalent.server.repository.db.read.dto.CandidateJobExperienceDto;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;

public class CandidateReadMapper implements RowMapper<CandidateReadDto> {

    private static final TypeReference<List<CandidateJobExperienceDto>> JOB_EXPERIENCE_LIST =
        new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public CandidateReadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CandidateReadDto mapRow(ResultSet rs, int rowNum) throws SQLException {

        CandidateReadDto dto = new CandidateReadDto();

        dto.setId(rs.getLong("id"));
        dto.setCandidateNumber(rs.getString("candidateNumber"));
        dto.setPublicId(rs.getString("publicId"));  

        dto.setCandidateJobExperiences(readJsonArray(rs, "candidateJobExperiences", JOB_EXPERIENCE_LIST));

        return dto;
    }

    private <T> List<T> readJsonArray(
        ResultSet rs,
        String column,
        TypeReference<List<T>> type) throws SQLException {

        String json = rs.getString(column);
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new SQLException(
                "Failed to parse JSON column '" + column + "': " + json, e);
        }
    }
}
