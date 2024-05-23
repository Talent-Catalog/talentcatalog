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
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.SearchType;

/**
 * A set of functions to assist with creation/use of elastic query construction. They'd be better as
 * independent functions in Kotlin, but not there yet, so wrapped as static methods in this class.
 */
public class TCElasticHelpers {

  /**
   * Creates a query based on the field and terms passed in. If a single term, delegates to provide
   * a single term.
   *
   * @param field the name of the field to filter on
   * @param terms the terms to use in the query. Either Long or String
   * @return a query
   */
  @NotNull
  public static Query getTermsQuery(String field, List<?> terms) {
    if (terms.size() == 1) {
      return getTermQuery(field, terms.getFirst());
    } else {
      if (terms instanceof List && !terms.isEmpty() && terms.get(0) instanceof Long) {
        return TermsQuery
            .of(t -> t.field(field)
                .terms(tt -> tt.value(terms.stream()
                    .map(term -> FieldValue.of((Long) term))
                    .toList())))
            ._toQuery();
      } else if (terms instanceof List && !terms.isEmpty() && terms.get(0) instanceof String) {
        return TermsQuery
            .of(t -> t.field(field)
                .terms(tt -> tt.value(terms.stream()
                    .map(term -> FieldValue.of((String) term))
                    .toList())))
            ._toQuery();
      } else {
        throw new RuntimeException("Expect either String or Long");
      }
    }
  }

  /**
   * Creates a query based on the field and value passed in.
   *
   * @param field the name of the field to filter on.
   * @param value the value to use in the query
   * @return a query
   */
  @NotNull
  public static Query getTermQuery(String field, Object value) {
    if (value instanceof String) {
      return QueryBuilders.term().field(field).value(FieldValue.of((String) value)).build()
          ._toQuery();
    } else {
      return QueryBuilders.term().field(field).value(FieldValue.of((Long) value)).build()
          ._toQuery();
    }
  }

  /**
   * Creates a term query using the field and values provided. Adds a must not to the query if not
   * is passed - not sure why it's in here.
   *
   * @param searchType if a specific type is required
   * @param field      to test
   * @param values     to use in the query
   * @return the constructed Query
   */
  @NotNull
  public static Query getTermsQuery(@Nullable SearchType searchType, String field, List<?> values) {
    Query qry = addTermFilter(field, values);

    BoolQuery.Builder builder = QueryBuilders.bool();
    if (searchType == SearchType.not) {
      builder = builder.mustNot(qry);
    } else {
      builder = builder.filter(qry);
    }

    return builder.build()._toQuery();
  }

  @NotNull
  private static Query addTermFilter(String field, List<?> values) {
    return getTermQuery(field, values);
  }

  /**
   * Simple function that will add the query into the builder if the query is not null.
   *
   * @param builder the boolean query builder to use.
   * @param qry     the query to add (if not null)
   * @return the BoolQuery.Builder
   */
  public static BoolQuery.Builder filterIfNotNull(BoolQuery.Builder builder, @Nullable Query qry) {
    if (qry == null) {
      return builder;
    } else {
      return builder.filter(qry);
    }
  }
}
