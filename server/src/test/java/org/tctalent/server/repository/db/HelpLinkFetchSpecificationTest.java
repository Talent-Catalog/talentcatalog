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
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.HelpFocus;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;

@ExtendWith(MockitoExtension.class)
class HelpLinkFetchSpecificationTest {

  @Mock private Root<HelpLink> helpLink;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;

  @Mock private Predicate conjunction;
  @Mock private Predicate combinedPredicate;

  @Mock private Path<Object> countryPath;
  @Mock private Path<Long> countryIdPath;
  @Mock private Predicate countryPredicate;

  @Mock private Path<CandidateOpportunityStage> caseStagePath;
  @Mock private Predicate caseStagePredicate;

  @Mock private Path<HelpFocus> focusPath;
  @Mock private Predicate focusPredicate;

  @Mock private Path<JobOpportunityStage> jobStagePath;
  @Mock private Predicate jobStagePredicate;

  @Mock private Path<Object> nextStepInfoPath;
  @Mock private Path<String> nextStepNamePath;
  @Mock private Predicate nextStepPredicate;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new HelpLinkFetchSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();

    assertThrows(IllegalArgumentException.class,
        () -> HelpLinkFetchSpecification.buildSearchQuery(request)
            .toPredicate(helpLink, null, cb));
  }

  @Test
  @DisplayName("should build base query when no filters are supplied")
  void buildSearchQuery_shouldBuildBaseQuery_whenNoFiltersAreSupplied() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = HelpLinkFetchSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should apply all help link context filters")
  void buildSearchQuery_shouldApplyAllHelpLinkContextFilters() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setCountryId(123L);
    request.setCaseStage(CandidateOpportunityStage.prospect);
    request.setFocus(HelpFocus.updateStage);
    request.setJobStage(JobOpportunityStage.candidateSearch);
    request.setNextStepName("Upload CV");

    stubBaseAnd();

    when(helpLink.<Object>get("country")).thenReturn(countryPath);
    when(countryPath.<Long>get("id")).thenReturn(countryIdPath);
    when(cb.equal(countryIdPath, 123L)).thenReturn(countryPredicate);

    when(helpLink.<CandidateOpportunityStage>get("caseStage")).thenReturn(caseStagePath);
    when(cb.equal(caseStagePath, CandidateOpportunityStage.prospect))
        .thenReturn(caseStagePredicate);

    when(helpLink.<HelpFocus>get("focus")).thenReturn(focusPath);
    when(cb.equal(focusPath, HelpFocus.updateStage)).thenReturn(focusPredicate);

    when(helpLink.<JobOpportunityStage>get("jobStage")).thenReturn(jobStagePath);
    when(cb.equal(jobStagePath, JobOpportunityStage.candidateSearch))
        .thenReturn(jobStagePredicate);

    when(helpLink.<Object>get("nextStepInfo")).thenReturn(nextStepInfoPath);
    when(nextStepInfoPath.<String>get("nextStepName")).thenReturn(nextStepNamePath);
    when(cb.equal(nextStepNamePath, "Upload CV")).thenReturn(nextStepPredicate);

    Predicate result = HelpLinkFetchSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertNotNull(result);

    verify(query).distinct(true);
    verify(cb).equal(countryIdPath, 123L);
    verify(cb).equal(caseStagePath, CandidateOpportunityStage.prospect);
    verify(cb).equal(focusPath, HelpFocus.updateStage);
    verify(cb).equal(jobStagePath, JobOpportunityStage.candidateSearch);
    verify(cb).equal(nextStepNamePath, "Upload CV");
  }

  @Test
  @DisplayName("should apply country filter only")
  void buildSearchQuery_shouldApplyCountryFilterOnly() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setCountryId(456L);

    stubBaseAnd();

    when(helpLink.<Object>get("country")).thenReturn(countryPath);
    when(countryPath.<Long>get("id")).thenReturn(countryIdPath);
    when(cb.equal(countryIdPath, 456L)).thenReturn(countryPredicate);

    Predicate result = HelpLinkFetchSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertNotNull(result);

    verify(query).distinct(true);
    verify(cb).equal(countryIdPath, 456L);
  }

  @Test
  @DisplayName("should apply case stage filter only")
  void buildSearchQuery_shouldApplyCaseStageFilterOnly() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setCaseStage(CandidateOpportunityStage.relocated);

    stubBaseAnd();

    when(helpLink.<CandidateOpportunityStage>get("caseStage")).thenReturn(caseStagePath);
    when(cb.equal(caseStagePath, CandidateOpportunityStage.relocated))
        .thenReturn(caseStagePredicate);

    Predicate result = HelpLinkFetchSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertNotNull(result);

    verify(query).distinct(true);
    verify(cb).equal(caseStagePath, CandidateOpportunityStage.relocated);
  }

  @Test
  @DisplayName("should apply focus filter only")
  void buildSearchQuery_shouldApplyFocusFilterOnly() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setFocus(HelpFocus.updateNextStep);

    stubBaseAnd();

    when(helpLink.<HelpFocus>get("focus")).thenReturn(focusPath);
    when(cb.equal(focusPath, HelpFocus.updateNextStep)).thenReturn(focusPredicate);

    Predicate result = HelpLinkFetchSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertNotNull(result);

    verify(query).distinct(true);
    verify(cb).equal(focusPath, HelpFocus.updateNextStep);
  }

  @Test
  @DisplayName("should apply job stage filter only")
  void buildSearchQuery_shouldApplyJobStageFilterOnly() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setJobStage(JobOpportunityStage.jobOffer);

    stubBaseAnd();

    when(helpLink.<JobOpportunityStage>get("jobStage")).thenReturn(jobStagePath);
    when(cb.equal(jobStagePath, JobOpportunityStage.jobOffer)).thenReturn(jobStagePredicate);

    Predicate result = HelpLinkFetchSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertNotNull(result);

    verify(query).distinct(true);
    verify(cb).equal(jobStagePath, JobOpportunityStage.jobOffer);
  }

  @Test
  @DisplayName("should apply next step name filter only")
  void buildSearchQuery_shouldApplyNextStepNameFilterOnly() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setNextStepName("Send contract");

    stubBaseAnd();

    when(helpLink.<Object>get("nextStepInfo")).thenReturn(nextStepInfoPath);
    when(nextStepInfoPath.<String>get("nextStepName")).thenReturn(nextStepNamePath);
    when(cb.equal(nextStepNamePath, "Send contract")).thenReturn(nextStepPredicate);

    Predicate result = HelpLinkFetchSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertNotNull(result);

    verify(query).distinct(true);
    verify(cb).equal(nextStepNamePath, "Send contract");
  }

  private void stubBaseAnd() {
    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
  }
}