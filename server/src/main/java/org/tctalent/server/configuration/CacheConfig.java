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

package org.tctalent.server.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.tctalent.server.logging.LogBuilder;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

  @Override
  public CacheErrorHandler errorHandler() {
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        logCacheError("get", cache, key, exception);
      }

      @Override
      public void handleCachePutError(RuntimeException exception, Cache cache, Object key,
          Object value) {
        logCacheError("put", cache, key, exception);
      }

      @Override
      public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logCacheError("evict", cache, key, exception);
      }

      @Override
      public void handleCacheClearError(RuntimeException exception, Cache cache) {
        logCacheError("clear", cache, null, exception);
      }

      private void logCacheError(String operation, Cache cache, Object key,
                                 RuntimeException exception) {
        LogBuilder.builder(log)
            .action("cache-" + operation)
            .message("Cache " + operation + " failed for cache '"
                + (cache != null ? cache.getName() : "unknown")
                + "'"
                + (key != null ? " key '" + key + "'" : "")
                + " - falling through to database")
            .logError(exception);
      }
    };
  }
}
