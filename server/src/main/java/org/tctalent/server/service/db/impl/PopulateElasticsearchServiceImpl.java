/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.es.CandidateEsRepository;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.PopulateElasticsearchService;

@Service
public class PopulateElasticsearchServiceImpl implements PopulateElasticsearchService {
    private static final Logger log = LoggerFactory.getLogger(PopulateElasticsearchServiceImpl.class);

    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final CandidateEsRepository candidateEsRepository;

    @Autowired
    public PopulateElasticsearchServiceImpl(
            CandidateRepository candidateRepository,
            CandidateService candidateService, CandidateEsRepository candidateEsRepository) {
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.candidateEsRepository = candidateEsRepository;
    }

    @Async
    @Override
    public void populateElasticCandidates(
        boolean deleteExisting, boolean createElastic, Integer fromPage, Integer toPage) {
        if (deleteExisting) {
            log.info("Replace all candidates in Elasticsearch - deleting old candidates");
            candidateRepository.clearAllCandidateTextSearchIds();
            try {
                candidateEsRepository.deleteAll();
            } catch (Exception ex) {
                log.error("ElasticSearch deleteAll failed", ex);
            }
            log.info("Old candidates deleted.");
        }

        log.info("Loading candidates.");

        Instant overallStart = Instant.now();

        String verb = createElastic ? "added" : "updated";

        final int pageSize = 20;
        boolean logTotal = true;

        //Loop through pages (keeps memory requirements down - compared to
        //loading all candidates in one go)
        int count = 0;
        int nUpdated;
        int startPage = fromPage != null ? fromPage : 0;
        Pageable page = PageRequest.of(startPage, pageSize, Sort.by("id"));
        do {
            nUpdated = candidateService.populateElasticCandidates(page, logTotal, createElastic);
            logTotal = false;

            count += nUpdated;

            page = page.next();
            int pageNum = page.getPageNumber();
            if (pageNum % 5 == 0) {
                log.info(count + " candidates (up to page " + (pageNum-1) + ") " + verb
                        + " to Elasticsearch");
            }
        } while (nUpdated > 0 && (toPage == null || page.getPageNumber() <= toPage));

        log.info("Done: " + count + " candidates " + verb + " to Elasticsearch");

        Instant overallEnd = Instant.now();
        Duration overallTimeElapsed = Duration.between(overallStart, overallEnd);
      log.info("Overall the indexing took {} minutes.", overallTimeElapsed.toMinutes());
    }

    @Async
    @Override
    public void populateCandidateFromElastic() {

        log.info("Loading candidates.");

        final int pageSize = 20;

        //Loop through pages (keeps memory requirements down - compared to
        //loading all candidates in one go)
        int count = 0;
        int nUpdated;
        Pageable page = PageRequest.of(0, pageSize, Sort.by("id"));
        do {
            nUpdated = candidateService.populateCandidatesFromElastic(page);

            count += nUpdated;

            page = page.next();
            int pageNum = page.getPageNumber();
            if (pageNum % 5 == 0) {
                log.info(count + " candidates (" + pageNum + " pages) updated.");
            }
        } while (nUpdated > 0);

        log.info("Done: " + count + " candidates updated");
    }
}
