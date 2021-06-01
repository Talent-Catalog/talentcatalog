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

import static org.tbbtalent.server.repository.db.CandidateSpecificationUtil.getOrderByOrders;

import io.jsonwebtoken.lang.Collections;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateAttachment;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.model.db.CandidateJobExperience;
import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.model.db.CandidateOccupation;
import org.tbbtalent.server.model.db.CandidateReviewStatusItem;
import org.tbbtalent.server.model.db.CandidateSkill;
import org.tbbtalent.server.model.db.CandidateStatus;
import org.tbbtalent.server.model.db.EducationLevel;
import org.tbbtalent.server.model.db.EducationMajor;
import org.tbbtalent.server.model.db.Language;
import org.tbbtalent.server.model.db.LanguageLevel;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.model.db.ReviewStatus;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.SearchType;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

public class CandidateSpecification {

    public static Specification<Candidate> buildSearchQuery(
            final SearchCandidateRequest request, @Nullable User loggedInUser) {
        return (candidate, query, builder) -> {
            
            //To better understand this code, look at the simpler but similar
            //GetSavedListCandidatesQuery. JC - The Programmer's Friend.
            
            Predicate conjunction = builder.conjunction();
            Join<Object, Object> user = null;
            Join<Object, Object> nationality = null;
            Join<Object, Object> country = null;
            Join<Candidate, CandidateEducation> candidateEducations = null;
            Join<Candidate, CandidateOccupation> candidateOccupations = null;
            Join<CandidateOccupation, Occupation> occupation = null;
            Join<Candidate, CandidateJobExperience> candidateJobExperiences = null;
            Join<Candidate, CandidateSkill> candidateSkills = null;
            Join<Candidate, CandidateAttachment> candidateAttachments = null;

            //CreatedBy date > from date is only set in the watcher notification code.
            if (request.getFromDate() != null) {
                conjunction.getExpressions().add(builder.greaterThanOrEqualTo(
                        candidate.get("createdDate"), 
                        getOffsetDateTime(
                                request.getFromDate(),LocalTime.MIN, request.getTimezone()))
                );
            }

            query.distinct(true);

            /*
              My theory on the reason for this - JC
              
              The main purpose is do the fetches which means that the returned
              results contain the user, nationality and country entities.
              
              Those fetches are performed by a join, which can also be reused 
              to do the sorting and other filters.

              This is much more efficient than making those attributes fetched 
              EAGERLY. Not 100% sure why, but it is.
              
              See https://thorben-janssen.com/hibernate-tip-left-join-fetch-join-criteriaquery/
              which uses this kind of code.   
             */
            boolean isCountQuery = query.getResultType().equals(Long.class); 
            if (!isCountQuery) {
                //Manage sorting
                Fetch<Object, Object> userFetch = candidate.fetch("user", JoinType.INNER);
                user = (Join<Object, Object>) userFetch;

                Fetch<Object, Object> nationalityFetch = candidate.fetch("nationality");
                nationality = (Join<Object, Object>) nationalityFetch;

                Fetch<Object, Object> countryFetch = candidate.fetch("country");
                country = (Join<Object, Object>) countryFetch;

                List<Order> orders = getOrderByOrders(request, candidate, builder, 
                        user, nationality, country);

                query.orderBy(orders);

            } else {
                //Count query - sort doesn't matter
                user = candidate.join("user");
                nationality = candidate.join("nationality");
                country = candidate.join("country");
            }
            
            //Review status
            //Only saved searches support review status - ie has this candidate 
            //been reviewed as belonging in this saved search.
            //We want candidates whose MOST RECENT review status for this search id is in filtered statuses.
            if (request.getSavedSearchId() != null) {
                if (CollectionUtils.isNotEmpty(request.getReviewStatusFilter())) {
                    Predicate pendingPredicate = null;
                    if (request.getReviewStatusFilter().contains(ReviewStatus.unverified)) {
                        Subquery<Candidate> sq = query.subquery(Candidate.class);
                        Root<Candidate> subCandidate = sq.from(Candidate.class);
                        /*
                        select Candidate from Candidate join 
                        candidateReviewStatusItems join 
                        SavedSearch where savedSearchid = requestid
                         pendingPredicate = candidate id not in the result of the query
                         
                         Is this just defaulting everything else as pending? 
                         */
                        Join<Object, Object> subReviewStatus = subCandidate.join("candidateReviewStatusItems");
                        Join<Object, Object> subSavedSearch = subReviewStatus.join("savedSearch");
                        sq.select(subCandidate).where(builder.equal(subSavedSearch.get("id"), request.getSavedSearchId()));
                        pendingPredicate = builder.not(builder.in(candidate.get("id")).value(sq));
                    }

                    Subquery<Candidate> sq = query.subquery(Candidate.class);
                    Root<Candidate> subCandidate = sq.from(Candidate.class);
                    Join<Object, Object> subReviewStatus = subCandidate.join("candidateReviewStatusItems");
                    Join<Object, Object> subSavedSearch = subReviewStatus.join("savedSearch");
                    sq.select(subCandidate).where(
                            builder.and(
                                    builder.equal(subSavedSearch.get("id"), request.getSavedSearchId()), 
                                    subReviewStatus.get("reviewStatus").in(request.getReviewStatusFilter())
                            )
                    );
                    
                    Predicate statusPredicate = builder.in(candidate.get("id")).value(sq);

                    if (pendingPredicate != null) {
                        conjunction.getExpressions().add(builder.and(builder.or(pendingPredicate, statusPredicate)));
                    } else {
                        conjunction.getExpressions().add(statusPredicate);
                    }
                } else if (query.getResultType().equals(Candidate.class)) {
                    Fetch<Candidate, CandidateReviewStatusItem> candidateReviewStatusItem = candidate.fetch("candidateReviewStatusItems", JoinType.LEFT);
                    Fetch<CandidateReviewStatusItem, SavedSearch> savedSearch = candidateReviewStatusItem.fetch("savedSearch", JoinType.LEFT);
                }
            }

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())) {
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String[] splittedText = lowerCaseMatchTerm.split("\\s+|,\\s*|\\.\\s*");
                List<Predicate> predicates = new ArrayList<>();
                candidateJobExperiences = candidateJobExperiences != null ? candidateJobExperiences : candidate.join("candidateJobExperiences", JoinType.LEFT);
                candidateSkills = candidateSkills != null ? candidateSkills : candidate.join("candidateSkills", JoinType.LEFT);
                candidateAttachments = candidateAttachments != null ? candidateAttachments : candidate.join("candidateAttachments", JoinType.LEFT);
                candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;
                candidateOccupations = candidate.join("candidateOccupations", JoinType.LEFT);
                occupation = candidateOccupations.join("occupation", JoinType.LEFT);

                for (String s : splittedText) {
                    String likeMatchTerm = "%" + s + "%";
                    predicates.add(builder.or(
                            builder.like(builder.lower(candidate.get("candidateNumber")), lowerCaseMatchTerm),
                            builder.like(builder.lower(user.get("firstName")), likeMatchTerm),
                            builder.like(builder.lower(user.get("lastName")), likeMatchTerm),
                            builder.like(builder.lower(user.get("email")), likeMatchTerm),
                            builder.like(builder.lower(candidate.get("phone")), lowerCaseMatchTerm),
                            builder.like(builder.lower(candidate.get("whatsapp")), lowerCaseMatchTerm),
                            builder.like(builder.lower(candidate.get("additionalInfo")), likeMatchTerm),
                            builder.like(builder.lower(candidateJobExperiences.get("description")), likeMatchTerm),
                            builder.like(builder.lower(candidateJobExperiences.get("role")), likeMatchTerm),
                            builder.like(builder.lower(candidateEducations.get("courseName")), likeMatchTerm),
                            builder.like(builder.lower(candidateOccupations.get("migrationOccupation")), likeMatchTerm),
                            builder.like(builder.lower(candidateSkills.get("skill")), likeMatchTerm),
                            builder.like(builder.lower(occupation.get("name")), likeMatchTerm)
                    ));

                    // This is adding an OR statement IF the keyword search includes uploaded files AND cv is true.
                    // May be a better way to do this, but it works.
//                    if(BooleanUtils.isTrue(request.getIncludeUploadedFiles())) {
//                        predicates.get(0).getExpressions().add(builder.and(
//                                builder.isTrue(candidateAttachments.get("cv")),
//                                builder.like(builder.lower(candidateAttachments.get("textExtract")), likeMatchTerm)));
//
//                    }
                }
                if (predicates.size() > 1) {
                    conjunction.getExpressions().add(builder.and(builder.and(predicates.toArray(new Predicate[0]))));
                } else {
                    conjunction.getExpressions().add(builder.and(predicates.toArray(new Predicate[0])));
                }


            }
            // STATUS SEARCH
            if (!Collections.isEmpty(request.getStatuses())) {
                List<CandidateStatus> statuses = request.getStatuses();
                conjunction.getExpressions().add(
                    builder.isTrue(candidate.get("status").in(statuses)));
            }

