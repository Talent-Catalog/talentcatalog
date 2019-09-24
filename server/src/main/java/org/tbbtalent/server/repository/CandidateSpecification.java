package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

public class CandidateSpecification {

    public static Specification<Candidate> buildSearchQuery(final SearchCandidateRequest request) {
        return (user, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            Join<Candidate, CandidateLanguage> candidateLanguages = user.join("candidateLanguages");

            // KEYWORD SEARCH
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
            // STATUS SEARCH
            if (!StringUtils.isBlank(request.getStatus())) {
                conjunction.getExpressions().add(
                        builder.equal(user.get("status"), Status.valueOf(request.getStatus()))
                        );
            }

            // UN REGISTERED SEARCH
            if(request.getRegisteredWithUN() != null) {
                conjunction.getExpressions().add(
                        builder.equal(user.get("registeredWithUN"), request.getRegisteredWithUN())
                );
            }

            // NATIONALITY SEARCH
            if(request.getNationalityId() != null) {
//                System.out.println(request.getNationalityId());
                conjunction.getExpressions().add(
                        builder.equal(user.get("nationality"), request.getNationalityId())
                );
            }

            // COUNTRY SEARCH
            if(request.getCountryId() != null) {
                conjunction.getExpressions().add(
                        builder.equal(user.get("country"), request.getCountryId())
                );
            }

            // GENDER SEARCH
            if(!StringUtils.isBlank(request.getGender())) {
                conjunction.getExpressions().add(
                        builder.equal(user.get("gender"), request.getGender())
                );
            }

            // EDUCATION LEVEL SEARCH
            if(!StringUtils.isBlank(request.getEducationLevel())) {
                conjunction.getExpressions().add(
                        builder.equal(user.get("educationLevel"), request.getEducationLevel())
                );
            }

            // LANGUAGE SEARCH
            if(request.getCandidateLanguageId() != null) {
                System.out.println(request.getCandidateLanguageId());
                conjunction.getExpressions().add(
                        builder.equal(candidateLanguages.get("id"), request.getCandidateLanguageId())
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
