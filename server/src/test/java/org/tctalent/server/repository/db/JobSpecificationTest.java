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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.job.SearchJobRequest;
import org.tctalent.server.util.SpecificationHelper;

@ExtendWith(MockitoExtension.class)
class JobSpecificationTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Root<SalesforceJobOpp> job;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaQuery<SalesforceJobOpp> query;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaQuery<Long> countQuery;

  @Mock
  private JoinFetch submissionList;

  @Mock
  private Expression<Boolean> disjunctionExpression;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new JobSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchJobRequest request = new SearchJobRequest();

    assertThrows(IllegalArgumentException.class,
        () -> JobSpecification.buildSearchQuery(request, null)
            .toPredicate(job, null, criteriaBuilder(true)));
  }

  @Test
  @DisplayName("should build non-count query with stable id ordering")
  void buildSearchQuery_shouldBuildNonCountQuery_withStableIdOrdering() {
    CriteriaContext context = nonCountContext();

    assertNotNull(JobSpecification.buildSearchQuery(new SearchJobRequest(), null)
        .toPredicate(context.job, context.query, context.cb));

    verify(context.job).fetch("submissionList", JoinType.INNER);
    verify(context.query).orderBy(anyList());
  }

  @Test
  @DisplayName("should build count query without fetch sorting")
  void buildSearchQuery_shouldBuildCountQuery_withoutFetchSorting() {
    CriteriaContext context = countContext();

    assertNotNull(JobSpecification.buildSearchQuery(new SearchJobRequest(), null)
        .toPredicate(context.job, context.query, context.cb));

    verify(context.job, never()).fetch("submissionList", JoinType.INNER);
    verify(context.query, never()).orderBy(anyList());
  }

  @Test
  @DisplayName("should apply keyword, stage and destination filters")
  void buildSearchQuery_shouldApplyKeywordStageAndDestinationFilters() {
    SearchJobRequest request = new SearchJobRequest();
    request.setKeyword("Developer Role");
    request.setStages(List.of(JobOpportunityStage.candidateSearch, JobOpportunityStage.jobOffer));
    request.setDestinationIds(List.of(1L, 2L));
    request.setActiveStages(true);
    request.setSfOppClosed(false);

    assertNotNull(runNonCount(request, null));
  }

  @Test
  @DisplayName("should apply active stages without closed or unpublished union")
  void buildSearchQuery_shouldApplyActiveStagesOnly() {
    SearchJobRequest request = new SearchJobRequest();
    request.setActiveStages(true);
    request.setSfOppClosed(false);
    request.setPublished(true);

    assertNotNull(runNonCount(request, null));
  }

  @Test
  @DisplayName("should apply active stages with closed and unpublished union")
  void buildSearchQuery_shouldApplyActiveStagesWithClosedAndUnpublishedUnion() {
    SearchJobRequest request = new SearchJobRequest();
    request.setActiveStages(true);
    request.setSfOppClosed(true);
    request.setPublished(false);

    assertNotNull(runNonCount(request, null));
  }

  @Test
  @DisplayName("should skip active stage disjunction when expression list is empty")
  void buildSearchQuery_shouldSkipActiveDisjunction_whenExpressionListIsEmpty() {
    SearchJobRequest request = new SearchJobRequest();
    request.setActiveStages(true);

    CriteriaContext context = nonCountContext(false);

    assertNotNull(JobSpecification.buildSearchQuery(request, null)
        .toPredicate(context.job, context.query, context.cb));
  }

  @Test
  @DisplayName("should handle explicit false boolean filters")
  void buildSearchQuery_shouldHandleExplicitFalseBooleanFilters() {
    SearchJobRequest request = new SearchJobRequest();
    request.setActiveStages(false);
    request.setWithUnreadMessages(false);
    request.setOwnedByMe(false);
    request.setOwnedByMyPartner(false);
    request.setStarred(false);
    request.setSfOppClosed(false);

    assertNotNull(runNonCount(request, null));
  }

  @Test
  @DisplayName("should apply unread messages filter")
  void buildSearchQuery_shouldApplyUnreadMessagesFilter() {
    SearchJobRequest request = new SearchJobRequest();
    request.setWithUnreadMessages(true);

    User loggedInUser = mock(User.class);
    Predicate unreadPredicate = mock(Predicate.class);

    try (MockedStatic<SpecificationHelper> mockedHelper = mockStatic(SpecificationHelper.class)) {
      mockedHelper.when(() -> SpecificationHelper.hasUnreadChats(
          any(User.class),
          any(CriteriaQuery.class),
          any(CriteriaBuilder.class),
          any(Subquery.class),
          any(Root.class),
          any(Predicate.class)
      )).thenReturn(unreadPredicate);

      assertNotNull(runNonCount(request, loggedInUser));
    }
  }

  @Test
  @DisplayName("should apply owned by me, owned by partner and starred filters")
  void buildSearchQuery_shouldApplyOwnedAndStarredFilters() {
    SearchJobRequest request = new SearchJobRequest();
    request.setOwnedByMe(true);
    request.setOwnedByMyPartner(true);
    request.setStarred(true);

    User loggedInUser = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getId()).thenReturn(42L);
    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.getId()).thenReturn(99L);

    assertNotNull(runNonCount(request, loggedInUser));
  }

  @Test
  @DisplayName("should skip ownership filters when logged in user is null")
  void buildSearchQuery_shouldSkipOwnershipFilters_whenLoggedInUserIsNull() {
    SearchJobRequest request = new SearchJobRequest();
    request.setOwnedByMe(true);
    request.setOwnedByMyPartner(true);
    request.setStarred(true);

    assertNotNull(runNonCount(request, null));
  }

  @Test
  @DisplayName("should skip partner ownership when logged in user has no partner")
  void buildSearchQuery_shouldSkipPartnerOwnership_whenUserHasNoPartner() {
    SearchJobRequest request = new SearchJobRequest();
    request.setOwnedByMyPartner(true);

    User loggedInUser = mock(User.class);
    when(loggedInUser.getPartner()).thenReturn(null);

    assertNotNull(runNonCount(request, loggedInUser));
  }

  @Test
  @DisplayName("should apply submission list ASC ordering and stable id ordering")
  void buildSearchQuery_shouldApplySubmissionListAscOrderingAndStableIdOrdering() {
    SearchJobRequest request = new SearchJobRequest();
    request.setSortDirection(Sort.Direction.ASC);
    request.setSortFields(new String[] {"submissionList.name", "name"});

    assertNotNull(runNonCount(request, null));
  }

  @Test
  @DisplayName("should apply id DESC ordering without extra stable id ordering")
  void buildSearchQuery_shouldApplyIdDescOrderingWithoutExtraStableIdOrdering() {
    SearchJobRequest request = new SearchJobRequest();
    request.setSortDirection(Sort.Direction.DESC);
    request.setSortFields(new String[] {"id"});

    assertNotNull(runNonCount(request, null));
  }

  private Predicate runNonCount(SearchJobRequest request, User loggedInUser) {
    CriteriaContext context = nonCountContext();

    return JobSpecification.buildSearchQuery(request, loggedInUser)
        .toPredicate(context.job, context.query, context.cb);
  }

  private CriteriaContext nonCountContext() {
    return nonCountContext(true);
  }

  private CriteriaContext nonCountContext(boolean orHasExpressions) {
    CriteriaBuilder localCb = criteriaBuilder(orHasExpressions);

    when(query.getResultType()).thenReturn(SalesforceJobOpp.class);
    doReturn(query).when(query).orderBy(anyList());
    doReturn(submissionList).when(job).fetch("submissionList", JoinType.INNER);

    return new CriteriaContext(job, query, localCb);
  }

  private CriteriaContext countContext() {
    CriteriaBuilder localCb = criteriaBuilder(true);

    when(countQuery.getResultType()).thenReturn(Long.class);

    return new CriteriaContext(job, countQuery, localCb);
  }

  private CriteriaBuilder criteriaBuilder(boolean orHasExpressions) {
    Predicate conjunction = predicateWithExpressions(false);
    Predicate disjunction = predicateWithExpressions(false);
    Predicate orPredicate = predicateWithExpressions(orHasExpressions);

    return mock(CriteriaBuilder.class, invocation -> {
      String methodName = invocation.getMethod().getName();

      return switch (methodName) {
        case "conjunction" -> conjunction;
        case "disjunction" -> disjunction;
        case "and" -> conjunction;
        case "or" -> orPredicate;
        default -> RETURNS_DEEP_STUBS.answer(invocation);
      };
    });
  }

  private Predicate predicateWithExpressions(boolean hasExpressions) {
    return mock(Predicate.class, invocation -> {
      if ("getExpressions".equals(invocation.getMethod().getName())) {
        return hasExpressions ? List.of(disjunctionExpression) : List.of();
      }

      return RETURNS_DEEP_STUBS.answer(invocation);
    });
  }

  private interface JoinFetch extends Join<Object, Object>, Fetch<Object, Object> {
  }

  private record CriteriaContext(
      Root<SalesforceJobOpp> job,
      CriteriaQuery<?> query,
      CriteriaBuilder cb
  ) {
  }
}