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

import static org.tctalent.server.repository.db.CandidateSpecificationUtil.getOrderByOrders;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.SavedListGetRequest;

/**
 * MODEL - Alternate way of creating JPA Specifications
 * <p/>
 * "Specification" which defines the database query to retrieve all candidates
 * in a Saved List based on a {@link SavedListGetRequest}.
 * <p>
 *   To me, this is a more comprehensible way of using the {@link Specification}
 *   interface.
 * </p>
 * <p>
 *     Instead of calling a static buildQuery method, you just pass in an
 *     instance of this to the JPA {@link CandidateRepository#findAll} method.
 *     The instance is created by passing the {@link SavedListGetRequest} to
 *     the constructor.
 * </p>
 * <p>
 *     Note that this Specification query handles the sorting internally
 *     so the {@link PageRequest} passed in should not provide any sorts.
 *     You can get this by calling
 *     {@link SavedListGetRequest#getPageRequestWithoutSort()}
 * </p>
 *     eg:
 *     <code>
 *     PageRequest pageRequest = request.getPageRequestWithoutSort();
 *     Page<Candidate> candidatesPage = candidateRepository.findAll(
 *                 new GetSavedListCandidatesQuery(request), pageRequest);
 *     </code>
 */
@RequiredArgsConstructor
public class GetSavedListCandidatesQuery implements Specification<Candidate> {
    private final SavedList savedList;
    private final SavedListGetRequest request;

    @Override
    public Predicate toPredicate(@NonNull Root<Candidate> candidate,
                                 CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {
        if (query == null) {
            throw new IllegalArgumentException("GetSavedListCandidatesQuery.CriteriaQuery should not be null");
        }

        //Start by adding fetches and Order by
        boolean isCountQuery = query.getResultType().equals(Long.class);
        if (!isCountQuery) {
            //Fetch to populate the key linked entities
            Fetch<Object, Object> userFetch = candidate.fetch("user", JoinType.LEFT);
            Fetch<Object, Object> partnerFetch = userFetch.fetch("partner", JoinType.LEFT);
            Fetch<Object, Object> nationalityFetch = candidate.fetch("nationality", JoinType.LEFT);
            Fetch<Object, Object> countryFetch = candidate.fetch("country", JoinType.LEFT);
            Fetch<Object, Object> educationLevelFetch = candidate.fetch("maxEducationLevel", JoinType.LEFT);

            //Do sorting by passing in the equivalent joins
            List<Order> orders = getOrderByOrders(request, candidate, cb,
                    (Join<Object, Object>) userFetch,
                    (Join<Object, Object>) partnerFetch,
                    (Join<Object, Object>) nationalityFetch,
                    (Join<Object, Object>) countryFetch,
                    (Join<Object, Object>) educationLevelFetch);
            query.orderBy(orders);
        }

        //Now construct the actual query
        /*
        select candidate from candidate
        where candidate in
            (select candidate from candidateSavedList
                where savedList.id = savedListId)
         */
        Subquery<Candidate> sq = query.subquery(Candidate.class);
        Root<CandidateSavedList> csl = sq.from(CandidateSavedList.class);

        Predicate conjunction = cb.conjunction();

        //Start with the basic candidate in list predicate
        conjunction = cb.and(conjunction,
            cb.equal(csl.get("savedList").get("id"), savedList.getId())
        );

        // KEYWORD SEARCH
        if (!StringUtils.isBlank(request.getKeyword())) {
            String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
            String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";

            //Add predicate where the keyword matches any of the various fields
            conjunction = cb.and(conjunction,
                cb.or(
                    cb.like(cb.lower(candidate.get("candidateNumber")), likeMatchTerm),
                    cb.like(cb.lower(candidate.get("user").get("firstName")), likeMatchTerm),
                    cb.like(cb.lower(candidate.get("user").get("lastName")), likeMatchTerm),
                    cb.like(cb.lower(
                        cb.concat(
                            cb.concat(candidate.get("user").get("firstName"), " "),
                            candidate.get("user").get("lastName")
                        )), likeMatchTerm)
                )
            );
        }

        //If savedList has a job, the default is to only show candidates with an opp for that
        //job which is not closed. If there is no job associated with the list, there is no extra
        //filtering.
        final SalesforceJobOpp sfJobOpp = savedList.getSfJobOpp();
        if (sfJobOpp != null) {
            //List is associated with a job - filter out candidates who don't have an opp
            //for the job.
            Join<Candidate, CandidateOpportunity> opp =
                candidate.join("candidateOpportunities", JoinType.LEFT);
            conjunction = cb.and(conjunction,
                cb.equal(opp.get("jobOpp").get("id"), sfJobOpp.getId())
            );
            if (request.getShowClosedOpps() == null || !request.getShowClosedOpps()) {
                //ShowClosedOpps is not true - so only select opps that are won or open (ie closed = true)
                conjunction = cb.and(conjunction,
                    cb.or(
                        cb.equal(opp.get("closed"), false),
                        cb.equal(opp.get("won"), true)
                    )
                );
            }
        }

        //Now create subquery
        sq.select(csl.get("candidate")).where(conjunction);

        //And the candidate must apear in the subquery
        return cb.in(candidate).value(sq);
    }
}
