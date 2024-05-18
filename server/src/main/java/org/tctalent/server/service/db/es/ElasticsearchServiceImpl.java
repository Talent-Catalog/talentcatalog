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

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpDeserializer;
import co.elastic.clients.json.JsonpMapper;
import com.google.gson.Gson;
import io.jsonwebtoken.lang.Collections;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria.Operator;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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

  private static final String STATUS_KEYWORD = "status.keyword";
  private static final String COUNTRY_KEYWORD = "country.keyword";

  private final ElasticsearchOperations elasticsearchOperations;
  private final UserService userService;

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> findByName(@NonNull String name) {
    BoolQueryBuilder boolQuery = computeFindByNameQuery(name);
    SearchHits<CandidateEs> hits = executeQuery(boolQuery);
    LinkedHashSet<Long> candidateIds = extractCandidateIds(hits);

    log.info("Found candidate IDs: " + candidateIds);

    return candidateIds;
  }

  @NotNull
  private BoolQueryBuilder computeFindByNameQuery(String name) {
    // Create match_bool_prefix query for name
    MatchBoolPrefixQueryBuilder nameQuery = QueryBuilders
        .matchBoolPrefixQuery("fullName", name)
        .operator(Operator.AND);

    // Construct the boolean query
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
        .must(nameQuery);

    // Filter out deleted Statuses and account for country restrictions
    boolQuery = filterOnDeletedStatus(boolQuery);
    boolQuery = filterOnSourceCountryRestrictions(boolQuery);

    log.info("Elasticsearch query:\n" + boolQuery);
    return boolQuery;
  }

  @NotNull
  private BoolQueryBuilder filterOnDeletedStatus(BoolQueryBuilder boolQuery) {
    boolQuery = addElasticTermFilter(boolQuery,
        SearchType.not,
        STATUS_KEYWORD,
        List.of(CandidateStatus.deleted)
    );
    return boolQuery;
  }

  @NotNull
  private BoolQueryBuilder filterOnSourceCountryRestrictions(BoolQueryBuilder boolQuery) {
    User user = userService.getLoggedInUser();
    List<Object> countries = new ArrayList<>();
    if (user != null && !Collections.isEmpty(user.getSourceCountries())){
      for (Country country: user.getSourceCountries()) {
        countries.add(country.getName());
      }
    }

    if (!countries.isEmpty()) {
      boolQuery = addElasticTermFilter(boolQuery,
          SearchType.or,
          COUNTRY_KEYWORD,
          countries
      );
    }
    return boolQuery;
  }

  @NotNull
  private BoolQueryBuilder addElasticTermFilter(
      BoolQueryBuilder builder, @Nullable SearchType searchType, String field,
      List<Object> values) {
    final int nValues = values.size();
    if (nValues > 0) {
      QueryBuilder queryBuilder;
      if (nValues == 1) {
        queryBuilder = QueryBuilders.termQuery(field, values.get(0));
      } else {
        queryBuilder = QueryBuilders.termsQuery(field, values.toArray());
      }
      if (searchType == SearchType.not) {
        builder = builder.mustNot(queryBuilder);
      } else {
        builder = builder.filter(queryBuilder);
      }
    } return builder;
  }

  // TODO (delete, replaced).
  @NotNull
  private SearchHits<CandidateEs> executeQuery(BoolQueryBuilder boolQuery) {

    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(boolQuery).withSorts(
            SortBuilders.fieldSort("masterId").order(SortOrder.ASC)
        )
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

  /*
   * Replacement for executeQuery(BoolQueryBuilder boolQuery)
   */
  @NotNull
  private SearchHits<CandidateEs> runQuery(Query boolQuery) {
    NativeQuery query = new NativeQueryBuilder()
        .withQuery(boolQuery)
        .withSort(s -> s
            .field(
            FieldSort.of(f -> f
                .field("masterId")
                .order(SortOrder.Desc))
        ))
        .withPageable(PageRequest.of(0, 10))
        .build();
    return elasticsearchOperations.search(query, CandidateEs.class, IndexCoordinates.of(CandidateEs.INDEX_NAME));
  }

  /*
   * Methods to create term queries.
   */
  @NotNull
  private Query getTermsQuery(String field, List<Object> terms) {
    // Need to convert to JsonData (string).... or create independent functions?
    // This won't work.
    String jsonTerms = new Gson().toJson(terms);

    if (terms.size() == 1) {
      return getTermQuery(field, jsonTerms);
    } else {
      return TermsQuery
          .of(t -> t.field(field)
              .terms(tt -> tt.value(terms.stream().map(FieldValue.of(jsonTerms)).toList())))._toQuery();
    }
  }

  @NotNull
  private Query getTermQuery(String field, String value) {
    return QueryBuilders.term().field(field).value(value).build()._toQuery();
  }
}