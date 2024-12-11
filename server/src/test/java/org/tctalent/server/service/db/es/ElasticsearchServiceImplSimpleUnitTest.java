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

package org.tctalent.server.service.db.es;

import static org.junit.jupiter.api.Assertions.assertEquals;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

class ElasticsearchServiceImplSimpleUnitTest {
    ElasticsearchService esService;

    @BeforeEach
    void setUp() {
        esService = new ElasticsearchServiceImpl(null, null);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void makeElasticTermsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = esService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        esService.addAnd(builder, nq);

        nq = esService.makeCompoundQuery(builder);

        System.out.println(esService.nativeQueryToJson(nq));
    }

    @Test
    void makeElasticTermsFilterNegatedCompound() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = esService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        esService.addAnd(builder, nq);

        nq = esService.makeRangeQuery(
            "candidateNumber", "12344", "12346");
        esService.addAnd(builder, nq);

        nq = esService.makeCompoundQuery(builder);
        System.out.println(esService.nativeQueryToJson(nq));
    }

    @Test
    void makeElasticTermFilter() {

        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = esService.makeTermQuery("firstName.keyword", "Jim");
        esService.addAnd(builder, nq);

        nq = esService.makeCompoundQuery(builder);
        System.out.println(esService.nativeQueryToJson(nq));
    }

    @Test
    void addElasticSimpleQueryStringFilter() {
    }

    @Test
    void addElasticExistsFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = esService.makeExistsQuery("firstName");
        esService.addAnd(builder, nq);

        nq = esService.makeCompoundQuery(builder);

    }

    @Test
    void addElasticRangeFilter() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = esService.makeRangeQuery(
            "candidateNumber", "12344", "12346");
        esService.addAnd(builder, nq);

        nq = esService.makeCompoundQuery(builder);

        System.out.println(esService.nativeQueryToJson(nq));

    }

    @Test
    void makeElasticNestedFilterTerms() {

        NativeQuery nq;

        //Loop through occupation values or'ing together "name in values" and "experience > 4"
        BoolQuery.Builder disjunctionBuilder = new BoolQuery.Builder();

        final List<Object> values = List.of("Basket weaver", "Snake charmer");
        for (Object value : values) {
            //Loop through constructing queries
            BoolQuery.Builder conjunctionBuilder = new BoolQuery.Builder();
            nq = esService.makeTermQuery("occupations.name.keyword", value);
            esService.addAnd(conjunctionBuilder, nq);
            nq = esService.makeRangeQuery("occupations.yearsExperience", 4, null);
            esService.addAnd(conjunctionBuilder, nq);

            //Make the conjunction into a query
            //eg Occupation name = Basket weaver and Years experience >= 4
            nq = esService.makeCompoundQuery(conjunctionBuilder);
            //And "or" it into the disjunction builder
            esService.addOr(disjunctionBuilder, nq);
        }

        nq = esService.makeNestedQuery("occupations", disjunctionBuilder);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        esService.addAnd(builder, nq);

        nq = esService.makeCompoundQuery(builder);

        System.out.println(esService.nativeQueryToJson(nq));

        String expectJson = "Query: " + """
            {"bool":{"filter":[{"nested":{"path":"occupations","query":{"bool":{"should":[{"bool":{"filter":[{"term":{"occupations.name.keyword":{"value":"Basket weaver"}}},{"range":{"occupations.yearsExperience":{"gte":4}}}]}},{"bool":{"filter":[{"term":{"occupations.name.keyword":{"value":"Snake charmer"}}},{"range":{"occupations.yearsExperience":{"gte":4}}}]}}]}}}}]}}""";
        assertEquals(expectJson, esService.nativeQueryToJson(nq));
    }

    @Test
    void makeOtherLanguageNestedFilterTerms() {
        NativeQuery nq;
        
        BoolQuery.Builder nestedQueryBuilder = new BoolQuery.Builder();
        
        String otherLanguageName = "Arabic";
        int minSpoken = 40;
        int minWritten = 40;
        

        nq = esService.makeTermQuery(
            "otherLanguages.name.keyword", otherLanguageName);
        esService.addAnd(nestedQueryBuilder, nq);

        Integer minOtherSpokenLevel = minSpoken;
        if (minOtherSpokenLevel != null) {
            nq = esService.makeRangeQuery(
                "otherLanguages.minSpokenLevel", minOtherSpokenLevel, null);
            esService.addAnd(nestedQueryBuilder, nq);
        }

        Integer minOtherWrittenLevel = minWritten;
        if (minOtherWrittenLevel != null) {
            nq = esService.makeRangeQuery(
                "otherLanguages.minWrittenLevel", minOtherWrittenLevel, null);
            esService.addAnd(nestedQueryBuilder, nq);
        }

        nq = esService.makeNestedQuery("otherLanguages", nestedQueryBuilder);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        esService.addAnd(builder, nq);
        
        nq = esService.makeCompoundQuery(builder);
        System.out.println(esService.nativeQueryToJson(nq));

        String expectJson = "Query: " + """
            {"bool":{"filter":[{"nested":{"path":"otherLanguages","query":{"bool":{"filter":[{"term":{"otherLanguages.name.keyword":{"value":"Arabic"}}},{"range":{"otherLanguages.minSpokenLevel":{"gte":40}}},{"range":{"otherLanguages.minWrittenLevel":{"gte":40}}}]}}}}]}}""";
        assertEquals(expectJson, esService.nativeQueryToJson(nq));
        
    }

    @Test
    void makeElasticNestedFilterTerm() {

        NativeQuery nq;
        //Occupation name = Basket weaver and Years experience >= 4
        BoolQuery.Builder subQueryBuilder = new BoolQuery.Builder();
        nq = esService.makeTermQuery(
            "occupations.name.keyword", "Basket weaver");
        esService.addAnd(subQueryBuilder, nq);
        nq = esService.makeRangeQuery(
            "occupations.yearsExperience", 4, null);

        esService.addAnd(subQueryBuilder, nq);
        nq = esService.makeNestedQuery("occupations", subQueryBuilder);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        esService.addAnd(builder, nq);

        nq = esService.makeCompoundQuery(builder);
    }

    @Test
    void addAnd() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = esService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        System.out.println(esService.nativeQueryToJson(nq));

        esService.addAnd(builder, nq);

        nq = esService.not(nq);
        System.out.println(esService.nativeQueryToJson(nq));

        esService.addAnd(builder, nq);

        nq = esService.makeCompoundQuery(builder);
        System.out.println(esService.nativeQueryToJson(nq));

    }

    @Test
    void addOr() {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        NativeQuery nq;
        nq = esService.makeTermsQuery(
            "firstName.keyword", Collections.singleton("Jim"));
        System.out.println(esService.nativeQueryToJson(nq));

        esService.addOr(builder, nq);

        nq = esService.not(nq);
        System.out.println(esService.nativeQueryToJson(nq));

        esService.addOr(builder, nq);

        nq = esService.makeCompoundQuery(builder);
        System.out.println(esService.nativeQueryToJson(nq));

    }

    @Test
    void not() {

        NativeQuery nq;
        nq = esService.makeTermsQuery(
             "firstName.keyword", Collections.singleton("Jim"));
        System.out.println(esService.nativeQueryToJson(nq));

        nq = esService.not(nq);

        System.out.println(esService.nativeQueryToJson(nq));

    }
}
