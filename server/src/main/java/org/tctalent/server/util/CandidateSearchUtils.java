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
    public static final String CANDIDATE__USER__JOIN__NATIVE =
        "users on candidate.user_id = users.id";
    public static final String CANDIDATE__EDUCATION_LEVEL__JOIN__NATIVE =
        "education_level on candidate.max_education_level_id = education_level.id";
    public static final String CANDIDATE__CANDIDATE_EDUCATION__JOIN__NATIVE =
        "candidate_education on candidate.id = candidate_education.candidate_id";
    public static final String CANDIDATE__CANDIDATE_OCCUPATION__JOIN__NATIVE =
        "candidate_occupation on candidate.id = candidate_occupation.candidate_id";
    public static final String CANDIDATE__CANDIDATE_JOB_EXPERIENCE__JOIN__NATIVE =
        "candidate_job_experience on candidate.id = candidate_job_experience.candidate_id";
    public static final String CANDIDATE__CANDIDATE_LANGUAGE__JOIN__NATIVE =
        "candidate_language on candidate.id = candidate_language.candidate_id";
    public static final String CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_SPOKEN__JOIN_NATIVE =
        "language_level as spoken_level on candidate_language.spoken_level_id = spoken_level_id";
    public static final String CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_WRITTEN__JOIN_NATIVE =
        "language_level as written_level on candidate_language.written_level_id = written_level_id";
    public static final String CANDIDATE_LANGUAGE__LANGUAGE__JOIN_NATIVE =
        "language on candidate_language.language_id = language.id";

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

    private static @NonNull String mapPropertyNameToDbField(String propertyName) {
        if (!propertyName.contains(".")) {
            propertyName = "candidate." + propertyName;
        }

        propertyName = propertyName.replace("user.partner", "partner.");
        propertyName = propertyName.replace("user.", "users.");

        return RegexHelpers.camelToSnakeCase(propertyName);
    }
}
