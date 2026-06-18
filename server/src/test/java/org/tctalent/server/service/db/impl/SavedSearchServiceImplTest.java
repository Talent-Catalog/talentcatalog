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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.tctalent.server.configuration.SystemAdminConfiguration;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.util.PagedCandidateBackProcessor;
import org.tctalent.server.util.background.PageContextBackRunner;

@SpringBootTest
class SavedSearchServiceImplTest extends BaseDBIntegrationTest {
    @Autowired
    private SavedSearchService savedSearchService;
    @Autowired
    private CandidateService candidateService;

    private SearchCandidateRequest request;
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    private SavedSearchServiceImpl savedSearchServiceImpl;

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @BeforeEach
    void setUp() {
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("saved-search-test-");
        taskScheduler.initialize();
        request = new SearchCandidateRequest();
    }

    @Test
    void searchCandidatesTwoWays() {
        request.setGender(Gender.male);
        final Page<Candidate> candidatesByCriteriaAPI = savedSearchService.searchCandidates(request);

        String sql = savedSearchService.extractFetchSQL(request);
        Set<Long> candidatesBySQL = candidateService.searchCandidatesUsingSql(sql);

        assertEquals(candidatesByCriteriaAPI.getTotalElements(), candidatesBySQL.size());

    }

    @Test
    void testPagedCandidateBackProcessor() {

        SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();

        PagedCandidateBackProcessor backProcessor =
            new PagedCandidateBackProcessor( "Logger action",
                searchCandidateRequest, candidateService, savedSearchService) {
            @Override
            protected void processCandidates(
                CandidateService candidateService, List<Candidate> candidates) {
                //TODO JC Call candidate service method to process the candidates
            }
        };

        PageContextBackRunner runner = new PageContextBackRunner();

        assertDoesNotThrow(() -> runner.start(taskScheduler, backProcessor, 20, "Test"));
    }

    @AfterEach
    void tearDown() {
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }


    @Test
    void extractFetchSqlIncludesBasicCandidateFilters() {
        request.setSavedSearchId(1L);
        request.setStatuses(List.of(CandidateStatus.active, CandidateStatus.pending));
        request.setCandidateNumbers(List.of("  CN-001  ", "CN'002", ""));
        request.setGender(Gender.female);
        request.setUnhcrStatuses(List.of(UnhcrStatus.MandateRefugee, UnhcrStatus.NotRegistered));
        request.setIncludePendingTermsCandidates(true);

        String sql = savedSearchService.extractFetchSQL(request);

        assertTrue(sql.startsWith("select distinct candidate.id from candidate"));
        assertTrue(sql.contains("candidate.status in ('active','pending')"));
        assertTrue(sql.contains("candidate.candidate_number in ('CN-001','CN''002')"));
        assertTrue(sql.contains("candidate.gender = 'female'"));
        assertTrue(sql.contains("candidate.unhcr_status in ('MandateRefugee','NotRegistered')"));
    }

    @Test
    void extractFetchSqlExcludesPendingTermsCandidatesByDefault() {
        request.setSavedSearchId(1L);

        String sql = savedSearchService.extractFetchSQL(request);

        assertTrue(sql.contains(
            "candidate.id not in (select candidate_id from candidate_saved_list where saved_list_id = "
                + SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID + ")"
        ));
    }

    @Test
    void extractFetchSqlDoesNotExcludePendingTermsCandidatesWhenRequested() {
        request.setSavedSearchId(1L);
        request.setIncludePendingTermsCandidates(true);

        String sql = savedSearchService.extractFetchSQL(request);

        assertFalse(sql.contains(
            "candidate.id not in (select candidate_id from candidate_saved_list where saved_list_id = "
                + SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID + ")"
        ));
    }

    @Test
    void extractFetchSqlIncludesOccupationExperienceFilters() {
        request.setSavedSearchId(1L);
        request.setOccupationIds(List.of(11L, 12L));
        request.setMinYrs(2);
        request.setMaxYrs(7);
        request.setIncludePendingTermsCandidates(true);

        String sql = savedSearchService.extractFetchSQL(request);

        assertTrue(sql.contains("left join candidate_occupation"));
        assertTrue(sql.contains("candidate_occupation.occupation_id in (11,12)"));
        assertTrue(sql.contains("candidate_occupation.years_experience >= 2"));
        assertTrue(sql.contains("candidate_occupation.years_experience <= 7"));
    }

    @Test
    void extractFetchSqlIncludesNationalityAndCountrySearchTypeFilters() {
        request.setSavedSearchId(1L);
        request.setNationalityIds(List.of(21L, 22L));
        request.setNationalitySearchType(SearchType.not);
        request.setCountryIds(List.of(31L, 32L));
        request.setCountrySearchType(SearchType.or);
        request.setIncludePendingTermsCandidates(true);

        String sql = savedSearchService.extractFetchSQL(request);

        assertTrue(sql.contains("candidate.nationality_id not in (21,22)"));
        assertTrue(sql.contains("candidate.country_id in (31,32)"));
    }

