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
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SavedSearchSubtype;
import org.tctalent.server.model.db.SavedSearchType;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.search.SearchSavedSearchRequest;

@ExtendWith(MockitoExtension.class)
class SavedSearchSpecificationTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Root<SavedSearch> savedSearch;
  @Mock private CriteriaQuery<?> query;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS) private CriteriaBuilder cb;

  @Mock private Predicate conjunction;
  @Mock private Predicate combinedPredicate;
  @Mock private Predicate disjunction;
  @Mock private Expression<Boolean> disjunctionExpression;

  @Mock private Path<String> namePath;
  @Mock private Expression<String> lowerNamePath;
  @Mock private Predicate nameLikePredicate;

  @Mock private Path<Status> statusPath;
  @Mock private Predicate statusPredicate;

  @Mock private Path<Boolean> defaultSearchPath;
  @Mock private Predicate notDefaultPredicate;

  @Mock private Path<String> typePath;
  @Mock private Predicate typePredicate;

  @Mock private Path<Boolean> fixedPath;
  @Mock private Predicate fixedPredicate;

  @Mock private Path<Boolean> globalPath;
  @Mock private Predicate globalPredicate;

  @Mock private Path<Long> idPath;
  @Mock private Predicate sharedPredicate;

  @Mock private User loggedInUser;
  @Mock private SavedSearch sharedSearchOne;
  @Mock private SavedSearch sharedSearchTwo;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new SavedSearchSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();

    assertThrows(IllegalArgumentException.class,
        () -> SavedSearchSpecification.buildSearchQuery(request, null)
            .toPredicate(savedSearch, null, cb));
  }

  @Test
  @DisplayName("should apply base active and not default filters")
  void buildSearchQuery_shouldApplyBaseFilters() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();

    stubBaseQueryWithEmptyDisjunction();

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, null)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).equal(statusPath, Status.active);
    verify(cb).not(defaultSearchPath);
  }

  @Test
  @DisplayName("should apply keyword and type filters")
  void buildSearchQuery_shouldApplyKeywordAndTypeFilters() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setKeyword("Software Developers");
    request.setSavedSearchType(SavedSearchType.profession);
    request.setSavedSearchSubtype(SavedSearchSubtype.it);

    stubBaseQueryWithEmptyDisjunction();
    stubKeyword();
    stubType(SavedSearch.makeStringSavedSearchType(
        SavedSearchType.profession,
        SavedSearchSubtype.it
    ));

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, null)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerNamePath, "%software developers%");
    verify(cb).equal(
        typePath,
        SavedSearch.makeStringSavedSearchType(SavedSearchType.profession, SavedSearchSubtype.it)
    );
  }

  @Test
  @DisplayName("should skip blank keyword and apply type without subtype")
  void buildSearchQuery_shouldSkipBlankKeywordAndApplyTypeWithoutSubtype() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setKeyword("   ");
    request.setSavedSearchType(SavedSearchType.job);

    stubBaseQueryWithEmptyDisjunction();
    stubType(SavedSearch.makeStringSavedSearchType(SavedSearchType.job, null));

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, null)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).equal(
        typePath,
        SavedSearch.makeStringSavedSearchType(SavedSearchType.job, null)
    );
  }

  @Test
  @DisplayName("should apply fixed true and global true filters")
  void buildSearchQuery_shouldApplyFixedAndGlobalFilters() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setFixed(true);
    request.setGlobal(true);

    stubBaseQueryWithNonEmptyDisjunction();
    stubFixed();
    stubGlobal();

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, null)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).equal(fixedPath, true);
    verify(cb).equal(globalPath, true);
  }

  @Test
  @DisplayName("should ignore fixed false and global false filters")
  void buildSearchQuery_shouldIgnoreFalseBooleanFilters() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setFixed(false);
    request.setGlobal(false);
    request.setShared(false);
    request.setOwned(false);

    stubBaseQueryWithEmptyDisjunction();

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
  }

  @Test
  @DisplayName("should apply shared searches when logged in user has shared searches")
  void buildSearchQuery_shouldApplySharedSearches() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setShared(true);

    when(loggedInUser.getSharedSearches())
        .thenReturn(Set.of(sharedSearchOne, sharedSearchTwo));
    when(sharedSearchOne.getId()).thenReturn(11L);
    when(sharedSearchTwo.getId()).thenReturn(22L);

    stubBaseQueryWithNonEmptyDisjunction();

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(loggedInUser).getSharedSearches();
    verify(sharedSearchOne).getId();
    verify(sharedSearchTwo).getId();
  }

  @Test
  @DisplayName("should skip shared searches when logged in user is null")
  void buildSearchQuery_shouldSkipSharedSearches_whenLoggedInUserIsNull() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setShared(true);

    stubBaseQueryWithEmptyDisjunction();

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, null)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
  }

  @Test
  @DisplayName("should skip shared filter when shared search set is empty")
  void buildSearchQuery_shouldSkipSharedFilter_whenSharedSearchesAreEmpty() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setShared(true);

    when(loggedInUser.getSharedSearches()).thenReturn(Set.of());

    stubBaseQueryWithEmptyDisjunction();

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(loggedInUser).getSharedSearches();
  }

  @Test
  @DisplayName("should apply owned filter")
  void buildSearchQuery_shouldApplyOwnedFilter() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setOwned(true);

    when(loggedInUser.getId()).thenReturn(42L);

    stubBaseQueryWithNonEmptyDisjunction();

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(loggedInUser).getId();
  }

  @Test
  @DisplayName("should skip owned filter when logged in user is null")
  void buildSearchQuery_shouldSkipOwnedFilter_whenLoggedInUserIsNull() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setOwned(true);

    stubBaseQueryWithEmptyDisjunction();

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, null)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
  }

  @Test
  @DisplayName("should apply global shared and owned disjunction together")
  void buildSearchQuery_shouldApplyGlobalSharedAndOwnedDisjunctionTogether() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setGlobal(true);
    request.setShared(true);
    request.setOwned(true);

    when(loggedInUser.getSharedSearches()).thenReturn(Set.of(sharedSearchOne));
    when(sharedSearchOne.getId()).thenReturn(55L);
    when(loggedInUser.getId()).thenReturn(66L);

    stubBaseQueryWithNonEmptyDisjunction();
    stubGlobal();

    Predicate result = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
        .toPredicate(savedSearch, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).equal(globalPath, true);
    verify(loggedInUser).getSharedSearches();
    verify(loggedInUser).getId();
  }

  private void stubBaseQueryWithEmptyDisjunction() {
    stubRequiredBasePredicates();
    when(cb.disjunction()).thenReturn(disjunction);
    when(disjunction.getExpressions()).thenReturn(List.of());
  }

  private void stubBaseQueryWithNonEmptyDisjunction() {
    stubRequiredBasePredicates();
    when(cb.disjunction()).thenReturn(disjunction);
    when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(disjunction);
    when(disjunction.getExpressions()).thenReturn(List.of(disjunctionExpression));
  }

  private void stubRequiredBasePredicates() {
    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);

    when(savedSearch.<Status>get("status")).thenReturn(statusPath);
    when(cb.equal(statusPath, Status.active)).thenReturn(statusPredicate);

    when(savedSearch.<Boolean>get("defaultSearch")).thenReturn(defaultSearchPath);
    when(cb.not(defaultSearchPath)).thenReturn(notDefaultPredicate);
  }

  private void stubKeyword() {
    when(savedSearch.<String>get("name")).thenReturn(namePath);
    when(cb.lower(namePath)).thenReturn(lowerNamePath);
    when(cb.like(lowerNamePath, "%" + "software developers" + "%"))
        .thenReturn(nameLikePredicate);
  }

  private void stubType(String type) {
    when(savedSearch.<String>get("type")).thenReturn(typePath);
    when(cb.equal(typePath, type)).thenReturn(typePredicate);
  }

  private void stubFixed() {
    when(savedSearch.<Boolean>get("fixed")).thenReturn(fixedPath);
    when(cb.equal(fixedPath, true)).thenReturn(fixedPredicate);
  }

  private void stubGlobal() {
    when(savedSearch.<Boolean>get("global")).thenReturn(globalPath);
    when(cb.equal(globalPath, true)).thenReturn(globalPredicate);
  }
}