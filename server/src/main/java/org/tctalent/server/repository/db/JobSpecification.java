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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.request.PagedSearchRequest;
import org.tctalent.server.request.job.SearchJobRequest;
import org.tctalent.server.util.SpecificationHelper;

/**
 * Specification for sorting and searching {@link SalesforceJobOpp} entities
 * <p/>
 * MODEL - JPA Specification using table join and sorting
 */
public class JobSpecification {

    public static Specification<SalesforceJobOpp> buildSearchQuery(
        final SearchJobRequest request, User loggedInUser) {
        return (job, query, builder) -> {
            Predicate conjunction = builder.conjunction();

            /*
              Note that there are two ways of retrieving a join.
              One is simply to call the join method - and that is what we use for Count queries.

              The other way is to use the "fetch" method, and then cast that to a join.
              This way is used for non count queries where we want to retrieve and optionally
              sort the results.

              This is much more efficient than making those attributes fetched EAGERLY.
              Not 100% sure why, but it is.

              See https://thorben-janssen.com/hibernate-tip-left-join-fetch-join-criteriaquery/
              which uses this kind of code.

              You might naturally think that we should always use the fetch way of getting a join
              but unfortunately the database does not like doing a fetch with count queries.
              An exception is thrown if you try it. So we need to use both ways of getting
              the join depending on whether the request is a count query.
             */

            boolean isCountQuery = query.getResultType().equals(Long.class);
            if (!isCountQuery) {
                Fetch<Object, Object> listFetch = job.fetch("submissionList", JoinType.INNER);
                Join<Object, Object> submissionList = (Join<Object, Object>) listFetch;

                //Manage sorting for non count queries
                List<Order> ordering = getOrdering(request, job, builder, submissionList);
                query.orderBy(ordering);
            }

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(job.get("name")), likeMatchTerm)
                        ));
            }

            final Boolean showActiveStages = request.getActiveStages();
            final boolean showClosed = request.getSfOppClosed() != null && request.getSfOppClosed();
            final boolean showUnpublished = request.getPublished() != null && !request.getPublished();

            boolean isStageFilterActive = false;

            // STAGE
            List<JobOpportunityStage> stages = request.getStages();
            if (!Collections.isEmpty(stages)) {
                conjunction.getExpressions().add(builder.isTrue(job.get("stage").in(stages)));
                isStageFilterActive = true;
            }

            // DESTINATION
            List<Long> destinationIds = request.getDestinationIds();
            if (!Collections.isEmpty(destinationIds)) {
                conjunction.getExpressions().add(builder.isTrue(job.get("country").in(destinationIds)));
            }

            //ACTIVE STAGES (ignored if doing stage filtering)
            if (!isStageFilterActive) {
                //Note that we only check "active stages" if explicit stages have not been requested.
                if (showActiveStages != null) {
                    //Only apply filter when we just want to display active stages
                    //Otherwise, if false, it will ONLY display inactive stages which we don't want
                    if (showActiveStages) {
                        final Predicate activePredicate = builder.between(job.get("stageOrder"),
                            JobOpportunityStage.candidateSearch.ordinal(),
                            JobOpportunityStage.jobOffer.ordinal());
                        if (showClosed || showUnpublished) {
                            //When active stages are requested as well as closed or unpublished,
                            //we need union of all that match active predicate, or that are closed
                            //or that are unpublished.
                            //ie The user might want to show jobs which are active OR closed OR
                            //unpublished
                            Predicate disjunction = builder.disjunction();
                            disjunction.getExpressions().add(activePredicate);
                            if (showClosed) {
                                disjunction.getExpressions().add(
                                    builder.equal(job.get("closed"), true));
                            }
                            if (showUnpublished) {
                                disjunction.getExpressions().add(
                                    builder.isNull(job.get("publishedDate")));
                            }
                            conjunction.getExpressions().add(disjunction);
                        } else {
                            conjunction.getExpressions().add(activePredicate);
                        }
                    }
                }
            }

            //CLOSED (ignored if we are doing stage filtering)
            //Only apply filter if we want to exclude closed opps.
            //Otherwise the filter when true will only show closed opps - which we don't want.
            if (!isStageFilterActive && !showClosed) {
                conjunction.getExpressions().add(builder.equal(job.get("closed"), false));
            }

            //UNREAD MESSAGES
            if (request.getWithUnreadMessages() != null && request.getWithUnreadMessages()) {

                Subquery<Long> numberOfChatsToRead = query.subquery(Long.class);
                Root<JobChat> numberOfChatsToReadRoot = numberOfChatsToRead.from(JobChat.class);

                final Predicate chatIsLinkedToJob = builder.equal(
                    numberOfChatsToReadRoot.get("jobOpp").get("id"), job.get("id"));

                //Create predicate so that we just look at chats directly associated with jobs
                List<JobChatType> belongsToJob = Arrays.asList(
                    JobChatType.JobCreatorAllSourcePartners, JobChatType.AllJobCandidates,
                    JobChatType.JobCreatorSourcePartner);
                final Predicate chatTypeBelongsToOpp = builder
                    .in(numberOfChatsToReadRoot.get("type")).value(belongsToJob);

                final Predicate chatBelongsToOpp = builder
                    .and(chatIsLinkedToJob, chatTypeBelongsToOpp);


                final Predicate oppHasUnreadChats = SpecificationHelper.hasUnreadChats(
                    loggedInUser, query, builder, numberOfChatsToRead, numberOfChatsToReadRoot,
                    chatBelongsToOpp);

                conjunction.getExpressions().add(oppHasUnreadChats);
            }

            // (starred OR owned)
            Predicate ors = builder.disjunction();

            //If owned by this user (ie by logged in user)
            if (request.getOwnedByMe() != null && request.getOwnedByMe()) {
                if (loggedInUser != null) {
                    ors.getExpressions().add(
                        builder.equal(job.get("createdBy"), loggedInUser.getId())
                    );
                    ors.getExpressions().add(
                        builder.equal(job.get("contactUser"), loggedInUser.getId())
                    );
                }
            }

            //If owned by this user's partner
            if (request.getOwnedByMyPartner() != null && request.getOwnedByMyPartner()) {
                if (loggedInUser != null) {
                    Partner loggedInUserPartner = loggedInUser.getPartner();
                    if (loggedInUserPartner != null) {
                        Join<Object, Object> owner = job.join("createdBy");
                        Join<Object, Object> partner = owner.join("partner");
                        ors.getExpressions().add(
                            builder.equal(partner.get("id"), loggedInUserPartner.getId())
                        );
                    }
                }
            }

            //If starred is specified, only supply jobs starred by the owner
            if (request.getStarred() != null && request.getStarred()) {
                if (loggedInUser != null) {
                    Join<Object, Object> users = job.join("starringUsers");
                    ors.getExpressions().add(
                        builder.equal(users.get("id"), loggedInUser.getId())
                    );
                }
            }

            if (!ors.getExpressions().isEmpty()) {
                conjunction.getExpressions().add(ors);
            }

            return conjunction;
        };
    }

    private static List<Order> getOrdering(PagedSearchRequest request,
        Root<SalesforceJobOpp> job,
        CriteriaBuilder builder, Join<Object, Object> submissionList) {

        List<Order> orders = new ArrayList<>();
        String[] sort = request.getSortFields();
        boolean idSort = false;
        if (sort != null) {
            for (String property : sort) {
                Join<Object, Object> join = null;
                String subProperty;

                if (property.startsWith("submissionList.")) {
                    join = submissionList;
                    subProperty = property.replaceAll("submissionList.", "");
                } else {
                    subProperty = property;
                    if (property.equals("id")) {
                        idSort = true;
                    }
                }

                Path<Object> path;
                if (join != null) {
                    path = join.get(subProperty);
                } else {
                    path = job.get(subProperty);
                }
                orders.add(request.getSortDirection().equals(Sort.Direction.ASC)
                    ? builder.asc(path) : builder.desc(path));
            }
        }

        //Need at least one id sort so that ordering is stable.
        //Otherwise, sorts with equal values will come out in random order,
        //which means that the contents of pages - computed at different times -
        //won't be predictable.
        if (!idSort) {
            orders.add(builder.desc(job.get("id")));
        }
        return orders;
    }

}
