/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.db.Candidate;
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
        //Guided by https://stackoverflow.com/questions/31841471/spring-data-jpa-specification-for-a-manytomany-unidirectional-relationship
            /*
            select savedList from savedList 
            where exists 
                (select candidate from candidate 
                    where candidate.id = candidateID
                    and
                    savedList in candidate.savedLists)  
             */
        Subquery<Candidate> candidateSubquery = query.subquery(Candidate.class);
        Root<Candidate> candidate = candidateSubquery.from(Candidate.class);
        Expression<Collection<SavedList>> candidateSavedLists =
                candidate.get("savedLists");
        candidateSubquery.select(candidate);
        candidateSubquery.where(
                cb.equal(candidate.get("id"), candidateId),
                cb.isMember(savedList, candidateSavedLists)
        );
        return cb.exists(candidateSubquery);
    }
}
