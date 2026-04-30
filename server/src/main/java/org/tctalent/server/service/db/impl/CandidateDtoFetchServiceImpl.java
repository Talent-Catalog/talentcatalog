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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.repository.db.read.cache.CandidateJsonCache;
import org.tctalent.server.repository.db.read.cache.CandidateJsonCacheDao;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.repository.db.read.cache.CandidateVersionDao;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.repository.db.read.sql.CandidateJsonDao;
import org.tctalent.server.service.db.CandidateDtoFetchService;
import org.tctalent.server.util.CandidateSearchUtils;
import org.tctalent.server.util.textExtract.IdAndRank;

/**
 * Strict read service for CandidateReadDto.
 * <p>
 * Architecture:
 *   L1: Redis (shared, versioned keys)
 *   L2: Postgres JSON cache (candidate_json_cache)
 *   L3: Postgres recomputation (SqlJsonQueryBuilder)
 * </p>
 * <p>
 * Correctness invariant:
 *   candidate.data_version is the source of truth.
 *   Cached JSON is valid IFF it was built against the same data_version.
 * </p>
 * <p>
 * This service guarantees:
 *   - No stale JSON is ever returned
 *   - Every requested candidateId is returned or an exception is thrown
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateDtoFetchServiceImpl implements CandidateDtoFetchService {
    @PersistenceContext
    private EntityManager entityManager;
    private final CandidateJsonDao jsonDao;
    private final ObjectMapper objectMapper;
    private final CandidateJsonCacheDao pgCacheDao;
    private final CandidateRedisCache redisCache;
    private final CandidateVersionDao versionDao;

    @Override
    public Page<CandidateReadDto> fetchPage(
        String fetchIdsSql, String countSql, @NonNull PageRequest pageRequest) {
        return fetchPage(fetchIdsSql, countSql, pageRequest, null);
    }

    @Override
    public Page<CandidateReadDto> fetchPage(
        String fetchIdsSql, String countSql, @NonNull PageRequest pageRequest,
        @Nullable String textQuery) {
        //Create and execute the query to return the candidate ids
        Query query = entityManager.createNativeQuery(fetchIdsSql);
        CandidateSearchUtils.bindTextSearchParameter(query, fetchIdsSql, textQuery);
        query.setFirstResult((int) pageRequest.getOffset());
        query.setMaxResults(pageRequest.getPageSize());

        LogBuilder.builder(log).action("findCandidates")
            .message("Query: " + fetchIdsSql).logInfo();
        long start = System.currentTimeMillis();
        long end;

        //Get results
        final List<?> results = query.getResultList();

        end = System.currentTimeMillis();
        long fetchIdsTime = end - start;

        start = end;

        //Process the results
        List<IdAndRank> idAndRanks =
            CandidateSearchUtils.processIdRankSearchResults(results, pageRequest.getSort());

        end = System.currentTimeMillis();
        long convertTime = end - start;
        start = end;

        //Get ids of sorted candidates
        List<Long> ids = idAndRanks.stream().map(IdAndRank::id).toList();

        end = System.currentTimeMillis();
        long fetchEntitiesTime = end - start;
        start = end;

        Map<Long, CandidateReadDto> candidatesByIdUnsorted;
        candidatesByIdUnsorted = fetchByIds(ids);

        end = System.currentTimeMillis();
        long fetchDtosTime = end - start;
        start = end;

        //Candidates need to be sorted the same as the ids.

        //Construct a sorted list of the candidates in the same order as the returned ids.
        List<CandidateReadDto> candidatesSorted = new ArrayList<>();
        for (IdAndRank idAndRank : idAndRanks) {
            final CandidateReadDto candidate = candidatesByIdUnsorted.get(idAndRank.id());

            //Optionally update candidate data with any ranking values.
            final Number rank = idAndRank.rank();
            //Rank is a transient field so no need to set to null
            if (rank != null) {
                candidate.setRank(rank);
            }
            candidatesSorted.add(candidate);
        }

        end = System.currentTimeMillis();
        long sortTime = end - start;

        LogBuilder.builder(log).action("countCandidates")
            .message("Query: " + countSql).logInfo();
        start = end;
        //Compute count
        Query countQuery = entityManager.createNativeQuery(countSql);
        CandidateSearchUtils.bindTextSearchParameter(countQuery, countSql, textQuery);
        long total =  ((Number) countQuery.getSingleResult()).longValue();

        end = System.currentTimeMillis();
        long countTime = end - start;

        LogBuilder.builder(log).action("findCandidates")
            .message("Timings: fetchIds: " + fetchIdsTime
                + " convert: " + convertTime
                + " fetchEntities: " + fetchEntitiesTime
                + " fetchDtos: " + fetchDtosTime
                + " sort: " + sortTime
                + " count: " + countTime
            ).logInfo();

        return new PageImpl<>(candidatesSorted, pageRequest, total);
    }

    @Override
    @NonNull
    public Map<Long, CandidateReadDto> fetchByIds(Collection<Long> ids)
        throws NoSuchObjectException {

        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        // ------------------------------------------------------------
        // Step 0: Fetch authoritative candidate versions
        // ------------------------------------------------------------

        Map<Long, Long> versions =
            versionDao.fetchCandidateVersions(new ArrayList<>(ids));

        // STRICT: check that all ids are valid - ie correspond to a candidate on the db.
        List<Long> badIds = ids.stream()
            .filter(id -> !versions.containsKey(id))
            .toList();

        if (!badIds.isEmpty()) {
            LogBuilder.builder(log)
                .action("CandidateDtoService.loadByIds")
                .message("Requested candidate IDs not found in candidate table. "
                    + "requestedIds=" + ids + ", missingIds=" + badIds)
                .logError();
            throw new NoSuchObjectException(
                "Candidates not found with ids: " + badIds
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

            List<CandidateJsonCache> pgRows =
                pgCacheDao.findByIds(redisMissIds);

            Map<Long, CandidateRedisCache.VersionedJson> redisUpdates =
                new HashMap<>();

            for (CandidateJsonCache jsonCache : pgRows) {

                // Cache hit only if versions match
                if (jsonCache.isCacheHit()) {

                    // ASSERTION: DB cache JSON must never be null
                    if (jsonCache.json() == null || jsonCache.json().isBlank()) {
                        throw new IllegalStateException(
                            "Null/blank JSON in candidate_json_cache for candidateId="
                                + jsonCache.candidateId()
                        );
                    }

                    jsonById.put(jsonCache.candidateId(), jsonCache.json());

                    redisUpdates.put(
                        jsonCache.candidateId(),
                        new CandidateRedisCache.VersionedJson(
                            jsonCache.candidateId(),
                            jsonCache.candidateVersion(),
                            jsonCache.json()
                        )
                    );
                }
            }

            // Update Redis from Postgres hits (shared benefit across nodes)
            if (!redisUpdates.isEmpty()) {
                redisCache.putAll(redisUpdates);
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

            //Update the redis cache
            redisCache.putAll(redisWrites);
        }

        // ------------------------------------------------------------
        // Step 4: STRICT completeness check
        // ------------------------------------------------------------

        List<Long> unprocessedIds = ids.stream()
            .filter(id -> !jsonById.containsKey(id))
            .toList();

        if (!unprocessedIds.isEmpty()) {
            LogBuilder.builder(log)
                .action("CandidateDtoService.loadByIds")
                .message("Candidate JSON missing after Redis + Postgres cache + recompute. "
                    + "requestedIds=" + ids + ", unprocessedIds=" + unprocessedIds
                    + ", versions=" + versions)
                .logError();
            throw new NoSuchObjectException(
                "Candidates not found with ids: " + unprocessedIds
            );
        }

        // ------------------------------------------------------------
        // Step 5: Deserialize JSON into DTOs
        // ------------------------------------------------------------

        Map<Long, CandidateReadDto> out = new HashMap<>(jsonById.size());
        for (Map.Entry<Long, String> e : jsonById.entrySet()) {
            try {
                out.put(e.getKey(), deserialize(e.getValue()));
            } catch (JsonProcessingException ex) {
                String mess = "Could not deserialize cached JSON for candidate id=" + e.getKey();
                throw new RuntimeException(mess, ex);
            }
        }
        return out;
    }

    private CandidateReadDto deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, CandidateReadDto.class);
    }
}
