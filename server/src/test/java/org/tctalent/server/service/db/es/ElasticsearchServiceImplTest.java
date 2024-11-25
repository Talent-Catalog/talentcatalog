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
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
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
        candidateEsRepository.deleteByCandidateNumber("9999998");
        candidateEsRepository.deleteByCandidateNumber("9999999");

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

        candidateEsRepository.delete(candidate);
    }

    @Test
    void addElasticTermsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = elasticsearchService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        elasticsearchService.addAnd(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder);

        System.out.println(elasticsearchService.nativeQueryToJson(nq));
        SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(nq);

        assertTrue(searchHits.getTotalHits() > 0);
    }

    @Test
    void addElasticTermsFilterNegatedCompound() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = elasticsearchService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        elasticsearchService.addAnd(builder, nq);

        nq = elasticsearchService.makeRangeQuery(
            "candidateNumber", "12344", "12346");
        elasticsearchService.addAnd(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder);
        System.out.println(elasticsearchService.nativeQueryToJson(nq));

        SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(nq);

        assertTrue(searchHits.getTotalHits() > 0);
    }

    @Test
    void addElasticTermFilter() {

        NativeQuery nq;
        BoolQuery.Builder builder = new BoolQuery.Builder();

        nq = elasticsearchService.makeTermQuery("firstName.keyword", "Jim");
        elasticsearchService.addAnd(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder);
        SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(nq);

        assertTrue(searchHits.getTotalHits() > 0);
    }

    @Test
    void addElasticSimpleQueryStringFilter() {
    }

    @Test
    void addElasticExistsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();
        NativeQuery nq;

        nq = elasticsearchService.makeExistsQuery("firstName");
        elasticsearchService.addAnd(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder);
        SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(nq);

        assertTrue(searchHits.getTotalHits() > 0);

    }

    @Test
    void addElasticRangeFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();
        NativeQuery nq;
        nq = elasticsearchService.makeRangeQuery(
            "candidateNumber", "12344", "12346");
        elasticsearchService.addAnd(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder);
        SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(nq);

        assertTrue(searchHits.getTotalHits() > 0);
    }

    @Test
    void addElasticNestedFilterTerms() {
        NativeQuery nq;
        //Occupation name = Basket weaver and Years experience >= 4
        BoolQuery.Builder subQueryBuilder = new BoolQuery.Builder();
        //TODO JC Think we have to loop through occupations
        nq = elasticsearchService.makeTermsQuery(
            "occupations.name.keyword", List.of("Basket weaver", "Snake charmer"));
        elasticsearchService.addAnd(subQueryBuilder, nq);
        nq = elasticsearchService.makeRangeQuery(
            "occupations.yearsExperience", 4, null);

        elasticsearchService.addAnd(subQueryBuilder, nq);
        nq = elasticsearchService.makeNestedQuery("occupations", subQueryBuilder);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        elasticsearchService.addAnd(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder);

        SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(nq);

        //There may be many occupations at least with 4 years or more experience,
        //but there should only be two that are Basket weaver or Snake charmers (Jim Dim and Joe Blow)
        assertEquals(2, searchHits.getTotalHits());

        String expectJson = "Query: " + """
            {"bool":{"filter":[{"nested":{"path":"occupations","query":{"bool":{"should":[{"bool":{"filter":[{"terms":{"occupations.name.keyword":["Basket weaver","Snake charmer"]}},{"range":{"occupations.yearsExperience":{"gte":4}}}]}}]}}}}]}}""";
        assertEquals(expectJson, elasticsearchService.nativeQueryToJson(nq));
    }

    @Test
    void addElasticNestedFilterTerm() {

        NativeQuery nq;
        //Occupation name = Basket weaver and Years experience >= 4
        BoolQuery.Builder subQueryBuilder = new BoolQuery.Builder();
        nq = elasticsearchService.makeTermQuery(
            "occupations.name.keyword", "Basket weaver");
        elasticsearchService.addAnd(subQueryBuilder, nq);
        nq = elasticsearchService.makeRangeQuery(
            "occupations.yearsExperience", 4, null);

        elasticsearchService.addAnd(subQueryBuilder, nq);
        nq = elasticsearchService.makeNestedQuery("occupations", subQueryBuilder);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        elasticsearchService.addAnd(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder);
        SearchHits<CandidateEs> searchHits = elasticsearchService.searchCandidateEs(nq);

        //There are two occupations at least with 4 years or more experience (Jim Dim and Joe Blow),
        //but there should only be one Basket weaver
        assertEquals(1, searchHits.getTotalHits());
    }
}
