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

package org.tctalent.server.service.db.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;

/**
 * Implementation of the {@link CacheService} interface for cache management.
 * <p>
 * author: sadatmalik
 */
@Service
public class CacheServiceImpl implements CacheService {

  private final CandidateRedisCache candidateRedisCache;

  public CacheServiceImpl(CandidateRedisCache candidateRedisCache) {
    this.candidateRedisCache = candidateRedisCache;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This method is annotated with {@code @CacheEvict} to remove all entries
   * in the "users" cache when invoked.
   * </p>
   */
  @CacheEvict(value = "users", allEntries = true)
  @Override
  public void flushUserCache() {
    // This method will remove all entries in the "users" cache
  }

  /**
   * {@inheritDoc}
   * <p>
   * Candidate JSON is stored directly in Redis via {@link CandidateRedisCache}
   * ({@code candidate:json:*} keys), not through Spring's cache abstraction.
   * {@code @CacheEvict} therefore cannot be used here — it would only clear a
   * named Spring cache and leave the candidate keys in place. This method
   * delegates to {@link CandidateRedisCache#clear()} to SCAN and delete those
   * keys. Postgres {@code candidate_json_cache} is left unchanged.
   * </p>
   */
  @Override
  public void flushCandidateCache() {
    candidateRedisCache.clear();
  }

}
