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

package org.tctalent.server.repository.es;

import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.client.erhlc.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.es.CandidateEs;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.util.html.JsoupTextExtracterImpl;
import org.tctalent.server.util.html.TextExtracter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
//@SpringBootTest
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
        TextExtracter textExtracter = new JsoupTextExtracterImpl();
        cand = candidateRepository.findByUserId(9710L);
        candes = new CandidateEs();
        candes.copy(cand, textExtracter);
        candidateEsRepository.save(candes);
    }

//    @AfterEach
//    public void after() {
//        candidateEsRepository.deleteAll();
//    }

//    @Transactional
//    @Test
    public void givenPersistedArticles_whenUseRegexQuery_thenRightArticlesFound() {
        FuzzyQuery qry = QueryBuilders.fuzzy(f -> f
            .field("candidateAttachments")
            .value("Condell")).fuzzy();

        final Query searchQuery = new NativeQueryBuilder()
            .withQuery(q -> q.fuzzy(qry))
            .build();

        final SearchHits<CandidateEs> cands = elasticsearchTemplate
                .search(searchQuery, CandidateEs.class, IndexCoordinates.of("jobs2"));

        assertEquals(1, cands.getTotalHits());
    }

//    @Transactional
//    @Test
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