            // Occupations SEARCH
            if (!Collections.isEmpty(request.getOccupationIds())) {
                candidateOccupations = candidateOccupations != null ? candidateOccupations: candidate.join("candidateOccupations", JoinType.LEFT);
                occupation = occupation != null ? occupation : candidateOccupations.join("occupation", JoinType.LEFT);

                conjunction.getExpressions().add(
                        occupation.get("id").in(request.getOccupationIds())
                );

                //Min / Max Age
                if (request.getMinYrs() != null) {
                    Integer minYrs = request.getMinYrs();
                    conjunction.getExpressions().add(builder.and(
                            builder.greaterThanOrEqualTo(candidateOccupations.get("yearsExperience"), minYrs),
                            builder.isTrue(occupation.get("id").in(request.getOccupationIds()))));
                }

                if (request.getMaxYrs() != null) {
                    Integer maxYrs = request.getMaxYrs();
                    conjunction.getExpressions().add(builder.and(
                            builder.lessThanOrEqualTo(candidateOccupations.get("yearsExperience"), maxYrs),
                            builder.isTrue(occupation.get("id").in(request.getOccupationIds()))));
                }
            }

            // NATIONALITY SEARCH
            if (!Collections.isEmpty(request.getNationalityIds())) {
                if (request.getNationalitySearchType() == null || SearchType.or.equals(request.getNationalitySearchType())) {
                    conjunction.getExpressions().add(
                            builder.isTrue(candidate.get("nationality").in(request.getNationalityIds()))
                    );
                } else {
                    conjunction.getExpressions().add(candidate.get("nationality").in(request.getNationalityIds()).not()
                    );
                }
            }

