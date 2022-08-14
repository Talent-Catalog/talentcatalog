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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.db.Job;
import org.tbbtalent.server.request.PagedSearchRequest;
import org.tbbtalent.server.request.job.SearchJobRequest;

/**
 * Specification for sorting and searching {@link Job} entities
 * <p/>
 * MODEL - JPA Specification using table join and sorting
 */
public class JobSpecification {

    public static Specification<Job> buildSearchQuery(final SearchJobRequest request) {
        return (job, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            Fetch<Object, Object> sfJobOppFetch = job.fetch("sfJobOpp", JoinType.INNER);
            Join<Object, Object> sfJobOpp = (Join<Object, Object>) sfJobOppFetch;

            //Manage sorting for non count queries
            boolean isCountQuery = query.getResultType().equals(Long.class);
            if (!isCountQuery) {
                //Manage order of results
                List<Order> ordering = getOrdering(request, job, builder, sfJobOpp);
                query.orderBy(ordering);
            }

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(sfJobOpp.get("name")), likeMatchTerm)
                        ));
            }

            if (request.getSfOppClosed() != null){
                conjunction.getExpressions().add(builder.equal(sfJobOpp.get("closed"), request.getSfOppClosed()));
            }

            return conjunction;
        };
    }

    private static List<Order> getOrdering(PagedSearchRequest request,
        Root<Job> job,
        CriteriaBuilder builder,
        Join<Object, Object> sfJobOpp) {

        List<Order> orders = new ArrayList<>();
        String[] sort = request.getSortFields();
        boolean idSort = false;
        if (sort != null) {
            for (String property : sort) {

                Join<Object, Object> join = null;
                String subProperty;
                if (property.startsWith("sfJobOpp.")) {
                    join = sfJobOpp;
                    subProperty = property.replaceAll("sfjobOpp.", "");
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
        //Otherwise sorts with equal values will come out in random order,
        //which means that the contents of pages - computed at different times -
        //won't be predictable.
        if (!idSort) {
            orders.add(builder.desc(job.get("id")));
        }
        return orders;
    }

}
