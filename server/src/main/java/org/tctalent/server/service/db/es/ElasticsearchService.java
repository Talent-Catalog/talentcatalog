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

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.es.CandidateEs;


public interface ElasticsearchService {

  /**
   * Adds a nested query to the Boolean query builder.
   * @param builder Elastic Java API BoolQuery builder
   * @param path Path to the nested query
   * @param nestedQueryBuilder Allows a boolean nested query - represented as another builder -
   *                           to be added to the given Boolean builder. The nested query is built,
   *                           and its result is used to construct the nested filter.
   * @return Updated builder
   */
  @NonNull
  BoolQuery.Builder addElasticNestedFilter(
      BoolQuery.Builder builder, String path, BoolQuery.Builder nestedQueryBuilder);

  /**
   * Adds a terms filter to the Boolean query builder. The terms are combined according
   * to the searchType
   * @param builder Elastic Java API BoolQuery builder
   * @param searchType Type of search - default is SearchType.and if null.
   * @param field Field to check against
   * @param values comparison values
   * @return Updated builder - with filter added according to searchType
   */
  @NonNull
  BoolQuery.Builder addElasticTermsFilter(
      BoolQuery.Builder builder, @Nullable SearchType searchType, String field,
      Collection<Object> values);

  /**
   * Adds a boolean query to the Boolean query builder.
   * @param builder Elastic Java API BoolQuery builder
   * @param searchType May be null, in which case default is SearchType.and which translates to
   *                   "filter", but it can also be SearchType.or which translates to "should".
   *                   SearchType.not is not supported - it is ignored and default is used.
   * @param subQueryBuilder Allows a boolean subquery - represented as another builder - to be added
   *                        to the given Boolean builder. The subquery is built, and its result
   *                        added to the filter.
   * @return Updated builder - with filter added according to searchType
   */
  @NonNull
  BoolQuery.Builder addElasticBooleanFilter(
      BoolQuery.Builder builder, @Nullable SearchType searchType, BoolQuery.Builder subQueryBuilder);

  /**
   * Adds a single term filter to the Boolean query builder.
   * @param builder Elastic Java API BoolQuery builder
   * @param field Field to check against
   * @param value comparison value
   * @return Updated builder
   */
  @NonNull
  BoolQuery.Builder addElasticTermFilter(BoolQuery.Builder builder, String field, Object value);

  /**
   * Adds a simple query string filter to the Boolean query builder.
   * @param builder Elastic Java API BoolQuery builder
   * @param simpleQueryString Query string which defines the query filter
   * @return Updated builder
   */
  @NonNull
  BoolQuery.Builder addElasticSimpleQueryStringFilter(
      BoolQuery.Builder builder, @NonNull String simpleQueryString);

  /**
   * Adds an exists filter to the Boolean query builder.
   * @param builder Elastic Java API BoolQuery builder
   * @param searchType Type of search - default is "SearchType.and" if null.
   * @param field Field to check whether it exists
   * @return Updated builder - with filter added according to searchType
   */
  @NonNull
  BoolQuery.Builder addElasticExistsFilter(
      BoolQuery.Builder builder, @Nullable SearchType searchType, @NonNull String field);

  /**
   * Adds a range filter to the Boolean query builder.
   * @param builder Elastic Java API BoolQuery builder
   * @param field Field to check against
   * @param min Minimum value (inclusive)
   * @param max Maximum value (inclusive)
   * @return Updated builder
   */
  @NonNull
  BoolQuery.Builder addElasticRangeFilter(
      BoolQuery.Builder builder, String field, @Nullable Object min, @Nullable Object max);

  /**
   * Searches for CandidateEs objects matching the given query
   * @param builder BoolQuery builder containing query to execute
   * @param pageRequest Optional page request containing sort and and paging info
   * @return Results
   */
  @NonNull
  SearchHits<CandidateEs> searchCandidateEs(
      BoolQuery.Builder builder, @Nullable PageRequest pageRequest);

}
