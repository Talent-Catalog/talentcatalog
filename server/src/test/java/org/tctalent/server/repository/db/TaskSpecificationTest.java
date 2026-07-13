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
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.request.task.SearchTaskRequest;

@ExtendWith(MockitoExtension.class)
class TaskSpecificationTest {

  @Mock private Root<TaskImpl> task;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;

  @Mock private Predicate conjunction;
  @Mock private Predicate combinedPredicate;

  @Mock private Path<String> namePath;
  @Mock private Path<String> displayNamePath;
  @Mock private Path<String> descriptionPath;

  @Mock private Expression<String> lowerNamePath;
  @Mock private Expression<String> lowerDisplayNamePath;
  @Mock private Expression<String> lowerDescriptionPath;

  @Mock private Predicate nameLikePredicate;
  @Mock private Predicate displayNameLikePredicate;
  @Mock private Predicate descriptionLikePredicate;
  @Mock private Predicate keywordPredicate;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new TaskSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchTaskRequest request = new SearchTaskRequest();

    assertThrows(IllegalArgumentException.class,
        () -> TaskSpecification.buildSearchQuery(request)
            .toPredicate(task, null, cb));
  }

  @Test
  @DisplayName("should build base query when keyword is null")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordIsNull() {
    SearchTaskRequest request = new SearchTaskRequest();

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = TaskSpecification.buildSearchQuery(request)
        .toPredicate(task, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should build base query when keyword is blank")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordIsBlank() {
    SearchTaskRequest request = new SearchTaskRequest();
    request.setKeyword("   ");

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = TaskSpecification.buildSearchQuery(request)
        .toPredicate(task, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should apply keyword filter")
  void buildSearchQuery_shouldApplyKeywordFilter() {
    SearchTaskRequest request = new SearchTaskRequest();
    request.setKeyword("Review Document");

    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);

    when(task.<String>get("name")).thenReturn(namePath);
    when(task.<String>get("displayName")).thenReturn(displayNamePath);
    when(task.<String>get("description")).thenReturn(descriptionPath);

    when(cb.lower(namePath)).thenReturn(lowerNamePath);
    when(cb.lower(displayNamePath)).thenReturn(lowerDisplayNamePath);
    when(cb.lower(descriptionPath)).thenReturn(lowerDescriptionPath);

    when(cb.like(lowerNamePath, "%review document%")).thenReturn(nameLikePredicate);
    when(cb.like(lowerDisplayNamePath, "%review document%"))
        .thenReturn(displayNameLikePredicate);
    when(cb.like(lowerDescriptionPath, "%review document%"))
        .thenReturn(descriptionLikePredicate);

    when(cb.or(nameLikePredicate, displayNameLikePredicate, descriptionLikePredicate))
        .thenReturn(keywordPredicate);

    Predicate result = TaskSpecification.buildSearchQuery(request)
        .toPredicate(task, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerNamePath, "%review document%");
    verify(cb).like(lowerDisplayNamePath, "%review document%");
    verify(cb).like(lowerDescriptionPath, "%review document%");
  }
}