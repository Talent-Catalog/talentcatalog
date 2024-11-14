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
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.json.JsonData;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.es.CandidateEs;
import org.tctalent.server.service.db.UserService;

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
 * {@link Query}. It has little functionality of its own - just serving as a bridge between
 * Spring and the Elasticsearch Java API. For example, you can't build complex queries from
 * NativeQuery's. All building is done with Java API and then just wrapped in a NativeQuery
 * as needed.
 *
 * @author sadatmalik
 * @author johncameron
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

  private static final String CANDIDATE_NUMBER_FIELD = "candidateNumber";
  private static final String EMAIL_FIELD = "email.keyword";
  private static final String EXTERNAL_ID_FIELD = "externalId.keyword";
  private static final String FULL_NAME_FIELD = "fullName";
  private static final String PHONE_NUMBER_FIELD = "phone.keyword";

  private static final String COUNTRY_KEYWORD = "country.keyword";
  private static final String STATUS_KEYWORD = "status.keyword";

  private final ElasticsearchOperations elasticsearchOperations;
  private final UserService userService;

  //todo revive
  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> findByNameWithLimit(@NonNull String name) {
    //TODO JC
    return new HashSet<>();
//    BoolQueryBuilder boolQuery = computeFindByNameQuery(name);
//    SearchHits<CandidateEs> hits = executeQuery(boolQuery);
//    LinkedHashSet<Long> candidateIds = extractCandidateIds(hits);
//
//    LogBuilder.builder(log)
//        .action("ElasticsearchServiceImpl.findByName")
//        .message("Found candidate IDs: " + candidateIds)
//        .logInfo();
//
//    return candidateIds;
  }

  //todo revive

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> findByNumberWithLimit(@NonNull String number) {
    //TODO JC
    return new HashSet<>();
//    BoolQueryBuilder boolQuery = computeFindByNumberQuery(number);
//    SearchHits<CandidateEs> hits = executeQuery(boolQuery);
//    LinkedHashSet<Long> candidateIds = extractCandidateIds(hits);
//
//    LogBuilder.builder(log)
//        .action("ElasticsearchServiceImpl.findByNumber")
//        .message("Found candidate IDs: " + candidateIds)
//        .logInfo();
//
//    return candidateIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> findByPhoneOrEmailWithLimit(@NonNull String number) {
    //TODO JC
    return new HashSet<>();
//    BoolQueryBuilder boolQuery = computeFindByPhoneOrEmailQuery(number);
//    SearchHits<CandidateEs> hits = executeQuery(boolQuery);
//    LinkedHashSet<Long> candidateIds = extractCandidateIds(hits);
//
//    LogBuilder.builder(log)
//        .action("ElasticsearchServiceImpl.findByPhoneOrEmail")
//        .message("Found candidate IDs: " + candidateIds)
//        .logInfo();
//
//    return candidateIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> findByExternalIdWithLimit(@NonNull String number) {
    //TODO JC
    return new HashSet<>();
//
//    BoolQueryBuilder boolQuery = computeFindByExternalIdQuery(number);
//    SearchHits<CandidateEs> hits = executeQuery(boolQuery);
//    LinkedHashSet<Long> candidateIds = extractCandidateIds(hits);
//
//    LogBuilder.builder(log)
//        .action("ElasticsearchServiceImpl.findByExternalId")
//        .message("Found candidate IDs: " + candidateIds)
//        .logInfo();
//
//    return candidateIds;
  }

  //TODO JC Support methods
//  @NotNull
//  private BoolQueryBuilder computeFindByNameQuery(String name) {
//    // Create match_bool_prefix query for name
//    MatchBoolPrefixQueryBuilder nameQuery = QueryBuilders
//        .matchBoolPrefixQuery(FULL_NAME_FIELD, name)
//        .operator(Operator.AND);
//
//    // Construct the boolean query
//    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
//        .must(nameQuery);
//
//    // Filter out deleted Statuses and account for country restrictions
//    boolQuery = filterOnDeletedStatus(boolQuery);
//    boolQuery = filterOnSourceCountryRestrictions(boolQuery);
//
//    LogBuilder.builder(log)
//        .action("ElasticsearchServiceImpl.computeFindByNameQuery")
//        .message("Constructed Elasticsearch query:\n " + boolQuery)
//        .logDebug();
//
//    return boolQuery;
//  }

//  @NotNull
//  private BoolQueryBuilder computeFindByNumberQuery(String number) {
//    // Create prefix query for candidate number
//    PrefixQueryBuilder cnQuery = QueryBuilders
//        .prefixQuery(CANDIDATE_NUMBER_FIELD, number);
//
//    // Construct the boolean query
//    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
//        .must(cnQuery);
//
//    // Filter out deleted Statuses and account for country restrictions
//    boolQuery = filterOnDeletedStatus(boolQuery);
//    boolQuery = filterOnSourceCountryRestrictions(boolQuery);
//
//    LogBuilder.builder(log)
//        .action("ElasticsearchServiceImpl.computeFindByNumberQuery")
//        .message("Constructed Elasticsearch query:\n " + boolQuery)
//        .logDebug();
//
//    return boolQuery;
//  }

