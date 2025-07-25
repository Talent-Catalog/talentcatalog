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

package org.tctalent.server.service.db.impl;

import static java.util.stream.Collectors.toMap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.service.db.CandidateSearchService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.CandidateSearchUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateSearchServiceImpl implements CandidateSearchService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CandidateRepository candidateRepository;
    private final UserService userService;

    @Override
    public Page<Candidate> searchCandidates(
        SearchCandidateRequest request, Set<Candidate> excludedCandidates) {
        User user = userService.getLoggedInUser();

        String whereSql = request.extractSQL(user, excludedCandidates);

        return findCandidatesFromSql(whereSql, request.getPageRequest());
    }

    private Page<Candidate> findCandidatesFromSql(String whereSql, Pageable pageable) {

        String sql = "SELECT distinct candidate.id FROM candidate" + whereSql;

        String countSql = "SELECT count(distinct candidate.id) FROM candidate" + whereSql;

        //Take sort into account
        String orderBySql = CandidateSearchUtils.buildOrderByClause(pageable.getSort());

        String sqlWithSort = sql + orderBySql;
        LogBuilder.builder(log).action("findCandidates")
            .message("Query: " + sqlWithSort).logInfo();

        //Create and execute the query to return the candidate ids
        Query query = entityManager.createNativeQuery(sqlWithSort);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Long> ids = executeIdsQuery(query);

        //Retrieve the candidate entities for those ids. They will come back unsorted.
        List<Candidate> candidatesUnsorted = candidateRepository.findByIds(ids);

        //Candidates need to be sorted the same as the ids.
        //Map the unsorted candidates by their ids
        Map<Long, Candidate> candidatesById = candidatesUnsorted.stream()
            .collect(toMap(Candidate::getId, c -> c));

        //Construct a sorted list of the candidates in the same order as the returned ids.
        List<Candidate> candidatesSorted = new ArrayList<>();
        for (Long id : ids) {
            candidatesSorted.add(candidatesById.get(id));
        }

        //Compute count
        long total =  ((Number) entityManager.createNativeQuery(countSql).getSingleResult()).longValue();
        return new PageImpl<>(candidatesSorted, pageable, total);
    }

    private List<Long> executeIdsQuery(Query query) {
        //Get results
        final List<?> results = query.getResultList();
        //and convert to List of Longs
        return results.stream()
            .map(r -> ((Number) r).longValue())
            .toList();
    }

}
