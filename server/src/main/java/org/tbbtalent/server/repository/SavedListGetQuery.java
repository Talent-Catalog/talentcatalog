/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.list.SearchSavedListRequest;

import lombok.RequiredArgsConstructor;

/**
 * Specification which defines a SavedListGetQuery
 *
 * @author John Cameron
 */
@RequiredArgsConstructor
public class SavedListGetQuery implements Specification<SavedList> {
    final private SearchSavedListRequest request;
    final private User loggedInUser;
    
    @Override
    public Predicate toPredicate(
            Root<SavedList> savedList, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate conjunction = cb.conjunction();
        query.distinct(true);

        // KEYWORD SEARCH
        if (!StringUtils.isBlank(request.getKeyword())){
            String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
            String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
            conjunction.getExpressions().add(
                     cb.like(cb.lower(savedList.get("name")), likeMatchTerm));
        }

        // AND (fixed OR shared OR owned) - if any are true
        Predicate ors = cb.disjunction();

        //If fixed is specified, only supply matching saved searches
        if (request.getFixed() != null && request.getFixed()) {
            ors.getExpressions().add(
                    cb.equal(savedList.get("fixed"), request.getFixed())
            );
        }

        //If shared is specified, only supply searches shared with the owner
        if (request.getShared() != null && request.getShared()) {
            if (loggedInUser != null) {
                Set<SavedList> sharedLists = loggedInUser.getSharedLists();
                if (!sharedLists.isEmpty()) {
                    Set<Long> sharedIDs = new HashSet<>();
                    for (SavedList sharedList : sharedLists) {
                        sharedIDs.add(sharedList.getId());
                    }
                    ors.getExpressions().add(
                            savedList.get("id").in( sharedIDs )
                    );
                }
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

        conjunction.getExpressions().add(ors);

        return conjunction;
    }
}
