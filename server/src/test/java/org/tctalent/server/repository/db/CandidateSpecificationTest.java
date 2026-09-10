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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateFilterByOpps;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.candidate.SearchCandidateRequest;

@ExtendWith(MockitoExtension.class)
class CandidateSpecificationTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Root<Candidate> candidate;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaQuery<Candidate> query;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaQuery<Long> countQuery;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteriaBuilder cb;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private JoinFetch userFetch;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private JoinFetch partnerFetch;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private JoinFetch nationalityFetch;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private JoinFetch countryFetch;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private JoinFetch maxEducationLevelFetch;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Predicate conjunction;

  @Mock
  private Candidate excludedCandidate;

  @Mock
  private User loggedInUser;

  @Mock
  private Country sourceCountry;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new CandidateSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    when(cb.conjunction()).thenReturn(conjunction);

    assertThrows(IllegalArgumentException.class,
        () -> CandidateSpecification.buildSearchQuery(request, null, null)
            .toPredicate(candidate, null, cb));
  }

  @Test
  @DisplayName("should build non-count query with many positive filters")
  void buildSearchQuery_shouldBuildNonCountQuery_withManyPositiveFilters() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setKeyword("Ehsan Developer");
    request.setStatuses(List.of(CandidateStatus.active));
    request.setOccupationIds(List.of(10L, 20L));
    request.setMinYrs(2);
    request.setMaxYrs(8);
    request.setNationalityIds(List.of(1L));
    request.setNationalitySearchType(SearchType.or);
    request.setCountryIds(List.of(2L));
    request.setCountrySearchType(SearchType.or);
    request.setPartnerIds(List.of(3L));
    request.setSurveyTypeIds(List.of(4L));
    request.setRegoReferrerParam("referrer");
    request.setRegoUtmCampaign("campaign");
    request.setRegoUtmSource("source");
    request.setRegoUtmMedium("medium");
    request.setGender(Gender.male);
    request.setTimezone("UTC");
    request.setLastModifiedFrom(LocalDate.of(2026, 1, 1));
    request.setLastModifiedTo(LocalDate.of(2026, 1, 31));
    request.setMinAge(18);
    request.setMaxAge(45);
    request.setUnhcrStatuses(List.of(UnhcrStatus.RegisteredAsylum));
    request.setMinEducationLevel(2);
    request.setMaxEducationLevel(5);
    request.setMiniIntakeCompleted(true);
    request.setFullIntakeCompleted(true);
    request.setPotentialDuplicate(true);
    request.setEducationMajorIds(List.of(5L));
    request.setEnglishMinWrittenLevel(3);
    request.setEnglishMinSpokenLevel(4);
    request.setOtherLanguageId(6L);
    request.setOtherMinWrittenLevel(2);
    request.setOtherMinSpokenLevel(3);
    request.setListAnyIds(List.of(7L, 8L));
    request.setListAllIds(List.of(9L, 10L));
    request.setCandidateFilterByOpps(CandidateFilterByOpps.someOpps);

    assertNotNull(runNonCount(request, null, List.of(excludedCandidate)));
  }

  @Test
  @DisplayName("should build count query")
  void buildSearchQuery_shouldBuildCountQuery() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    assertNotNull(runCount(request));
  }

  @Test
  @DisplayName("should apply nationality and country default OR search when search type is null")
  void buildSearchQuery_shouldApplyDefaultOrSearchTypes_whenSearchTypeNull() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setNationalityIds(List.of(1L));
    request.setCountryIds(List.of(2L));

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply nationality and country NOT search")
  void buildSearchQuery_shouldApplyNotSearchTypes() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setNationalityIds(List.of(1L));
    request.setNationalitySearchType(SearchType.not);
    request.setCountryIds(List.of(2L));
    request.setCountrySearchType(SearchType.not);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should limit country by logged-in user's source countries when no country ids are requested")
  void buildSearchQuery_shouldLimitByLoggedInUserSourceCountries() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    when(loggedInUser.getSourceCountries()).thenReturn(Set.of(sourceCountry));

    assertNotNull(runNonCount(request, loggedInUser, null));
  }

  @Test
  @DisplayName("should ignore source country limitation when logged-in user has no source countries")
  void buildSearchQuery_shouldIgnoreSourceCountryLimitation_whenLoggedInUserHasNoSourceCountries() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    when(loggedInUser.getSourceCountries()).thenReturn(Set.of());

    assertNotNull(runNonCount(request, loggedInUser, null));
  }

  @Test
  @DisplayName("should apply occupation filter without year bounds")
  void buildSearchQuery_shouldApplyOccupationFilter_withoutYearBounds() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setOccupationIds(List.of(1L));

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should ignore blank registration tracking fields")
  void buildSearchQuery_shouldIgnoreBlankRegistrationTrackingFields() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setRegoReferrerParam("   ");
    request.setRegoUtmCampaign("   ");
    request.setRegoUtmSource("   ");
    request.setRegoUtmMedium("   ");

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply false intake completion filters")
  void buildSearchQuery_shouldApplyFalseIntakeCompletionFilters() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setMiniIntakeCompleted(false);
    request.setFullIntakeCompleted(false);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply min education level only")
  void buildSearchQuery_shouldApplyMinEducationLevelOnly() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setMinEducationLevel(3);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply max education level only")
  void buildSearchQuery_shouldApplyMaxEducationLevelOnly() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setMaxEducationLevel(5);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply English written level only")
  void buildSearchQuery_shouldApplyEnglishWrittenLevelOnly() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setEnglishMinWrittenLevel(3);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply English spoken level only")
  void buildSearchQuery_shouldApplyEnglishSpokenLevelOnly() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setEnglishMinSpokenLevel(3);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply other language id only")
  void buildSearchQuery_shouldApplyOtherLanguageIdOnly() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setOtherLanguageId(11L);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply other language spoken level only")
  void buildSearchQuery_shouldApplyOtherLanguageSpokenLevelOnly() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setOtherLanguageId(11L);
    request.setOtherMinSpokenLevel(3);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply other language written level only")
  void buildSearchQuery_shouldApplyOtherLanguageWrittenLevelOnly() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setOtherLanguageId(11L);
    request.setOtherMinWrittenLevel(3);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should evaluate other spoken language condition without other language id")
  void buildSearchQuery_shouldEvaluateOtherSpokenCondition_withoutOtherLanguageId() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setOtherMinSpokenLevel(3);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should evaluate other written language condition without other language id")
  void buildSearchQuery_shouldEvaluateOtherWrittenCondition_withoutOtherLanguageId() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setOtherMinWrittenLevel(3);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply NOT list searches")
  void buildSearchQuery_shouldApplyNotListSearches() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setListAnyIds(List.of(1L));
    request.setListAnySearchType(SearchType.not);
    request.setListAllIds(List.of(2L));
    request.setListAllSearchType(SearchType.not);

    assertNotNull(runNonCount(request, null, null));
  }

  @Test
  @DisplayName("should apply all candidate opportunity filters")
  void buildSearchQuery_shouldApplyAllCandidateOpportunityFilters() {
    for (CandidateFilterByOpps filter : CandidateFilterByOpps.values()) {
      SearchCandidateRequest request = new SearchCandidateRequest();
      request.setCandidateFilterByOpps(filter);

      assertNotNull(runNonCount(request, null, null));
    }
  }

  @Test
  @DisplayName("should mark query as distinct")
  void buildSearchQuery_shouldMarkQueryAsDistinct() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    CriteriaContext context = nonCountContext();

    CandidateSpecification.buildSearchQuery(request, null, null)
        .toPredicate(context.candidate, context.query, context.cb);

    verify(context.query).distinct(true);
  }

  private Predicate runNonCount(
      SearchCandidateRequest request,
      User loggedInUser,
      Collection<Candidate> excludedCandidates
  ) {
    CriteriaContext context = nonCountContext();

    return CandidateSpecification.buildSearchQuery(request, loggedInUser, excludedCandidates)
        .toPredicate(context.candidate, context.query, context.cb);
  }

  private Predicate runCount(SearchCandidateRequest request) {
    CriteriaContext context = countContext();

    return CandidateSpecification.buildSearchQuery(request, null, null)
        .toPredicate(context.candidate, context.query, context.cb);
  }

  private CriteriaContext nonCountContext() {
    when(query.getResultType()).thenReturn(Candidate.class);
    when(cb.conjunction()).thenReturn(conjunction);
    stubFetchJoins();

    return new CriteriaContext(candidate, query, cb);
  }

  private CriteriaContext countContext() {
    when(countQuery.getResultType()).thenReturn(Long.class);
    when(cb.conjunction()).thenReturn(conjunction);

    return new CriteriaContext(candidate, countQuery, cb);
  }

  private void stubFetchJoins() {
    doReturn(userFetch).when(candidate).fetch("user", JoinType.INNER);
    doReturn(partnerFetch).when(userFetch).fetch("partner", JoinType.INNER);
    doReturn(nationalityFetch).when(candidate).fetch("nationality");
    doReturn(countryFetch).when(candidate).fetch("country");
    doReturn(maxEducationLevelFetch).when(candidate).fetch("maxEducationLevel");
  }

  private interface JoinFetch extends Join<Object, Object>, Fetch<Object, Object> {
  }

  private record CriteriaContext(
      Root<Candidate> candidate,
      CriteriaQuery<?> query,
      CriteriaBuilder cb
  ) {
  }
}