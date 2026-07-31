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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.request.language.SearchLanguageRequest;

@ExtendWith(MockitoExtension.class)
class LanguageSpecificationTest {

  @Mock private Root<Language> language;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;

  @Mock private Predicate conjunction;
  @Mock private Predicate combinedPredicate;

  @Mock private Path<String> namePath;
  @Mock private Expression<String> lowerNamePath;
  @Mock private Predicate nameLikePredicate;

  @Mock private Path<Status> statusPath;
  @Mock private Predicate statusPredicate;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new LanguageSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchLanguageRequest request = new SearchLanguageRequest();

    assertThrows(IllegalArgumentException.class,
        () -> LanguageSpecification.buildSearchQuery(request)
            .toPredicate(language, null, cb));
  }

  @Test
  @DisplayName("should build base query when keyword and status are null")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordAndStatusAreNull() {
    SearchLanguageRequest request = new SearchLanguageRequest();

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = LanguageSpecification.buildSearchQuery(request)
        .toPredicate(language, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should build base query when keyword is blank")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordIsBlank() {
    SearchLanguageRequest request = new SearchLanguageRequest();
    request.setKeyword("   ");

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = LanguageSpecification.buildSearchQuery(request)
        .toPredicate(language, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should apply keyword filter")
  void buildSearchQuery_shouldApplyKeywordFilter() {
    SearchLanguageRequest request = new SearchLanguageRequest();
    request.setKeyword("English");

    stubBaseAnd();
    stubKeyword("english");

    Predicate result = LanguageSpecification.buildSearchQuery(request)
        .toPredicate(language, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerNamePath, "%english%");
  }

  @Test
  @DisplayName("should apply status filter")
  void buildSearchQuery_shouldApplyStatusFilter() {
    SearchLanguageRequest request = new SearchLanguageRequest();
    request.setStatus(Status.active);

    stubBaseAnd();
    stubStatus();

    Predicate result = LanguageSpecification.buildSearchQuery(request)
        .toPredicate(language, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).equal(statusPath, Status.active);
  }

  @Test
  @DisplayName("should apply keyword and status filters")
  void buildSearchQuery_shouldApplyKeywordAndStatusFilters() {
    SearchLanguageRequest request = new SearchLanguageRequest();
    request.setKeyword("Dari");
    request.setStatus(Status.active);

    stubBaseAnd();
    stubKeyword("dari");
    stubStatus();

    Predicate result = LanguageSpecification.buildSearchQuery(request)
        .toPredicate(language, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerNamePath, "%dari%");
    verify(cb).equal(statusPath, Status.active);
  }

  private void stubBaseAnd() {
    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
  }

  private void stubKeyword(String lowerCaseKeyword) {
    when(language.<String>get("name")).thenReturn(namePath);
    when(cb.lower(namePath)).thenReturn(lowerNamePath);
    when(cb.like(lowerNamePath, "%" + lowerCaseKeyword + "%"))
        .thenReturn(nameLikePredicate);
  }

  private void stubStatus() {
    when(language.<Status>get("status")).thenReturn(statusPath);
    when(cb.equal(statusPath, Status.active)).thenReturn(statusPredicate);
  }
}