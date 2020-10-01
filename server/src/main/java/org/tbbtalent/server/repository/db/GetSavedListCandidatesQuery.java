/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateSavedList;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;

import lombok.RequiredArgsConstructor;
import static org.tbbtalent.server.repository.db.CandidateSpecificationUtil.getOrderByOrders;

/**
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
    private final long savedListId;
    private final SavedListGetRequest request;

    @Override
    public Predicate toPredicate(Root<Candidate> candidate, 
                                 CriteriaQuery<?> query, CriteriaBuilder cb) {

        //Start by adding fetches and Order by
        boolean isCountQuery = query.getResultType().equals(Long.class);
        if (!isCountQuery) {
            //Fetch to populate the key linked entities
            Fetch<Object, Object> userFetch = candidate.fetch("user", JoinType.LEFT);
            Fetch<Object, Object> nationalityFetch = candidate.fetch("nationality", JoinType.LEFT);
            Fetch<Object, Object> countryFetch = candidate.fetch("country", JoinType.LEFT);

            //Do sorting by passing in the equivalent joins
            List<Order> orders = getOrderByOrders(request, candidate, cb,
                    (Join<Object, Object>) userFetch,
                    (Join<Object, Object>) nationalityFetch,
                    (Join<Object, Object>) countryFetch);
            query.orderBy(orders);
        }

        //Now construct the actual query
        /*
        select candidate from candidate 
        where candidate in  
            (select candidate from candidateSavedList 
                where savedList.id = savedListID)  
         */
        Subquery<Candidate> sq = query.subquery(Candidate.class);
        Root<CandidateSavedList> csl = sq.from(CandidateSavedList.class);
        sq.select(csl.get("candidate")).where(cb.equal(csl.get("savedList").get("id"), savedListId));
        
        return cb.in(candidate).value(sq);
    }
}
