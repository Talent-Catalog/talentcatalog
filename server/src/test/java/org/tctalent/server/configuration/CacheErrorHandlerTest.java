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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class CacheErrorHandlerTest {

  static class TestCacheableService {
    private final CountingRepository countingRepository;

    TestCacheableService(CountingRepository countingRepository) {
      this.countingRepository = countingRepository;
    }

    @Cacheable(value = "users", key = "#username")
    public String loadByUsername(String username) {
      return countingRepository.load(username);
    }
  }

  static class CountingRepository {
    private final AtomicInteger calls = new AtomicInteger();

    String load(String username) {
      return "db-value-" + calls.incrementAndGet() + "-" + username;
    }

    int calls() {
      return calls.get();
    }
  }

  static class ThrowingCache implements Cache {
    private final String name;

    ThrowingCache(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Object getNativeCache() {
      return this;
    }

    @Override
    public ValueWrapper get(Object key) {
      throw new RuntimeException("Cache get failure");
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
      throw new RuntimeException("Cache get failure");
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
      throw new RuntimeException("Cache get failure");
    }

    @Override
    public void put(Object key, Object value) {
      throw new RuntimeException("Cache put failure");
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
      throw new RuntimeException("Cache put failure");
    }

    @Override
    public void evict(Object key) {
      throw new RuntimeException("Cache evict failure");
    }

    @Override
    public void clear() {
      throw new RuntimeException("Cache clear failure");
    }
  }

  @Test
  void cacheErrorsFallThroughToRepository() {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.register(CacheConfig.class);
      context.registerBean(CacheManager.class, () -> {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(new ThrowingCache("users")));
        return cacheManager;
      });
      context.registerBean(CountingRepository.class);
      context.registerBean(TestCacheableService.class);
      context.refresh();

      TestCacheableService service = context.getBean(TestCacheableService.class);
      CountingRepository repository = context.getBean(CountingRepository.class);

      String firstResult = service.loadByUsername("alice");
      String secondResult = service.loadByUsername("alice");

      assertThat(firstResult).isEqualTo("db-value-1-alice");
      assertThat(secondResult).isEqualTo("db-value-2-alice");
      assertThat(repository.calls()).isEqualTo(2);
    }
  }
}
