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
import org.tctalent.server.model.db.SavedListLink;
import org.tctalent.server.request.link.SearchLinkRequest;

@ExtendWith(MockitoExtension.class)
class SavedListLinkSpecificationTest {

  @Mock private Root<SavedListLink> savedListLink;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;

  @Mock private Predicate conjunction;
  @Mock private Predicate combinedPredicate;

  @Mock private Path<String> namePath;
  @Mock private Expression<String> lowerNamePath;
  @Mock private Predicate nameLikePredicate;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new SavedListLinkSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchLinkRequest request = new SearchLinkRequest();

    assertThrows(IllegalArgumentException.class,
        () -> SavedListLinkSpecification.buildSearchQuery(request)
            .toPredicate(savedListLink, null, cb));
  }

  @Test
  @DisplayName("should build base query when keyword is null")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordIsNull() {
    SearchLinkRequest request = new SearchLinkRequest();

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = SavedListLinkSpecification.buildSearchQuery(request)
        .toPredicate(savedListLink, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should build base query when keyword is blank")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordIsBlank() {
    SearchLinkRequest request = new SearchLinkRequest();
    request.setKeyword("   ");

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = SavedListLinkSpecification.buildSearchQuery(request)
        .toPredicate(savedListLink, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should apply keyword filter")
  void buildSearchQuery_shouldApplyKeywordFilter() {
    SearchLinkRequest request = new SearchLinkRequest();
    request.setKeyword("Important Link");

    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);

    when(savedListLink.<String>get("name")).thenReturn(namePath);
    when(cb.lower(namePath)).thenReturn(lowerNamePath);
    when(cb.like(lowerNamePath, "%important link%")).thenReturn(nameLikePredicate);

    Predicate result = SavedListLinkSpecification.buildSearchQuery(request)
        .toPredicate(savedListLink, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerNamePath, "%important link%");
  }
}