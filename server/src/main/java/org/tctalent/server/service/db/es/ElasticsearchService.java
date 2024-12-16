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

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.es.CandidateEs;

/**
 * Service for applying boolean queries to an Elasticsearch server.
 */
public interface ElasticsearchService {

  /**
   * Adds the given query to the given builder by adding it with a boolean "and".
   * @param builder Builder of a boolean query
   * @param nq Query to add
   */
  void addAnd(BoolQuery.Builder builder, NativeQuery nq);

  /**
   * Adds the given query to the given builder by adding it with a boolean "or".
   * @param builder Builder of a boolean query
   * @param nq Query to add
   */
  void addOr(BoolQuery.Builder builder, NativeQuery nq);

  /**
   * Make a query using the given builder.
   * <p/>
   * The builder is built (build is called) and its result is used to construct the query.
   * @param builder Builder used to create query
   * @return Native query
   */
  @NonNull
  NativeQuery makeCompoundQuery(BoolQuery.Builder builder);

  /**
   * Make a query using the given builder and optional PageRequest.
   * <p/>
   * The builder is built (build is called) and its result is used to construct the query.
   * @param builder Builder used to create query
   * @param pageRequest Optional PageRequest used to create query
   * @return Native query
   */
  @NonNull
  NativeQuery makeCompoundQueryWithPaging(BoolQuery.Builder builder, @Nullable PageRequest pageRequest);

  /**
   * Make an exists query.
   * @param field Field to check whether it exists
   * @return Native query
   */
  @NonNull
  NativeQuery makeExistsQuery(@NonNull String field);

  /**
   * Make a nested query.
   * @param path Path to the nested query
   * @param nestedQueryBuilder The nested query is created from this builder. This builder is built
   *                           (build is called) and its result is used to construct the nested
   *                           query.
   * @return Native query
   */
  @NonNull
  NativeQuery makeNestedQuery(@NonNull String path, @NonNull BoolQuery.Builder nestedQueryBuilder);

  /**
   * Make a range query.
   * @param field Field to check against
   * @param min Minimum value (inclusive)
   * @param max Maximum value (inclusive)
   * @return Native query
   */
  @NonNull
  NativeQuery makeRangeQuery(@NonNull String field, @Nullable Object min, @Nullable Object max);

  /**
   * Make a simple string query.
   * @param simpleQueryString Query string which defines the query
   * @return Native query
   */
  @NonNull
  NativeQuery makeSimpleStringQuery(@NonNull String simpleQueryString);

  /**
   * Make a term query.
   * @param field Field to check against
   * @param value comparison value
   * @return Native query
   */
  @NonNull
  NativeQuery makeTermQuery(String field, Object value);

  /**
   * Make a terms query.
   * @param field Field to check against
   * @param values comparison values
   * @return Native query
   */
  @NonNull
  NativeQuery makeTermsQuery(String field, Collection<Object> values);

  /**
   * Extracts the JSON query (if any) from the given native query.
   * @param nativeQuery Native query
   * @return JSON corresponding to underlying query - null if no query found
   */
  @Nullable
  String nativeQueryToJson(@Nullable NativeQuery nativeQuery);

  /**
   * Creates a query which is the negation of the given query.
   * ie "not" the result of the given query
   * @param nq a query
   * @return Native query which is "not" the given query.
   */
  NativeQuery not(NativeQuery nq);

  /**
   * Searches for CandidateEs objects matching the given native query
   * @param nativeQuery Native query to be executed
   * @return Results
   */
  @NonNull
  SearchHits<CandidateEs> searchCandidateEs(NativeQuery nativeQuery);
}
