/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.request.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;

class SearchCandidateRequestTest {

    private SearchCandidateRequest request;

    @BeforeEach
    void setUp() {
        request = new SearchCandidateRequest();
    }

    @Test
    @DisplayName("SQL generated from empty request")
    void extractSQLFromEmptyRequest() {
        String sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate", sql);
    }

    @Test
    @DisplayName("SQL generated from request needing join")
    void extractSQLFromRequestNeedingJoin() {
        List<Long> partnerIds = new ArrayList<>();
        partnerIds.add(123L);
        partnerIds.add(456L);
        request.setPartnerIds(partnerIds);
        String sql = request.extractSQL(true);
        assertEquals(
            "select distinct candidate.id from candidate "
                + "left join users on candidate.user_id = users.id "
                + "where users.partner_id in (123,456)", sql);
    }

    @Test
    @DisplayName("SQL generated from request needing multiple joins")
    void extractSQLFromRequestNeedingJoins() {
        List<Long> partnerIds = new ArrayList<>();
        partnerIds.add(123L);
        partnerIds.add(456L);
        request.setPartnerIds(partnerIds);
        request.setMinEducationLevel(1);
        String sql = request.extractSQL(true);
        assertEquals(
            "select distinct candidate.id from candidate "
                + "left join users on candidate.user_id = users.id "
                + "left join education_level on candidate.max_education_level_id = education_level.id "
                + "where users.partner_id in (123,456) and education_level.level >= 1"
                , sql);
    }