            // COUNTRY SEARCH
            if (!Collections.isEmpty(request.getCountryIds())) {
                conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("country").in(request.getCountryIds()))
                );
            } else if (loggedInUser != null &&
                    !Collections.isEmpty(loggedInUser.getSourceCountries())) {
                conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("country").in(loggedInUser.getSourceCountries()))
                );
            }

            // GENDER SEARCH
            if (request.getGender() != null) {
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

            //Min / Max Age
            if (request.getMinAge() != null) {
                LocalDate minDob = LocalDate.now().minusYears(request.getMinAge() + 1);
                conjunction.getExpressions().add(builder.or(builder.lessThanOrEqualTo(candidate.get("dob"), minDob), builder.isNull(candidate.get("dob"))));
            }

            if (request.getMaxAge() != null) {
                LocalDate maxDob = LocalDate.now().minusYears(request.getMaxAge() + 1);

                conjunction.getExpressions().add(builder.or(builder.greaterThan(candidate.get("dob"), maxDob), builder.isNull(candidate.get("dob"))));
            }

            // EDUCATION LEVEL SEARCH
            if (request.getMinEducationLevel() != null) {
                Join<Candidate, EducationLevel> educationLevel = candidate.join("maxEducationLevel", JoinType.LEFT);
                conjunction.getExpressions().add(
                        builder.greaterThanOrEqualTo(educationLevel.get("level"), request.getMinEducationLevel())
                );
            }

            // MAJOR SEARCH
            if (!Collections.isEmpty(request.getEducationMajorIds())) {
                candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;
                Join<Candidate, EducationMajor> major = candidateEducations.join("educationMajor", JoinType.LEFT);
                Join<Candidate, EducationMajor> migrationMajor = candidate.join("migrationEducationMajor", JoinType.LEFT);

                conjunction.getExpressions().add(builder.or(
                        builder.isTrue(major.get("id").in(request.getEducationMajorIds())),
                        builder.isTrue(migrationMajor.get("id").in(request.getEducationMajorIds())))
                );
            }

            // LANGUAGE SEARCH
            if (request.getEnglishMinSpokenLevel() != null || request.getEnglishMinWrittenLevel() != null || request.getOtherLanguageId() != null
                    || request.getOtherMinSpokenLevel() != null || request.getOtherMinWrittenLevel() != null) {
                Join<Candidate, CandidateLanguage> candidateLanguages = candidate.join("candidateLanguages", JoinType.LEFT);
                Join<CandidateLanguage, LanguageLevel> writtenLevel = candidateLanguages.join("writtenLevel", JoinType.LEFT);
                Join<CandidateLanguage, LanguageLevel> spokenLevel = candidateLanguages.join("spokenLevel", JoinType.LEFT);
                Join<CandidateLanguage, Language> language = candidateLanguages.join("language", JoinType.LEFT);
                if (request.getEnglishMinWrittenLevel() != null && request.getEnglishMinSpokenLevel() != null) {
                    conjunction.getExpressions().add(builder.and(builder.equal(builder.lower(language.get("name")), "english"),
                            builder.greaterThanOrEqualTo(writtenLevel.get("level"), request.getEnglishMinWrittenLevel()),
                            builder.greaterThanOrEqualTo(spokenLevel.get("level"), request.getEnglishMinSpokenLevel())));
                } else if (request.getEnglishMinWrittenLevel() != null) {
                    conjunction.getExpressions().add(builder.and(builder.equal(builder.lower(language.get("name")), "english"),
                            builder.greaterThanOrEqualTo(writtenLevel.get("level"), request.getEnglishMinWrittenLevel())));
                } else if (request.getEnglishMinSpokenLevel() != null) {
                    conjunction.getExpressions().add(builder.and(builder.equal(builder.lower(language.get("name")), "english"),
                            builder.greaterThanOrEqualTo(spokenLevel.get("level"), request.getEnglishMinSpokenLevel())));
                }
                if (request.getOtherLanguageId() != null) {
                    if (request.getOtherMinSpokenLevel() != null && request.getOtherMinWrittenLevel() != null) {
                        conjunction.getExpressions().add(builder.and(builder.equal(language.get("id"), request.getOtherLanguageId()),
                                builder.greaterThanOrEqualTo(writtenLevel.get("level"), request.getOtherMinWrittenLevel()),
                                builder.greaterThanOrEqualTo(spokenLevel.get("level"), request.getOtherMinSpokenLevel())));
                    } else if (request.getOtherMinSpokenLevel() != null) {
                        conjunction.getExpressions().add(builder.and(builder.equal(language.get("id"), request.getOtherLanguageId()),
                                builder.greaterThanOrEqualTo(spokenLevel.get("level"), request.getOtherMinSpokenLevel())));
                    } else if (request.getOtherMinWrittenLevel() != null) {
                        conjunction.getExpressions().add(builder.and(builder.equal(language.get("id"), request.getOtherLanguageId()),
                                builder.greaterThanOrEqualTo(writtenLevel.get("level"), request.getOtherMinWrittenLevel())));
                    }
                }


            }


            return conjunction;
        };
    }

    private static OffsetDateTime getOffsetDateTime(LocalDate localDate, LocalTime time, String timezone) {
        return OffsetDateTime.of(localDate, time, !StringUtils.isBlank(timezone) ? ZoneId.of(timezone).getRules().getOffset(Instant.now()) : ZoneOffset.UTC);
    }

}
