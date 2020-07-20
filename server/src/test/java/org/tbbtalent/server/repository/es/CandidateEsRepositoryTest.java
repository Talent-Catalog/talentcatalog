/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.es;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.es.CandidateEs;
import org.tbbtalent.server.repository.db.CandidateRepository;

import static org.elasticsearch.index.query.QueryBuilders.fuzzyQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@SpringBootTest
public class CandidateEsRepositoryTest {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateEsRepository candidateEsRepository;

    @Transactional
    @BeforeEach
    void before() {
        CandidateEs candes;
        Candidate cand;
        
        cand = candidateRepository.findByUserIdLoadText(9710L);
        candes = new CandidateEs(cand);
        candidateEsRepository.save(candes);
    }

//    @AfterEach
//    public void after() {
//        candidateEsRepository.deleteAll();
//    }

    @Transactional
    @Test
    public void givenPersistedArticles_whenUseRegexQuery_thenRightArticlesFound() {
        final Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(fuzzyQuery("candidateAttachments", "Condell"))
                .build();

        final SearchHits<CandidateEs> cands = elasticsearchTemplate
                .search(searchQuery, CandidateEs.class, IndexCoordinates.of("jobs2"));

        assertEquals(1, cands.getTotalHits());
    }

    @Transactional
    @Test
    public void testSimpleQuery() {
        Page<CandidateEs> candidates = candidateEsRepository
                .simpleQueryString("sql + html", 
                        PageRequest.of(0, 20, 
                                Sort.by(Sort.Direction.DESC, "masterId")));

        assertNotEquals(0, candidates.getTotalElements());

        for (CandidateEs candidateEs : candidates) {
            
        }
    }
    
}
