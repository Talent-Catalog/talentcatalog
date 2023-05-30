/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.repository.db;

import io.jsonwebtoken.lang.Collections;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.db.CandidateOpportunity;
import org.tbbtalent.server.model.db.CandidateOpportunityStage;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.PagedSearchRequest;
import org.tbbtalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;

/**
 * Specification for sorting and searching {@link CandidateOpportunity} entities
 */
public class CandidateOpportunitySpecification {

    public static Specification<CandidateOpportunity> buildSearchQuery(
        final SearchCandidateOpportunityRequest request, User loggedInUser) {
        return (opp, query, builder) -> {
            Predicate conjunction = builder.conjunction();

            boolean isCountQuery = query.getResultType().equals(Long.class);
            if (!isCountQuery) {
                //Manage sorting for non count queries
                List<Order> ordering = getOrdering(request, opp, builder);
                query.orderBy(ordering);
            }

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(opp.get("name")), likeMatchTerm)
                        ));
            }

            // STAGE
            List<CandidateOpportunityStage> stages = request.getStages();
            if (!Collections.isEmpty(stages)) {
                conjunction.getExpressions().add(builder.isTrue(opp.get("stage").in(stages)));
            }

            //CLOSED
            if (request.getSfOppClosed() != null) {
                conjunction.getExpressions().add(builder.equal(opp.get("closed"), request.getSfOppClosed()));
            }

            //TODO JC Need live checkbox as well as My Opps- defaults to true (ie Closed = false)

            //TODO Also - display candidate name when not Candidate Jobs tab.

            //TODO JC Different logic for owned. Source partner needs to be managing candidate
//            Predicate ors = builder.disjunction();
//
//            //If owned by this user (ie by logged in user)
//            if (request.getOwnedByMe() != null && request.getOwnedByMe()) {
//                if (loggedInUser != null) {
//                    ors.getExpressions().add(
//                        builder.equal(opp.get("createdBy"), loggedInUser.getId())
//                    );
//                }
//            }
//
//            //If owned by this user's partner
//            if (request.getOwnedByMyPartner() != null && request.getOwnedByMyPartner()) {
//                if (loggedInUser != null) {
//                    Partner loggedInUserPartner = loggedInUser.getPartner();
//                    if (loggedInUserPartner != null) {
//                        Join<Object, Object> owner = opp.join("createdBy");
//                        Join<Object, Object> partner = owner.join("partner");
//                        ors.getExpressions().add(
//                            builder.equal(partner.get("id"), loggedInUserPartner.getId())
//                        );
//                    }
//                }
//            }
//
//            if (ors.getExpressions().size() != 0) {
//                conjunction.getExpressions().add(ors);
//            }

            return conjunction;
        };
    }

    private static List<Order> getOrdering(PagedSearchRequest request,
        Root<CandidateOpportunity> opp, CriteriaBuilder builder) {

        List<Order> orders = new ArrayList<>();
        String[] sort = request.getSortFields();
        boolean idSort = false;
        if (sort != null) {
            for (String property : sort) {
                String subProperty = property;
                if (property.equals("id")) {
                    idSort = true;
                }

                Path<Object> path = opp.get(subProperty);
                orders.add(request.getSortDirection().equals(Sort.Direction.ASC)
                    ? builder.asc(path) : builder.desc(path));
            }
        }

        //Need at least one id sort so that ordering is stable.
        //Otherwise, sorts with equal values will come out in random order,
        //which means that the contents of pages - computed at different times -
        //won't be predictable.
        if (!idSort) {
            orders.add(builder.desc(opp.get("id")));
        }
        return orders;
    }

}
