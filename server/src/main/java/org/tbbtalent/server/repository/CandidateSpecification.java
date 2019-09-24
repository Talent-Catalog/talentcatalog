package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

import javax.persistence.criteria.Predicate;

public class CandidateSpecification {

    public static Specification<Candidate> buildSearchQuery(final SearchCandidateRequest request) {
        return (user, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(user.get("candidateNumber")), likeMatchTerm),
                                builder.like(builder.lower(user.get("firstName")), likeMatchTerm),
                                builder.like(builder.lower(user.get("lastName")), likeMatchTerm),
                                builder.like(builder.lower(user.get("email")), likeMatchTerm)
                        ));
            }

            if (!StringUtils.isBlank(request.getStatus())) {
                conjunction.getExpressions().add(
                        builder.equal(user.get("status"), Status.valueOf(request.getStatus()))
                        );
            }


            if(request.getRegisteredWithUN() != null) {
                conjunction.getExpressions().add(
                        builder.equal(user.get("registeredWithUN"), request.getRegisteredWithUN())
                );
            }

            return conjunction;
        };
    }

}
