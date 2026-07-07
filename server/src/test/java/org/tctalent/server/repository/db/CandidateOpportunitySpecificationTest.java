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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;
import org.tctalent.server.request.opportunity.OpportunityOwnershipType;
import org.tctalent.server.util.SpecificationHelper;

@ExtendWith(MockitoExtension.class)
class CandidateOpportunitySpecificationTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Root<CandidateOpportunity> opp;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaQuery<CandidateOpportunity> query;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaQuery<Long> countQuery;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaBuilder cb;

  @Mock private Predicate conjunction;
  @Mock private Path<Object> idPath;
  @Mock private Order idOrder;

  @Mock private Path<Object> sortNamePath;
  @Mock private Order sortNameOrder;

  @Mock private Path<String> keywordNamePath;
  @Mock private Expression<String> lowerNamePath;
  @Mock private Predicate keywordPredicate;
  @Mock private Predicate keywordConjunction;

  @Mock private Path<CandidateOpportunityStage> stagePath;
  @Mock private Predicate stageInPredicate;
  @Mock private Predicate stagePredicate;
  @Mock private Predicate stageConjunction;

  @Mock private Path<Integer> stageOrderPath;
  @Mock private Predicate activeStagesPredicate;
  @Mock private Predicate activeStagesConjunction;

  @Mock private Path<Boolean> closedPath;
  @Mock private Predicate closedPredicate;
  @Mock private Predicate activeOrClosedPredicate;
  @Mock private Predicate activeOrClosedConjunction;
  @Mock private Predicate notClosedConjunction;

  @Mock private Path<LocalDate> nextStepDueDatePath;
  @Mock private Predicate hasDueDatePredicate;
  @Mock private Predicate overduePredicate;
  @Mock private Predicate overdueConjunction;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Join<Object, Object> jobOppJoin;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Join<Object, Object> candidateJoin;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Join<Object, Object> userJoin;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Join<Object, Object> partnerJoin;

  @Mock private Predicate ownedPredicate;
  @Mock private Predicate ownedConjunction;
  @Mock private Predicate matchContactUserPredicate;
  @Mock private Predicate matchCreatedByPredicate;
  @Mock private Predicate ownerDisjunction;

  @Mock private Predicate emptyDisjunction;
  @Mock private Predicate nonEmptyDisjunction;
  @Mock private Expression<Boolean> disjunctionExpression;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new CandidateOpportunitySpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();

    Specification<CandidateOpportunity> specification =
        CandidateOpportunitySpecification.buildSearchQuery(request, null);

    assertThrows(IllegalArgumentException.class,
        () -> specification.toPredicate(opp, null, cb));
  }

  @Test
  @DisplayName("should apply stable id sort when sort is not requested")
  void buildSearchQuery_shouldApplyStableIdSort_whenSortNotRequested() {
    stubNonCountQuery();

    Predicate result = toPredicate(new SearchCandidateOpportunityRequest(), null);

    assertEquals(conjunction, result);
    verify(query).orderBy(List.of(idOrder));
  }

  @Test
  @DisplayName("should apply requested ASC sort and stable id sort")
  void buildSearchQuery_shouldApplyRequestedAscSortAndStableIdSort() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setSortDirection(Sort.Direction.ASC);
    request.setSortFields(new String[] {"name"});

    when(opp.get("name")).thenReturn(sortNamePath);
    when(cb.asc(sortNamePath)).thenReturn(sortNameOrder);

    Predicate result = toPredicate(request, null);

    assertEquals(conjunction, result);
    verify(query).orderBy(List.of(sortNameOrder, idOrder));
  }

  @Test
  @DisplayName("should apply requested id DESC sort without extra stable id sort")
  void buildSearchQuery_shouldApplyRequestedIdDescSortWithoutExtraStableIdSort() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setSortDirection(Sort.Direction.DESC);
    request.setSortFields(new String[] {"id"});

    Predicate result = toPredicate(request, null);

    assertEquals(conjunction, result);
    verify(query).orderBy(List.of(idOrder));
  }

  @Test
  @DisplayName("should not apply ordering for count query")
  void buildSearchQuery_shouldNotApplyOrdering_forCountQuery() {
    when(countQuery.getResultType()).thenReturn(Long.class);
    when(cb.conjunction()).thenReturn(conjunction);

    Specification<CandidateOpportunity> specification =
        CandidateOpportunitySpecification.buildSearchQuery(
            new SearchCandidateOpportunityRequest(), null);

    Predicate result = specification.toPredicate(opp, countQuery, cb);

    assertEquals(conjunction, result);
    verify(countQuery, never()).orderBy(anyList());
    verify(opp, never()).get("id");
  }

  @Test
  @DisplayName("should apply lowercase keyword filter")
  void buildSearchQuery_shouldApplyLowercaseKeywordFilter() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setKeyword("Case Name");

    when(opp.<String>get("name")).thenReturn(keywordNamePath);
    when(cb.lower(keywordNamePath)).thenReturn(lowerNamePath);
    when(cb.like(lowerNamePath, "%case name%")).thenReturn(keywordPredicate);
    when(cb.and(conjunction, keywordPredicate)).thenReturn(keywordConjunction);

    Predicate result = toPredicate(request, null);

    assertEquals(keywordConjunction, result);
  }

  @Test
  @DisplayName("should apply stage filter and ignore active and closed filters")
  void buildSearchQuery_shouldApplyStageFilterAndIgnoreActiveAndClosedFilters() {
    stubNonCountQuery();

    List<CandidateOpportunityStage> stages = List.of(
        CandidateOpportunityStage.prospect,
        CandidateOpportunityStage.jobOfferRetracted
    );

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setStages(stages);
    request.setActiveStages(true);
    request.setSfOppClosed(false);

    when(opp.<CandidateOpportunityStage>get("stage")).thenReturn(stagePath);
    when(stagePath.in(stages)).thenReturn(stageInPredicate);
    when(cb.isTrue(stageInPredicate)).thenReturn(stagePredicate);
    when(cb.and(conjunction, stagePredicate)).thenReturn(stageConjunction);

    Predicate result = toPredicate(request, null);

    assertEquals(stageConjunction, result);
    verify(opp, never()).get("stageOrder");
    verify(opp, never()).get("closed");
  }

  @Test
  @DisplayName("should apply active stages only")
  void buildSearchQuery_shouldApplyActiveStagesOnly() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setActiveStages(true);

    when(opp.<Integer>get("stageOrder")).thenReturn(stageOrderPath);
    when(cb.between(
        stageOrderPath,
        CandidateOpportunityStage.prospect.ordinal(),
        CandidateOpportunityStage.relocating.ordinal()
    )).thenReturn(activeStagesPredicate);
    when(cb.and(conjunction, activeStagesPredicate)).thenReturn(activeStagesConjunction);

    Predicate result = toPredicate(request, null);

    assertEquals(activeStagesConjunction, result);
  }

  @Test
  @DisplayName("should include active stages or closed opportunities when both are requested")
  void buildSearchQuery_shouldIncludeActiveStagesOrClosedOpportunities() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setActiveStages(true);
    request.setSfOppClosed(true);

    when(opp.<Integer>get("stageOrder")).thenReturn(stageOrderPath);
    when(cb.between(
        stageOrderPath,
        CandidateOpportunityStage.prospect.ordinal(),
        CandidateOpportunityStage.relocating.ordinal()
    )).thenReturn(activeStagesPredicate);

    when(opp.<Boolean>get("closed")).thenReturn(closedPath);
    when(cb.equal(closedPath, true)).thenReturn(closedPredicate);
    when(cb.or(activeStagesPredicate, closedPredicate)).thenReturn(activeOrClosedPredicate);
    when(cb.and(conjunction, activeOrClosedPredicate)).thenReturn(activeOrClosedConjunction);

    Predicate result = toPredicate(request, null);

    assertEquals(activeOrClosedConjunction, result);
  }

  @Test
  @DisplayName("should skip active filtering when active stages is false")
  void buildSearchQuery_shouldSkipActiveFiltering_whenActiveStagesFalse() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setActiveStages(false);

    Predicate result = toPredicate(request, null);

    assertEquals(conjunction, result);
    verify(opp, never()).get("stageOrder");
  }

  @Test
  @DisplayName("should skip closed filtering when closed is true and active is not requested")
  void buildSearchQuery_shouldSkipClosedFiltering_whenClosedTrue() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setSfOppClosed(true);

    Predicate result = toPredicate(request, null);

    assertEquals(conjunction, result);
  }

  @Test
  @DisplayName("should exclude closed opportunities when closed is false")
  void buildSearchQuery_shouldExcludeClosedOpportunities_whenClosedFalse() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setSfOppClosed(false);

    when(opp.<Boolean>get("closed")).thenReturn(closedPath);
    when(cb.equal(closedPath, false)).thenReturn(closedPredicate);
    when(cb.and(conjunction, closedPredicate)).thenReturn(notClosedConjunction);

    Predicate result = toPredicate(request, null);

    assertEquals(notClosedConjunction, result);
  }

  @Test
  @DisplayName("should apply overdue filter")
  void buildSearchQuery_shouldApplyOverdueFilter() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOverdue(true);

    when(opp.<LocalDate>get("nextStepDueDate")).thenReturn(nextStepDueDatePath);
    when(cb.isNotNull(nextStepDueDatePath)).thenReturn(hasDueDatePredicate);
    when(cb.lessThan(nextStepDueDatePath, LocalDate.now())).thenReturn(overduePredicate);
    when(cb.and(conjunction, hasDueDatePredicate, overduePredicate))
        .thenReturn(overdueConjunction);

    Predicate result = toPredicate(request, null);

    assertEquals(overdueConjunction, result);
  }

  @Test
  @DisplayName("should skip overdue filter when overdue is false")
  void buildSearchQuery_shouldSkipOverdueFilter_whenOverdueFalse() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOverdue(false);

    Predicate result = toPredicate(request, null);

    assertEquals(conjunction, result);
    verify(opp, never()).get("nextStepDueDate");
  }

  @Test
  @DisplayName("should apply unread messages filter")
  void buildSearchQuery_shouldApplyUnreadMessagesFilter() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setWithUnreadMessages(true);

    User loggedInUser = mock(User.class);
    Predicate unreadPredicate = mock(Predicate.class);
    Predicate unreadConjunction = mock(Predicate.class);

    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(conjunction);
    when(cb.and(conjunction, unreadPredicate)).thenReturn(unreadConjunction);

    try (MockedStatic<SpecificationHelper> mockedHelper = mockStatic(SpecificationHelper.class)) {
      mockedHelper.when(() -> SpecificationHelper.hasUnreadChats(
          any(User.class),
          any(CriteriaQuery.class),
          any(CriteriaBuilder.class),
          any(Subquery.class),
          any(Root.class),
          any(Predicate.class)
      )).thenReturn(unreadPredicate);

      Predicate result = toPredicate(request, loggedInUser);

      assertEquals(unreadConjunction, result);
    }
  }

  @Test
  @DisplayName("should skip unread messages filter when unread is false")
  void buildSearchQuery_shouldSkipUnreadMessagesFilter_whenUnreadFalse() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setWithUnreadMessages(false);

    Predicate result = toPredicate(request, null);

    assertEquals(conjunction, result);
    verify(query, never()).subquery(Long.class);
  }

  @Test
  @DisplayName("should skip ownership when ownership type is null")
  void buildSearchQuery_shouldSkipOwnership_whenOwnershipTypeIsNull() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnedByMe(true);
    request.setOwnedByMyPartner(true);

    Predicate result = toPredicate(request, null);

    assertEquals(conjunction, result);
    verify(opp, never()).join("jobOpp");
    verify(opp, never()).join("candidate");
  }

  @Test
  @DisplayName("should skip ownership when logged in user is null")
  void buildSearchQuery_shouldSkipOwnership_whenLoggedInUserIsNull() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_JOB_CREATOR);
    request.setOwnedByMe(true);

    Predicate result = toPredicate(request, null);

    assertEquals(conjunction, result);
    verify(opp, never()).join("jobOpp");
  }

  @Test
  @DisplayName("should skip ownership when logged in user has no partner")
  void buildSearchQuery_shouldSkipOwnership_whenLoggedInUserHasNoPartner() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_JOB_CREATOR);
    request.setOwnedByMe(true);

    User loggedInUser = mock(User.class);
    when(loggedInUser.getPartner()).thenReturn(null);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(conjunction, result);
    verify(opp, never()).join("jobOpp");
  }

  @Test
  @DisplayName("should filter job creator ownership by my partner")
  void buildSearchQuery_shouldFilterJobCreatorOwnershipByMyPartner() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_JOB_CREATOR);
    request.setOwnedByMyPartner(true);

    User loggedInUser = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.getId()).thenReturn(99L);

    doReturn(jobOppJoin).when(opp).join("jobOpp");

    when(cb.equal(jobOppJoin.get("jobCreator").get("id"), 99L)).thenReturn(ownedPredicate);
    when(cb.and(conjunction, ownedPredicate)).thenReturn(ownedConjunction);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(ownedConjunction, result);
  }

  @Test
  @DisplayName("should filter job creator ownership by logged in user")
  void buildSearchQuery_shouldFilterJobCreatorOwnershipByLoggedInUser() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_JOB_CREATOR);
    request.setOwnedByMe(true);

    User loggedInUser = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUser.getId()).thenReturn(42L);

    doReturn(jobOppJoin).when(opp).join("jobOpp");

    when(cb.and(any(Predicate.class), any(Predicate.class)))
        .thenReturn(matchContactUserPredicate, matchCreatedByPredicate, ownedConjunction);
    when(cb.or(matchContactUserPredicate, matchCreatedByPredicate)).thenReturn(ownerDisjunction);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(ownedConjunction, result);
  }

  @Test
  @DisplayName("should skip job creator owned by me when flag is false")
  void buildSearchQuery_shouldSkipJobCreatorOwnedByMe_whenFlagFalse() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_JOB_CREATOR);
    request.setOwnedByMyPartner(false);
    request.setOwnedByMe(false);

    User loggedInUser = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(conjunction, result);
    verify(opp, never()).join("jobOpp");
  }

  @Test
  @DisplayName("should filter source partner ownership by candidate partner")
  void buildSearchQuery_shouldFilterSourcePartnerOwnershipByCandidatePartner() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_SOURCE_PARTNER);
    request.setOwnedByMyPartner(true);

    User loggedInUser = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.isSourcePartner()).thenReturn(true);
    when(loggedInUserPartner.getId()).thenReturn(123L);

    stubCandidatePartnerJoin();

    when(cb.equal(partnerJoin.get("id"), 123L)).thenReturn(ownedPredicate);
    when(cb.and(conjunction, ownedPredicate)).thenReturn(ownedConjunction);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(ownedConjunction, result);
  }

  @Test
  @DisplayName("should skip source partner ownership when logged in partner is not source partner")
  void buildSearchQuery_shouldSkipSourcePartnerOwnership_whenPartnerIsNotSourcePartner() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_SOURCE_PARTNER);
    request.setOwnedByMyPartner(true);

    User loggedInUser = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.isSourcePartner()).thenReturn(false);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(conjunction, result);
    verify(opp, never()).join("candidate");
  }

  @Test
  @DisplayName("should filter source partner owned by me without default contact")
  void buildSearchQuery_shouldFilterSourcePartnerOwnedByMeWithoutDefaultContact() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_SOURCE_PARTNER);
    request.setOwnedByMe(true);

    User loggedInUser = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.isSourcePartner()).thenReturn(true);
    when(loggedInUserPartner.getId()).thenReturn(123L);
    when(loggedInUser.getId()).thenReturn(42L);
    when(loggedInUserPartner.getDefaultContact()).thenReturn(null);

    stubCandidatePartnerJoin();
    doReturn(jobOppJoin).when(opp).join("jobOpp");

    when(cb.disjunction()).thenReturn(emptyDisjunction);
    when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(nonEmptyDisjunction);
    when(nonEmptyDisjunction.getExpressions()).thenReturn(List.of(disjunctionExpression));
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(conjunction);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(conjunction, result);
  }

  @Test
  @DisplayName("should filter source partner owned by me when default contact does not match")
  void buildSearchQuery_shouldFilterSourcePartnerOwnedByMeWhenDefaultContactDoesNotMatch() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_SOURCE_PARTNER);
    request.setOwnedByMe(true);

    User loggedInUser = mock(User.class);
    User defaultContact = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.isSourcePartner()).thenReturn(true);
    when(loggedInUserPartner.getId()).thenReturn(123L);
    when(loggedInUser.getId()).thenReturn(42L);
    when(loggedInUserPartner.getDefaultContact()).thenReturn(defaultContact);
    when(defaultContact.getId()).thenReturn(99L);

    stubCandidatePartnerJoin();
    doReturn(jobOppJoin).when(opp).join("jobOpp");

    when(cb.disjunction()).thenReturn(emptyDisjunction);
    when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(nonEmptyDisjunction);
    when(nonEmptyDisjunction.getExpressions()).thenReturn(List.of(disjunctionExpression));
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(conjunction);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(conjunction, result);
  }

  @Test
  @DisplayName("should filter source partner owned by me when logged in user is default contact")
  void buildSearchQuery_shouldFilterSourcePartnerOwnedByMeWhenLoggedInUserIsDefaultContact() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_SOURCE_PARTNER);
    request.setOwnedByMe(true);

    User loggedInUser = mock(User.class);
    User defaultContact = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.isSourcePartner()).thenReturn(true);
    when(loggedInUserPartner.getId()).thenReturn(123L);
    when(loggedInUser.getId()).thenReturn(42L);
    when(loggedInUserPartner.getDefaultContact()).thenReturn(defaultContact);
    when(defaultContact.getId()).thenReturn(42L);

    stubCandidatePartnerJoin();
    doReturn(jobOppJoin).when(opp).join("jobOpp");

    when(cb.disjunction()).thenReturn(emptyDisjunction);
    when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(nonEmptyDisjunction);
    when(nonEmptyDisjunction.getExpressions()).thenReturn(List.of(disjunctionExpression));
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(conjunction);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(conjunction, result);
    verify(query).subquery(User.class);
  }

  @Test
  @DisplayName("should skip source partner owned by me final disjunction when it is empty")
  void buildSearchQuery_shouldSkipSourcePartnerOwnedByMeFinalDisjunctionWhenEmpty() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_SOURCE_PARTNER);
    request.setOwnedByMe(true);

    User loggedInUser = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.isSourcePartner()).thenReturn(true);
    when(loggedInUserPartner.getId()).thenReturn(123L);
    when(loggedInUser.getId()).thenReturn(42L);
    when(loggedInUserPartner.getDefaultContact()).thenReturn(null);

    stubCandidatePartnerJoin();
    doReturn(jobOppJoin).when(opp).join("jobOpp");

    when(cb.disjunction()).thenReturn(emptyDisjunction);
    when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(emptyDisjunction);
    when(emptyDisjunction.getExpressions()).thenReturn(List.of());
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(conjunction);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(conjunction, result);
  }

  @Test
  @DisplayName("should skip source partner owned by me when flag is false")
  void buildSearchQuery_shouldSkipSourcePartnerOwnedByMe_whenFlagFalse() {
    stubNonCountQuery();

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_SOURCE_PARTNER);
    request.setOwnedByMyPartner(false);
    request.setOwnedByMe(false);

    User loggedInUser = mock(User.class);
    PartnerImpl loggedInUserPartner = mock(PartnerImpl.class);

    when(loggedInUser.getPartner()).thenReturn(loggedInUserPartner);
    when(loggedInUserPartner.isSourcePartner()).thenReturn(true);

    Predicate result = toPredicate(request, loggedInUser);

    assertEquals(conjunction, result);
    verify(opp, never()).join("candidate");
  }

  private Predicate toPredicate(SearchCandidateOpportunityRequest request, User loggedInUser) {
    Specification<CandidateOpportunity> specification =
        CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser);

    return specification.toPredicate(opp, query, cb);
  }

  private void stubNonCountQuery() {
    when(query.getResultType()).thenReturn(CandidateOpportunity.class);
    when(cb.conjunction()).thenReturn(conjunction);
    when(opp.get("id")).thenReturn(idPath);
    when(cb.desc(idPath)).thenReturn(idOrder);
    when(query.orderBy(anyList())).thenReturn(query);
  }

  private void stubCandidatePartnerJoin() {
    doReturn(candidateJoin).when(opp).join("candidate");
    doReturn(userJoin).when(candidateJoin).join("user");
    doReturn(partnerJoin).when(userJoin).join("partner");
  }
}