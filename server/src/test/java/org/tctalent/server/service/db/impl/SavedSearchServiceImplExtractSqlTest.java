/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort.Direction;
import org.tctalent.server.configuration.SystemAdminConfiguration;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.util.CandidateSearchUtils;

/**
 * @author John Cameron
 */
@ExtendWith(MockitoExtension.class)
class SavedSearchServiceImplExtractSqlTest {

    @InjectMocks
    SavedSearchServiceImpl savedSearchService;

    private SearchCandidateRequest request;

    private static final String FROM_CANDIDATE = " from candidate";
    private static final String JOIN = " left join ";
    private static final String ID_SORT = "candidate.id DESC";
    private static final String ORDER_BY = " order by ";
    private static final String WHERE = " where ";
    private static final String UNORDERED_SELECT = "select distinct candidate.id" + FROM_CANDIDATE;
    private static final String ORDERED_SELECT_PREFIX = "select distinct candidate.id";
    private static final String EXCLUDE_PENDING_TERMS_CLAUSE =
        "candidate.id not in (select candidate_id from candidate_saved_list where saved_list_id = ";

    @BeforeEach
    void setUp() {
        request = new SearchCandidateRequest();
        request.setIncludePendingTermsCandidates(true);
        SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID = 123L;
    }

    @Test
    @DisplayName("SQL generated from empty request")
    void extractFetchSQLFromEmptyRequest() {
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT, sql);
    }

    @Test
    @DisplayName("SQL generated from empty ordered request")
    void extractFetchSQLFromEmptyOrderedRequest() {
        String sql = savedSearchService.extractFetchSQL(request, null, null, true);
        assertEquals(ORDERED_SELECT_PREFIX + FROM_CANDIDATE + ORDER_BY + ID_SORT, sql);
    }

    @Test
    @DisplayName("SQL generated from ordered request")
    void extractFetchSQLFromOrderedRequest() {
        request.setSortFields(new String[] {"gender"});
        request.setSortDirection(Direction.ASC);
        String sql = savedSearchService.extractFetchSQL(request, null, null, true);
        assertEquals(ORDERED_SELECT_PREFIX + ",candidate.gender" + FROM_CANDIDATE +
            ORDER_BY + "candidate.gender " + Direction.ASC + "," + ID_SORT, sql);
    }

    @Test
    @DisplayName("SQL generated from ordered request requiring join")
    void extractFetchSQLFromOrderedRequestRequiringJoin() {
        request.setSortFields(new String[] {"user.firstName"});
        request.setSortDirection(Direction.ASC);
        String sql = savedSearchService.extractFetchSQL(request, null, null, true);
        assertEquals(ORDERED_SELECT_PREFIX + ",users.first_name" + FROM_CANDIDATE +
            JOIN + CandidateSearchUtils.getTableJoin("users") +
            ORDER_BY + "users.first_name " + Direction.ASC + "," + ID_SORT, sql);
    }

    @Test
    @DisplayName("SQL generated from ordered request requiring join which filter has already joined")
    void extractFetchSQLFromOrderedRequestRequiringJoinWithFilter() {
        request.setPartnerIds(List.of(123L));
        request.setSortFields(new String[] {"user.lastName"});
        request.setSortDirection(Direction.ASC);
        String sql = savedSearchService.extractFetchSQL(request, null, null, true);
        assertEquals(ORDERED_SELECT_PREFIX + ",users.last_name" + FROM_CANDIDATE +
            JOIN + CandidateSearchUtils.getTableJoin("users") +
            WHERE + "users.partner_id in (123)" +
            ORDER_BY + "users.last_name " + Direction.ASC + "," + ID_SORT, sql);
    }

    @Test
    @DisplayName("SQL generated from ordered request requiring joins from filter and order")
    void extractFetchSQLFromOrderedRequestRequiringJoinsFromFilterAndOrder() {
        request.setPartnerIds(List.of(123L));
        request.setSortFields(new String[] {"nationality.name"});
        request.setSortDirection(Direction.ASC);
        String sql = savedSearchService.extractFetchSQL(request, null, null, true);
        assertEquals(ORDERED_SELECT_PREFIX + ",nationality.name" + FROM_CANDIDATE +
            JOIN + CandidateSearchUtils.getTableJoin("users") +
            JOIN + CandidateSearchUtils.getTableJoin("nationality") +
            WHERE + "users.partner_id in (123)" +
            ORDER_BY + "nationality.name " + Direction.ASC + "," + ID_SORT, sql);
    }

    @Test
    @DisplayName("SQL generated from ordered request on country")
    void extractFetchSQLFromOrderedRequestOnCountry() {
        request.setPartnerIds(List.of(1L));
        request.setSortFields(new String[] {"country.name"});
        request.setSortDirection(Direction.ASC);
        String sql = savedSearchService.extractFetchSQL(request, null, null, true);
        assertEquals(ORDERED_SELECT_PREFIX + ",country.name" + FROM_CANDIDATE +
            JOIN + CandidateSearchUtils.getTableJoin("users") +
            JOIN + CandidateSearchUtils.getTableJoin("country") +
            WHERE + "users.partner_id in (1)" +
            ORDER_BY + "country.name " + Direction.ASC + "," + ID_SORT, sql);
    }

    @Test
    @DisplayName("SQL generated from ordered request on partner abbreviation")
    void extractFetchSQLFromOrderedRequestOnPartnerAbbreviation() {
        request.setPartnerIds(List.of(1L));
        request.setSortFields(new String[] {"user.partner.abbreviation"});
        request.setSortDirection(Direction.ASC);
        String sql = savedSearchService.extractFetchSQL(request, null, null, true);
        assertEquals(ORDERED_SELECT_PREFIX + ",partner.abbreviation" + FROM_CANDIDATE +
            JOIN + CandidateSearchUtils.getTableJoin("users") +
            JOIN + CandidateSearchUtils.getTableJoin("partner") +
            WHERE + "users.partner_id in (1)" +
            ORDER_BY + "partner.abbreviation " + Direction.ASC + "," + ID_SORT, sql);
    }

    @Test
    @DisplayName("SQL generated from ordered request on partner abbreviation when user join is not needed")
    void extractFetchSQLFromOrderedRequestOnPartnerAbbreviationWithoutUserJoin() {
        request.setSortFields(new String[] {"user.partner.abbreviation"});
        request.setSortDirection(Direction.ASC);
        String sql = savedSearchService.extractFetchSQL(request, null, null, true);
        assertEquals(ORDERED_SELECT_PREFIX + ",partner.abbreviation" + FROM_CANDIDATE +
            JOIN + CandidateSearchUtils.getTableJoin("users") +
            JOIN + CandidateSearchUtils.getTableJoin("partner") +
            ORDER_BY + "partner.abbreviation " + Direction.ASC + "," + ID_SORT, sql);
    }

    @Test
    @DisplayName("SQL generated from local enumeration")
    void extractFetchSQLFromLocalEnumerationRequest() {
        Gender gender = Gender.male;
        request.setGender(gender);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " where candidate.gender = '" + gender.name() + "'", sql);
    }

    @Test
    @DisplayName("SQL generated from request needing join")
    void extractFetchSQLFromRequestNeedingJoin() {
        List<Long> partnerIds = new ArrayList<>();
        partnerIds.add(123L);
        partnerIds.add(456L);
        request.setPartnerIds(partnerIds);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT + " left join users on candidate.user_id = users.id"
            + " where users.partner_id in (123,456)", sql);
    }

    @Test
    @DisplayName("SQL generated from request needing multiple joins")
    void extractFetchSQLFromRequestNeedingJoins() {
        List<Long> partnerIds = new ArrayList<>();
        partnerIds.add(123L);
        partnerIds.add(456L);
        request.setPartnerIds(partnerIds);
        request.setMinEducationLevel(1);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals( UNORDERED_SELECT +
                " left join users on candidate.user_id = users.id"
                + " left join education_level on candidate.max_education_level_id = education_level.id"
                + " where users.partner_id in (123,456) and education_level.level >= 1"
            , sql);
    }

    @Test
    @DisplayName("SQL generated from major education request")
    void extractFetchSQLFromMajorEducationRequest() {
        List<Long> majorIds = new ArrayList<>();
        majorIds.add(123L);
        request.setEducationMajorIds(majorIds);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
                " left join candidate_education on candidate.id = candidate_education.candidate_id"
                + " where major_id in (123)"
            , sql);
    }

    @Test
    @DisplayName("SQL generated from list any request")
    void extractFetchSQLFromListAnyRequest() {
        List<Long> listAnyIds = List.of(123L, 456L);
        request.setListAnyIds(listAnyIds);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
                " where candidate.id in (select candidate_id from candidate_saved_list"
                + " where saved_list_id in (123,456))"
            , sql);

        request.setListAnySearchType(SearchType.not);
        sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
                " where not (candidate.id in (select candidate_id from candidate_saved_list"
                + " where saved_list_id in (123,456)))"
            , sql);
    }

    @Test
    @DisplayName("SQL generated from list all request")
    void extractFetchSQLFromListAllRequest() {
        List<Long> listAllIds = List.of(123L, 456L);
        request.setListAllIds(listAllIds);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
                " where candidate.id in (select candidate_id from candidate_saved_list"
                + " where saved_list_id = 123)"
                + " and candidate.id in (select candidate_id from candidate_saved_list"
                + " where saved_list_id = 456)"
            , sql);

        request.setListAllSearchType(SearchType.not);
        sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
                " where not (candidate.id in (select candidate_id from candidate_saved_list"
                + " where saved_list_id = 123)"
                + " and candidate.id in (select candidate_id from candidate_saved_list"
                + " where saved_list_id = 456))"
            , sql);
    }

    @Test
    @DisplayName("SQL generated from local nullable")
    void extractFetchSQLFromLocalNullableRequest() {
        request.setMiniIntakeCompleted(true);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " where mini_intake_completed_date is not null", sql);

        request.setMiniIntakeCompleted(false);
        sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " where mini_intake_completed_date is null", sql);

        request.setFullIntakeCompleted(false);
        sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " where mini_intake_completed_date is null and full_intake_completed_date is null", sql);
    }

    @Test
    @DisplayName("SQL generated from occupations")
    void extractFetchSQLFromOccupationsRequest() {
        List<Long> occupationIds = new ArrayList<>();
        occupationIds.add(9426L);
        request.setOccupationIds(occupationIds);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " left join candidate_occupation on candidate.id = candidate_occupation.candidate_id"
            + " where candidate_occupation.occupation_id in (9426)", sql);

        request.setMinYrs(1);
        sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " left join candidate_occupation on candidate.id = candidate_occupation.candidate_id"
            + " where candidate_occupation.occupation_id in (9426)"
            + " and candidate_occupation.years_experience >= 1", sql);

        request.setMaxYrs(5);
        sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " left join candidate_occupation on candidate.id = candidate_occupation.candidate_id"
            + " where candidate_occupation.occupation_id in (9426)"
            + " and candidate_occupation.years_experience >= 1"
            + " and candidate_occupation.years_experience <= 5", sql);
    }

    @Test
    @DisplayName("SQL generated from languages request and including pending terms")
    void extractFetchSQLFromLanguagesRequestAndIncludingPendingTerms() {
        request.setEnglishMinSpokenLevel(20);
        request.setIncludePendingTermsCandidates(false);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT
            + " where "
            + EXCLUDE_PENDING_TERMS_CLAUSE
            + SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID + ")"
            + " and exists ("
                + "select 1 from candidate_language "
                + "join language_level on language_level.id = spoken_level_id "
                + "where candidate_language.candidate_id = candidate.id and "
                + "candidate_language.language_id = 0 and language_level.level >= 20"
                + ")"
            , sql);

        request.setEnglishMinWrittenLevel(20);
        sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT
            + " where "
            + EXCLUDE_PENDING_TERMS_CLAUSE
            + SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID + ")"
                + " and exists ("
                + "select 1 from candidate_language "
                + "join language_level on language_level.id = spoken_level_id "
                + "where candidate_language.candidate_id = candidate.id and "
                + "candidate_language.language_id = 0 and language_level.level >= 20"
                + ")"
                + " and exists ("
                + "select 1 from candidate_language "
                + "join language_level on language_level.id = written_level_id "
                + "where candidate_language.candidate_id = candidate.id and "
                + "candidate_language.language_id = 0 and language_level.level >= 20"
                + ")"
            , sql);

        request.setOtherLanguageId(344L);
        request.setOtherMinSpokenLevel(30);
        sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT
            + " where "
            + EXCLUDE_PENDING_TERMS_CLAUSE
            + SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID + ")"
                + " and exists ("
                + "select 1 from candidate_language "
                + "join language_level on language_level.id = spoken_level_id "
                + "where candidate_language.candidate_id = candidate.id and "
                + "candidate_language.language_id = 0 and language_level.level >= 20"
                + ")"
                + " and exists ("
                + "select 1 from candidate_language "
                + "join language_level on language_level.id = written_level_id "
                + "where candidate_language.candidate_id = candidate.id and "
                + "candidate_language.language_id = 0 and language_level.level >= 20"
                + ")"
                + " and exists ("
                + "select 1 from candidate_language "
                + "join language_level on language_level.id = spoken_level_id "
                + "where candidate_language.candidate_id = candidate.id and "
                + "candidate_language.language_id = 344 and language_level.level >= 30"
                + ")"
            , sql);

    }

    @Test
    @DisplayName("SQL generated from local collection")
    void extractFetchSQLFromLocalCollectionRequest() {
        List<CandidateStatus> statuses = new ArrayList<>();
        statuses.add(CandidateStatus.active);
        statuses.add(CandidateStatus.pending);

        request.setStatuses(statuses);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " where candidate.status in ('active','pending')", sql);
    }

    @Test
    @DisplayName("SQL generated from local unhcr status collection")
    void extractFetchSQLFromLocalCollectionUnhcrStatusRequest() {
        List<UnhcrStatus> statuses = new ArrayList<>();
        statuses.add(UnhcrStatus.NoResponse);
        statuses.add(UnhcrStatus.MandateRefugee);

        request.setUnhcrStatuses(statuses);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " where candidate.unhcr_status in ('NoResponse','MandateRefugee')", sql);
    }

    @Test
    @DisplayName("SQL generated from excluded candidates")
    void extractFetchSQLFromExcludedCandidates() {
        List<Candidate> excluded = new ArrayList<>();
        Candidate candidate;
        candidate = new Candidate();
        candidate.setId(123L);
        excluded.add(candidate);
        candidate = new Candidate();
        candidate.setId(456L);
        excluded.add(candidate);
        String sql = savedSearchService.extractFetchSQL(request, null, excluded, false);
        assertEquals(UNORDERED_SELECT +
            " where candidate.id not in (123,456)", sql);
    }

    @Test
    @DisplayName("SQL generated from user source countries")
    void extractFetchSQLFromUserSourceCountries() {
        User user = new User();
        Set<Country> countries = new HashSet<>();
        Country country;
        country = new Country();
        country.setId(123L);
        countries.add(country);
        country = new Country();
        country.setId(456L);
        countries.add(country);
        user.setSourceCountries(countries);
        String sql = savedSearchService.extractFetchSQL(request, user, null, false);
        assertEquals(UNORDERED_SELECT +
            " where candidate.country_id in (456,123)", sql);
    }

    @Test
    @DisplayName("SQL generated from date")
    void extractFetchSQLFromDateRequest() {
        request.setLastModifiedFrom(LocalDate.parse("2019-01-01"));
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " where candidate.updated_date >= '2019-01-01T00:00Z'", sql);

        request.setLastModifiedFrom(LocalDate.parse("2019-01-01"));
        request.setTimezone("Australia/Brisbane");
        sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
            " where candidate.updated_date >= '2019-01-01T00:00+10:00'", sql);
    }

    //    @Test - modify for current date before running test
    @DisplayName("SQL generated from min age")
    void extractFetchSQLFromMinAgeRequest() {
        request.setMinAge(20);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
                " where (candidate.dob <= '2003-07-15' or candidate.dob is null)"
            , sql);
    }

    //    @Test - modify for current date before running test
    @DisplayName("SQL generated from max age")
    void extractFetchSQLFromMaxAgeRequest() {
        request.setMaxAge(20);
        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
                " where (candidate.dob > '2003-07-15' or candidate.dob is null)"
            , sql);
    }

    @Test
    @DisplayName("And together multiple filters")
    void extractFetchSQLFromMultipleFiltersRequest() {
        List<CandidateStatus> statuses = new ArrayList<>();
        statuses.add(CandidateStatus.active);
        statuses.add(CandidateStatus.pending);

        request.setStatuses(statuses);

        Gender gender = Gender.male;
        request.setGender(gender);

        String sql = savedSearchService.extractFetchSQL(request);
        assertEquals(UNORDERED_SELECT +
                " where candidate.status in ('active','pending') and candidate.gender = 'male'"
            , sql);
    }
}
