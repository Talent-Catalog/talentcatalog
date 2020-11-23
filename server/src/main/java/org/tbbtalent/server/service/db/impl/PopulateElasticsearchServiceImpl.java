/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.es.CandidateEsRepository;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.PopulateElasticsearchService;

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
    public void populateElasticCandidates(boolean deleteExisting) {
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
        String verb = deleteExisting ? "added" : "updated";

        final int pageSize = 20;
        boolean logTotal = true;

        //Loop through pages (keeps memory requirements down - compared to
        //loading all candidates in one go)
        int count = 0;
        int nUpdated;
        Pageable page = PageRequest.of(0, pageSize, Sort.by("id"));
        do {
            nUpdated = candidateService.populateElasticCandidates(page, logTotal, deleteExisting);
            logTotal = false;

            count += nUpdated;

            page = page.next();
            int pageNum = page.getPageNumber();
            if (pageNum % 5 == 0) {
                log.info(count + " candidates (" + pageNum + " pages) " + verb  
                        + " to Elasticsearch");
            }
        } while (nUpdated > 0);

        log.info("Done: " + count + " candidates " + verb + " to Elasticsearch");
    }
}
