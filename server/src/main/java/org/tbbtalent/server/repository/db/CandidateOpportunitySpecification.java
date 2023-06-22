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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.db.CandidateOpportunity;
import org.tbbtalent.server.model.db.CandidateOpportunityStage;
import org.tbbtalent.server.model.db.PartnerJobRelation;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.db.partner.Partner;
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

            //TODO CandidateOpportunitySpecification and JobSpecification are both opportunity
            //specifications and duplicate a lot of code which should be refactored out.

            final Boolean showActiveStages = request.getActiveStages();
            final Boolean showClosed = request.getSfOppClosed();

            boolean isStageFilterActive = false;

            // STAGE
            List<CandidateOpportunityStage> stages = request.getStages();
            if (!Collections.isEmpty(stages)) {
                conjunction.getExpressions().add(builder.isTrue(opp.get("stage").in(stages)));
                isStageFilterActive = true;
            }

            //ACTIVE STAGES (ignored if doing stage filtering)
            if (!isStageFilterActive) {
                //Note that we only check "active stages" if explicit stages have not been requested.
                if (showActiveStages != null) {
                    //Only apply filter when we just want to display active stages
                    //Otherwise, if false, it will ONLY display inactive stages which we don't want
                    if (showActiveStages) {
                        final Predicate activePredicate = builder.between(opp.get("stageOrder"),
                            CandidateOpportunityStage.prospect.ordinal(),
                            CandidateOpportunityStage.relocating.ordinal());
                        if (showClosed != null && showClosed) {
                            //When active stages are requested as well as closed, we need both.
                            //ie We need to show opps which are active OR closed
                            conjunction.getExpressions().add(
                                builder.or(activePredicate,
                                builder.equal(opp.get("closed"), true)));
                        } else {
                            conjunction.getExpressions().add(activePredicate);
                        }
                    }
                }
            }

            //CLOSED (ignored if we are doing stage filtering)
            if (!isStageFilterActive && showClosed != null) {
                //Only apply filter if we want to exclude closed opps.
                //Otherwise the filter when true will only show closed opps - which we don't want.
                if (!showClosed) {
                    conjunction.getExpressions().add(builder.equal(opp.get("closed"), false));
                }
            }

            //OWNERSHIP
            // Owned by me or Owned by my partner
            Predicate ors = builder.disjunction();

            //If managed by this user (ie by logged in user)
            if (request.getOwnedByMe() != null && request.getOwnedByMe()) {
                //This is the where clause we are constructing
                        /*
                           where jobOpp in
                           (select job from PartnerJobRelation
                               where partner.id = partnerId and contact.id = userId)
                         */
                //In other words where the candidate opportunity's associated job
                //is one of the jobs that the logged in user has been nominated by their
                //partner to be the contact for.
                if (loggedInUser != null) {
                    Partner loggedInUserPartner = loggedInUser.getPartner();
                    if (loggedInUserPartner != null) {

                        //Get the opp's jobOpp
                        Join<Object, Object> jobOpp = opp.join("jobOpp");

                        //Get the partner associated with the opp's candidate
                        Join<Object, Object> partner = getOppCandidatePartnerJoin(opp);

                        Long partnerId = loggedInUserPartner.getId();
                        Long userId = loggedInUser.getId();

                        //Create the Select subquery - giving all the jobs the user is contact for
                        Subquery<SalesforceJobOpp> usersJobs = query.subquery(SalesforceJobOpp.class);
                        Root<PartnerJobRelation> pjr = usersJobs.from(PartnerJobRelation.class);
                        usersJobs.select(pjr.get("job")).where(
                            builder.and(
                                builder.equal(pjr.get("partner").get("id"), partnerId),
                                builder.equal(pjr.get("contact").get("id"), userId)
                            )
                        );

                        ors.getExpressions().add(
                            builder.and(
                                //User's partner must be the partner associated with the opp's candidate
                                builder.equal(partner.get("id"), loggedInUserPartner.getId()),

                                //and this job must be one of the jobs that this user is the contact
                                //for.
                                builder.in(jobOpp).value(usersJobs)
                            )
                        );
                    }
                }
            }

            //If managed by this user's partner
            if (request.getOwnedByMyPartner() != null && request.getOwnedByMyPartner()) {
                //Just check that the candidate associated with the opportunity is managed
                //by the logged in user's partner.
                if (loggedInUser != null) {
                    Partner loggedInUserPartner = loggedInUser.getPartner();
                    if (loggedInUserPartner != null) {
                        Join<Object, Object> partner = getOppCandidatePartnerJoin(opp);
                        ors.getExpressions().add(
                            builder.equal(partner.get("id"), loggedInUserPartner.getId())
                        );
                    }
                }
            }

            if (ors.getExpressions().size() != 0) {
                conjunction.getExpressions().add(ors);
            }

            return conjunction;
        };
    }

    /**
     * Walk down the joins from the opp to find the partner associated with the opp's candidate
     * @param opp Candidate opportunity
     * @return Associated partner
     */
    private static Join<Object, Object> getOppCandidatePartnerJoin(Root<CandidateOpportunity> opp) {
        Join<Object, Object> candidate = opp.join("candidate");
        Join<Object, Object> user = candidate.join("user");
        Join<Object, Object> partner = user.join("partner");
        return partner;
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
