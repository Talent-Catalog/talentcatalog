/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link CacheService} interface for cache management.
 * <p>
 * author: sadatmalik
 */
@Service
public class CacheServiceImpl implements CacheService {

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

}
