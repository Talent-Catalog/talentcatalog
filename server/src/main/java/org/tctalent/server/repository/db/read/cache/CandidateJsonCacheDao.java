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
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for the Postgres-backed candidate JSON cache.
 * <p>
 * Stores exactly one JSON blob per candidate, together with the data_version
 * it was computed against.
 * </p>
 * <p>
 * This cache is authoritative for stored JSON, but correctness is governed by
 * candidate.data_version, not by this table alone.
 * </p>
 */
@Repository
@Transactional
@RequiredArgsConstructor
public class CandidateJsonCacheDao {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Fetch cached JSON rows for the given candidate IDs.
     * <p>
     * This method returns:
     * <ul>
     *  <li>candidate_id</li> 
     *  <li>candidate.data_version (authoritative)</li>
     *  <li>cached.data_version (what the JSON was built against)</li>
     *  <li>cached JSON (may be null if no cache row exists)</li>
     * </ul>
     * </p>
     * <p>
     * Callers decide whether the cache row is valid by comparing versions.
     * </p>
     */
    @Transactional(readOnly = true)
    public List<CandidateJsonCache> findByIds(@Nullable Collection<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return jdbc.query(
            """
            select
              c.id            as candidate_id,
              c.data_version  as candidate_version,
              cj.data_version as cached_version,
              cj.json         as json
            from candidate c
            left join candidate_json_cache cj
              on cj.candidate_id = c.id
            where c.id in (:ids)
            """,
            Map.of("ids", ids),
            (rs, rowNum) -> new CandidateJsonCache(
                rs.getLong("candidate_id"),
                rs.getLong("candidate_version"),
                rs.getObject("cached_version", Long.class),
                rs.getString("json")
            )
        );
    }

    /**
     * Insert or update cached JSON for a candidate.
     * <p>
     * Uses Postgres UPSERT (ON CONFLICT) so callers do not need to care
     * whether a cache row already exists.
     * </p>
     * <p>
     * STRICT:
     *  - JSON must never be null or blank here
     *  - If it is, this indicates a serious upstream bug
     * </p>
     */
    public void upsert(long candidateId, long dataVersion, String json) {

        // Fail fast if JSON is missing — corrupted cache entries are unacceptable
        if (json == null || json.isBlank()) {
            throw new IllegalStateException(
                "Attempting to cache null/blank JSON for candidateId=" + candidateId
            );
        }

        jdbc.update(
            """
            insert into candidate_json_cache (
                candidate_id,
                data_version,
                json
            )
            values (
                :candidateId,
                :dataVersion,
                cast(:json as jsonb)
            )
            on conflict (candidate_id)
            do update set
                data_version = excluded.data_version,
                json         = excluded.json,
                computed_at  = now()
            """,
            Map.of(
                "candidateId", candidateId,
                "dataVersion", dataVersion,
                "json", json
            )
        );
    }
}
