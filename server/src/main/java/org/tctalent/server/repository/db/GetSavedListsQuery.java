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

import java.util.HashSet;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.list.SearchSavedListRequest;

/**
 * Specification which defines a GetSavedListsQuery
 *
 * @author John Cameron
 */
@RequiredArgsConstructor
public class GetSavedListsQuery implements Specification<SavedList> {
    final private SearchSavedListRequest request;
    @Nullable final private User loggedInUser;

    @Override
    public Predicate toPredicate(
            Root<SavedList> savedList, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate conjunction = cb.conjunction();
        query.distinct(true);

        //Only return lists which are not Selection lists -
        //ie lists with no associated saved search.
        conjunction.getExpressions().add(
                cb.isNull(savedList.get("savedSearch")));

        // KEYWORD SEARCH
        if (!StringUtils.isBlank(request.getKeyword())){
            String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
            String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
            conjunction.getExpressions().add(
                     cb.like(cb.lower(savedList.get("name")), likeMatchTerm));
        }

        //If fixed is specified, only supply matching saved lists
        if (request.getFixed() != null && request.getFixed()) {
            conjunction.getExpressions().add(cb.equal(savedList.get("fixed"), true)
            );
        }

        //If registeredJob is specified and true, only supply matching saved lists
        if (request.getRegisteredJob() != null && request.getRegisteredJob()) {
            conjunction.getExpressions().add(cb.equal(savedList.get("registeredJob"), true));
        }

        //If sfOppIsClosed is specified, and sfJobOpp is not null, only supply saved list with
        // matching job closed.
        if (request.getSfOppClosed() != null) {
            //Closed condition is only meaningful if sfJobOpp is present
            conjunction.getExpressions().add(cb.isNotNull(savedList.get("sfJobOpp")));
            conjunction.getExpressions().add(cb.equal(savedList.get("sfOppIsClosed"), request.getSfOppClosed()));
        }

        //If short name is specified, only supply matching saved lists. If false, remove
        if (request.getShortName() != null && request.getShortName()) {
            conjunction.getExpressions().add(
                    cb.isNotNull(savedList.get("tbbShortName"))
            );
        } else if (request.getShortName() != null && !request.getShortName()){
            conjunction.getExpressions().add(
                    cb.isNull(savedList.get("tbbShortName"))
            );
        }

        // (shared OR owned OR global)
        Predicate ors = cb.disjunction();

        if (request.getGlobal() != null && request.getGlobal()) {
            ors.getExpressions().add(
                cb.equal(savedList.get("global"), request.getGlobal())
            );
        }

        //If shared is specified, only supply searches shared with the owner
        if (request.getShared() != null && request.getShared()) {
            if (loggedInUser != null) {
                Set<SavedList> sharedLists = loggedInUser.getSharedLists();
                Set<Long> sharedIDs = new HashSet<>();
                for (SavedList sharedList : sharedLists) {
                    sharedIDs.add(sharedList.getId());
                }
                ors.getExpressions().add(
                        savedList.get("id").in( sharedIDs )
                );
            }
        }

        //If owned by this user (ie by logged in user)
        if (request.getOwned() != null && request.getOwned()) {
            if (loggedInUser != null) {
                ors.getExpressions().add(
                        cb.equal(savedList.get("createdBy"), loggedInUser.getId())
                );
            }
        }

        if (!ors.getExpressions().isEmpty()) {
            conjunction.getExpressions().add(ors);
        }

        return conjunction;
    }
}
