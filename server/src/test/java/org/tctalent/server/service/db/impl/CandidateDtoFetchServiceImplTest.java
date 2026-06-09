package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.repository.db.read.cache.CandidateJsonCache;
import org.tctalent.server.repository.db.read.cache.CandidateJsonCacheDao;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.repository.db.read.cache.CandidateVersionDao;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.repository.db.read.sql.CandidateJsonDao;
import org.tctalent.server.util.CandidateSearchUtils;
import org.tctalent.server.util.textExtract.IdAndRank;

@ExtendWith(MockitoExtension.class)
class CandidateDtoFetchServiceImplTest {

  @Mock
  private EntityManager entityManager;

  @Mock
  private CandidateJsonDao jsonDao;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private CandidateJsonCacheDao pgCacheDao;

  @Mock
  private CandidateRedisCache redisCache;

  @Mock
  private CandidateVersionDao versionDao;

  @InjectMocks
  private CandidateDtoFetchServiceImpl service;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(service, "entityManager", entityManager);
  }

  @Test
  void fetchByIdsReturnsEmptyMapForNullOrEmptyInput() {
    assertTrue(service.fetchByIds(null).isEmpty());
    assertTrue(service.fetchByIds(List.of()).isEmpty());

    verifyNoInteractions(versionDao);
    verifyNoInteractions(redisCache);
    verifyNoInteractions(pgCacheDao);
    verifyNoInteractions(jsonDao);
    verifyNoInteractions(objectMapper);
  }

  @Test
  void fetchByIdsThrowsWhenCandidateVersionMissing() {
    when(versionDao.fetchCandidateVersions(List.of(1L, 2L)))
        .thenReturn(Map.of(1L, 100L));

    assertThrows(NoSuchObjectException.class,
        () -> service.fetchByIds(List.of(1L, 2L)));

    verifyNoInteractions(redisCache);
    verifyNoInteractions(pgCacheDao);
    verifyNoInteractions(jsonDao);
    verifyNoInteractions(objectMapper);
  }

  @Test
  void fetchByIdsReturnsDtosFromRedisOnly() throws Exception {
    CandidateReadDto dto = mock(CandidateReadDto.class);

    when(versionDao.fetchCandidateVersions(List.of(1L)))
        .thenReturn(Map.of(1L, 100L));
    when(redisCache.multiGet(Map.of(1L, 100L)))
        .thenReturn(Map.of(1L, "{\"id\":1}"));
    when(objectMapper.readValue("{\"id\":1}", CandidateReadDto.class))
        .thenReturn(dto);

    Map<Long, CandidateReadDto> result = service.fetchByIds(List.of(1L));

    assertEquals(1, result.size());
    assertSame(dto, result.get(1L));

    verify(pgCacheDao, never()).findByIds(any());
    verify(jsonDao, never()).loadJsonByIds(any());
    verify(pgCacheDao, never()).upsert(anyLong(), anyLong(), anyString());
    verify(redisCache, never()).putAll(any());
  }

  @Test
  void fetchByIdsUsesPostgresCacheForRedisMissAndWarmsRedis() throws Exception {
    CandidateReadDto dto = mock(CandidateReadDto.class);
    CandidateJsonCache pgHit = validPgCacheHit(1L, 100L, "{\"id\":1}");

    when(versionDao.fetchCandidateVersions(List.of(1L)))
        .thenReturn(Map.of(1L, 100L));
    when(redisCache.multiGet(Map.of(1L, 100L)))
        .thenReturn(Map.of());
    when(pgCacheDao.findByIds(List.of(1L))).thenReturn(List.of(pgHit));
    when(objectMapper.readValue("{\"id\":1}", CandidateReadDto.class))
        .thenReturn(dto);

    Map<Long, CandidateReadDto> result = service.fetchByIds(List.of(1L));

    assertEquals(1, result.size());
    assertSame(dto, result.get(1L));

    verify(redisCache).putAll(any());
    verify(jsonDao, never()).loadJsonByIds(any());
    verify(pgCacheDao, never()).upsert(anyLong(), anyLong(), anyString());
  }

  @Test
  void fetchByIdsIgnoresPostgresCacheMissAndRecomputesJson() throws Exception {
    CandidateReadDto dto = mock(CandidateReadDto.class);
    CandidateJsonCache pgMiss = pgCacheMiss();

    when(versionDao.fetchCandidateVersions(List.of(1L)))
        .thenReturn(Map.of(1L, 100L));
    when(redisCache.multiGet(Map.of(1L, 100L)))
        .thenReturn(Map.of());
    when(pgCacheDao.findByIds(List.of(1L))).thenReturn(List.of(pgMiss));
    when(jsonDao.loadJsonByIds(List.of(1L)))
        .thenReturn(Map.of(1L, "{\"id\":1}"));
    when(objectMapper.readValue("{\"id\":1}", CandidateReadDto.class))
        .thenReturn(dto);

    Map<Long, CandidateReadDto> result = service.fetchByIds(List.of(1L));

    assertEquals(1, result.size());
    assertSame(dto, result.get(1L));

    verify(pgCacheDao).upsert(1L, 100L, "{\"id\":1}");
    verify(redisCache).putAll(any());
  }

  @Test
  void fetchByIdsThrowsWhenPostgresCacheHitHasBlankJson() throws JsonProcessingException {
    CandidateJsonCache badPgHit = blankPgCacheHit(1L, " ");

    when(versionDao.fetchCandidateVersions(List.of(1L)))
        .thenReturn(Map.of(1L, 100L));
    when(redisCache.multiGet(Map.of(1L, 100L)))
        .thenReturn(Map.of());
    when(pgCacheDao.findByIds(List.of(1L))).thenReturn(List.of(badPgHit));

    assertThrows(IllegalStateException.class,
        () -> service.fetchByIds(List.of(1L)));

    verify(jsonDao, never()).loadJsonByIds(any());
    verify(objectMapper, never()).readValue(any(String.class), any(Class.class));
  }

  @Test
  void fetchByIdsThrowsWhenRecomputedJsonIsBlank() throws JsonProcessingException {
    when(versionDao.fetchCandidateVersions(List.of(1L)))
        .thenReturn(Map.of(1L, 100L));
    when(redisCache.multiGet(Map.of(1L, 100L)))
        .thenReturn(Map.of());
    when(pgCacheDao.findByIds(List.of(1L))).thenReturn(List.of());
    when(jsonDao.loadJsonByIds(List.of(1L)))
        .thenReturn(Map.of(1L, " "));

    assertThrows(IllegalStateException.class,
        () -> service.fetchByIds(List.of(1L)));

    verify(pgCacheDao, never()).upsert(anyLong(), anyLong(), anyString());
    verify(redisCache, never()).putAll(any());
    verify(objectMapper, never()).readValue(any(String.class), any(Class.class));
  }

  @Test
  void fetchByIdsWrapsJsonProcessingException() throws Exception {
    when(versionDao.fetchCandidateVersions(List.of(1L)))
        .thenReturn(Map.of(1L, 100L));
    when(redisCache.multiGet(Map.of(1L, 100L)))
        .thenReturn(Map.of(1L, "bad-json"));
    when(objectMapper.readValue("bad-json", CandidateReadDto.class))
        .thenThrow(JsonMappingException.fromUnexpectedIOE(new IOException("bad json")));

    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> service.fetchByIds(List.of(1L)));

    assertTrue(ex.getMessage().contains("candidate id=1"));
  }

  @Test
  void fetchByIdsHandlesMixedRedisPostgresAndRecomputeSources() throws Exception {
    CandidateReadDto redisDto = mock(CandidateReadDto.class);
    CandidateReadDto pgDto = mock(CandidateReadDto.class);
    CandidateReadDto recomputedDto = mock(CandidateReadDto.class);

    CandidateJsonCache pgHit = validPgCacheHit(2L, 200L, "{\"id\":2}");

    when(versionDao.fetchCandidateVersions(List.of(1L, 2L, 3L)))
        .thenReturn(Map.of(1L, 100L, 2L, 200L, 3L, 300L));
    when(redisCache.multiGet(Map.of(1L, 100L, 2L, 200L, 3L, 300L)))
        .thenReturn(Map.of(1L, "{\"id\":1}"));
    when(pgCacheDao.findByIds(List.of(2L, 3L))).thenReturn(List.of(pgHit));
    when(jsonDao.loadJsonByIds(List.of(3L)))
        .thenReturn(Map.of(3L, "{\"id\":3}"));

    when(objectMapper.readValue("{\"id\":1}", CandidateReadDto.class))
        .thenReturn(redisDto);
    when(objectMapper.readValue("{\"id\":2}", CandidateReadDto.class))
        .thenReturn(pgDto);
    when(objectMapper.readValue("{\"id\":3}", CandidateReadDto.class))
        .thenReturn(recomputedDto);

    Map<Long, CandidateReadDto> result = service.fetchByIds(List.of(1L, 2L, 3L));

    assertEquals(3, result.size());
    assertSame(redisDto, result.get(1L));
    assertSame(pgDto, result.get(2L));
    assertSame(recomputedDto, result.get(3L));

    verify(pgCacheDao).findByIds(List.of(2L, 3L));
    verify(jsonDao).loadJsonByIds(List.of(3L));
    verify(pgCacheDao).upsert(3L, 300L, "{\"id\":3}");
    verify(redisCache, times(2)).putAll(any());
  }

  @Test
  void fetchPageReturnsSortedDtosAppliesRankAndRunsCount() throws Exception {
    String fetchIdsSql = "select id from candidate";
    String countSql = "select count(*) from candidate";

    Query idsQuery = mock(Query.class);
    Query countQuery = mock(Query.class);

    PageRequest pageRequest = PageRequest.of(1, 2);
    List<Object> rawRows = List.of(new Object(), new Object());
    List<IdAndRank> idAndRanks = List.of(
        new IdAndRank(2L, 0.75),
        new IdAndRank(1L, null)
    );

    CandidateReadDto dtoOne = mock(CandidateReadDto.class);
    CandidateReadDto dtoTwo = mock(CandidateReadDto.class);

    when(entityManager.createNativeQuery(fetchIdsSql)).thenReturn(idsQuery);
    when(entityManager.createNativeQuery(countSql)).thenReturn(countQuery);
    when(idsQuery.getResultList()).thenReturn(rawRows);
    when(countQuery.getSingleResult()).thenReturn(22L);

    when(versionDao.fetchCandidateVersions(List.of(2L, 1L)))
        .thenReturn(Map.of(1L, 100L, 2L, 200L));
    when(redisCache.multiGet(Map.of(1L, 100L, 2L, 200L)))
        .thenReturn(Map.of(1L, "{\"id\":1}", 2L, "{\"id\":2}"));
    when(objectMapper.readValue("{\"id\":1}", CandidateReadDto.class))
        .thenReturn(dtoOne);
    when(objectMapper.readValue("{\"id\":2}", CandidateReadDto.class))
        .thenReturn(dtoTwo);

    try (MockedStatic<CandidateSearchUtils> utilities =
        mockStatic(CandidateSearchUtils.class)) {
      utilities.when(() -> CandidateSearchUtils.processIdRankSearchResults(
              rawRows, pageRequest.getSort()))
          .thenReturn(idAndRanks);

      Page<CandidateReadDto> result =
          service.fetchPage(fetchIdsSql, countSql, pageRequest);

      assertEquals(22L, result.getTotalElements());
      assertEquals(2, result.getContent().size());
      assertSame(dtoTwo, result.getContent().get(0));
      assertSame(dtoOne, result.getContent().get(1));

      verify(dtoTwo).setRank(0.75);
      verify(dtoOne, never()).setRank(any());
      verify(idsQuery).setFirstResult(2);
      verify(idsQuery).setMaxResults(2);
    }
  }

  private static CandidateJsonCache validPgCacheHit(Long id, Long version, String json) {
    CandidateJsonCache cache = mock(CandidateJsonCache.class);
    when(cache.isCacheHit()).thenReturn(true);
    when(cache.json()).thenReturn(json);
    when(cache.candidateId()).thenReturn(id);
    when(cache.candidateVersion()).thenReturn(version);
    return cache;
  }

  private static CandidateJsonCache blankPgCacheHit(Long id, String json) {
    CandidateJsonCache cache = mock(CandidateJsonCache.class);
    when(cache.isCacheHit()).thenReturn(true);
    when(cache.json()).thenReturn(json);
    when(cache.candidateId()).thenReturn(id);
    return cache;
  }

  private static CandidateJsonCache pgCacheMiss() {
    CandidateJsonCache cache = mock(CandidateJsonCache.class);
    when(cache.isCacheHit()).thenReturn(false);
    return cache;
  }
}