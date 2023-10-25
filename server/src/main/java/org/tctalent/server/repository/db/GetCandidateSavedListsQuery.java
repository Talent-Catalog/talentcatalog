/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.SavedList;

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
