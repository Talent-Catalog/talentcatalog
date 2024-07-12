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
     * Generates numbers of candidates by birth year.
     * <p/>
     * Candidates are selected which have the given gender, and who registered between 
     * the given dates.
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

}
