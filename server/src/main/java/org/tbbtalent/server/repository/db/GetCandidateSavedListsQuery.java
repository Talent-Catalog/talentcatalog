/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.db.CandidateSavedList;
import org.tbbtalent.server.model.db.SavedList;

import lombok.RequiredArgsConstructor;

/**
 * "Specification" which defines the database query to retrieve all saved lists
 * for a Candidate.
 */
@RequiredArgsConstructor
public class GetCandidateSavedListsQuery implements Specification<SavedList> {
    private final long candidateId;

    @Override
    public Predicate toPredicate(Root<SavedList> savedList, 
                                 CriteriaQuery<?> query, CriteriaBuilder cb) {

        //Now construct the actual query
        /*
        select savedList from savedList 
        where savedList in  
            (select savedList from candidateSavedList 
                where candidate.id = candidateId)  
         */
        Subquery<SavedList> sq = query.subquery(SavedList.class);
        Root<CandidateSavedList> csl = sq.from(CandidateSavedList.class);
        sq.select(csl.get("savedList")).where(cb.equal(csl.get("candidate").get("id"), candidateId));

        return cb.in(savedList).value(sq);
    }
}
