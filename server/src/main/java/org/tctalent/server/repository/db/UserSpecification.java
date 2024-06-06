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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.user.SearchUserRequest;

/*
MODEL: Simple join in specification
 */

public class UserSpecification {

    public static Specification<User> buildSearchQuery(final SearchUserRequest request) {
        return (user, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                predicates.add(
                        builder.or(
                                builder.like(builder.lower(user.get("firstName")), likeMatchTerm),
                                builder.like(builder.lower(user.get("lastName")), likeMatchTerm),
                                builder.like(builder.lower(user.get("email")), likeMatchTerm),
                                builder.like(builder.lower(user.get("username")), likeMatchTerm)
                        ));
            }

            // ROLE SEARCH
            if (Collections.isEmpty(request.getRole())) {
                //If no roles specified, just exclude candidate users.
                predicates.add(
                    builder.notEqual(user.get("role"), Role.user)
                );
            } else {
                predicates.add(
                        builder.isTrue(user.get("role").in(request.getRole()))
                );
            }

            // PARTNER
            if (request.getPartnerId() != null){
                Join<Object, Object> partner = user.join("partner", JoinType.LEFT);
                predicates.add(builder.equal(partner.get("id"), request.getPartnerId()));
            }

            // STATUS
            if (request.getStatus() != null){
                predicates.add(builder.equal(user.get("status"), request.getStatus()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
