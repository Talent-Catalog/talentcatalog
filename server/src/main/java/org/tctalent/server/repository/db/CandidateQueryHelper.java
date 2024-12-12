/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.repository.db;

/**
 * Utilities and constants to help constructing candidate queries
 *
 * @author John Cameron
 */
public class CandidateQueryHelper {
    public static final String CANDIDATE__USER__JOIN__NATIVE =
        "users on candidate.user_id = users.id";
    public static final String CANDIDATE__EDUCATION_LEVEL__JOIN__NATIVE =
        "education_level on candidate.max_education_level_id = education_level.id";
    public static final String CANDIDATE__CANDIDATE_EDUCATION__JOIN__NATIVE =
        "candidate_education on candidate.id = candidate_education.candidate_id";
    public static final String CANDIDATE__CANDIDATE_OCCUPATION__JOIN__NATIVE =
        "candidate_occupation on candidate.id = candidate_occupation.candidate_id";
    public static final String CANDIDATE__CANDIDATE_LANGUAGE__JOIN__NATIVE =
        "candidate_language on candidate.id = candidate_language.candidate_id";
    public static final String CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_SPOKEN__JOIN_NATIVE =
        "language_level as spoken_level on candidate_language.spoken_level_id = spoken_level_id";
    public static final String CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_WRITTEN__JOIN_NATIVE =
        "language_level as written_level on candidate_language.written_level_id = written_level_id";
    public static final String CANDIDATE_LANGUAGE__LANGUAGE__JOIN_NATIVE =
        "language on candidate_language.language_id = language.id";
}
