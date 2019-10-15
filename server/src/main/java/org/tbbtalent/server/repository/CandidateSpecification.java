package org.tbbtalent.server.repository;

import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

import javax.persistence.criteria.Fetch;
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

            if (request.getSavedSearchId() != null) {
                if (request.getShortlistStatus() != null) {
                    Join<Candidate, CandidateShortlistItem> candidateShortlistItem = candidate.join("candidateShortlistItems", JoinType.LEFT);
                    Join<CandidateShortlistItem, SavedSearch> savedSearch = candidateShortlistItem.join("savedSearch", JoinType.LEFT);
                    if (request.getShortlistStatus().equals(ShortlistStatus.pending)){
                        conjunction.getExpressions().add(builder.notEqual(savedSearch.get("id"), request.getSavedSearchId()));
                    }else {
                        conjunction.getExpressions().add(
                                builder.and(builder.equal(candidateShortlistItem.get("shortlistStatus"), request.getShortlistStatus()),
                                        builder.equal(savedSearch.get("id"), request.getSavedSearchId())));
                    }

                } else if (query.getResultType().equals(Candidate.class)) {
                    Fetch<Candidate, CandidateShortlistItem> candidateShortlistItem = candidate.fetch("candidateShortlistItems", JoinType.LEFT);
                     Fetch<CandidateShortlistItem, SavedSearch> savedSearch = candidateShortlistItem.fetch("savedSearch", JoinType.LEFT);
                }

            }
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
            if(!Collections.isEmpty(request.getStatuses())) {
                conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("status").in(request.getStatuses()))
                );
            }

            // UN REGISTERED SEARCH
            if(request.getUnRegistered() != null) {
                conjunction.getExpressions().add(
                        builder.equal(candidate.get("registeredWithUN"), request.getUnRegistered())
                );
            }

            // NATIONALITY SEARCH
            if(!Collections.isEmpty(request.getNationalityIds())) {
                conjunction.getExpressions().add(
                      builder.isTrue(candidate.get("nationality").in(request.getNationalityIds()))
                );
            }

//            // COUNTRY SEARCH
//            if(request.getCountryId() != null) {
//                conjunction.getExpressions().add(
//                        builder.equal(candidate.get("country"), request.getCountryId())
//                );
//            }

            // GENDER SEARCH
            if(!StringUtils.isBlank(request.getGender())) {
                conjunction.getExpressions().add(
                        builder.equal(candidate.get("gender"), request.getGender())
                );
            }

//            // EDUCATION LEVEL SEARCH
//            if(!StringUtils.isBlank(request.getEducationLevel())) {
//                conjunction.getExpressions().add(
//                        builder.equal(candidate.get("educationLevel"), request.getEducationLevel())
//                );
//            }

            // LANGUAGE SEARCH
//            if(request.getCandidateLanguageId() != null) {
//                System.out.println(request.getCandidateLanguageId());
//                conjunction.getExpressions().add(
//                        builder.equal(candidateLanguages.get("id"), request.getCandidateLanguageId())
//                );
//            }
            

            return conjunction;
        };
    }

}
