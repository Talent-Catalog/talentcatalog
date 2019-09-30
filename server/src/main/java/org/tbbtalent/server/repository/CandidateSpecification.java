package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

public class CandidateSpecification {

    public static Specification<Candidate> buildSearchQuery(final SearchCandidateRequest request) {
        return (candidate, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);
            Join<Candidate, CandidateLanguage> candidateLanguages = candidate.join("candidateLanguages", JoinType.LEFT);
            Join<Candidate, User> user = candidate.join("user");

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(candidate.get("candidateNumber")), likeMatchTerm),
                                builder.like(builder.lower(user.get("firstName")), likeMatchTerm),
                                builder.like(builder.lower(user.get("lastName")), likeMatchTerm),
                                builder.like(builder.lower(user.get("email")), likeMatchTerm)
                        ));
            }
            // STATUS SEARCH
            if(!(request.getSelectedStatus()).isEmpty()) {
                conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("status").in(request.getSelectedStatus()))
                );
            }

            // UN REGISTERED SEARCH
            if(request.getRegisteredWithUN() != null) {
                conjunction.getExpressions().add(
                        builder.equal(candidate.get("registeredWithUN"), request.getRegisteredWithUN())
                );
            }

            // NATIONALITY SEARCH
            if(!(request.getSelectedNationalities()).isEmpty()) {
                conjunction.getExpressions().add(
                      builder.isTrue(candidate.get("nationality").in(request.getSelectedNationalities()))
                );
            }

            // COUNTRY SEARCH
            if(request.getCountryId() != null) {
                conjunction.getExpressions().add(
                        builder.equal(candidate.get("country"), request.getCountryId())
                );
            }

            // GENDER SEARCH
            if(!StringUtils.isBlank(request.getGender())) {
                conjunction.getExpressions().add(
                        builder.equal(candidate.get("gender"), request.getGender())
                );
            }

            // EDUCATION LEVEL SEARCH
            if(!StringUtils.isBlank(request.getEducationLevel())) {
                conjunction.getExpressions().add(
                        builder.equal(candidate.get("educationLevel"), request.getEducationLevel())
                );
            }

            // LANGUAGE SEARCH
            if(request.getCandidateLanguageId() != null) {
                System.out.println(request.getCandidateLanguageId());
                conjunction.getExpressions().add(
                        builder.equal(candidateLanguages.get("id"), request.getCandidateLanguageId())
                );
            }
            

            return conjunction;
        };
    }

}
