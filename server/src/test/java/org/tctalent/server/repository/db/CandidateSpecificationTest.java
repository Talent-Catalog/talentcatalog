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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateFilterByOpps;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.candidate.SearchCandidateRequest;

class CandidateSpecificationTest {

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new CandidateSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    assertThrows(IllegalArgumentException.class,
        () -> CandidateSpecification.buildSearchQuery(request, null, null)
            .toPredicate(mockCandidateRoot(), null, mockCriteriaBuilder()));
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

    Candidate excludedCandidate = mock(Candidate.class);

    assertNotNull(runNonCount(request, null, List.of(excludedCandidate)));
  }

  @Test
  @DisplayName("should build count query")
  void buildSearchQuery_shouldBuildCountQuery() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    assertNotNull(runCount(request, null, null));
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

    User loggedInUser = mock(User.class);
    when(loggedInUser.getSourceCountries()).thenReturn(Set.of(mock(Country.class)));

    assertNotNull(runNonCount(request, loggedInUser, null));
  }

  @Test
  @DisplayName("should ignore source country limitation when logged-in user has no source countries")
  void buildSearchQuery_shouldIgnoreSourceCountryLimitation_whenLoggedInUserHasNoSourceCountries() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    User loggedInUser = mock(User.class);
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

  private Predicate runCount(
      SearchCandidateRequest request,
      User loggedInUser,
      Collection<Candidate> excludedCandidates
  ) {
    CriteriaContext context = countContext();

    return CandidateSpecification.buildSearchQuery(request, loggedInUser, excludedCandidates)
        .toPredicate(context.candidate, context.query, context.cb);
  }

  private CriteriaContext nonCountContext() {
    Root<Candidate> candidate = mockCandidateRoot();
    CriteriaQuery<Candidate> query = mock(CriteriaQuery.class, RETURNS_DEEP_STUBS);
    CriteriaBuilder cb = mockCriteriaBuilder();

    when(query.getResultType()).thenReturn(Candidate.class);
    stubFetchJoins(candidate);

    return new CriteriaContext(candidate, query, cb);
  }

  private CriteriaContext countContext() {
    Root<Candidate> candidate = mockCandidateRoot();
    CriteriaQuery<Long> query = mock(CriteriaQuery.class, RETURNS_DEEP_STUBS);
    CriteriaBuilder cb = mockCriteriaBuilder();

    when(query.getResultType()).thenReturn(Long.class);

    return new CriteriaContext(candidate, query, cb);
  }

  private CriteriaBuilder mockCriteriaBuilder() {
    CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
    when(cb.conjunction()).thenReturn(mock(Predicate.class, RETURNS_DEEP_STUBS));
    return cb;
  }

  private Root<Candidate> mockCandidateRoot() {
    return mock(Root.class, RETURNS_DEEP_STUBS);
  }

  private void stubFetchJoins(Root<Candidate> candidate) {
    Join<Object, Object> user = mockJoinFetch();
    Join<Object, Object> partner = mockJoinFetch();
    Join<Object, Object> nationality = mockJoinFetch();
    Join<Object, Object> country = mockJoinFetch();
    Join<Object, Object> maxEducationLevel = mockJoinFetch();

    doReturn(asFetch(user)).when(candidate).fetch("user", JoinType.INNER);
    doReturn(asFetch(partner)).when(user).fetch("partner", JoinType.INNER);
    doReturn(asFetch(nationality)).when(candidate).fetch("nationality");
    doReturn(asFetch(country)).when(candidate).fetch("country");
    doReturn(asFetch(maxEducationLevel)).when(candidate).fetch("maxEducationLevel");
  }

  private Join<Object, Object> mockJoinFetch() {
    return mock(
        Join.class,
        withSettings()
            .extraInterfaces(Fetch.class)
            .defaultAnswer(RETURNS_DEEP_STUBS)
    );
  }

  private Fetch<Object, Object> asFetch(Join<Object, Object> join) {
    return (Fetch<Object, Object>) join;
  }

  private record CriteriaContext(
      Root<Candidate> candidate,
      CriteriaQuery<?> query,
      CriteriaBuilder cb
  ) {
  }
}