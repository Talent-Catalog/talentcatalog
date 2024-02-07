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

import io.jsonwebtoken.lang.Collections;
import java.time.LocalDate;
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
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.PartnerJobRelation;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.request.PagedSearchRequest;
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;

/**
 * Specification for sorting and searching {@link CandidateOpportunity} entities.
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

            //OVERDUE - if true we only display overdue opps
            if (request.getOverdue() != null) {
                if (request.getOverdue()) {
                    conjunction.getExpressions()
                        .add(
                            builder.and(
                                builder.isNotNull(opp.get("nextStepDueDate")),
                                builder.lessThan(opp.get("nextStepDueDate"), LocalDate.now())
                            )
                        );
                }
            }

            //OWNERSHIP
            //Can ony search by ownership criteria if the type of ownership is specified.
            if (request.getOwnershipType() != null) {
                //Can only determine ownership if we have a non null logged in user and associated
                // partner
                Partner loggedInUserPartner;
                if (loggedInUser != null
                    && (loggedInUserPartner = loggedInUser.getPartner()) != null) {

                    switch (request.getOwnershipType()) {
                        case AS_JOB_CREATOR -> {
                            //If the user is a job creator then they own a candidate opportunity
                            // if they are the partner associated with the opportunity job.

                            //If opportunity job is owned by this user's partner
                            if (request.getOwnedByMyPartner() != null && request.getOwnedByMyPartner()) {
                                //Just check that the job associated with the opportunity was created
                                //by the logged in user's partner.
                                Join<Object, Object> jobOpp = opp.join("jobOpp");
                                conjunction.getExpressions().add(builder.equal(
                                    jobOpp.get("jobCreator").get("id"), loggedInUserPartner.getId())
                                );
                            } else {
                                //If owned by this user (ie by logged in user)
                                if (request.getOwnedByMe() != null && request.getOwnedByMe()) {
                                    Join<Object, Object> jobOpp = opp.join("jobOpp");
                                    //Not null contact user is me or I am createdBy user
                                    final Predicate matchContactUser = builder.and(
                                        builder.isNotNull(jobOpp.get("contactUser")),
                                        builder.equal(jobOpp.get("contactUser").get("id"),
                                            loggedInUser.getId())
                                    );
                                    final Predicate matchCreatingUser = builder.and(
                                        builder.isNotNull(jobOpp.get("createdBy")),
                                        builder.equal(jobOpp.get("createdBy").get("id"),
                                            loggedInUser.getId())
                                    );
                                    conjunction.getExpressions().add(
                                        builder.or(matchContactUser, matchCreatingUser)
                                    );
                                }
                            }
                        }
                        case AS_SOURCE_PARTNER -> {
                            if (loggedInUserPartner.isSourcePartner()) {
                                //If the user is a source partner then they own a candidate opportunity if they are
                                //the partner associated with the opportunity candidate.

                                //If opportunity candidate owned by this user's source partner
                                if (request.getOwnedByMyPartner() != null && request.getOwnedByMyPartner()) {
                                    //Just check that the candidate associated with the opportunity is managed
                                    //by the logged in user's partner.
                                    Join<Object, Object> partner = getOppCandidatePartnerJoin(opp);
                                    conjunction.getExpressions().add(
                                        builder.equal(partner.get("id"), loggedInUserPartner.getId())
                                    );
                                } else {
                                    //If owned by this user (ie by logged in user)
                                    if (request.getOwnedByMe() != null && request.getOwnedByMe()) {
                                        //In other words where the candidate opportunity's associated job
                                        //is one of the jobs that the logged in user has been nominated by their
                                        //partner to be the contact for.

                                        //Candidate must be owned by user's partner
                                        Join<Object, Object> partner = getOppCandidatePartnerJoin(opp);
                                        conjunction.getExpressions().add(
                                            builder.equal(partner.get("id"), loggedInUserPartner.getId())
                                        );

                                        //And...
                                        //The job for the candidate opp is in the jobs that logged in user is
                                        //the source contact for.
                                        //  or (if I am the default partner contact)
                                        //Nobody else is nominated as the contact, so use me
                                        Predicate ors = builder.disjunction();

                                        Long partnerId = loggedInUserPartner.getId();
                                        Long userId = loggedInUser.getId();

                                        //Create the Select subquery - giving all the jobs the user is contact for
                                        Subquery<SalesforceJobOpp> usersJobs = query.subquery(
                                            SalesforceJobOpp.class);
                                        Root<PartnerJobRelation> pjr = usersJobs.from(PartnerJobRelation.class);
                                        usersJobs.select(pjr.get("job")).where(
                                            builder.and(
                                                builder.equal(pjr.get("partner").get("id"), partnerId),
                                                builder.equal(pjr.get("contact").get("id"), userId)
                                            )
                                        );
                                        //Get the opp's jobOpp and check whether it is one of the above jobs
                                        Join<Object, Object> jobOpp = opp.join("jobOpp");
                                        ors.getExpressions().add(
                                            builder.in(jobOpp).value(usersJobs)
                                        );

                                        //Special case if I am the default partner contact

                                        //If so, we can add an "or" to the where clause if nobody else from this
                                        //partner has been nominated has been nominated as contact.
                                        User defaultContact = loggedInUserPartner.getDefaultContact();
                                        if (defaultContact != null && userId.equals(defaultContact.getId())) {
                                            //I am the default partner user.
                                            //If nobody else from this partner is contact for this job, then
                                            //it defaults to me
                                            Subquery<User> contact = query.subquery(User.class);
                                            Root<PartnerJobRelation> pjrc = contact.from(
                                                PartnerJobRelation.class);
                                            contact.select(pjrc.get("contact")).where(
                                                builder.and(
                                                    builder.equal(pjrc.get("partner").get("id"), partnerId),
                                                    builder.equal(pjrc.get("job").get("id"), jobOpp.get("id"))
                                                )
                                            );
                                            ors.getExpressions().add(builder.isNull(contact));
                                        }

                                        if (!ors.getExpressions().isEmpty()) {
                                            conjunction.getExpressions().add(ors);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
