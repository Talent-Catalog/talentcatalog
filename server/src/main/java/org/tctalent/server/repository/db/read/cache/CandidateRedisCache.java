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

package org.tctalent.server.repository.db.read.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import org.tctalent.server.configuration.properties.CandidateCacheProperties;

/**
 * Redis-backed L1 cache for candidate JSON.
 * <p>
 * Keys are versioned:
 *   candidate:json:{candidateId}:v:{dataVersion}
 * </p>
 * <p>
 * Versioned keys mean:
 *  - no explicit invalidation
 *  - stale entries become unreachable automatically
 * </p>
 * <p>
 * TTL (if configured) is for memory hygiene only, not correctness.
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class CandidateRedisCache {

    private final StringRedisTemplate redisTemplate;
    private final CandidateCacheProperties cacheProperties;

    private ValueOperations<String, String> values() {
        return redisTemplate.opsForValue();
    }

    /**
     * Build the Redis key for a candidate/version pair.
     */
    public String key(long candidateId, long version) {
        return "candidate:json:" + candidateId + ":v:" + version;
    }

    /**
     * Batch fetch JSON for the given candidate IDs and versions.
     *
     * @param idToVersion map of candidateId -> dataVersion
     * @return map of candidateId -> JSON (only entries found in Redis)
     */
    public Map<Long, String> multiGet(Map<Long, Long> idToVersion) {

        if (idToVersion == null || idToVersion.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = new ArrayList<>(idToVersion.keySet());

        List<String> keys = ids.stream()
            .map(id -> key(id, idToVersion.get(id)))
            .toList();

        List<String> valuesList = values().multiGet(keys);
        if (valuesList == null) {
            return Map.of();
        }

        Map<Long, String> result = new HashMap<>();

        for (int i = 0; i < ids.size(); i++) {
            String json = valuesList.get(i);
            if (json != null && !json.isBlank()) {
                result.put(ids.get(i), json);
            }
        }

        return result;
    }

    /**
     * Store multiple JSON blobs in Redis.
     * <p>
     * Uses individual SET operations because Redis MSET
     * does not support per-key TTL.
     * </p>
     * <p>
     * If TTL is non-positive, entries are stored without expiry.
     * </p>
     */
    public void putAll(Map<Long, VersionedJson> rows) {

        if (rows == null || rows.isEmpty()) {
            return;
        }

        Duration ttl = cacheProperties.getTtl();
        boolean hasTtl = ttl != null && !ttl.isZero() && !ttl.isNegative();

        for (VersionedJson row : rows.values()) {
            String key = key(row.candidateId(), row.version());

            if (!hasTtl) {
                values().set(key, row.json());
            } else {
                values().set(key, row.json(), ttl);
            }
        }
    }

    /**
     * Simple value holder used when loading Redis from DB cache
     * or after recomputation.
     */
    public record VersionedJson(
        long candidateId,
        long version,
        String json
    ) {}
}
