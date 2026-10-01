/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.tctalent.server.configuration.properties.CandidateCacheProperties;

@ExtendWith(MockitoExtension.class)
class CandidateRedisCacheTest {

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private CandidateCacheProperties cacheProperties;

  @Mock
  private Cursor<String> cursor;

  private CandidateRedisCache candidateRedisCache;

  @BeforeEach
  void setUp() {
    candidateRedisCache = new CandidateRedisCache(redisTemplate, cacheProperties);
  }

  @Test
  void clear_deletesCandidateCacheKeys() {
    List<String> keys = List.of(
        "candidate:json:1:v:1",
        "candidate:json:2:v:3",
        "candidate:json:100:v:7"
    );

    stubScan(keys);
    List<List<String>> deletedBatches = captureDeletedBatches();

    candidateRedisCache.clear();

    assertEquals(1, deletedBatches.size());
    assertEquals(keys, deletedBatches.get(0));

    verify(cursor).close();
  }

  @Test
  void clear_scansOnlyCandidateJsonKeys() {
    ArgumentCaptor<ScanOptions> optionsCaptor =
        ArgumentCaptor.forClass(ScanOptions.class);

    when(redisTemplate.scan(optionsCaptor.capture())).thenReturn(cursor);
    when(cursor.hasNext()).thenReturn(false);

    candidateRedisCache.clear();

    ScanOptions options = optionsCaptor.getValue();

    assertEquals("candidate:json:*", options.getPattern());
    assertEquals(1000L, options.getCount());

    verify(cursor).close();
  }

  @Test
  void clear_doesNotDeleteAnythingWhenCandidateCacheIsEmpty() {
    stubScan(List.of());

    candidateRedisCache.clear();

    verify(redisTemplate, never()).delete(anyCollection());
    verify(cursor).close();
  }

  @Test
  void clear_deletesCandidateKeysInBatches() {
    List<String> keys = new ArrayList<>();

    for (int i = 1; i <= 1501; i++) {
      keys.add("candidate:json:" + i + ":v:1");
    }

    stubScan(keys);
    List<List<String>> deletedBatches = captureDeletedBatches();

    candidateRedisCache.clear();

    assertEquals(2, deletedBatches.size());

    assertEquals(1000, deletedBatches.get(0).size());
    assertEquals(keys.subList(0, 1000), deletedBatches.get(0));

    assertEquals(501, deletedBatches.get(1).size());
    assertEquals(keys.subList(1000, 1501), deletedBatches.get(1));

    verify(cursor).close();
  }

  private void stubScan(List<String> keys) {
    Iterator<String> iterator = keys.iterator();

    when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
    when(cursor.hasNext()).thenAnswer(invocation -> iterator.hasNext());

    if (!keys.isEmpty()) {
      when(cursor.next()).thenAnswer(invocation -> iterator.next());
    }
  }

  private List<List<String>> captureDeletedBatches() {
    List<List<String>> deletedBatches = new ArrayList<>();

    doAnswer(invocation -> {
      Collection<String> keys = invocation.getArgument(0);

      deletedBatches.add(new ArrayList<>(keys));

      return (long) keys.size();
    }).when(redisTemplate).delete(anyCollection());

    return deletedBatches;
  }
}