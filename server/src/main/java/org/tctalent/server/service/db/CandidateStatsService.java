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

package org.tctalent.server.service.db;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.DataRow;
import org.tctalent.server.model.db.Gender;

/**
 * Provides methods for generating standard stats on candidates.
 *
 * @author John Cameron
 */
public interface CandidateStatsService {

    /**
     * Generates numbers of candidates by birth year for each gender.
     * <p/>
     * Candidates are selected who registered between the given dates.
     * <p/>
     * Candidates also can be restricted by
     * <ul>
     *     <li>candidate ids (eg all candidates in a list)</li>
     *     <li>a search query, as defined by the constraintPredicate</li>
     *     <li>source countries where the candidates are located</li>
     * </ul>
     * @param gender Candidate gender - if null any gender is accepted
     * @param dateFrom Candidate date of registration should be this date or after
     * @param dateTo Candidate date of registration should be this date or before
     * @param candidateIds If not null, only candidates with these ids are counted
     * @param sourceCountryIds If not null, only candidates located in these countries are counted.
     * @param constraintPredicate If not null, other candidates satisfying this predicate are
     *                            counted. The predicate is in the form of a SQL expression.
     * @return List of counts for each value.
     */
    List<DataRow> computeBirthYearStats(
        @Nullable Gender gender, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds,
        @Nullable String constraintPredicate);

    /**
     * Generates numbers of candidates by gender.
     * <p/>
     * See above doc for standard parameters and return value.
     */
    List<DataRow> computeGenderStats(
        @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds,
        @Nullable String constraintPredicate);

    /**
     * Generates numbers by language and gender.
     * <p/>
     * See above doc for standard parameters and return value.
     */
    List<DataRow> computeLanguageStats(
        @Nullable Gender gender, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds,
        @Nullable String constraintPredicate);

    //TODO JC Doc
    List<DataRow> computeLinkedInExistsStats(@Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    /**
     * Generates numbers with LinkedIn links by registration date
     * <p/>
     * See above doc for standard parameters and return value.
     */
    List<DataRow> computeLinkedInStats(
        @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds,
        @Nullable String constraintPredicate);

    //TODO JC Doc
    List<DataRow> computeMaxEducationStats(@Nullable Gender gender, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    //TODO JC Doc
    List<DataRow> computeMostCommonOccupationStats(@Nullable Gender gender, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    //TODO JC Doc
    List<DataRow> computeNationalityStats(@Nullable Gender gender, @Nullable String country, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    //TODO JC Doc
    List<DataRow> computeOccupationStats(@Nullable Gender gender, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    //TODO JC Doc
    List<DataRow> computeReferrerStats(@Nullable Gender gender, @Nullable String country, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    //TODO JC Doc
    List<DataRow> computeRegistrationOccupationStats(@Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    /**
     * Generates numbers by registration date
     * <p/>
     * See above doc for standard parameters and return value.
     */
    List<DataRow> computeRegistrationStats(
        @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds,
        @Nullable String constraintPredicate);

    //TODO JC Doc
    List<DataRow> computeSourceCountryStats(@Nullable Gender gender, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    //TODO JC Doc
    List<DataRow> computeSpokenLanguageLevelStats(@Nullable Gender gender, @Nullable String language, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    //TODO JC Doc
    List<DataRow> computeStatusStats(@Nullable Gender gender, @Nullable String country, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    //TODO JC Doc
    List<DataRow> computeSurveyStats(@Nullable Gender gender, @Nullable String country, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo, @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, @Nullable String constraint);

    /**
     * Generates UNHCR numbers by registration.
     * <p/>
     * See above doc for standard parameters and return value.
     */
    List<DataRow> computeUnhcrRegisteredStats(
        @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds,
        @Nullable String constraintPredicate);

    /**
     * Generates UNHCR numbers by registration status.
     * <p/>
     * See above doc for standard parameters and return value.
     */
    List<DataRow> computeUnhcrStatusStats(
        @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds,
        @Nullable String constraintPredicate);

}