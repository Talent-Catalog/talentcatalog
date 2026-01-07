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

package org.tctalent.server.repository.db.read.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO responsible for fetching candidate.data_version values.
 *
 * WHY THIS EXISTS:
 * ----------------
 * candidate.data_version is the authoritative signal for whether cached JSON
 * (in Redis or Postgres) is still valid.
 *
 * We fetch versions first so we can:
 *  - build versioned Redis keys (candidate:{id}:v:{version})
 *  - avoid serving stale JSON
 *  - avoid any explicit cache invalidation logic
 *
 * This DAO deliberately does NOT fetch JSON.
 * Keeping it separate keeps responsibilities clear and queries cheap.
 */
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CandidateVersionDao {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Fetch data_version for each candidate ID.
     *
     * @param candidateIds candidate IDs to look up
     * @return map of candidateId -> data_version
     *
     * If a candidateId is missing from the returned map, the candidate
     * does not exist in the database. Callers that require strict behaviour
     * must treat that as an error.
     */
    public Map<Long, Long> fetchCandidateVersions(Collection<Long> candidateIds) {

        if (candidateIds == null || candidateIds.isEmpty()) {
            return Map.of();
        }

        return jdbc.query(
            """
            select id, data_version
            from candidate
            where id in (:ids)
            """,
            Map.of("ids", candidateIds),
            rs -> {
                Map<Long, Long> result = new HashMap<>();
                while (rs.next()) {
                    result.put(
                        rs.getLong("id"),
                        rs.getLong("data_version")
                    );
                }
                return result;
            }
        );
    }
}
