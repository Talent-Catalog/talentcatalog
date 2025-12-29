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

package org.tctalent.server.service.db.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.service.db.CandidateDtoService;

/**
 * Strict read service for CandidateReadDto.
 *
 * Architecture:
 *   L1: Redis (shared, versioned keys)
 *   L2: Postgres JSON cache (candidate_json_cache)
 *   L3: Postgres recomputation (SqlJsonQueryBuilder)
 *
 * Correctness invariant:
 *   candidate.data_version is the source of truth.
 *   Cached JSON is valid IFF it was built against the same data_version.
 *
 * This service guarantees:
 *   - No stale JSON is ever returned
 *   - Every requested candidateId is returned or an exception is thrown
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateDtoServiceImpl implements CandidateDtoService {
    private final CandidateRedisCache redisCache;
    private final CandidateVersionDao versionDao;
    private final CandidateJsonCacheDao pgCacheDao;
    private final CandidateJsonDao jsonDao;
    private final ObjectMapper objectMapper;

    /**
     * Load CandidateReadDto for the given IDs.
     *
     * STRICT:
     *  - If any requested ID cannot be resolved to JSON, this method throws.
     *  - Silent partial results are never returned.
     */
    @Override
    @NonNull
    public Map<Long, CandidateReadDto> loadByIds(Collection<Long> ids) throws JsonProcessingException {

        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        // ------------------------------------------------------------
        // Step 0: Fetch authoritative candidate versions
        // ------------------------------------------------------------

        Map<Long, Long> versions =
            versionDao.fetchCandidateVersions(new ArrayList<>(ids));

        // STRICT: all requested candidates must exist
        List<Long> missingCandidates = ids.stream()
            .filter(id -> !versions.containsKey(id))
            .toList();

        if (!missingCandidates.isEmpty()) {
            log.error(
                "Requested candidate IDs not found in candidate table. requestedIds={}, missingIds={}",
                ids, missingCandidates
            );
            throw new IllegalStateException(
                "Candidates not found: " + missingCandidates
            );
        }

        // ------------------------------------------------------------
        // Step 1: L1 Redis lookup (versioned keys)
        // ------------------------------------------------------------

        Map<Long, String> jsonById =
            new HashMap<>(redisCache.multiGet(versions));

        // ------------------------------------------------------------
        // Step 2: L2 Postgres JSON cache lookup (for Redis misses)
        // ------------------------------------------------------------

        List<Long> redisMissIds = ids.stream()
            .filter(id -> !jsonById.containsKey(id))
            .toList();

        if (!redisMissIds.isEmpty()) {

            List<CachedCandidateRow> pgRows =
                pgCacheDao.fetchCached(redisMissIds);

            Map<Long, CandidateRedisCache.VersionedJson> redisWarm =
                new HashMap<>();

            for (CachedCandidateRow row : pgRows) {

                // Cache hit only if versions match
                if (row.isCacheHit()) {

                    // ASSERTION: DB cache JSON must never be null
                    if (row.jsonData() == null || row.jsonData().isBlank()) {
                        throw new IllegalStateException(
                            "Null/blank JSON in candidate_json_cache for candidateId="
                                + row.candidateId()
                        );
                    }

                    jsonById.put(row.candidateId(), row.jsonData());

                    redisWarm.put(
                        row.candidateId(),
                        new CandidateRedisCache.VersionedJson(
                            row.candidateId(),
                            row.candidateVersion(),
                            row.jsonData()
                        )
                    );
                }
            }

            // Warm Redis from Postgres hits (shared benefit across nodes)
            if (!redisWarm.isEmpty()) {
                redisCache.putAll(redisWarm);
            }
        }

        // ------------------------------------------------------------
        // Step 3: Recompute JSON for remaining misses
        // ------------------------------------------------------------

        List<Long> remainingMissIds = ids.stream()
            .filter(id -> !jsonById.containsKey(id))
            .toList();

        if (!remainingMissIds.isEmpty()) {

            Map<Long, String> recomputed =
                jsonDao.loadJsonByIds(remainingMissIds);

            Map<Long, CandidateRedisCache.VersionedJson> redisWrites =
                new HashMap<>();

            for (Long id : remainingMissIds) {
                String json = recomputed.get(id);

                // ASSERTION: recomputation must produce JSON
                if (json == null || json.isBlank()) {
                    throw new IllegalStateException(
                        "Recomputed JSON is null/blank for candidateId=" + id
                    );
                }

                long version = versions.get(id);

                // Persist to Postgres cache (UPSERT, race-safe)
                pgCacheDao.upsert(id, version, json);

                // Prepare Redis write (versioned key)
                redisWrites.put(
                    id,
                    new CandidateRedisCache.VersionedJson(id, version, json)
                );

                // Merge into in-memory results
                jsonById.put(id, json);
            }

            if (!redisWrites.isEmpty()) {
                redisCache.putAll(redisWrites);
            }
        }

        // ------------------------------------------------------------
        // Step 4: STRICT completeness check
        // ------------------------------------------------------------

        List<Long> stillMissing = ids.stream()
            .filter(id -> !jsonById.containsKey(id))
            .toList();

        if (!stillMissing.isEmpty()) {
            log.error(
                "Candidate JSON missing after Redis + Postgres cache + recompute. " +
                    "requestedIds={}, missingIds={}, versions={}",
                ids, stillMissing, versions
            );
            throw new IllegalStateException(
                "Missing candidate JSON for ids=" + stillMissing
            );
        }

        // ------------------------------------------------------------
        // Step 5: Deserialize JSON into DTOs
        // ------------------------------------------------------------

        Map<Long, CandidateReadDto> out = new HashMap<>(jsonById.size());
        for (Map.Entry<Long, String> e : jsonById.entrySet()) {
            out.put(e.getKey(), deserialize(e.getValue()));
        }
        return out;
    }

    private CandidateReadDto deserialize(String json) {
        try {
            return objectMapper.readValue(json, CandidateReadDto.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to deserialize candidate JSON", e
            );
        }
    }
}
