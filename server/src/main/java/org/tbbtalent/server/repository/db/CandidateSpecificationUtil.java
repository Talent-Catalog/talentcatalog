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

import org.springframework.data.domain.Sort;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.request.PagedSearchRequest;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

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
                                               Join<Object, Object> country,
                                               Join<Object, Object> educationLevel,
                                               CriteriaQuery query) {
        List<Order> orders = new ArrayList<>();
        String[] sort = request.getSortFields();
        Subquery<String> sq = null;
        boolean idSort = false;
        if (sort != null) {
            for (String property : sort) {

                Join<Object, Object> join = null;
                String subProperty = null;
                if (property.startsWith("user.")) {
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
                } else if (property.equals("ieltsScore")) {
                    subProperty = "ieltsScore";
//                    // select distinct score from candidate_exam ce left join candidate c on c.id = ce.candidate_id where ce.exam = 'IELTSGen';
//                    //select * from Candidate c inner join (select score, candidate_id from candidate_exam ce where ce.exam = 'IELTSGen') sc on c.id = sc.candidate_id order by score desc;
//                    sq = query.subquery(String.class);
//                    Root<CandidateExam> ce = sq.from(CandidateExam.class);
//                    Join<Object, Object> cand = ce.join("candidate");
//                    sq.select(ce.get("score")).where(builder.and (
//                            builder.equal((ce.get("exam")), "IELTSGen")),
//                            builder.equal((cand.get("id")), ce.get("candidateId"))
//                    );
                } else {
                    subProperty = property;
                    if (property.equals("id")) {
                        idSort = true;
                    }
                }

                Path<Object> path;
                if (subProperty.equals("ieltsScore")) {
                    orders.add(request.getSortDirection().equals(Sort.Direction.ASC)
                            ? builder.asc(candidate.get("score")) : builder.desc(candidate.get("score")));
                } else {
                    if (join != null) {
                        path = join.get(subProperty);
                    } else {
                        path = candidate.get(subProperty);
                    }
                    orders.add(request.getSortDirection().equals(Sort.Direction.ASC)
                            ? builder.asc(path) : builder.desc(path));
                }
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
