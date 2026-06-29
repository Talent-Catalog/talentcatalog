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
 * You should have received a copy of the GNU General Public License.
 * If not, see https://www.gnu.org/licenses/.
 */

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.list.SearchSavedListRequest;

@ExtendWith(MockitoExtension.class)
class GetSavedListsQueryTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Root<SavedList> savedList;
  @Mock private CriteriaQuery<?> query;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS) private CriteriaBuilder cb;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Join<Object, Object> jobOpp;

  @Mock private Predicate disjunction;
  @Mock private Expression<Boolean> disjunctionExpression;

  @Mock private User loggedInUser;
  @Mock private PartnerImpl loggedInUserPartner;
  @Mock private SavedList sharedListOne;
  @Mock private SavedList sharedListTwo;

  @Test
  @DisplayName("should throw when criteria query is null")
  void toPredicate_shouldThrow_whenCriteriaQueryIsNull() {
    SearchSavedListRequest request = new SearchSavedListRequest();

    assertThrows(IllegalArgumentException.class,
        () -> new GetSavedListsQuery(request, null)
            .toPredicate(savedList, null, cb));
  }

  @Test
  @DisplayName("should build base query for non-selection saved lists")
  void toPredicate_shouldBuildBaseQueryForNonSelectionSavedLists() {
    SearchSavedListRequest request = new SearchSavedListRequest();
    stubBaseQueryWithEmptyDisjunction();

    assertNotNull(new GetSavedListsQuery(request, null)
        .toPredicate(savedList, query, cb));

    verify(query).distinct(true);
    verify(savedList).join("sfJobOpp", JoinType.LEFT);
  }

  @Test
  @DisplayName("should apply keyword and all true filters")
  void toPredicate_shouldApplyKeywordAndAllTrueFilters() {
    SearchSavedListRequest request = new SearchSavedListRequest();
    request.setKeyword("Developer List");
    request.setFixed(true);
    request.setRegisteredJob(true);
    request.setSfOppClosed(true);
    request.setShortName(true);
    request.setGlobal(true);
    request.setShared(true);
    request.setOwnedByMyPartner(true);
    request.setOwned(true);

    when(loggedInUser.getSharedLists()).thenReturn(Set.of(sharedListOne, sharedListTwo));
    when(sharedListOne.getId()).thenReturn(11L);
    when(sharedListTwo.getId()).thenReturn(22L);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.getId()).thenReturn(99L);
    when(loggedInUser.getId()).thenReturn(42L);

    stubBaseQueryWithNonEmptyDisjunction();

    assertNotNull(new GetSavedListsQuery(request, loggedInUser)
        .toPredicate(savedList, query, cb));

    verify(query).distinct(true);
    verify(savedList).join("sfJobOpp", JoinType.LEFT);
    verify(loggedInUser).getSharedLists();
    verify(loggedInUser).getPartner();
    verify(loggedInUser).getId();
    verify(loggedInUserPartner).getId();
  }

  @Test
  @DisplayName("should apply false filters and short name false")
  void toPredicate_shouldApplyFalseFiltersAndShortNameFalse() {
    SearchSavedListRequest request = new SearchSavedListRequest();
    request.setFixed(false);
    request.setRegisteredJob(false);
    request.setSfOppClosed(false);
    request.setShortName(false);
    request.setGlobal(false);
    request.setShared(false);
    request.setOwnedByMyPartner(false);
    request.setOwned(false);

    stubBaseQueryWithEmptyDisjunction();

    assertNotNull(new GetSavedListsQuery(request, loggedInUser)
        .toPredicate(savedList, query, cb));

    verify(query).distinct(true);
    verify(savedList).join("sfJobOpp", JoinType.LEFT);
  }

  @Test
  @DisplayName("should skip shared owned and partner filters when user is null")
  void toPredicate_shouldSkipUserFilters_whenLoggedInUserIsNull() {
    SearchSavedListRequest request = new SearchSavedListRequest();
    request.setShared(true);
    request.setOwnedByMyPartner(true);
    request.setOwned(true);

    stubBaseQueryWithEmptyDisjunction();

    assertNotNull(new GetSavedListsQuery(request, null)
        .toPredicate(savedList, query, cb));

    verify(query).distinct(true);
    verify(savedList).join("sfJobOpp", JoinType.LEFT);
  }

  @Test
  @DisplayName("should apply shared filter with empty shared lists")
  void toPredicate_shouldApplySharedFilterWithEmptySharedLists() {
    SearchSavedListRequest request = new SearchSavedListRequest();
    request.setShared(true);

    when(loggedInUser.getSharedLists()).thenReturn(Set.of());

    stubBaseQueryWithNonEmptyDisjunction();

    assertNotNull(new GetSavedListsQuery(request, loggedInUser)
        .toPredicate(savedList, query, cb));

    verify(loggedInUser).getSharedLists();
  }

  private void stubBaseQueryWithEmptyDisjunction() {
    doReturn(jobOpp).when(savedList).join("sfJobOpp", JoinType.LEFT);
    when(cb.disjunction()).thenReturn(disjunction);
    when(disjunction.getExpressions()).thenReturn(List.of());
  }

  private void stubBaseQueryWithNonEmptyDisjunction() {
    doReturn(jobOpp).when(savedList).join("sfJobOpp", JoinType.LEFT);
    when(cb.disjunction()).thenReturn(disjunction);
    when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(disjunction);
    when(disjunction.getExpressions()).thenReturn(List.of(disjunctionExpression));
  }
}