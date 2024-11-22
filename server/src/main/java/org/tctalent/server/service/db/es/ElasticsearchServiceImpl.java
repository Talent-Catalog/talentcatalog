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

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.json.JsonData;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.es.CandidateEs;
import org.tctalent.server.security.AuthService;

/**
 * Implementation of {@link ElasticsearchService} that interacts with Elasticsearch to perform
 * search operations based on candidate names.
 * <p>
 * This service class handles the construction and execution of search queries, filtering based on
 * deletion status and source country restrictions, and extracts candidate IDs from search hits.
 * <p/>
 * Converted to use new Spring NativeQuery together with Elasticsearch's Java API.
 * <p/>
 * Note that {@link NativeQuery} is just a wrapper for a single Elasticsearch Java API
 * {@link Query}. It has little functionality of its own - just serving as a bridge between Spring
 * and the Elasticsearch Java API. For example, you can't build complex queries from NativeQuery's.
 * All building is done with Java API and then just wrapped in a NativeQuery as needed.
 *
 * @author sadatmalik
 * @author johncameron
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final AuthService authService;

    @NonNull
    @Override
    public BoolQuery.Builder addElasticBooleanFilter(
        BoolQuery.Builder builder, @Nullable SearchType searchType, BoolQuery.Builder subQueryBuilder) {

        return SearchType.or.equals(searchType)
            ? builder.should(subQueryBuilder.build()._toQuery())
            : builder.filter(subQueryBuilder.build()._toQuery());
    }

    @NotNull
    public BoolQuery.Builder addElasticTermsFilter(
        BoolQuery.Builder builder, @Nullable SearchType searchType, String field,
        Collection<Object> values) {

        final int nValues = values.size();
        if (nValues > 0) {

            //Construct the field values to be checked against
            TermsQueryField fieldValues = new TermsQueryField.Builder()
                .value(values.stream().map(FieldValue::of).toList())
                .build();
            //Build the native query
            NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                    .terms(ma -> ma
                        .field(field)
                        .terms(fieldValues)
                    )
                )
                .build();

            if (searchType == SearchType.not) {
                builder = builder.mustNot(query.getQuery());
            } else if (searchType == SearchType.or) {
                builder = builder.should(query.getQuery());
            } else {
                builder = builder.filter(query.getQuery());
            }
        }
        return builder;
    }

    @NotNull
    public BoolQuery.Builder addElasticTermFilter(
        BoolQuery.Builder builder, String field, Object value) {

        //Build the native query
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q
                .term(ma -> ma
                    .field(field)
                    .value(FieldValue.of(value))
                )
            )
            .build();

        return builder.filter(query.getQuery());
    }

    @NotNull
    @Override
    public BoolQuery.Builder addElasticSimpleQueryStringFilter(
        BoolQuery.Builder builder, @NonNull String simpleQueryString) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q
                .simpleQueryString(ss -> ss
                    .query(simpleQueryString)
                )
            )
            .build();

        return builder.filter(query.getQuery());
    }

    @NotNull
    @Override
    public BoolQuery.Builder addElasticExistsFilter(
        BoolQuery.Builder builder, @Nullable SearchType searchType, @NonNull String field) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q
                .exists(ex -> ex
                    .field(field)
                )
            )
            .build();

        if (searchType == SearchType.not) {
            builder = builder.mustNot(query.getQuery());
        } else {
            builder = builder.filter(query.getQuery());
        }

        return builder;
    }

    @NotNull
    @Override
    public BoolQuery.Builder addElasticRangeFilter(BoolQuery.Builder builder, String field,
        Object min, Object max) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q
                .range(ra -> {
                        Builder build = ra.field(field);
                        if (min != null) {
                            build.gte(JsonData.of(min));
                        }
                        if (max != null) {
                            build.lte(JsonData.of(max));
                        }
                        return build;
                    }
                )
            )
            .build();

        return builder.filter(query.getQuery());
    }

    @NotNull
    @Override
    public BoolQuery.Builder addElasticNestedFilter(
        BoolQuery.Builder builder, String path, BoolQuery.Builder nestedQueryBuilder) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q
                .nested(ne -> ne
                    .path(path)
                    .query(nestedQueryBuilder.build()._toQuery())
                )
            )
            .build();

        return builder.filter(query.getQuery());
    }

    @NonNull
    @Override
    public NativeQuery constructNativeQuery(
        BoolQuery.Builder builder, @Nullable PageRequest pageRequest) {

        NativeQueryBuilder nqBuilder = NativeQuery.builder()
            .withQuery(builder.build()._toQuery());

        if (pageRequest != null) {
            nqBuilder = nqBuilder.withPageable(pageRequest);
        }

        return nqBuilder.build();
    }

    @Nullable
    @Override
    public String nativeQueryToJson(@Nullable NativeQuery nativeQuery) {
        String json;
        if (nativeQuery == null) {
            json = null;
        } else {
            Query query = nativeQuery.getQuery();
            json = query == null ? null : query.toString();
        }
        return json;
    }

    @NonNull
    @Override
    public SearchHits<CandidateEs> searchCandidateEs(NativeQuery nativeQuery) {

        // Convert query to a compact JSON string
        String queryAsJson = nativeQueryToJson(nativeQuery);

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("searchCandidateEs")
            .message("Elasticsearch query: " + queryAsJson)
            .logInfo();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("searchCandidateEs")
            .message("Elasticsearch sort: " + nativeQuery.getPageable())
            .logInfo();

        return elasticsearchOperations.search(
            nativeQuery, CandidateEs.class, IndexCoordinates.of(CandidateEs.INDEX_NAME));
    }
}