//  @NotNull
//  private BoolQueryBuilder computeFindByPhoneOrEmailQuery(String input) {
//    // Add wildcard characters to the input for full wildcard matching
//    String wildcardInput = "*" + input + "*";
//
//    // Create wildcard query for phone number
//    WildcardQueryBuilder phoneQuery = QueryBuilders
//        .wildcardQuery(PHONE_NUMBER_FIELD, wildcardInput);
//
//    // Create wildcard query for email
//    WildcardQueryBuilder emailQuery = QueryBuilders
//        .wildcardQuery(EMAIL_FIELD, wildcardInput);
//
//    // Construct the boolean query with should clause
//    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
//        .should(phoneQuery)
//        .should(emailQuery)
//        .minimumShouldMatch(1);  // Ensures at least one should clause must match
//
//    // Filter out deleted Statuses and account for country restrictions
//    boolQuery = filterOnDeletedStatus(boolQuery);
//    boolQuery = filterOnSourceCountryRestrictions(boolQuery);
//
//    LogBuilder.builder(log)
//        .action("ElasticsearchServiceImpl.computeFindByPhoneOrEmailQuery")
//        .message("Constructed Elasticsearch query:\n " + boolQuery)
//        .logDebug();
//
//    return boolQuery;
//  }

//  @NotNull
//  private BoolQueryBuilder computeFindByExternalIdQuery(String externalId) {
//    // Create prefix query for external ID
//    PrefixQueryBuilder externalIdQuery = QueryBuilders
//        .prefixQuery(EXTERNAL_ID_FIELD, externalId);
//
//    // Construct the boolean query
//    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
//        .must(externalIdQuery);
//
//    // Filter out deleted Statuses and account for country restrictions
//    boolQuery = filterOnDeletedStatus(boolQuery);
//    boolQuery = filterOnSourceCountryRestrictions(boolQuery);
//
//    LogBuilder.builder(log)
//        .action("ElasticsearchServiceImpl.computeFindByExternalIdQuery")
//        .message("Constructed Elasticsearch query:\n " + boolQuery)
//        .logDebug();
//
//    return boolQuery;
//  }


//  @NotNull
//  private BoolQueryBuilder filterOnDeletedStatus(BoolQueryBuilder boolQuery) {
//    boolQuery = addElasticTermFilter(boolQuery,
//        SearchType.not,
//        STATUS_KEYWORD,
//        List.of(CandidateStatus.deleted)
//    );
//    return boolQuery;
//  }

//  @NotNull
//  private BoolQueryBuilder filterOnSourceCountryRestrictions(BoolQueryBuilder boolQuery) {
//    User user = userService.getLoggedInUser();
//    List<Object> countries = new ArrayList<>();
//    if (user != null && !Collections.isEmpty(user.getSourceCountries())){
//      for (Country country: user.getSourceCountries()) {
//        countries.add(country.getName());
//      }
//    }
//
//    if (!countries.isEmpty()) {
//      boolQuery = addElasticTermFilter(boolQuery,
//          SearchType.or,
//          COUNTRY_KEYWORD,
//          countries
//      );
//    }
//    return boolQuery;
//  }

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
  public BoolQuery.Builder addElasticRangeFilter(BoolQuery.Builder builder, String field, Object min, Object max) {
    NativeQuery query = NativeQuery.builder()
        .withQuery(q -> q
            .range(ra -> ra
                .field(field)
                .gte(JsonData.of(min))
                .lte(JsonData.of(max))
            )
        )
        .build();

    return builder.filter(query.getQuery());
  }

  @NotNull
  @Override
  public BoolQuery.Builder addElasticNestedFilter(BoolQuery.Builder builder, String path, Query nestedQuery) {
    NativeQuery query = NativeQuery.builder()
        .withQuery(q -> q
            .nested(ne -> ne
                .path(path)
                .query(nestedQuery)
            )
        )
        .build();

    return builder.filter(query.getQuery());
  }

  //todo revive
//  /**
//   * Returns the first few hits of the given query
//   * @param boolQuery Query
//   * @return the first page of hits (currently 10)
//   */
//  @NotNull
//  private SearchHits<CandidateEs> executeQuery(BoolQueryBuilder boolQuery) {
//    NativeSearchQuery query = new NativeSearchQueryBuilder()
//        .withQuery(boolQuery).withSorts(
//            SortBuilders.fieldSort("masterId").order(SortOrder.ASC)
//        )
//        .withPageable(PageRequest.of(0, 10))
//        .build();
//
//    return elasticsearchOperations.search(
//        query, CandidateEs.class, IndexCoordinates.of(CandidateEs.INDEX_NAME)
//    );
//  }

  @NotNull
  private static LinkedHashSet<Long> extractCandidateIds(SearchHits<CandidateEs> hits) {
    LinkedHashSet<Long> candidateIds = new LinkedHashSet<>();
    for (SearchHit<CandidateEs> hit : hits) {
      candidateIds.add(hit.getContent().getMasterId());
    }
    return candidateIds;
  }

}
