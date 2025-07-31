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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Helpers for performing Candidate Searches
 *
 * @author John Cameron
 */
public abstract class CandidateSearchUtils {

    private static final Map<String, String> tableJoins = new HashMap<>();
    static {
        tableJoins.put("candidate_education",
            "candidate_education on candidate.id = candidate_education.candidate_id");
        tableJoins.put("candidate_job_experience",
            "candidate_job_experience on candidate.id = candidate_job_experience.candidate_id");
        tableJoins.put("candidate_language",
            "candidate_language on candidate.id = candidate_language.candidate_id");
        tableJoins.put("candidate_occupation",
            "candidate_occupation on candidate.id = candidate_occupation.candidate_id");
        tableJoins.put("country",
            "country on candidate.country_id = country.id");
        tableJoins.put("education_level",
            "education_level on candidate.max_education_level_id = education_level.id");
        tableJoins.put("language",
            "language on candidate_language.language_id = language.id");
        tableJoins.put("nationality",
            "country as nationality on candidate.nationality_id = nationality.id");
        tableJoins.put("partner",
            "partner on users.partner_id = partner.id");
        tableJoins.put("spoken_level",
            "language_level as spoken_level on candidate_language.spoken_level_id = spoken_level_id");
        tableJoins.put("written_level",
            "language_level as written_level on candidate_language.written_level_id = written_level_id");
        tableJoins.put("users",
            "users on candidate.user_id = users.id");
    }

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

        return orderBy.isEmpty() ? "" : " order by " + orderBy;
    }

    /**
     * Generates list of fields in a given Sort excluding the default id field
     * @param sort Sort specifying fields
     * @return list of non id fields delimited by comma, if any. If none, returns an empty string.
     */
    public static @NonNull String buildNonIdFieldList(Sort sort) {
        String fields = "";
        if (sort != null && !sort.isUnsorted()) {
            //Excluding the id field, map other fields
            fields = sort.stream()
                .map(Order::getProperty)
                .filter(property -> !property.equals("id"))
                .map(CandidateSearchUtils::mapPropertyNameToDbField)
                .collect(Collectors.joining(","));
        }

        return fields;
    }

    public static @NonNull List<String> buildNonCandidateTableList(Sort sort) {
        List<String> tables = new ArrayList<>();
        if (sort != null && !sort.isUnsorted()) {
            sort.stream()
                .map(Order::getProperty)
                .map(CandidateSearchUtils::mapPropertyNameToDbReference)
                .filter(property -> !property.startsWith("candidate."))
                .forEach(property -> {
                    //All but the last part (which is the field name) will be table names.
                    //eg users.partner.abbreviation
                    final String[] parts = property.split("\\.");
                    if (parts.length > 1) {
                        tables.addAll(Arrays.asList(parts).subList(0, parts.length - 1));
                    }
                });
        }
        return tables;
    }

    /**
     * Builds a Postgres tsQuery string which corresponds to the given Elasticsearch Simple Query.
     * @param esQuery Elasticsearch simple query. If null, it will return an empty string.
     * @return Postgres tsQuery SQL
     */
    public static @NonNull String buildTsQuerySQL(@Nullable String esQuery) {
        if (esQuery == null || esQuery.trim().isEmpty()) {
            return "";
        }

        // Step 1: Handle quoted phrases: "quick brown" => quick <-> brown
        StringBuilder result = new StringBuilder();
        boolean inQuote = false;
        StringBuilder phraseBuffer = new StringBuilder();
        for (int i = 0; i < esQuery.length(); i++) {
            char c = esQuery.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
                if (!inQuote) {
                    // End of quote
                    String phrase = phraseBuffer.toString().trim().replaceAll("\\s+", "<->");
                    result.append(phrase);
                    phraseBuffer.setLength(0);
                }
                continue;
            }
            if (inQuote) {
                phraseBuffer.append(c);
            } else {
                result.append(c);
            }
        }

        String intermediate = result.toString();

        // Step 2: Handle operators
        String tsquery = intermediate
            .replaceAll("\\s+\\+\\s+", "&") // + -> &
            .replaceAll("\\s+", "|") // spaces -> |
            //Now add space padding back in around operators
            .replaceAll("&", " & ")
            .replaceAll("\\|", " | ")
            .replaceAll("<->", " <-> ");

        return tsquery.trim();
    }

    public static String getTableJoin(String table) {
        return tableJoins.get(table);
    }

    /**
     * Converts a property name into a field name that will be used when referencing the field
     * in generated SQL queries. In SQL queries fields are referred to just as table.field.
     * <p>
     *     Examples
     * </p>
     * <ul>
     *     <li>
     *     user.partner.abbreviation -> partner.abbreviation
     *     </li>
     *     <li>
     *         yearOfArrival -> candidate.year_of_arrival
     *     </li>
     * </ul>
     * @param propertyName Name received from the front end code
     * @return Field name as it appears in generated SQL queries
     */
    private static @NonNull String mapPropertyNameToDbField(String propertyName) {
        String dbReference = mapPropertyNameToDbReference(propertyName);
        String[] parts = dbReference.split("\\.");
        //Note that references returned by mapPropertyNameToDbReference will always have at least
        //two fields: xxx.yyy
        return parts[parts.length-2] + "." + parts[parts.length-1];
    }

    /**
     * Converts a property name received from front end code (usually in the sort fields
     * of a request) into a standard form which refers to the database relative to the
     * candidate table.
     * <p>
     *     For example:
     * </p><p>
     *     <code>
     *         users.partner.abbreviation
     *     </code>
     *</p><p>
     *     This refers to the abbreviation field of the partner table which is joined to the
     *     users table which is joined to the candidate table.
     * </p><p>
     *     Property names received from the front end as sort fields don't mention the candidate
     *     table - that is implied if no other table is supplied.
     *     Front end code also refers to the "users" table as "user".
     *     Lastly property names received from the front end are expressed in camel case. But they
     *     are converted by this method to snakeCase - which is what is used in the database.
     * </p><p>
     *     So, for example, user.partner.notificationEmail is received from the front end in the Sort
     *     fields, and this method will convert it to users.partner.notification_email.
     * </p>
     * @param propertyName Name received from the front end code
     * @return property name converted into a standard database reference like
     * table.table...table.field
     */
    private static @NonNull String mapPropertyNameToDbReference(String propertyName) {
        //Can assume that candidate and user tables are in select so sorting by those fields
        if (!propertyName.contains(".")) {
            propertyName = "candidate." + propertyName;
        }

        propertyName = propertyName.replace("user.", "users.");

        return RegexHelpers.camelToSnakeCase(propertyName);
    }
}
