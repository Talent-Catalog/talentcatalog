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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.service.db.CandidateSearchService;
import org.tctalent.server.service.db.UserService;

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
        final PageRequest pageRequest = request.getPageRequest();

        String sql = request.extractFetchSQL(user, excludedCandidates, true);
        LogBuilder.builder(log).action("findCandidates")
            .message("Query: " + sql).logInfo();

        //Create and execute the query to return the candidate ids
        Query query = entityManager.createNativeQuery(sql);
        query.setFirstResult((int) pageRequest.getOffset());
        query.setMaxResults(pageRequest.getPageSize());
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
        String countSql = request.extractCountSQL(user, excludedCandidates);
        LogBuilder.builder(log).action("countCandidates")
            .message("Query: " + countSql).logInfo();
        long total =  ((Number) entityManager.createNativeQuery(countSql).getSingleResult()).longValue();
        return new PageImpl<>(candidatesSorted, pageRequest, total);

    }

    private List<Long> executeIdsQuery(Query query) {
        //Get results
        final List<?> results = query.getResultList();
        //and convert to List of Longs
        return results.stream()
            .map(r -> (Object[]) r)
            .map(r -> ((Number) r[0]).longValue())
            .toList();
    }

}
