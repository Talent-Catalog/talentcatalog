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

/**
 * Represents a candidate joined with its cached JSON (if any).
 *
 * Fields:
 *  - candidateId       : the candidate's primary key
 *  - candidateVersion  : authoritative version from candidate.data_version
 *  - cachedVersion     : version the cached JSON was computed against (nullable)
 *  - json              : cached JSON from candidate_json_cache (nullable)
 *
 * This is a simple data carrier. All cache validity logic lives elsewhere.
 */
public record CandidateJsonCache(
    long candidateId,
    long candidateVersion,
    Long cachedVersion,
    String json
) {

    /**
     * Cache hit if and only if:
     *  - a cached row exists (cachedVersion != null)
     *  - and it was computed against the current candidate version
     */
    public boolean isCacheHit() {
        return cachedVersion != null && cachedVersion == candidateVersion;
    }
}
