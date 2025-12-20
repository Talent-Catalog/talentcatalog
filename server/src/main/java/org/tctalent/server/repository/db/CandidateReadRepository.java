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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.api.dto.CandidateFlatDto;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CandidateReadRepository {
    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Creates a CandidateFlatDto from a ResultSet.
     */
    private RowMapper<CandidateFlatDto> candidateFlatDtoRowMapper =
        (rs, rowNum) -> {
            CandidateFlatDto flatDto = new CandidateFlatDto();
            flatDto.setId(rs.getLong("id"));
            flatDto.setCandidateNumber(rs.getString("candidate_number"));
            flatDto.setPublicId(rs.getString("public_id"));
            flatDto.setCandidateJobExperiences(rs.getString("candidateJobExperiences"));
            return flatDto;
    };

    public List<CandidateFlatDto> findByIds(Collection<Long> ids) {

        //TODO JC Construct the sql query - this could be computed in code
        //It needs to select all the simple attribute fields in CandidateSimpleAttributes
        //and compute JSONB string fields from joined attributes using the names of the joined
        //attributes in the Candidate entity.
        String sql = """
            with base as (
              select c.id, c.candidate_number, c.public_id, c.user_id
              from candidate c
              where c.id = any(:ids)
            )
            select
              b.id,
              b.candidate_number,
              b.public_id,
              b.user_id,

              coalesce((
                select jsonb_build_object(
                  'firstName', u.first_name,
                  'lastName', u.last_name,
                  'username', u.username,
                  'email', u.email,
                  'partner',
                        jsonb_build_object(
                          'id', partner.id,
                          'publicId', partner.public_id,
                          'abbreviation', partner.abbreviation,
                          'name', partner.name,
                          'websiteUrl', partner.website_url
                        )
                )
                from users u
                left join partner on partner.id = u.partner_id
                where u.id = b.user_id
              ), '[]'::jsonb) as "user",

              coalesce((
                select jsonb_agg(jsonb_build_object(
                  'companyName', cje.company_name,
                  'role', cje.role,
                  'description', cje.description
                ))
                from candidate_job_experience cje
                where cje.candidate_id = b.id
              ), '[]'::jsonb) as candidateJobExperiences

            from base b
            """;
        return jdbc.query(
            sql,
            Map.of("ids", ids.toArray(Long[]::new)),
            candidateFlatDtoRowMapper
            );
    }
}
