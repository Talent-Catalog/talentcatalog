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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.SavedListGetRequest;

@ExtendWith(MockitoExtension.class)
class GetSavedListCandidatesQueryTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Root<Candidate> candidate;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaQuery<Candidate> query;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaQuery<Long> countQuery;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaBuilder cb;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Root<CandidateSavedList> candidateSavedList;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Join<Candidate, CandidateOpportunity> candidateOpportunity;

  @Mock
  private SavedList savedList;

  @Mock
  private SalesforceJobOpp sfJobOpp;

  @Mock
  private JoinFetch userFetch;

  @Mock
  private JoinFetch partnerFetch;

  @Mock
  private JoinFetch nationalityFetch;

  @Mock
  private JoinFetch countryFetch;

  @Mock
  private JoinFetch educationLevelFetch;

  @Test
  @DisplayName("should throw when criteria query is null")
  void toPredicate_shouldThrow_whenCriteriaQueryIsNull() {
    SavedListGetRequest request = new SavedListGetRequest();

    assertThrows(IllegalArgumentException.class,
        () -> new GetSavedListCandidatesQuery(savedList, request)
            .toPredicate(candidate, null, cb));
  }

  @Test
  @DisplayName("should build non-count query and apply fetches and ordering")
  void toPredicate_shouldBuildNonCountQueryAndApplyFetchesAndOrdering() {
    SavedListGetRequest request = new SavedListGetRequest();

    stubNonCountQuery();

    assertNotNull(new GetSavedListCandidatesQuery(savedList, request)
        .toPredicate(candidate, query, cb));

    verify(candidate).fetch("user", JoinType.LEFT);
    verify(userFetch).fetch("partner", JoinType.LEFT);
    verify(candidate).fetch("nationality", JoinType.LEFT);
    verify(candidate).fetch("country", JoinType.LEFT);
    verify(candidate).fetch("maxEducationLevel", JoinType.LEFT);
    verify(query).orderBy(anyList());
  }

  @Test
  @DisplayName("should build count query without fetches and ordering")
  void toPredicate_shouldBuildCountQueryWithoutFetchesAndOrdering() {
    SavedListGetRequest request = new SavedListGetRequest();

    when(countQuery.getResultType()).thenReturn(Long.class);
    when(countQuery.subquery(Candidate.class).from(CandidateSavedList.class))
        .thenReturn(candidateSavedList);

    assertNotNull(new GetSavedListCandidatesQuery(savedList, request)
        .toPredicate(candidate, countQuery, cb));

    verify(candidate, never()).fetch("user", JoinType.LEFT);
    verify(countQuery, never()).orderBy(anyList());
  }

  @Test
  @DisplayName("should apply keyword filter")
  void toPredicate_shouldApplyKeywordFilter() {
    SavedListGetRequest request = new SavedListGetRequest();
    request.setKeyword("Ehsan Candidate");

    stubNonCountQuery();

    assertNotNull(new GetSavedListCandidatesQuery(savedList, request)
        .toPredicate(candidate, query, cb));

    verify(cb).like(
        cb.lower(candidate.get("candidateNumber")),
        "%ehsan candidate%"
    );
    verify(cb).like(
        cb.lower(candidate.get("user").get("firstName")),
        "%ehsan candidate%"
    );
    verify(cb).like(
        cb.lower(candidate.get("user").get("lastName")),
        "%ehsan candidate%"
    );
  }

  @Test
  @DisplayName("should skip keyword filter when keyword is blank")
  void toPredicate_shouldSkipKeywordFilterWhenKeywordIsBlank() {
    SavedListGetRequest request = new SavedListGetRequest();
    request.setKeyword("   ");

    stubNonCountQuery();

    assertNotNull(new GetSavedListCandidatesQuery(savedList, request)
        .toPredicate(candidate, query, cb));

    verify(candidate, never()).get("candidateNumber");
  }

  @Test
  @DisplayName("should apply job filter and default open or won opportunity filter when show closed is null")
  void toPredicate_shouldApplyJobFilterAndDefaultOppFilterWhenShowClosedIsNull() {
    SavedListGetRequest request = new SavedListGetRequest();

    stubNonCountQuery();
    stubSavedListWithJob();

    assertNotNull(new GetSavedListCandidatesQuery(savedList, request)
        .toPredicate(candidate, query, cb));

    verify(candidate).join("candidateOpportunities", JoinType.LEFT);
    verify(candidateOpportunity).get("jobOpp");
    verify(candidateOpportunity).get("closed");
    verify(candidateOpportunity).get("won");
  }

  @Test
  @DisplayName("should apply job filter and open or won opportunity filter when show closed is false")
  void toPredicate_shouldApplyJobFilterAndDefaultOppFilterWhenShowClosedIsFalse() {
    SavedListGetRequest request = new SavedListGetRequest();
    request.setShowClosedOpps(false);

    stubNonCountQuery();
    stubSavedListWithJob();

    assertNotNull(new GetSavedListCandidatesQuery(savedList, request)
        .toPredicate(candidate, query, cb));

    verify(candidate).join("candidateOpportunities", JoinType.LEFT);
    verify(candidateOpportunity).get("jobOpp");
    verify(candidateOpportunity).get("closed");
    verify(candidateOpportunity).get("won");
  }

  @Test
  @DisplayName("should apply job filter but skip closed opportunity filter when show closed is true")
  void toPredicate_shouldApplyJobFilterButSkipClosedOppFilterWhenShowClosedIsTrue() {
    SavedListGetRequest request = new SavedListGetRequest();
    request.setShowClosedOpps(true);

    stubNonCountQuery();
    stubSavedListWithJob();

    assertNotNull(new GetSavedListCandidatesQuery(savedList, request)
        .toPredicate(candidate, query, cb));

    verify(candidate).join("candidateOpportunities", JoinType.LEFT);
    verify(candidateOpportunity).get("jobOpp");
    verify(candidateOpportunity, never()).get("closed");
    verify(candidateOpportunity, never()).get("won");
  }

  @Test
  @DisplayName("should skip job opportunity filtering when saved list has no job")
  void toPredicate_shouldSkipJobOpportunityFilteringWhenSavedListHasNoJob() {
    SavedListGetRequest request = new SavedListGetRequest();

    stubNonCountQuery();

    assertNotNull(new GetSavedListCandidatesQuery(savedList, request)
        .toPredicate(candidate, query, cb));

    verify(candidate, never()).join("candidateOpportunities", JoinType.LEFT);
  }

  private void stubNonCountQuery() {
    when(query.getResultType()).thenReturn(Candidate.class);
    when(query.subquery(Candidate.class).from(CandidateSavedList.class))
        .thenReturn(candidateSavedList);

    doReturn(userFetch).when(candidate).fetch("user", JoinType.LEFT);
    doReturn(partnerFetch).when(userFetch).fetch("partner", JoinType.LEFT);
    doReturn(nationalityFetch).when(candidate).fetch("nationality", JoinType.LEFT);
    doReturn(countryFetch).when(candidate).fetch("country", JoinType.LEFT);
    doReturn(educationLevelFetch).when(candidate).fetch("maxEducationLevel", JoinType.LEFT);
  }

  private void stubSavedListWithJob() {
    when(savedList.getSfJobOpp()).thenReturn(sfJobOpp);
    when(sfJobOpp.getId()).thenReturn(55L);
    doReturn(candidateOpportunity)
        .when(candidate).join("candidateOpportunities", JoinType.LEFT);
  }

  private interface JoinFetch extends Join<Object, Object>, Fetch<Object, Object> {
  }
}