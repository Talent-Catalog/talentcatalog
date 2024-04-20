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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateFilterByOpps;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.candidate.SearchCandidateRequest;

public class CandidateSpecification {

    //Hard coded id of Country USA - nasty but temporary hack - see use below
    private static final int usaId = 6178;

    public static Specification<Candidate> buildSearchQuery(
            final SearchCandidateRequest request, @Nullable User loggedInUser,
            final @Nullable Collection<Candidate> excludedCandidates) {
        return (candidate, query, builder) -> {

            //To better understand this code, look at the simpler but similar
            //GetSavedListCandidatesQuery. JC - The Programmer's Friend.

            Predicate conjunction = builder.conjunction();

            //These joins are only created as needed - depending on the query.
            //eg candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;
            //Some joins are always needed - eg the user one, and the joins needed to support
            //sorting.
            Join<Object, Object> user = null;
            Join<Object, Object> partner = null;
            Join<Object, Object> nationality = null;
            Join<Object, Object> country = null;
            Join<Object, Object> maxEducationLevel = null;
            Join<Candidate, CandidateEducation> candidateEducations = null;
            Join<Candidate, CandidateOccupation> candidateOccupations = null;
            Join<CandidateOccupation, Occupation> occupation = null;
            Join<Candidate, CandidateJobExperience> candidateJobExperiences = null;
            Join<Candidate, CandidateSkill> candidateSkills = null;
            Join<Candidate, CandidateAttachment> candidateAttachments = null;

            query.distinct(true);

            /*
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

                Fetch<Object, Object> partnerFetch = user.fetch("partner", JoinType.INNER);
                partner = (Join<Object, Object>) partnerFetch;

                Fetch<Object, Object> nationalityFetch = candidate.fetch("nationality");
                nationality = (Join<Object, Object>) nationalityFetch;

                Fetch<Object, Object> countryFetch = candidate.fetch("country");
                country = (Join<Object, Object>) countryFetch;

                Fetch<Object, Object> educationLevelFetch = candidate.fetch("maxEducationLevel");
                maxEducationLevel = (Join<Object, Object>) educationLevelFetch;

                List<Order> orders = CandidateSpecificationUtil.getOrderByOrders(request, candidate, builder,
                        user, partner, nationality, country, maxEducationLevel);

                query.orderBy(orders);

            } else {
                //Count query - sort doesn't matter
                user = candidate.join("user");
                partner = user.join("partner");
                nationality = candidate.join("nationality");
                country = candidate.join("country");
                maxEducationLevel = candidate.join("maxEducationLevel");
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

            // EXCLUDED CANDIDATES (eg from Review Status)
            if (excludedCandidates != null && excludedCandidates.size() > 0) {
                conjunction.getExpressions().add(candidate.in(excludedCandidates).not()
                    );
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

            // COUNTRY SEARCH - taking into account user source country limitations
            // If request ids is NOT EMPTY we can just accept them because the options
            // presented to the user will be limited to the allowed source countries
            if (!Collections.isEmpty(request.getCountryIds())) {
                if (request.getCountrySearchType() == null || SearchType.or.equals(request.getCountrySearchType())) {
                    conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("country").in(request.getCountryIds()))
                    );
                } else {
                    conjunction.getExpressions().add(candidate.get("country").in(request.getCountryIds()).not());
                }
            // If request ids IS EMPTY only show source countries
            } else if (loggedInUser != null &&
                    !Collections.isEmpty(loggedInUser.getSourceCountries())) {
                conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("country").in(loggedInUser.getSourceCountries()))
                );
            }

            // PARTNER SEARCH
            if (!Collections.isEmpty(request.getPartnerIds())) {
                conjunction.getExpressions().add(
                    builder.isTrue(user.get("partner").in(request.getPartnerIds()))
                );
            }

            // REMOVE US AFGHANS FROM ALL SEARCHES.
            // This is a temporary hack for the us-afghan parolee push.
            // We want US-afghans out of the searches w/ source countries or not BUT if candidate is US SOURCE COUNTRY then in the searches.
            //if source countries is not null, check that it's not US
//            if (loggedInUser != null && !Collections.isEmpty(loggedInUser.getSourceCountries())) {
//                boolean us = loggedInUser.getSourceCountries().stream().anyMatch(c -> c.getId() == usaId);
//                if (!us) {
//                    //This is not a US user, so don't show US Afghans
//                  Join<Candidate, SurveyType> surveyType
//                      = candidate.join("surveyType", JoinType.LEFT);
//                  conjunction.getExpressions()
//                      .add(builder.or(
//                          builder.isNull(candidate.get("surveyType")),
//                          builder.notEqual(builder.lower(surveyType.get("name")), "us-afghan")
//                      ));
//                }
//            } else {
//                // if source countries is null, remove us afghans
//              Join<Candidate, SurveyType> surveyType
//                  = candidate.join("surveyType", JoinType.LEFT);
//                conjunction.getExpressions()
//                    .add(builder.or(
//                        builder.isNull(candidate.get("surveyType")),
//                        builder.notEqual(builder.lower(surveyType.get("name")), "us-afghan")
//                    ));
//            }

            // SURVEY TYPE SEARCH
            if (!Collections.isEmpty(request.getSurveyTypeIds())) {
                conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("surveyType").in(request.getSurveyTypeIds()))
                );
            }

            // REFERRER
            if (request.getRegoReferrerParam() != null && request.getRegoReferrerParam().trim().length() != 0) {
                conjunction.getExpressions().add(
                        builder.like(builder.lower(candidate.get("regoReferrerParam")), request.getRegoReferrerParam().toLowerCase())
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
            
            // MINI INTAKE COMPLETE
            if (request.getMiniIntakeCompleted() != null) {
                if(request.getMiniIntakeCompleted()) {
                    conjunction.getExpressions().add(
                        builder.isNotNull(candidate.get("miniIntakeCompletedDate"))); 
                } else {
                    conjunction.getExpressions().add(
                        builder.isNull(candidate.get("miniIntakeCompletedDate")));
                }
            }

            // FULL INTAKE COMPLETE
            if (request.getFullIntakeCompleted() != null) {
                if(request.getFullIntakeCompleted()) {
                    conjunction.getExpressions().add(
                        builder.isNotNull(candidate.get("fullIntakeCompletedDate")));
                } else {
                    conjunction.getExpressions().add(
                        builder.isNull(candidate.get("fullIntakeCompletedDate")));
                }
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

            //CANDIDATE OPPORTUNITIES
            final CandidateFilterByOpps candidateFilterByOpps = request.getCandidateFilterByOpps();
            if (candidateFilterByOpps != null) {
               Boolean anyOpps = candidateFilterByOpps.getAnyOpps();
               Boolean closedOpps = candidateFilterByOpps.getClosedOpps();
               Boolean relocatedOpps = candidateFilterByOpps.getRelocatedOpps();

                //This is the where clause we are constructing
                /*
                   where 0 <
                   (select count(*) from candidate_opportunity
                   where candidate_id = candidate.id)

                   - Plus possible other where clauses - eg "and closed = false"
                */
                //This "conjunction predicate" will AND together all the where clauses we add to it.
                Predicate oppsWhereClauses = builder.conjunction();

                //Create the Select subquery which will return the opportunity count as a Long
                Subquery<Long> sq = query.subquery(Long.class);
                Root<CandidateOpportunity> opp = sq.from(CandidateOpportunity.class);

                //This where clause is always there: candidate_id = candidate.id
                oppsWhereClauses.getExpressions().add(
                    builder.equal(opp.get("candidate").get("id"), candidate.get("id")));

                boolean countMustBeNonZero = true;
                if (anyOpps != null) {
                    //The "AnyOpp" request doesn't add any other clauses.
                    //It just changes whether we are testing that the count of opportunities should
                    //or should not equal zero. ie are we looking for candidates with some opp or
                    //no opps.
                    countMustBeNonZero = anyOpps;
                } else {
                    if (closedOpps != null) {
                        boolean closedClauseValue = closedOpps;
                        //Add the where clause "closed = true" or "closed = false"
                        oppsWhereClauses.getExpressions().add(
                            builder.equal(opp.get("closed"), closedClauseValue));
                    }
                    if (relocatedOpps != null) {
                        //Add the where clause checking whether the integer value associated with
                        //opps stage is before (ie less than) the relocated stage or not.
                        //ie the clause is effectively "stage < relocated" or "stage >= relocated"
                        int relocatedStageOrder = CandidateOpportunityStage.relocated.ordinal();
                        Predicate relocatedPredicate = relocatedOpps ?
                            builder.greaterThanOrEqualTo(opp.get("stageOrder"), relocatedStageOrder) :
                            builder.lessThan(opp.get("stageOrder"), relocatedStageOrder);
                        oppsWhereClauses.getExpressions().add(relocatedPredicate);
                    }
                }

                //Do the subquery select - ie do the opportunity count, by "and-ing" together all
                //the where clauses we have added to oppsConjunction
                sq.select(builder.count(opp)).where(oppsWhereClauses);

                conjunction.getExpressions().add(
                    //Check for zero or non-zero opportunity count depending on the above logic.
                    countMustBeNonZero ? builder.greaterThan(sq, 0L) : builder.equal(sq, 0L)
                );
            }

            return conjunction;
        };
    }

    private static OffsetDateTime getOffsetDateTime(LocalDate localDate, LocalTime time, String timezone) {
        return OffsetDateTime.of(localDate, time, !StringUtils.isBlank(timezone) ? ZoneId.of(timezone).getRules().getOffset(Instant.now()) : ZoneOffset.UTC);
    }

}
