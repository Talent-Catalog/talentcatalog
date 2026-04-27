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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.util.PagedCandidateBackProcessor;
import org.tctalent.server.util.background.PageContextBackRunner;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
class SavedSearchServiceImplTest {
    @Autowired
    private SavedSearchService savedSearchService;
    @Autowired
    private CandidateService candidateService;

    private SearchCandidateRequest request;
    private ThreadPoolTaskScheduler taskScheduler;


    @BeforeEach
    void setUp() {
        taskScheduler = new ThreadPoolTaskScheduler();
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
        runner.start(taskScheduler, backProcessor, 20, "Test");

    }

}
