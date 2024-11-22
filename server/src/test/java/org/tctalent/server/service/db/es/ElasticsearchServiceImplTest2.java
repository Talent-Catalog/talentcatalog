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
import org.tctalent.server.model.db.SearchType;

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
    void addElasticTermsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticTermsFilter(builder,
            null, "firstName.keyword", Collections.singleton("Jim"));

        NativeQuery nativeQuery =
            elasticsearchService.constructNativeQuery(builder, null);

        System.out.println(elasticsearchService.nativeQueryToJson(nativeQuery));
    }

    @Test
    void addElasticTermsFilterNegatedCompound() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticTermsFilter(builder,
            null, "firstName.keyword", Collections.singleton("Jim"));

        builder = elasticsearchService.addElasticRangeFilter(builder,
            "candidateNumber", "12344", "12346");

        NativeQuery nativeQuery =
            elasticsearchService.constructNativeQuery(builder, null);
    }

    @Test
    void addElasticTermFilter() {

        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticTermFilter(builder,
            "firstName.keyword", "Jim");

        NativeQuery nativeQuery =
            elasticsearchService.constructNativeQuery(builder, null);
    }

    @Test
    void addElasticSimpleQueryStringFilter() {
    }

    @Test
    void addElasticExistsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticExistsFilter(builder,
            null, "firstName");

        NativeQuery nativeQuery =
            elasticsearchService.constructNativeQuery(builder, null);

    }

    @Test
    void addElasticRangeFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        builder = elasticsearchService.addElasticRangeFilter(builder,
            "candidateNumber", "12344", "12346");

        NativeQuery nativeQuery =
            elasticsearchService.constructNativeQuery(builder, null);

        System.out.println(elasticsearchService.nativeQueryToJson(nativeQuery));

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

        NativeQuery nativeQuery =
            elasticsearchService.constructNativeQuery(builder, null);

        String expectJson = "Query: " + """
            {"bool":{"filter":[{"nested":{"path":"occupations","query":{"bool":{"should":[{"bool":{"filter":[{"terms":{"occupations.name.keyword":["Basket weaver","Snake charmer"]}},{"range":{"occupations.yearsExperience":{"gte":4}}}]}}]}}}}]}}""";
        assertEquals(expectJson, elasticsearchService.nativeQueryToJson(nativeQuery));
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

        NativeQuery nativeQuery =
            elasticsearchService.constructNativeQuery(builder, null);
    }
}
