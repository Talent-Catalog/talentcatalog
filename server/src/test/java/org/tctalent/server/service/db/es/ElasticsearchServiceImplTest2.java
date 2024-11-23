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

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

class ElasticsearchServiceImplTest2 {
    ElasticsearchService elasticsearchService;

    @BeforeEach
    void setUp() {
        elasticsearchService = new ElasticsearchServiceImpl(null, null);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void makeElasticTermsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = elasticsearchService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder, null);

        System.out.println(elasticsearchService.nativeQueryToJson(nq));
    }

    @Test
    void makeElasticTermsFilterNegatedCompound() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = elasticsearchService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.makeRangeQuery(
            "candidateNumber", "12344", "12346");
        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder, null);
        System.out.println(elasticsearchService.nativeQueryToJson(nq));
    }

    @Test
    void makeElasticTermFilter() {

        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = elasticsearchService.makeTermQuery("firstName.keyword", "Jim");
        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder, null);
        System.out.println(elasticsearchService.nativeQueryToJson(nq));
    }

    @Test
    void addElasticSimpleQueryStringFilter() {
    }

    @Test
    void addElasticExistsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = elasticsearchService.makeExistsQuery("firstName");
        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder, null);

    }

    @Test
    void addElasticRangeFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = elasticsearchService.makeRangeQuery(
            "candidateNumber", "12344", "12346");
        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder, null);

        System.out.println(elasticsearchService.nativeQueryToJson(nq));

    }

    @Test
    void makeElasticNestedFilterTerms() {

        NativeQuery nq;
        //Occupation name = Basket weaver and Years experience >= 4
        BoolQuery.Builder subQueryBuilder = new BoolQuery.Builder();
        //TODO JC Think we have to loop through occupations
        nq = elasticsearchService.makeTermsQuery(
            "occupations.name.keyword", List.of("Basket weaver", "Snake charmer"));
        elasticsearchService.addConjunction(subQueryBuilder, nq);
        nq = elasticsearchService.makeRangeQuery(
            "occupations.yearsExperience", 4, null);

        elasticsearchService.addConjunction(subQueryBuilder, nq);
        nq = elasticsearchService.makeNestedQuery("occupations", subQueryBuilder);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder, null);

        String expectJson = "Query: " + """
            {"bool":{"filter":[{"nested":{"path":"occupations","query":{"bool":{"should":[{"bool":{"filter":[{"terms":{"occupations.name.keyword":["Basket weaver","Snake charmer"]}},{"range":{"occupations.yearsExperience":{"gte":4}}}]}}]}}}}]}}""";
        assertEquals(expectJson, elasticsearchService.nativeQueryToJson(nq));
    }

    @Test
    void makeElasticNestedFilterTerm() {

        NativeQuery nq;
        //Occupation name = Basket weaver and Years experience >= 4
        BoolQuery.Builder subQueryBuilder = new BoolQuery.Builder();
        nq = elasticsearchService.makeTermQuery(
            "occupations.name.keyword", "Basket weaver");
        elasticsearchService.addConjunction(subQueryBuilder, nq);
        nq = elasticsearchService.makeRangeQuery(
            "occupations.yearsExperience", 4, null);

        elasticsearchService.addConjunction(subQueryBuilder, nq);
        nq = elasticsearchService.makeNestedQuery("occupations", subQueryBuilder);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder, null);
    }

    @Test
    void addConjunction() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = elasticsearchService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        System.out.println(elasticsearchService.nativeQueryToJson(nq));

        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.negate(nq);
        System.out.println(elasticsearchService.nativeQueryToJson(nq));

        elasticsearchService.addConjunction(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder, null);
        System.out.println(elasticsearchService.nativeQueryToJson(nq));

    }

    @Test
    void addDisjunction() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = elasticsearchService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        System.out.println(elasticsearchService.nativeQueryToJson(nq));

        elasticsearchService.addDisjunction(builder, nq);

        nq = elasticsearchService.negate(nq);
        System.out.println(elasticsearchService.nativeQueryToJson(nq));

        elasticsearchService.addDisjunction(builder, nq);

        nq = elasticsearchService.makeCompoundQuery(builder, null);
        System.out.println(elasticsearchService.nativeQueryToJson(nq));

    }

    @Test
    void negate() {

        NativeQuery nq;
        nq = elasticsearchService.makeTermsQuery(
             "firstName.keyword", Collections.singleton("Jim"));
        System.out.println(elasticsearchService.nativeQueryToJson(nq));

        nq = elasticsearchService.negate(nq);

        System.out.println(elasticsearchService.nativeQueryToJson(nq));

    }
}
