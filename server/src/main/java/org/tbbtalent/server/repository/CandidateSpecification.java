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
import java.time.*;

public class CandidateSpecification {

    public static Specification<Candidate> buildSearchQuery(final SearchCandidateRequest request) {
        return (candidate, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);
            Join<Candidate, User> user = candidate.join("user");
            Join<Candidate, CandidateEducation> candidateEducations = null;

            //Short lists
            if (request.getSavedSearchId() != null) {
                if (request.getShortlistStatus() != null) {
                    //Filter for short list candidates by status
                    Join<Candidate, CandidateShortlistItem> candidateShortlistItem = candidate.join("candidateShortlistItems", JoinType.LEFT);
                    Join<CandidateShortlistItem, SavedSearch> savedSearch = candidateShortlistItem.join("savedSearch", JoinType.LEFT);
                    if (request.getShortlistStatus().equals(ShortlistStatus.pending)){
                        conjunction.getExpressions().add(builder.notEqual(savedSearch.get("id"), request.getSavedSearchId()));
                    }else {
                        conjunction.getExpressions().add(
                                builder.and(builder.equal(candidateShortlistItem.get("shortlistStatus"), request.getShortlistStatus()),
                                        builder.equal(savedSearch.get("id"), request.getSavedSearchId())));
                    }
                //Fetch short lists todo only fetch for specific search
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

            // occupations SEARCH
            if(!Collections.isEmpty(request.getVerifiedOccupationIds()) || !Collections.isEmpty(request.getOccupationIds())) {
                Join<Candidate, CandidateOccupation> candidateOccupations = candidate.join("candidateOccupations", JoinType.LEFT);
                if (!Collections.isEmpty(request.getVerifiedOccupationIds())) {
                    if (request.getVerifiedOccupationSearchType().equals(SearchType.not)){
                        conjunction.getExpressions().add(builder.notEqual(builder.isTrue(candidateOccupations.get("verified")),
                                builder.isTrue(candidateOccupations.get("id").in(request.getVerifiedOccupationIds()))
                        ));
                    } else {
                        conjunction.getExpressions().add(builder.and(builder.isTrue(candidateOccupations.get("verified")),
                                builder.isTrue(candidateOccupations.get("id").in(request.getVerifiedOccupationIds()))
                        ));
                    }

                }
                if (!Collections.isEmpty(request.getOccupationIds())){
                    if (StringUtils.isBlank(request.getOrProfileKeyword())){
                        conjunction.getExpressions().add(
                                builder.isTrue(candidateOccupations.get("id").in(request.getOccupationIds()))
                        );
                    } else {
                        String lowerCaseMatchTerm = request.getOrProfileKeyword().toLowerCase();
                        String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                        Join<Candidate, CandidateJobExperience> candidateJobExperiences = candidate.join("candidateJobExperiences", JoinType.LEFT);
                        candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;

                        conjunction.getExpressions().add(builder.or(
                                builder.isTrue(candidateOccupations.get("id").in(request.getOccupationIds())),
                                builder.or(
                                                builder.like(builder.lower(candidateJobExperiences.get("description")), likeMatchTerm),
                                                builder.like(builder.lower(candidateJobExperiences.get("role")), likeMatchTerm),
                                                builder.like(builder.lower(candidateEducations.get("courseName")), likeMatchTerm)
                                ))
                        );
                    }

                }

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

            // COUNTRY SEARCH
            if(!Collections.isEmpty(request.getCountryIds())) {
                conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("country").in(request.getCountryIds()))
                );
            }

            // GENDER SEARCH
            if(!StringUtils.isBlank(request.getGender())) {
                conjunction.getExpressions().add(
                        builder.equal(candidate.get("gender"), request.getGender())
                );
            }


            //Modified From
            if (request.getLastModifiedFrom() != null) {
                conjunction.getExpressions().add(builder.greaterThanOrEqualTo(candidate.get("updatedDate"), getOffsetDateTime(request.getLastModifiedFrom(), LocalTime.MIN, request.getTimezone())));
            }

            if (request.getLastModifiedTo() != null) {
                conjunction.getExpressions().add(builder.lessThanOrEqualTo(candidate.get("updatedDate"), getOffsetDateTime(request.getLastModifiedTo(), LocalTime.MAX, request.getTimezone())));
            }

            //Registered From - change to registered
            if (request.getRegisteredFrom() != null) {
                conjunction.getExpressions().add(builder.greaterThanOrEqualTo(candidate.get("registeredDate"), getOffsetDateTime(request.getRegisteredFrom(), LocalTime.MIN, request.getTimezone())));
            }

            if (request.getRegisteredTo() != null) {
                conjunction.getExpressions().add(builder.lessThanOrEqualTo(candidate.get("registeredDate"), getOffsetDateTime(request.getRegisteredTo(), LocalTime.MAX, request.getTimezone())));
            }

            //Min / Max Age
            if (request.getMinAge() != null) {
                LocalDate minDob = LocalDate.now().minusYears(request.getMinAge()+1);
                conjunction.getExpressions().add(builder.lessThanOrEqualTo(candidate.get("dob"), minDob));
            }

            if (request.getMaxAge() != null) {
                LocalDate maxDob = LocalDate.now().minusYears(request.getMaxAge()+1);

                conjunction.getExpressions().add(builder.greaterThan(candidate.get("dob"), maxDob));
            }

            // EDUCATION LEVEL SEARCH
            if(request.getMinEducationLevel() != null) {
                Join<Candidate, EducationLevel> educationLevel = candidate.join("maxEducationLevel", JoinType.LEFT);
                conjunction.getExpressions().add(
                        builder.greaterThanOrEqualTo(educationLevel.get("level"), request.getMinEducationLevel())
                );
            }

            // MAJOR SEARCH
            if(!Collections.isEmpty(request.getEducationMajorIds())) {
                candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;
                Join<Candidate, EducationMajor> major = candidateEducations.join("educationMajor", JoinType.LEFT);

                conjunction.getExpressions().add(
                        builder.isTrue(major.get("id").in(request.getEducationMajorIds()))
                );
            }

            // LANGUAGE SEARCH
            if(request.getEnglishMinSpokenLevel() != null || request.getEnglishMinWrittenLevel() != null || request.getOtherLanguageId() != null
                        || request.getOtherMinSpokenLevel() != null || request.getOtherMinWrittenLevel() != null) {
                Join<Candidate, CandidateLanguage> candidateLanguages = candidate.join("candidateLanguages", JoinType.LEFT);
                Join<CandidateLanguage, LanguageLevel> writtenLevel = candidateLanguages.join("writtenLevel", JoinType.LEFT);
                Join<CandidateLanguage, LanguageLevel> spokenLevel = candidateLanguages.join("spokenLevel", JoinType.LEFT);
                Join<CandidateLanguage, Language> language = candidateLanguages.join("language", JoinType.LEFT);
                if (request.getEnglishMinWrittenLevel() != null && request.getEnglishMinSpokenLevel() != null){
                    conjunction.getExpressions().add(builder.and(builder.equal(builder.lower(language.get("name")), "english"),
                            builder.greaterThanOrEqualTo(writtenLevel.get("level"), request.getEnglishMinSpokenLevel()),
                            builder.greaterThanOrEqualTo(spokenLevel.get("level"), request.getEnglishMinWrittenLevel())));
                }
                if (request.getOtherLanguageId() != null && request.getOtherMinSpokenLevel() != null && request.getOtherMinWrittenLevel() != null){
                    conjunction.getExpressions().add(builder.and(builder.equal(language.get("id"), request.getOtherLanguageId()),
                            builder.greaterThanOrEqualTo(writtenLevel.get("level"), request.getOtherMinWrittenLevel()),
                            builder.greaterThanOrEqualTo(spokenLevel.get("level"), request.getOtherMinSpokenLevel())));
                }
            }


            return conjunction;
        };
    }

    private static OffsetDateTime getOffsetDateTime(LocalDate localDate, LocalTime time, String timezone){
        return OffsetDateTime.of(localDate, time, !StringUtils.isBlank(timezone) ? ZoneId.of(timezone).getRules().getOffset(Instant.now()) : ZoneOffset.UTC);
    }

}
