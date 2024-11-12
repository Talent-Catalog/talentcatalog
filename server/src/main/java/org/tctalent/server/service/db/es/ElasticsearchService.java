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
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.util.Collection;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.SearchType;


public interface ElasticsearchService {

  //TODO JC
  @NotNull
  Builder addElasticNestedFilter(Builder builder, String path, Query nestedQuery);

  /**
   * Adds a terms filter to the query builder.
   * @param builder Elastic Java API BoolQuery builder
   * @param searchType Type of search - Only 'not' is checked.
   * @param field Field to check against
   * @param values comparison values
   * @return Updated builder - with filter added according to searchType
   */
  @NotNull
  BoolQuery.Builder addElasticTermsFilter(
      BoolQuery.Builder builder, @Nullable SearchType searchType, String field,
      Collection<Object> values);

  //TODO JC
  @NotNull
  BoolQuery.Builder addElasticTermFilter(BoolQuery.Builder builder, String field, Object value);

  //TODO JC
  @NotNull
  BoolQuery.Builder addElasticSimpleQueryStringFilter(
      BoolQuery.Builder builder, @NonNull String simpleQueryString);

  //TODO JC
  @NotNull
  BoolQuery.Builder addElasticExistsFilter(
      BoolQuery.Builder builder, @Nullable SearchType searchType, @NonNull String field);

  //TODO JC
  @NotNull
  BoolQuery.Builder addElasticRangeFilter(
      BoolQuery.Builder builder, String field, @Nullable Object min, @Nullable Object max);

  /**
   * Retrieves the first few candidate IDs by elastic searching for a specified name.
   *
   * @param name the full name to search for in the Elasticsearch index. Must not be null.
   * @return a {@link Set} of {@link Long} candidate IDs that match the search criteria.
   *         The set will be empty if no candidates are found.
   * @throws IllegalArgumentException if the provided name is null.
   */
  Set<Long> findByNameWithLimit(@NonNull String name);

  /**
   * Retrieves the first few candidate IDs by elastic searching for a specified candidate number.
   *
   * @param number the candidate number to search for in the Elasticsearch index. Must not be null.
   * @return a {@link Set} of {@link Long} candidate IDs that match the search criteria.
   *         The set will be empty if no candidates are found.
   * @throws IllegalArgumentException if the provided name is null.
   */
  Set<Long> findByNumberWithLimit(@NonNull String number);

  /**
   * Retrieves the first few candidate IDs by elastic searching for a specified input string
   * that matches either the phone number or email in the Elasticsearch index.
   *
   * @param input the input string to search for in the Elasticsearch index. Must not be null.
   * @return a {@link Set} of {@link Long} candidate IDs that match the search criteria.
   *         The set will be empty if no candidates are found.
   * @throws IllegalArgumentException if the provided input string is null.
   */
  Set<Long> findByPhoneOrEmailWithLimit(@NonNull String input);

  /**
   * Retrieves the first few candidate IDs by elastic searching for a specified external ID
   *
   * @param externalId the external ID string to search for in the Elasticsearch index. Must not be
   *                  null.
   * @return a {@link Set} of {@link Long} candidate IDs that match the search criteria.
   *         The set will be empty if no candidates are found.
   * @throws IllegalArgumentException if the provided input string is null.
   */
  Set<Long> findByExternalIdWithLimit(@NonNull String externalId);

}
