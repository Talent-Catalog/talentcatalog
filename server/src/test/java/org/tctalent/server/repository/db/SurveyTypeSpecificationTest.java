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
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.request.survey.SearchSurveyTypeRequest;

@ExtendWith(MockitoExtension.class)
class SurveyTypeSpecificationTest {

  @Mock private Root<SurveyType> surveyType;
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
    assertNotNull(new SurveyTypeSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchSurveyTypeRequest request = new SearchSurveyTypeRequest();

    assertThrows(IllegalArgumentException.class,
        () -> SurveyTypeSpecification.buildSearchQuery(request)
            .toPredicate(surveyType, null, cb));
  }

  @Test
  @DisplayName("should build base query when keyword and status are null")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordAndStatusAreNull() {
    SearchSurveyTypeRequest request = new SearchSurveyTypeRequest();

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = SurveyTypeSpecification.buildSearchQuery(request)
        .toPredicate(surveyType, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should build base query when keyword is blank")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordIsBlank() {
    SearchSurveyTypeRequest request = new SearchSurveyTypeRequest();
    request.setKeyword("   ");

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = SurveyTypeSpecification.buildSearchQuery(request)
        .toPredicate(surveyType, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should apply keyword filter")
  void buildSearchQuery_shouldApplyKeywordFilter() {
    SearchSurveyTypeRequest request = new SearchSurveyTypeRequest();
    request.setKeyword("Mini Intake");

    stubBaseAnd();
    stubKeyword("mini intake");

    Predicate result = SurveyTypeSpecification.buildSearchQuery(request)
        .toPredicate(surveyType, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerNamePath, "%mini intake%");
  }

  @Test
  @DisplayName("should apply status filter")
  void buildSearchQuery_shouldApplyStatusFilter() {
    SearchSurveyTypeRequest request = new SearchSurveyTypeRequest();
    request.setStatus(Status.active);

    stubBaseAnd();
    stubStatus(Status.active);

    Predicate result = SurveyTypeSpecification.buildSearchQuery(request)
        .toPredicate(surveyType, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).equal(statusPath, Status.active);
  }

  @Test
  @DisplayName("should apply keyword and status filters")
  void buildSearchQuery_shouldApplyKeywordAndStatusFilters() {
    SearchSurveyTypeRequest request = new SearchSurveyTypeRequest();
    request.setKeyword("Full Intake");
    request.setStatus(Status.active);

    stubBaseAnd();
    stubKeyword("full intake");
    stubStatus(Status.active);

    Predicate result = SurveyTypeSpecification.buildSearchQuery(request)
        .toPredicate(surveyType, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerNamePath, "%full intake%");
    verify(cb).equal(statusPath, Status.active);
  }

  private void stubBaseAnd() {
    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
  }

  private void stubKeyword(String lowerCaseKeyword) {
    when(surveyType.<String>get("name")).thenReturn(namePath);
    when(cb.lower(namePath)).thenReturn(lowerNamePath);
    when(cb.like(lowerNamePath, "%" + lowerCaseKeyword + "%"))
        .thenReturn(nameLikePredicate);
  }

  private void stubStatus(Status status) {
    when(surveyType.<Status>get("status")).thenReturn(statusPath);
    when(cb.equal(statusPath, status)).thenReturn(statusPredicate);
  }
}