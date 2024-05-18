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
 * A set of functions to assist with creation/use of elastic query construction.
 * They'd be better as independent functions in Kotlin, but not there yet, so wrapped as static
 * methods in this class.
 */
public class TCElasticHelpers {
  /*
   * Methods to create term queries.
   */
  @NotNull
  public static Query getTermsQuery(String field, List<Object> terms) {
    if (terms.size() == 1) {
      return getTermQuery(field, terms.getFirst());
    } else {
      return TermsQuery
          .of(t -> t.field(field)
              .terms(tt -> tt.value(terms.stream().map(FieldValue::of).toList())))._toQuery();
    }
  }

  @NotNull
  public static Query getTermQuery(String field, Object value) {
    return QueryBuilders.term().field(field).value(FieldValue.of(value)).build()._toQuery();
  }

  @NotNull
  public static Query addTermFilter(@Nullable SearchType searchType, String field, List<Object> values) {
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
  private static Query addTermFilter(String field, List<Object> values) {
    return getTermQuery(field, values);
  }

  /**
   * Simple function that will add the query into the builder if the query is not null.
   * @param builder
   * @param qry the query to add (if not null)
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