    @Test
    void extractFetchSqlIncludesSavedListFilters() {
        request.setSavedSearchId(1L);
        request.setListAnyIds(List.of(101L, 102L));
        request.setListAnySearchType(SearchType.or);
        request.setListAllIds(List.of(201L, 202L));
        request.setListAllSearchType(SearchType.not);
        request.setIncludePendingTermsCandidates(true);

        String sql = savedSearchService.extractFetchSQL(request);

        assertTrue(sql.contains(
            "candidate.id in (select candidate_id from candidate_saved_list where saved_list_id in (101,102))"
        ));
        assertTrue(sql.contains(
            "not (candidate.id in (select candidate_id from candidate_saved_list where saved_list_id = 201)"
        ));
        assertTrue(sql.contains(
            "candidate.id in (select candidate_id from candidate_saved_list where saved_list_id = 202)"
        ));
    }

    @Test
    void extractFetchSqlIncludesEducationFilters() {
        request.setSavedSearchId(1L);
        request.setMinEducationLevel(3);
        request.setMaxEducationLevel(5);
        request.setEducationMajorIds(List.of(301L, 302L));
        request.setIncludePendingTermsCandidates(true);

        String sql = savedSearchService.extractFetchSQL(request);

        assertTrue(sql.contains("left join education_level"));
        assertTrue(sql.contains("education_level.level >= 3"));
        assertTrue(sql.contains("education_level.level <= 5"));
        assertTrue(sql.contains("left join candidate_education"));
        assertTrue(sql.contains("major_id in (301,302)"));
    }

    @Test
    void extractFetchSqlIncludesTextAndUtmFilters() {
        request.setSavedSearchId(1L);
        request.setSimpleQueryString("software developer");
        request.setRegoReferrerParam(" PartnerReferral ");
        request.setRegoUtmCampaign(" CampaignA ");
        request.setRegoUtmSource(" SourceA ");
        request.setRegoUtmMedium(" MediumA ");
        request.setIncludePendingTermsCandidates(true);

        String sql = savedSearchService.extractFetchSQL(request);

        assertTrue(sql.contains("@@"));
        assertTrue(sql.contains("lower(candidate.rego_referrer_param) like 'partnerreferral'"));
        assertTrue(sql.contains("lower(candidate.rego_utm_campaign) like 'campaigna'"));
        assertTrue(sql.contains("lower(candidate.rego_utm_source) like 'sourcea'"));
        assertTrue(sql.contains("lower(candidate.rego_utm_medium) like 'mediuma'"));
    }

    @Test
    void extractFetchSqlIncludesBooleanCompletionFilters() {
        request.setSavedSearchId(1L);
        request.setMiniIntakeCompleted(true);
        request.setFullIntakeCompleted(false);
        request.setPotentialDuplicate(true);
        request.setIncludePendingTermsCandidates(true);

        String sql = savedSearchService.extractFetchSQL(request);

        assertTrue(sql.contains("mini_intake_completed_date is not null"));
        assertTrue(sql.contains("full_intake_completed_date is null"));
        assertTrue(sql.contains("candidate.potential_duplicate = true"));
    }

    @Test
    void statusListConvertersRoundTrip() {
        String statusList = savedSearchServiceImpl.getStatusListAsString(
            List.of(CandidateStatus.active, CandidateStatus.pending)
        );

        assertEquals("active,pending", statusList);
        assertEquals(
            List.of(CandidateStatus.active, CandidateStatus.pending),
            savedSearchServiceImpl.getStatusListFromString(statusList)
        );
    }

    @Test
    void statusListConvertersHandleNull() {
      assertNull(savedSearchServiceImpl.getStatusListAsString(null));
      assertNull(savedSearchServiceImpl.getStatusListFromString(null));
    }

    @Test
    void unhcrStatusListConvertersRoundTrip() {
        String statusList = savedSearchServiceImpl.getUnhcrStatusListAsString(
            List.of(UnhcrStatus.MandateRefugee, UnhcrStatus.NotRegistered)
        );

        assertEquals("MandateRefugee,NotRegistered", statusList);
        assertEquals(
            List.of(UnhcrStatus.MandateRefugee, UnhcrStatus.NotRegistered),
            savedSearchServiceImpl.getUnhcrStatusListFromString(statusList)
        );
    }

    @Test
    void unhcrStatusListConvertersHandleNull() {
      assertNull(savedSearchServiceImpl.getUnhcrStatusListAsString(null));
      assertNull(savedSearchServiceImpl.getUnhcrStatusListFromString(null));
    }
}
