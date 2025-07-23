/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.util;

import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Helpers for performing Candidate Searches
 *
 * @author John Cameron
 */
public abstract class CandidateSearchUtils {

    /**
     * Generates ORDER BY clause given a Sort.
     * @param sort Sort specifying fields and direction
     * @return ORDER BY clause, if any. If none, returns an empty string.
     */
    public static String buildOrderByClause(Sort sort) {
        //Always sort - at a minimum by id
        if (sort == null || sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.DESC, "id");
        }

        //Need at least one id sort so that ordering is stable.
        //Otherwise, sorts with equal values will come out in random order,
        //which means that the contents of pages - computed at different times -
        //won't be predictable.
        boolean hasIdSort = sort.stream().anyMatch(order -> order.getProperty().equals("id"));
        if (!hasIdSort) {
            //Doesn't have an id sort, so add one at the end.
            sort = sort.and(Sort.by(Sort.Direction.DESC, "id"));
        }

        //Construct the order by clause, mapping Candidate entity field names to database names.
        String orderBy = sort.stream()
            .map( order -> {
                String propertyName = order.getProperty();
                String column = mapPropertyNameToDbField(propertyName);
                return column + " " + order.getDirection().name();
            })
            .collect(Collectors.joining(","));

        return orderBy.isEmpty() ? "" : " ORDER BY " + orderBy;
    }

    /**
     * Builds a Postgres tsQuery string which corresponds to the given Elasticsearch Simple Query.
     * @param simpleQueryString Elasticsearch simple query. If null, it will return an empty string.
     * @return Postgres tsQuery SQL
     */
    public static @NonNull String buildTsQuerySQL(@Nullable String simpleQueryString) {
        String tsQuery = "";
        if (simpleQueryString != null) {
            //TODO JC Implement computeTsQuerySQL
        }
        return tsQuery;
    }

    private static @NonNull String mapPropertyNameToDbField(String propertyName) {
        if (!propertyName.contains(".")) {
            propertyName = "candidate." + propertyName;
        }

        propertyName = propertyName.replace("user.partner", "partner.");
        propertyName = propertyName.replace("user.", "users.");

        return RegexHelpers.camelToSnakeCase(propertyName);
    }
}
