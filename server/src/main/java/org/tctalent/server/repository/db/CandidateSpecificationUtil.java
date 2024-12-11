/*
 * Copyright (c) 2024 Talent Catalog.
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

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Sort;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.request.PagedSearchRequest;

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
                                               Join<Object, Object> partner,
                                               Join<Object, Object> nationality,
                                               Join<Object, Object> country,
                                               Join<Object, Object> educationLevel) {
        List<Order> orders = new ArrayList<>();
        String[] sort = request.getSortFields();
        boolean idSort = false;
        if (sort != null) {
            for (String property : sort) {

                Join<Object, Object> join = null;
                String subProperty;
                if (property.startsWith("user.partner.")) {
                    join = partner;
                    subProperty = property.replaceAll("user.partner.", "");
                } else if (property.startsWith("user.")) {
                    join = user;
                    subProperty = property.replaceAll("user.", "");
                } else if (property.startsWith("nationality.")) {
                    join = nationality;
                    subProperty = property.replaceAll("nationality.", "");
                } else if (property.startsWith("country.")) {
                    join = country;
                    subProperty = property.replaceAll("country.", "");
                } else if (property.startsWith("maxEducationLevel.")) {
                    join = educationLevel;
                    subProperty = property.replaceAll("maxEducationLevel.", "");
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
            orders.add(builder.desc(candidate.get("id")));
        }
        return orders;
    }

}
