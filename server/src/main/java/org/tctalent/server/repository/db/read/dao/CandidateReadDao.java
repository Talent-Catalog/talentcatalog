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

package org.tctalent.server.repository.db.read.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.repository.db.read.mapper.CandidateReadMapper;
import org.tctalent.server.repository.db.read.sql.SqlJsonQueryBuilder;

/**
 * Data Access Object which fetches candidate data from the Postgres database in the form of a List 
 * of {@link CandidateReadDto} objects.
 * <p>
 *     Note that this does not return JPA entities. Instead, it returns DTO objects which will
 *     not typically be modified (except to maybe add extra computed or tagging data fields which
 *     are not present on the database). It is optimized for performance.
 * </p>
 *
 * @author John Cameron
 */
@Repository
@Transactional(readOnly = true)
@Slf4j
public class CandidateReadDao {
    private final CandidateReadMapper mapper;
    private final NamedParameterJdbcTemplate jdbc;
    private final SqlJsonQueryBuilder sqlJsonQueryBuilder;

    /**
     * Initialize repository
     * @param jdbc Autowired jdbc support 
     * @param objectMapper Autowired Jackson object for mapping between JSON and Java
     * @param sqlJsonQueryBuilder Code which constructs the SQL query from DTO definitions.
     */
    public CandidateReadDao(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper,
        SqlJsonQueryBuilder sqlJsonQueryBuilder) {
        this.jdbc = jdbc;
        
        //Create a RowMapper to convert the database response to the query SQL into CandidateReadDto
        //objets.
        this.mapper = new CandidateReadMapper(objectMapper);
        
        this.sqlJsonQueryBuilder = sqlJsonQueryBuilder;
    }

    /**
     * Return the candidates with the given candidate ids.
     * @param candidateIds Ids of candidates required
     * @return Candidate data. May be empty if non found. 
     */
    @NonNull
    public List<CandidateReadDto> findByIds(@Nullable Collection<Long> candidateIds) {
        if (candidateIds == null || candidateIds.isEmpty()) {
            return List.of();
        }
        
        String sql = sqlJsonQueryBuilder.buildByIdsQuery(CandidateReadDto.class,"ids");

        return jdbc.query(sql, Map.of("ids", candidateIds), mapper);
    }
}
