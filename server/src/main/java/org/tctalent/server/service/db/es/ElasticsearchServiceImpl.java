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

import static org.tctalent.server.service.db.es.TCElasticHelpers.getTermsQuery;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import io.jsonwebtoken.lang.Collections;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.es.CandidateEs;
import org.tctalent.server.service.db.UserService;

/**
 * Implementation of {@link ElasticsearchService} that interacts with Elasticsearch to perform
 * search operations based on candidate names.
 * <p>
 * This service class handles the construction and execution of search queries, filtering based on
 * deletion status and source country restrictions, and extracts candidate IDs from search hits.
 *
 * @author sadatmalik
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

  private static final String CANDIDATE_NUMBER_FIELD = "candidateNumber";
  private static final String EMAIL_FIELD = "email";
  private static final String EXTERNAL_ID_FIELD = "externalId";
  private static final String FULL_NAME_FIELD = "fullName";
  private static final String PHONE_NUMBER_FIELD = "phone";

  private static final String COUNTRY_KEYWORD = "country.keyword";
  private static final String STATUS_KEYWORD = "status.keyword";

  private final ElasticsearchOperations elasticsearchOperations;
  private final UserService userService;

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> findByName(@NonNull String name) {
    BoolQuery.Builder bqb = computeFindByNameQuery(name);
    Query boolQuery = bqb.build()._toQuery();
    SearchHits<CandidateEs> hits = executeQuery(boolQuery);
    LinkedHashSet<Long> candidateIds = extractCandidateIds(hits);

    log.info("Found candidate IDs: " + candidateIds);

    return candidateIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> findByNumber(@NonNull String number) {
    BoolQueryBuilder boolQuery = computeFindByNumberQuery(number);
    SearchHits<CandidateEs> hits = executeQuery(boolQuery);
    LinkedHashSet<Long> candidateIds = extractCandidateIds(hits);

    log.info("Found candidate IDs: " + candidateIds);

    return candidateIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> findByPhoneOrEmail(@NonNull String number) {
    BoolQueryBuilder boolQuery = computeFindByPhoneOrEmailQuery(number);
    SearchHits<CandidateEs> hits = executeQuery(boolQuery);
    LinkedHashSet<Long> candidateIds = extractCandidateIds(hits);

    log.info("Found candidate IDs: " + candidateIds);

    return candidateIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> findByExternalId(@NonNull String number) {
    BoolQueryBuilder boolQuery = computeFindByExternalIdQuery(number);
    SearchHits<CandidateEs> hits = executeQuery(boolQuery);
    LinkedHashSet<Long> candidateIds = extractCandidateIds(hits);

    log.info("Found candidate IDs: " + candidateIds);

    return candidateIds;
  }

  @NotNull
  private BoolQuery.Builder computeFindByNameQuery(String name) {
    // Create match_bool_prefix query for name
    Query nameQuery = QueryBuilders.matchBoolPrefix()
        .field(FULL_NAME_FIELD)
        .query(name)
        .operator(Operator.And)
        .build()
        ._toQuery();

    // Construct the boolean query
    BoolQuery.Builder boolQuery = QueryBuilders.bool()
        .must(nameQuery);

    // Filter out deleted Statuses and account for country restrictions
    boolQuery = filterOnDeletedStatus(boolQuery.build()._toQuery());
    boolQuery = filterOnSourceCountryRestrictions(boolQuery.build()._toQuery());

    log.info("Elasticsearch query:\n" + boolQuery.build()._toQuery());
    return boolQuery;
  }

  @NotNull
  private BoolQueryBuilder computeFindByNumberQuery(String number) {
    // Create prefix query for candidate number
    PrefixQueryBuilder cnQuery = QueryBuilders
        .prefixQuery(CANDIDATE_NUMBER_FIELD, number);

    // Construct the boolean query
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
        .must(cnQuery);

    // Filter out deleted Statuses and account for country restrictions
    boolQuery = filterOnDeletedStatus(boolQuery);
    boolQuery = filterOnSourceCountryRestrictions(boolQuery);

    log.debug("Elasticsearch query:\n" + boolQuery);
    return boolQuery;
  }

  @NotNull
  private BoolQueryBuilder computeFindByPhoneOrEmailQuery(String input) {
    // Create prefix query for phone number
    PrefixQueryBuilder phoneQuery = QueryBuilders
        .prefixQuery(PHONE_NUMBER_FIELD, input);

    // Create prefix query for email
    PrefixQueryBuilder emailQuery = QueryBuilders
        .prefixQuery(EMAIL_FIELD, input);

    // Construct the boolean query with should clause
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
        .should(phoneQuery)
        .should(emailQuery)
        .minimumShouldMatch(1);  // Ensures at least one should clause must match

    // Filter out deleted Statuses and account for country restrictions
    boolQuery = filterOnDeletedStatus(boolQuery);
    boolQuery = filterOnSourceCountryRestrictions(boolQuery);

    log.debug("Elasticsearch query:\n" + boolQuery);
    return boolQuery;
  }

  @NotNull
  private BoolQueryBuilder computeFindByExternalIdQuery(String externalId) {
    // Create prefix query for external ID
    PrefixQueryBuilder externalIdQuery = QueryBuilders
        .prefixQuery(EXTERNAL_ID_FIELD, externalId);

    // Construct the boolean query
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
        .must(externalIdQuery);

    // Filter out deleted Statuses and account for country restrictions
    boolQuery = filterOnDeletedStatus(boolQuery);
    boolQuery = filterOnSourceCountryRestrictions(boolQuery);

    log.debug("Elasticsearch query:\n" + boolQuery);
    return boolQuery;
  }


  @NotNull
  private BoolQuery.Builder filterOnDeletedStatus(Query boolQuery) {
    BoolQuery.Builder bqb = QueryBuilders.bool().filter(boolQuery);

    bqb = bqb.filter(getTermsQuery(
        SearchType.not,
        STATUS_KEYWORD,
        List.of(CandidateStatus.deleted)
    ));
    return bqb;
  }

  @NotNull
  private BoolQuery.Builder filterOnSourceCountryRestrictions(Query boolQuery) {
    User user = userService.getLoggedInUser();
    List<Object> countries = new ArrayList<>();
    if (user != null && !Collections.isEmpty(user.getSourceCountries())){
      for (Country country: user.getSourceCountries()) {
        countries.add(country.getName());
      }
    }

    BoolQuery.Builder bqb = QueryBuilders.bool().filter(boolQuery);
    if (!countries.isEmpty()) {
      bqb = bqb.filter(getTermsQuery(
          SearchType.or,
          COUNTRY_KEYWORD,
          countries
      ));
    }
    return bqb;
  }

  @NotNull
  private SearchHits<CandidateEs> executeQuery(Query boolQuery) {
    NativeQuery query = new NativeQueryBuilder()
        .withQuery(boolQuery)
        .withSort(Sort.by("masterId").descending())
        .withPageable(PageRequest.of(0, 10))
        .build();

    return elasticsearchOperations.search(
        query, CandidateEs.class, IndexCoordinates.of(CandidateEs.INDEX_NAME)
    );
  }

  @NotNull
  private static LinkedHashSet<Long> extractCandidateIds(SearchHits<CandidateEs> hits) {
    LinkedHashSet<Long> candidateIds = new LinkedHashSet<>();
    for (SearchHit<CandidateEs> hit : hits) {
      candidateIds.add(hit.getContent().getMasterId());
    }
    return candidateIds;
  }

}
