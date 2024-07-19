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

package org.tctalent.server.service.db.impl;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.DataRow;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.service.db.CandidateStatsService;

/**
 * Methods performing our standard candidate stats.
 * <p/>
 * Note that I have been forced to go to native queries for these more complex queries.
 * The non-native queries seem a bit buggy.
 * Anyway - I couldn't get them working. Simpler to use normal SQL. JC.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateStatsServiceImpl implements CandidateStatsService {

    @PersistenceContext
    private EntityManager entityManager;

    //Standard SQL building blocks for our stats.
    private static final String candidatesCondition =
        " and candidate.id in (:candidateIds)";
    private static final String sourceCountriesCondition =
        " and candidate.country_id in (:sourceCountryIds)";
    private static final String notTestCandidateCondition =
        " and candidate.id NOT IN (select candidate_id from candidate_saved_list" +
            " where saved_list_id = "
            + "(select id from saved_list where name = 'TestCandidates' and global = true))";
    private static final String countingStandardFilter =
        " users.status = 'active' and candidate.status != 'draft'" + notTestCandidateCondition;
    private static final String dateConditionFilter =
        " and users.created_date >= (:dateFrom) and users.created_date <= (:dateTo)";
    private static final String excludeIneligible = " and candidate.status != 'ineligible'";

    @Override
    public List<DataRow> computeBirthYearStats(
        @Nullable Gender gender, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds,
        @Nullable String constraintPredicate) {

        String countByBirthYearSelectSQL =
    """
            select cast(extract(year from dob) as bigint) as year, count(distinct candidate) as PeopleCount
                 from candidate left join users on candidate.user_id = users.id
                 where gender like :gender
    """
        //Ignore null birthdate and only look at plausible birth years
        + " and dob is not null and extract(year from dob) > 1940 and";

        countByBirthYearSelectSQL +=
            standardConstraints(candidateIds, sourceCountryIds, constraintPredicate);

        String countByBirthYearGroupBySQL = " group by year order by year asc";
        String sql = countByBirthYearSelectSQL + countByBirthYearGroupBySQL;

        LogBuilder.builder(log).action("computeBirthYearStats")
            .message("Query: " + sql).logInfo();

        Query query = entityManager.createNativeQuery(sql);

        query.setParameter("gender", genderStr(gender));

        setStandardQueryParameters(query,
            dateFrom, dateTo, candidateIds, sourceCountryIds);

        return runQuery(query);
    }

    private static String countryStr(String country) {
        return country == null ? "%" : country;
    }

    private static LocalDate defaultDateFrom(LocalDate dateFrom) {
       return dateFrom == null ? LocalDate.parse("2000-01-01") : dateFrom;
    }

    private static LocalDate defaultDateTo(LocalDate dateTo) {
        return dateTo == null ? LocalDate.now() : dateTo;
    }

    private static String genderStr(Gender gender) {
        return gender == null ? "%" : gender.toString();
    }

    private static List<DataRow> runQuery(Query query) {
        final List resultList = query.getResultList();
        return toRows(resultList);
    }

    private static void setStandardQueryParameters(Query query,
        @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds) {

        query.setParameter("dateFrom", defaultDateFrom(dateFrom));
        query.setParameter("dateTo", defaultDateTo(dateTo));
        if (sourceCountryIds != null && !sourceCountryIds.isEmpty()) {
            query.setParameter("sourceCountryIds", sourceCountryIds);
        }
        if (candidateIds != null) {
            query.setParameter("candidateIds", candidateIds);
        }

    }

    private static String standardConstraints(
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds,
        @Nullable String constraintPredicate) {

        String s = countingStandardFilter + dateConditionFilter;
        if (sourceCountryIds != null && !sourceCountryIds.isEmpty()) {
            s += sourceCountriesCondition;
        }
        if (constraintPredicate != null) {
            s += " and " + constraintPredicate;
        }
        if (candidateIds != null) {
            s += candidatesCondition;
        }

        //Stats that are not based on predefined candidate ids or constraintPredicate,
        // should exclude ineligible.
        //(With candidate ids, it is up to the associated list or search to decide whether to
        // exclude ineligible)
        if (candidateIds == null && constraintPredicate == null) {
            s += excludeIneligible;
        }

        return s;
    }

    private static List<DataRow> toRows(List<Object[]> objects) {
        List<DataRow> dataRows = new ArrayList<>(objects.size());
        for (Object[] row: objects) {
            String label = row[0] == null ? "undefined" : row[0].toString();
            DataRow dataRow = new DataRow(label, (BigInteger)row[1]);
            dataRows.add(dataRow);
        }
        return dataRows;
    }

}
