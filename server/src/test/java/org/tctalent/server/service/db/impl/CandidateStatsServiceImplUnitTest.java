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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.DataRow;
import org.tctalent.server.model.db.Gender;

@ExtendWith(MockitoExtension.class)
class CandidateStatsServiceImplUnitTest {

  private static final LocalDate DATE_FROM = LocalDate.of(2020, 1, 1);
  private static final LocalDate DATE_TO = LocalDate.of(2020, 12, 31);
  private static final Set<Long> CANDIDATE_IDS = Set.of(11L, 22L);
  private static final List<Long> SOURCE_COUNTRY_IDS = List.of(101L, 202L);
  private static final String CONSTRAINT =
      "candidate.id in (select id from candidate where foo = true)";

  @Mock
  private EntityManager entityManager;

  @Mock
  private Query query;

  @InjectMocks
  private CandidateStatsServiceImpl service;

  @BeforeEach
  void setUp() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    lenient().when(query.getResultList()).thenReturn(defaultResultRows());
  }

  @Test
  void everyStatsMethodBuildsNativeSqlBindsParametersAndMapsRows() {
    List<List<DataRow>> results = List.of(
        service.computeBirthYearStats(
            Gender.female, DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS,
            CONSTRAINT),
        service.computeGenderStats(
            DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeLanguageStats(
            null, DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeLinkedInExistsStats(
            DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeLinkedInStats(
            DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeMaxEducationStats(
            Gender.female, DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS,
            CONSTRAINT),
        service.computeMostCommonOccupationStats(
            Gender.female, DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS,
            CONSTRAINT),
        service.computeNationalityStats(
            null, null, DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS,
            CONSTRAINT),
        service.computeOccupationStats(
            Gender.female, DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS,
            CONSTRAINT),
        service.computeReferrerStats(
            Gender.female, "jordan", DATE_FROM, DATE_TO, CANDIDATE_IDS,
            SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeRegistrationStats(
            DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeSourceCountryStats(
            Gender.female, DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS,
            CONSTRAINT),
        service.computeSpokenLanguageLevelStats(
            Gender.female, "English", DATE_FROM, DATE_TO, CANDIDATE_IDS,
            SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeStatusStats(
            null, null, DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS,
            CONSTRAINT),
        service.computeSurveyStats(
            Gender.female, "jordan", DATE_FROM, DATE_TO, CANDIDATE_IDS,
            SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeRegistrationOccupationStats(
            DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeUnhcrRegisteredStats(
            DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS, CONSTRAINT),
        service.computeUnhcrStatusStats(
            DATE_FROM, DATE_TO, CANDIDATE_IDS, SOURCE_COUNTRY_IDS, CONSTRAINT)
    );

    results.forEach(CandidateStatsServiceImplUnitTest::assertDefaultRows);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(entityManager, times(18)).createNativeQuery(sqlCaptor.capture());

    List<String> sqls = sqlCaptor.getAllValues();
    String allSql = String.join("\n---\n", sqls);

    assertTrue(sqls.get(0).contains("extract(year from dob)"));
    assertTrue(sqls.get(1).contains("select gender"));
    assertTrue(sqls.get(2).contains("candidate_language"));
    assertTrue(sqls.get(3).contains("as hasLink"));
    assertTrue(sqls.get(4).contains("linked_in_link is not null"));
    assertTrue(sqls.get(5).contains("EducationLevel"));
    assertTrue(sqls.get(6).contains("not (lower(occupation.name)"));
    assertTrue(sqls.get(7).contains("nationality.name"));
    assertTrue(sqls.get(8).contains("left join occupation"));
    assertTrue(sqls.get(9).contains("rego_referrer_param"));
    assertTrue(sqls.get(10).contains("DATE(users.created_date)"));
    assertTrue(sqls.get(11).contains("source.name"));
    assertTrue(sqls.get(12).contains("lower(language.name) = lower(:language)"));
    assertTrue(sqls.get(13).contains("candidate.status"));
    assertTrue(sqls.get(14).contains("survey_type.name"));
    assertTrue(sqls.get(15).contains("candidate_occupation"));
    assertTrue(sqls.get(16).contains("UNHCRRegistered"));
    assertTrue(sqls.get(17).contains("unhcr_status is not null"));

    assertTrue(allSql.contains("candidate.country_id in (:sourceCountryIds)"));
    assertTrue(allSql.contains(CONSTRAINT));
    assertTrue(allSql.contains("candidate.id in (:candidateIds)"));
    assertFalse(allSql.contains("candidate.status != 'ineligible'"));

    assertTrue(sqls.get(0).contains("candidate.status != 'draft'"));
    assertFalse(sqls.get(10).contains("candidate.status != 'draft'"));
    assertFalse(sqls.get(13).contains("candidate.status != 'draft'"));

    verify(query, times(18)).setParameter("dateFrom", DATE_FROM);
    verify(query, times(18)).setParameter("dateTo", DATE_TO);
    verify(query, times(18)).setParameter("sourceCountryIds", SOURCE_COUNTRY_IDS);
    verify(query, times(18)).setParameter("candidateIds", CANDIDATE_IDS);

    verify(query, atLeastOnce()).setParameter("gender", Gender.female.toString());
    verify(query, atLeastOnce()).setParameter("gender", "%");
    verify(query, atLeastOnce()).setParameter("country", "%");
    verify(query, atLeastOnce()).setParameter("country", "jordan");
    verify(query).setParameter("language", "English");
  }

  @Test
  void nullDatesCandidateIdsSourceCountriesAndConstraintUseDefaultsAndExcludeIneligible() {
    LocalDate before = LocalDate.now();

    service.computeGenderStats(null, null, null, null, null);

    LocalDate after = LocalDate.now();

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(entityManager).createNativeQuery(sqlCaptor.capture());

    String sql = sqlCaptor.getValue();

    assertTrue(sql.contains("users.created_date >= :dateFrom"));
    assertTrue(sql.contains("users.created_date <= :dateTo"));
    assertTrue(sql.contains("users.status = 'active'"));
    assertTrue(sql.contains("candidate.status != 'draft'"));
    assertTrue(sql.contains("candidate.status != 'ineligible'"));
    assertTrue(sql.contains("saved_list where name = 'TestCandidates'"));

    assertFalse(sql.contains("candidate.country_id in (:sourceCountryIds)"));
    assertFalse(sql.contains("candidate.id in (:candidateIds)"));

    verify(query).setParameter("dateFrom", LocalDate.of(2000, 1, 1));

    ArgumentCaptor<Object> dateToCaptor = ArgumentCaptor.forClass(Object.class);
    verify(query).setParameter(eq("dateTo"), dateToCaptor.capture());

    LocalDate actualDateTo = (LocalDate) dateToCaptor.getValue();
    assertFalse(actualDateTo.isBefore(before));
    assertFalse(actualDateTo.isAfter(after));

    verify(query, never()).setParameter(eq("sourceCountryIds"), any());
    verify(query, never()).setParameter(eq("candidateIds"), any());
  }

  @Test
  void emptySourceCountryListDoesNotAddSqlConditionOrBindParameter() {
    service.computeGenderStats(
        DATE_FROM, DATE_TO, null, List.of(), "candidate.id > 0");

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(entityManager).createNativeQuery(sqlCaptor.capture());

    String sql = sqlCaptor.getValue();

    assertTrue(sql.contains("and candidate.id > 0"));
    assertFalse(sql.contains("candidate.country_id in (:sourceCountryIds)"));
    assertFalse(sql.contains("candidate.id in (:candidateIds)"));
    assertFalse(sql.contains("candidate.status != 'ineligible'"));

    verify(query, never()).setParameter(eq("sourceCountryIds"), any());
    verify(query, never()).setParameter(eq("candidateIds"), any());
  }

  @Test
  void limitedStatsCollapseRowsAfterLimitIntoOther() {
    when(query.getResultList()).thenReturn(numberedRows(16));

    List<DataRow> rows = service.computeLanguageStats(
        Gender.female, DATE_FROM, DATE_TO, null, null, null);

    assertEquals(15, rows.size());
    assertEquals("row-1", rows.get(0).getLabel());
    assertEquals("row-14", rows.get(13).getLabel());
    assertEquals("Other", rows.get(14).getLabel());
    assertBigDecimalEquals(31L, rows.get(14).getValue());
  }

  @Test
  void limitedStatsDoNotAddOtherWhenRemainderIsZero() {
    when(query.getResultList()).thenReturn(rowsWithZeroRemainderAfterLimit());

    List<DataRow> rows = service.computeSourceCountryStats(
        Gender.female, DATE_FROM, DATE_TO, null, null, null);

    assertEquals(14, rows.size());
    assertFalse(rows.stream().anyMatch(row -> "Other".equals(row.getLabel())));
  }

  private static List<Object[]> defaultResultRows() {
    return List.of(
        new Object[] {"alpha", 2L},
        new Object[] {null, 3L}
    );
  }

  private static List<Object[]> numberedRows(int count) {
    List<Object[]> rows = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      rows.add(new Object[] {"row-" + i, (long) i});
    }
    return rows;
  }

  private static List<Object[]> rowsWithZeroRemainderAfterLimit() {
    List<Object[]> rows = new ArrayList<>();
    for (int i = 1; i <= 14; i++) {
      rows.add(new Object[] {"row-" + i, 1L});
    }
    rows.add(new Object[] {"row-15", 0L});
    rows.add(new Object[] {"row-16", 0L});
    return rows;
  }

  private static void assertDefaultRows(List<DataRow> rows) {
    assertEquals(2, rows.size());

    assertEquals("alpha", rows.get(0).getLabel());
    assertBigDecimalEquals(2L, rows.get(0).getValue());

    assertEquals("undefined", rows.get(1).getLabel());
    assertBigDecimalEquals(3L, rows.get(1).getValue());
  }

  private static void assertBigDecimalEquals(long expected, BigDecimal actual) {
    assertEquals(0, BigDecimal.valueOf(expected).compareTo(actual));
  }
}