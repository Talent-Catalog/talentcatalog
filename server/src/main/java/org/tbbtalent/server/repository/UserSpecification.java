package org.tbbtalent.server.repository;

import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.user.SearchUserRequest;

import javax.persistence.criteria.Predicate;

public class UserSpecification {

    public static Specification<User> buildSearchQuery(final SearchUserRequest request) {
        return (user, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(user.get("firstName")), likeMatchTerm),
                                builder.like(builder.lower(user.get("lastName")), likeMatchTerm),
                                builder.like(builder.lower(user.get("email")), likeMatchTerm),
                                builder.like(builder.lower(user.get("username")), likeMatchTerm)
                        ));
            }

//            if (request.getRole() != null){
//                conjunction.getExpressions().add(builder.equal(user.get("role"), request.getRole()));
//            }
            // ROLE SEARCH
            if (!Collections.isEmpty(request.getRole())) {
                conjunction.getExpressions().add(
                        builder.isTrue(user.get("role").in(request.getRole()))
                );
            }

            if (request.getStatus() != null){
                conjunction.getExpressions().add(builder.equal(user.get("status"), request.getStatus()));
            }

            return conjunction;
        };
    }

}
