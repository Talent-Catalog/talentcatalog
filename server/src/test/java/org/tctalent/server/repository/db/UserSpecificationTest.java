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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.user.SearchUserRequest;

@ExtendWith(MockitoExtension.class)
class UserSpecificationTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Root<User> user;
  @Mock private CriteriaQuery<?> query;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS) private CriteriaBuilder cb;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Join<Object, Object> partner;

  @Mock private Predicate conjunction;
  @Mock private Predicate keywordPredicate;
  @Mock private Predicate combinedPredicate;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new UserSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchUserRequest request = new SearchUserRequest();

    assertThrows(IllegalArgumentException.class,
        () -> UserSpecification.buildSearchQuery(request)
            .toPredicate(user, null, cb));
  }

  @Test
  @DisplayName("should exclude candidate users when role list is null")
  void buildSearchQuery_shouldExcludeCandidateUsers_whenRoleListIsNull() {
    SearchUserRequest request = new SearchUserRequest();

    stubBaseQuery();

    assertNotNull(UserSpecification.buildSearchQuery(request)
        .toPredicate(user, query, cb));

    verify(query).distinct(true);
    verify(cb).notEqual(user.get("role"), Role.user);
  }

  @Test
  @DisplayName("should exclude candidate users when role list is empty")
  void buildSearchQuery_shouldExcludeCandidateUsers_whenRoleListIsEmpty() {
    SearchUserRequest request = new SearchUserRequest();
    request.setRole(List.of());

    stubBaseQuery();

    assertNotNull(UserSpecification.buildSearchQuery(request)
        .toPredicate(user, query, cb));

    verify(query).distinct(true);
    verify(cb).notEqual(user.get("role"), Role.user);
  }

  @Test
  @DisplayName("should apply keyword, role, partner and status filters")
  void buildSearchQuery_shouldApplyKeywordRolePartnerAndStatusFilters() {
    SearchUserRequest request = new SearchUserRequest();
    request.setKeyword("Ehsan Test");
    request.setRole(List.of(Role.systemadmin, Role.admin));
    request.setPartnerId(123L);
    request.setStatus(Status.active);

    stubBaseQuery();
    doReturn(partner).when(user).join("partner", JoinType.LEFT);

    assertNotNull(UserSpecification.buildSearchQuery(request)
        .toPredicate(user, query, cb));

    verify(query).distinct(true);
    verify(user).join("partner", JoinType.LEFT);
    verify(cb).equal(partner.get("id"), 123L);
    verify(cb).equal(user.get("status"), Status.active);
  }

  @Test
  @DisplayName("should skip keyword when it is blank")
  void buildSearchQuery_shouldSkipKeyword_whenBlank() {
    SearchUserRequest request = new SearchUserRequest();
    request.setKeyword("   ");
    request.setRole(List.of(Role.partneradmin));

    stubBaseQuery();

    assertNotNull(UserSpecification.buildSearchQuery(request)
        .toPredicate(user, query, cb));

    verify(query).distinct(true);
  }

  @Test
  @DisplayName("should apply partner filter only")
  void buildSearchQuery_shouldApplyPartnerFilterOnly() {
    SearchUserRequest request = new SearchUserRequest();
    request.setPartnerId(99L);

    stubBaseQuery();
    doReturn(partner).when(user).join("partner", JoinType.LEFT);

    assertNotNull(UserSpecification.buildSearchQuery(request)
        .toPredicate(user, query, cb));

    verify(user).join("partner", JoinType.LEFT);
    verify(cb).equal(partner.get("id"), 99L);
  }

  @Test
  @DisplayName("should apply status filter only")
  void buildSearchQuery_shouldApplyStatusFilterOnly() {
    SearchUserRequest request = new SearchUserRequest();
    request.setStatus(Status.active);

    stubBaseQuery();

    assertNotNull(UserSpecification.buildSearchQuery(request)
        .toPredicate(user, query, cb));

    verify(cb).equal(user.get("status"), Status.active);
  }

  private void stubBaseQuery() {
    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
    when(cb.or(
        any(Predicate.class),
        any(Predicate.class),
        any(Predicate.class),
        any(Predicate.class)
    )).thenReturn(keywordPredicate);
  }
}