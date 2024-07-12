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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.DataRow;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.service.db.CandidateStatsService;

@Service
@RequiredArgsConstructor
public class CandidateStatsServiceImpl implements CandidateStatsService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<DataRow> computeBirthYearStats(
        @Nullable Gender gender, @Nullable LocalDate dateFrom, @Nullable LocalDate dateTo,
        @Nullable Set<Long> candidateIds, @Nullable List<Long> sourceCountryIds, 
        @Nullable String constraintPredicate) {

        //Default null dates
        if (dateFrom == null) {
            dateFrom = LocalDate.parse("2000-01-01");
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }

        String candidatesCondition = " and candidate.id in (:candidateIds)";
        String sourceCountriesCondition = " and candidate.country_id in (:sourceCountryIds)";
        String notTestCandidateCondition =
            " and candidate.id NOT IN (select candidate_id from candidate_saved_list" +
                " where saved_list_id = (select id from saved_list where name = 'TestCandidates' and global = true))";
        String countingStandardFilter =
            " users.status = 'active' and candidate.status != 'draft'" + notTestCandidateCondition;
        String dateConditionFilter = " and users.created_date >= (:dateFrom) and users.created_date <= (:dateTo)";

        String excludeIneligible = " and candidate.status != 'ineligible'";

        //Note that I have been forced to go to native queries for these more
        //complex queries. The non-native queries seem a bit buggy.
        //Anyway - I couldn't get them working. Simpler to use normal SQL. JC.
        String countByBirthYearSelectSQL =
    """
            select cast(extract(year from dob) as bigint) as year, count(distinct candidate) as PeopleCount
                 from candidate left join users on candidate.user_id = users.id
                 where gender like :gender
                 and dob is not null and extract(year from dob) > 1940 and
    """
         + countingStandardFilter + dateConditionFilter;
        if (sourceCountryIds != null && !sourceCountryIds.isEmpty()) {
            countByBirthYearSelectSQL += sourceCountriesCondition;
        }
        if (constraintPredicate != null) {
            countByBirthYearSelectSQL += " and " + constraintPredicate;
        }
        if (candidateIds != null) {
            countByBirthYearSelectSQL += candidatesCondition;
        }

        //Stats that are not based on predefined candidate ids or constraintPredicate, 
        // should exclude ineligible.
        //(With candidate ids, it is up to the associated list or search to decide whether to
        // exclude ineligible)
        if (candidateIds == null && constraintPredicate == null) {
            countByBirthYearSelectSQL += excludeIneligible;
        }
        String countByBirthYearGroupBySQL = " group by year order by year asc";
        String sql = countByBirthYearSelectSQL + countByBirthYearGroupBySQL;
        Query query = entityManager.createNativeQuery(sql);
        
        
        query.setParameter("gender", genderStr(gender));
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        if (sourceCountryIds != null && !sourceCountryIds.isEmpty()) {
            query.setParameter("sourceCountryIds", sourceCountryIds);
        }
        if (candidateIds != null) {
            query.setParameter("candidateIds", candidateIds);
        }
        final List resultList = query.getResultList();

        return toRows(resultList);
    }

    private static String countryStr(String country) {
        return country == null ? "%" : country;
    }

    private static String genderStr(Gender gender) {
        return gender == null ? "%" : gender.toString();
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
