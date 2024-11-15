/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.service.db.es;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.tctalent.server.model.es.CandidateEs;
import org.tctalent.server.repository.es.CandidateEsRepository;

@SpringBootTest
class ElasticsearchServiceImplTest {
    @Autowired
    ElasticsearchService elasticsearchService;

    @Autowired
    CandidateEsRepository candidateEsRepository;

    @BeforeEach
    void setUp() {
        CandidateEs candidate = new CandidateEs();
        candidate.setCandidateNumber("12345");
        candidate.setFirstName("Jim");
        candidate.setLastName("Dim");

        candidateEsRepository.save(candidate);
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void createCandidateEs() {
        CandidateEs candidate = new CandidateEs();
        candidate.setCandidateNumber("12345");
        candidate.setFirstName("Jim");
        candidate.setLastName("Dim");

        CandidateEs savedCandidate = candidateEsRepository.save(candidate);

        assertNotNull(savedCandidate);
        assertNotNull(savedCandidate.getId());

        assertEquals("Jim", savedCandidate.getFirstName());
        assertEquals("Dim", savedCandidate.getLastName());

        candidateEsRepository.delete(savedCandidate);
    }

    @Test
    void addElasticTermsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticTermsFilter(builder,
            null, "firstName.keyword", Collections.singleton("Jim"));

        NativeQuery query = NativeQuery.builder()
            .withQuery(builder.build()._toQuery())
            .build();

        System.out.println(elasticsearchService.convertNativeQueryToJson(query));

        final SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(query);

        assertTrue(searchHits.getTotalHits()>= 1);
    }

    @Test
    void addElasticTermFilter() {

        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticTermFilter(builder, "firstName.keyword", "Jim");

        NativeQuery query = NativeQuery.builder()
            .withQuery(builder.build()._toQuery())
            .build();

        System.out.println(elasticsearchService.convertNativeQueryToJson(query));

        final SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(query);

        assertTrue(searchHits.getTotalHits()>= 1);
    }

    @Test
    void addElasticSimpleQueryStringFilter() {
    }

    @Test
    void addElasticExistsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticExistsFilter(builder, null, "firstName");

        NativeQuery query = NativeQuery.builder()
            .withQuery(builder.build()._toQuery())
            .build();

        System.out.println(elasticsearchService.convertNativeQueryToJson(query));

        final SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(query);

        assertTrue(searchHits.getTotalHits() > 0);

    }

    @Test
    void addElasticRangeFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticRangeFilter(builder,
            "candidateNumber", "12344", "12346");

        NativeQuery query = NativeQuery.builder()
            .withQuery(builder.build()._toQuery())
            .build();

        System.out.println(elasticsearchService.convertNativeQueryToJson(query));

        final SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(query);

        assertTrue(searchHits.getTotalHits() > 0);
    }

    @Test
    void addElasticNestedFilter() {
    }
}
