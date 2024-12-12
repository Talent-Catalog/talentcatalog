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
    public NativeQuery makeTermsQuery(String field,
        @NonNull Collection<Object> values) {

        //Construct the field values to be checked against
        TermsQueryField fieldValues = new TermsQueryField.Builder()
            .value(values.stream().map(FieldValue::of).toList())
            .build();
        //Build the native query
        return NativeQuery.builder()
            .withQuery(q -> q
                .terms(ma -> ma
                    .field(field)
                    .terms(fieldValues)
                )
            ).build();
    }

    @Override
    public void addAnd(BoolQuery.Builder builder, NativeQuery nq) {
        builder.filter(nq.getQuery());
    }

    @Override
    public void addOr(BoolQuery.Builder builder, NativeQuery nq) {
        builder.should(nq.getQuery());
    }

    @Override
    public NativeQuery not(NativeQuery nq) {
        BoolQuery.Builder builder = new BoolQuery.Builder();
        builder = builder.mustNot(nq.getQuery());
        return makeCompoundQuery(builder);
    }

    @NotNull
    @Override
    public NativeQuery makeTermQuery(String field, Object value) {
        //Build the native query
        return NativeQuery.builder()
            .withQuery(q -> q
                .term(ma -> ma
                    .field(field)
                    .value(FieldValue.of(value))
                )
            )
            .build();
    }

    @NotNull
    @Override
    public NativeQuery makeSimpleStringQuery(@NotNull String simpleQueryString) {
        return NativeQuery.builder()
            .withQuery(q -> q
                .simpleQueryString(ss -> ss
                    .query(simpleQueryString)
                )
            )
            .build();
    }

    @NotNull
    @Override
    public NativeQuery makeExistsQuery(@NotNull String field) {
        return NativeQuery.builder()
            .withQuery(q -> q
                .exists(ex -> ex
                    .field(field)
                )
            )
            .build();
    }

    @NotNull
    @Override
    public NativeQuery makeRangeQuery(
        @NotNull String field, @Nullable Object min, @Nullable Object max) {
        return NativeQuery.builder()
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
    }

    @NotNull
    @Override
    public NativeQuery makeNestedQuery(
        @NonNull String path, @NonNull BoolQuery.Builder nestedQueryBuilder) {
        return NativeQuery.builder()
            .withQuery(q -> q
                .nested(ne -> ne
                    .path(path)
                    .query(nestedQueryBuilder.build()._toQuery())
                )
            )
            .build();
    }

    @NonNull
    @Override
    public NativeQuery makeCompoundQueryWithPaging(
        BoolQuery.Builder builder, @Nullable PageRequest pageRequest) {

        NativeQueryBuilder nqBuilder = NativeQuery.builder()
            .withQuery(builder.build()._toQuery());

        if (pageRequest != null) {
            nqBuilder = nqBuilder.withPageable(pageRequest);
        }

        return nqBuilder.build();
    }

    @NotNull
    @Override
    public NativeQuery makeCompoundQuery(BoolQuery.Builder builder) {
        return makeCompoundQueryWithPaging(builder, null);
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
