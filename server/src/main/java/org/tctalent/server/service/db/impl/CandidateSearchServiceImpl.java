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
import org.tctalent.server.util.CandidateSearchUtils;
import org.tctalent.server.util.textExtract.IdAndRank;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateSearchServiceImpl implements CandidateSearchService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CandidateRepository candidateRepository;
    private final UserService userService;

    /**
     * Overview of searching:
     * <ol>
     *    <li>
     *        Construct a query which collects the sorted candidate ids of a page of candidates who
     *        match the given search request.
     *        This request will look like:
     *        <p>
     *           <code>
     *              select distinct candidate.id from candidate ...
     *              <br>
     *              where ...
     *              <br>
     *              order by ...
     *           </code>
     *        </p>
     *    </li>
     *    <li>
     *      Construct a very similar query which counts the total number of candidates matching
     *      the search request. This count is used to support paging.
     *      This request will look like:
     *        <p>
     *           <code>
     *              select count(distinct candidate.id) from candidate ...
     *              <br>
     *              where ...
     *           </code>
     *        </p>
     *        <p>
     *            Note that there is no order by. It is unnecessary to get the total count. But the
     *            where clause is the same for both queries.
     *        </p>
     *    </li>
     *    <li>
     *        Retrieve the candidate entities for the page of id's that are returned in the first
     *        query and sort those candidates in the same order as the retrieved ids.
     *    </li>
     *    <li>
     *        <p>
     *        If the original query was sorted by a computed ranking then the ranking values will
     *        have been returned in the results of the first query. Those ranks are
     *        added to the candidate entity data so that they can be displayed to the user.
     *        </p>
     *        <p>
     *           Matching keywords against a candidate's text data is an example of a ranking.
     *           Close matches will display a higher ranking.
     *        </p>
     *    </li>
     * </ol>
     * @param request Specifies the details of the search
     * @param excludedCandidates If specified, indicates candidates to be excluded from the search.
     * @return A page of candidates
     */
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

        //Get results
        final List<?> results = query.getResultList();
        //Process the results
        List<IdAndRank> idAndRanks =
            CandidateSearchUtils.processIdRankSearchResults(results, request.getSort());

        //Get ids of sorted candidates
        List<Long> ids = idAndRanks.stream().map(IdAndRank::id).toList();

        //Retrieve the candidate entities for those ids. They will come back unsorted.
        List<Candidate> candidatesUnsorted = candidateRepository.findByIds(ids);

        //Candidates need to be sorted the same as the ids.
        //Map the unsorted candidates by their ids
        Map<Long, Candidate> candidatesById = candidatesUnsorted.stream()
            .collect(toMap(Candidate::getId, c -> c));

        //Construct a sorted list of the candidates in the same order as the returned ids.
        List<Candidate> candidatesSorted = new ArrayList<>();
        for (IdAndRank idAndRank : idAndRanks) {
            final Candidate candidate = candidatesById.get(idAndRank.id());

            //Optionally update candidate data with any ranking values.
            final Number rank = idAndRank.rank();
            //Rank is a transient field so no need to set to null
            if (rank != null) {
                candidate.setRank(rank);
            }
            candidatesSorted.add(candidate);
        }

        //Compute count
        String countSql = request.extractCountSQL(user, excludedCandidates);
        LogBuilder.builder(log).action("countCandidates")
            .message("Query: " + countSql).logInfo();
        long total =  ((Number) entityManager.createNativeQuery(countSql).getSingleResult()).longValue();
        return new PageImpl<>(candidatesSorted, pageRequest, total);

    }
}