    @Test
    @DisplayName("SQL generated from major education request")
    void extractSQLFromMajorEducationRequest() {
        List<Long> majorIds = new ArrayList<>();
        majorIds.add(123L);
        request.setEducationMajorIds(majorIds);
        String sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
                + "left join candidate_education on candidate.id = candidate_education.candidate_id "
                + "where (major_id in (123) or migration_education_major_id in (123))"
                , sql);
    }

    @Test
    @DisplayName("SQL generated from local enumeration")
    void extractSQLFromLocalEnumerationRequest() {
        Gender gender = Gender.male;
        request.setGender(gender);
        String sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "where candidate.gender = '" + gender.name() + "'", sql);
    }

    @Test
    @DisplayName("SQL generated from local nullable")
    void extractSQLFromLocalNullableRequest() {
        request.setMiniIntakeCompleted(true);
        String sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "where mini_intake_completed_date is not null", sql);

        request.setMiniIntakeCompleted(false);
        sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "where mini_intake_completed_date is null", sql);

        request.setFullIntakeCompleted(false);
        sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "where mini_intake_completed_date is null and full_intake_completed_date is null", sql);
    }

    @Test
    @DisplayName("SQL generated from occupations")
    void extractSQLFromOccupationsRequest() {
        List<Long> occupationIds = new ArrayList<>();
        occupationIds.add(9426L);
        request.setOccupationIds(occupationIds);
        String sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "left join candidate_occupation on candidate.id = candidate_occupation.candidate_id "
            + "where candidate_occupation.occupation_id in (9426)", sql);

        request.setMinYrs(1);
        sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "left join candidate_occupation on candidate.id = candidate_occupation.candidate_id "
            + "where candidate_occupation.occupation_id in (9426) "
            + "and candidate_occupation.years_experience >= 1", sql);

        request.setMaxYrs(5);
        sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "left join candidate_occupation on candidate.id = candidate_occupation.candidate_id "
            + "where candidate_occupation.occupation_id in (9426) "
            + "and candidate_occupation.years_experience >= 1 "
            + "and candidate_occupation.years_experience <= 5", sql);
    }

    @Test
    @DisplayName("SQL generated from languages request")
    void extractSQLFromLanguagesRequest() {
        request.setEnglishMinSpokenLevel(2);
        String sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "left join candidate_language on candidate.id = candidate_language.candidate_id "
            + "left join language on candidate_language.language_id = language.id "
            + "left join language_level as spoken_level on candidate_language.spoken_level_id = spoken_level_id "
            + "where lower(language.name) = 'english' and spoken_level.level >= 2", sql);

        request.setEnglishMinWrittenLevel(2);
        sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "left join candidate_language on candidate.id = candidate_language.candidate_id "
            + "left join language on candidate_language.language_id = language.id "
            + "left join language_level as spoken_level on candidate_language.spoken_level_id = spoken_level_id "
            + "left join language_level as written_level on candidate_language.written_level_id = written_level_id "
            + "where lower(language.name) = 'english' and spoken_level.level >= 2 "
            + "and written_level.level >= 2", sql);

        request.setOtherLanguageId(344L);
        request.setOtherMinSpokenLevel(3);
        sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "left join candidate_language on candidate.id = candidate_language.candidate_id "
            + "left join language on candidate_language.language_id = language.id "
            + "left join language_level as spoken_level on candidate_language.spoken_level_id = spoken_level_id "
            + "left join language_level as written_level on candidate_language.written_level_id = written_level_id "
            + "where lower(language.name) = 'english' and spoken_level.level >= 2 "
            + "and written_level.level >= 2 "
            + "and candidate_language.language_id = 344 and spoken_level.level >= 3", sql);

    }

    @Test
    @DisplayName("SQL generated from local collection")
    void extractSQLFromLocalCollectionRequest() {
        List<CandidateStatus> statuses = new ArrayList<>();
        statuses.add(CandidateStatus.active);
        statuses.add(CandidateStatus.pending);

        request.setStatuses(statuses);
        String sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "where candidate.status in ('active','pending')", sql);
    }

    @Test
    @DisplayName("SQL generated from local unhcr status collection")
    void extractSQLFromLocalCollectionUnhcrStatusRequest() {
        List<UnhcrStatus> statuses = new ArrayList<>();
        statuses.add(UnhcrStatus.NoResponse);
        statuses.add(UnhcrStatus.MandateRefugee);

        request.setUnhcrStatuses(statuses);
        String sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
            + "where candidate.unhcr_status in ('NoResponse','MandateRefugee')", sql);
    }

    @Test
    @DisplayName("SQL generated from excluded candidates")
    void extractSQLFromExcludedCandidates() {
        List<Candidate> excluded = new ArrayList<>();
        Candidate candidate;
        candidate = new Candidate();
        candidate.setId(123L);
        excluded.add(candidate);
        candidate = new Candidate();
        candidate.setId(456L);
        excluded.add(candidate);
        String sql = request.extractSQL(true, null, excluded);
        assertEquals("select distinct candidate.id from candidate "
            + "where not candidate.id in (123,456)", sql);
    }

    @Test
    @DisplayName("SQL generated from user source countries")
    void extractSQLFromUserSourceCountries() {
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
        String sql = request.extractSQL(true, user, null);
        assertEquals("select distinct candidate.id from candidate "
            + "where candidate.country_id in (456,123)", sql);
    }

    @Test
    @DisplayName("SQL generated from date")
    void extractSQLFromDateRequest() {
        request.setLastModifiedFrom(LocalDate.parse("2019-01-01"));
        String sql = request.extractSQL(true);
        assertEquals(
            "select distinct candidate.id from candidate "
                + "where candidate.updated_date >= '2019-01-01T00:00Z'"
            , sql);

        request.setLastModifiedFrom(LocalDate.parse("2019-01-01"));
        request.setTimezone("Australia/Brisbane");
        sql = request.extractSQL(true);
        assertEquals(
            "select distinct candidate.id from candidate "
                + "where candidate.updated_date >= '2019-01-01T00:00+10:00'"
            , sql);
    }

//    @Test - modify for current date before running test
    @DisplayName("SQL generated from min age")
    void extractSQLFromMinAgeRequest() {
        request.setMinAge(20);
        String sql = request.extractSQL(true);
        assertEquals(
            "select distinct candidate.id from candidate "
                + "where (candidate.dob <= '2003-07-15' or candidate.dob is null)"
            , sql);
    }

    //    @Test - modify for current date before running test
    @DisplayName("SQL generated from max age")
    void extractSQLFromMaxAgeRequest() {
        request.setMaxAge(20);
        String sql = request.extractSQL(true);
        assertEquals(
            "select distinct candidate.id from candidate "
                + "where (candidate.dob > '2003-07-15' or candidate.dob is null)"
            , sql);
    }

    @Test
    @DisplayName("And together multiple filters")
    void extractSQLFromMultipleFiltersRequest() {
        List<CandidateStatus> statuses = new ArrayList<>();
        statuses.add(CandidateStatus.active);
        statuses.add(CandidateStatus.pending);

        request.setStatuses(statuses);

        Gender gender = Gender.male;
        request.setGender(gender);

        String sql = request.extractSQL(true);
        assertEquals("select distinct candidate.id from candidate "
                + "where candidate.status in ('active','pending') and candidate.gender = 'male'"
            , sql);
    }

}
