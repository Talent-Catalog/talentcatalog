/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.PagedSearchRequest;

/**
 * Extract some common utility methods
 *
 * @author John Cameron
 */
public class CandidateSpecificationUtil {

    public static List<Order> getOrderByOrders(PagedSearchRequest request,
                                               Root<Candidate> candidate,
                                               CriteriaBuilder builder,
                                               Join<Object, Object> user,
                                               Join<Object, Object> nationality,
                                               Join<Object, Object> country) {
        List<Order> orders = new ArrayList<>();
        String[] sort = request.getSortFields();
        boolean idSort = false;
        if (sort != null) {
            for (String property : sort) {

                Join<Object, Object> join = null;
                String subProperty;
                if (property.startsWith("user.")) {
                    join = user;
                    subProperty = property.replaceAll("user.", "");
                } else if (property.startsWith("nationality.")) {
                    join = nationality;
                    subProperty = property.replaceAll("nationality.", "");
                } else if (property.startsWith("country.")) {
                    join = country;
                    subProperty = property.replaceAll("country.", "");
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
                    path = candidate.get(subProperty);
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
            orders.add(builder.asc(candidate.get("id")));
        }
        return orders;
    }
    
}
