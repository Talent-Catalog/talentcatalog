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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.es.CandidateEs;
import org.tctalent.server.model.es.CandidateEs.Occupation;
import org.tctalent.server.repository.es.CandidateEsRepository;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
class ElasticsearchServiceImplTest {
    @Autowired
    ElasticsearchService elasticsearchService;

    @Autowired
    CandidateEsRepository candidateEsRepository;

    private CandidateEs testCandidate;
    private CandidateEs testCandidate2;

    @BeforeEach
    void setUp() {
        testCandidate = new CandidateEs();
        testCandidate.setCandidateNumber("9999998");
        testCandidate.setFirstName("Jim");
        testCandidate.setLastName("Dim");

        List<CandidateEs.Occupation> occupations = new ArrayList<>();
        Occupation occupation = new Occupation();
        occupation.setName("Basket weaver");
        occupation.setYearsExperience(4L);
        occupations.add(occupation);
        testCandidate.setOccupations(occupations);

        candidateEsRepository.save(testCandidate);

        testCandidate2 = new CandidateEs();
        testCandidate2.setCandidateNumber("9999999");
        testCandidate2.setFirstName("Joe");
        testCandidate2.setLastName("Blow");

        List<CandidateEs.Occupation> occupations2 = new ArrayList<>();
        Occupation occupation2 = new Occupation();
        occupation2.setName("Snake charmer");
        occupation2.setYearsExperience(4L);
        occupations2.add(occupation2);
        testCandidate2.setOccupations(occupations2);

        candidateEsRepository.save(testCandidate2);
    }

    @AfterEach
    void tearDown() {
        candidateEsRepository.delete(testCandidate);
        candidateEsRepository.delete(testCandidate2);
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
    }

    @Test
    void addElasticTermsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticTermsFilter(builder,
            null, "firstName.keyword", Collections.singleton("Jim"));

        SearchHits<CandidateEs> searchHits =
            elasticsearchService.searchCandidateEs(builder, null);

        assertTrue(searchHits.getTotalHits() > 0);
    }

    @Test
    void addElasticTermFilter() {

        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticTermFilter(builder,
            "firstName.keyword", "Jim");

        SearchHits<CandidateEs> searchHits =
            elasticsearchService.searchCandidateEs(builder, null);

        assertTrue(searchHits.getTotalHits() > 0);
    }

    @Test
    void addElasticSimpleQueryStringFilter() {
    }

    @Test
    void addElasticExistsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticExistsFilter(builder,
            null, "firstName");

        SearchHits<CandidateEs> searchHits =
            elasticsearchService.searchCandidateEs(builder, null);

        assertTrue(searchHits.getTotalHits() > 0);

    }

    @Test
    void addElasticRangeFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticRangeFilter(builder,
            "candidateNumber", "12344", "12346");

        SearchHits<CandidateEs> searchHits =
            elasticsearchService.searchCandidateEs(builder, null);

        assertTrue(searchHits.getTotalHits() > 0);
    }

    @Test
    void addElasticNestedFilterTerms() {

        //Occupation name = Basket weaver and Years experience >= 4
        BoolQuery.Builder subQueryBuilder = new BoolQuery.Builder();
        subQueryBuilder = elasticsearchService.addElasticTermsFilter(subQueryBuilder,
            null, "occupations.name.keyword",
            List.of("Basket weaver", "Snake charmer"));
        subQueryBuilder = elasticsearchService.addElasticRangeFilter(
            subQueryBuilder, "occupations.yearsExperience", 4, null);

        //Or together above matching occupations and experience
        //(Occupation = occ1 and Years exp1) or (Occupation = occ2 and Years exp2) or ...
        BoolQuery.Builder nestedQueryBuilder = new BoolQuery.Builder();
        nestedQueryBuilder = elasticsearchService.addElasticBooleanFilter(
            nestedQueryBuilder, SearchType.or, subQueryBuilder);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        builder = elasticsearchService.addElasticNestedFilter(
            builder,"occupations", nestedQueryBuilder);

        SearchHits<CandidateEs> searchHits =
            elasticsearchService.searchCandidateEs(builder, null);

        //There may be many occupations at least with 4 years or more experience,
        //but there should only be two that are Basket weaver or Snake charmers (Jim Dim and Joe Blow)
        assertEquals(2, searchHits.getTotalHits());
    }

    @Test
    void addElasticNestedFilterTerm() {

        //Occupation name = Basket weaver and Years experience >= 4
        BoolQuery.Builder subQueryBuilder = new BoolQuery.Builder();
        subQueryBuilder = elasticsearchService.addElasticTermFilter(subQueryBuilder,
             "occupations.name.keyword", "Basket weaver");
        subQueryBuilder = elasticsearchService.addElasticRangeFilter(
            subQueryBuilder, "occupations.yearsExperience", 4, null);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        builder = elasticsearchService.addElasticNestedFilter(
            builder,"occupations", subQueryBuilder);

        SearchHits<CandidateEs> searchHits =
            elasticsearchService.searchCandidateEs(builder, null);

        //There are two occupations at least with 4 years or more experience (Jim Dim and Joe Blow),
        //but there should only be one Basket weaver
        assertEquals(1, searchHits.getTotalHits());
    }
}
