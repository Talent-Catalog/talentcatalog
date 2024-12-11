/*
 * Copyright (c) 2024 Talent Catalog.
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

import static org.tctalent.server.util.locale.LocaleHelper.getOffsetDateTime;

import io.jsonwebtoken.lang.Collections;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
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
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.candidate.SearchCandidateRequest;

public class CandidateSpecification {

    public static Specification<Candidate> buildSearchQuery(
            final SearchCandidateRequest request, @Nullable User loggedInUser,
            final @Nullable Collection<Candidate> excludedCandidates) {
        return (candidate, query, cb) -> {
            if (query == null) {
                throw new IllegalArgumentException("CandidateSpecification.CriteriaQuery should not be null");
            }
            query.distinct(true);

            Predicate conjunction = cb.conjunction();

            //These joins are only created as needed - depending on the query.
            //eg candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;
            //Some joins are always needed - eg the user one, and the joins needed to support
            //sorting.
            Join<Object, Object> user;
            Join<Object, Object> partner;
            Join<Object, Object> nationality;
            Join<Object, Object> country;
            Join<Object, Object> maxEducationLevel;
            Join<Candidate, CandidateEducation> candidateEducations = null;
            Join<Candidate, CandidateOccupation> candidateOccupations = null;
            Join<CandidateOccupation, Occupation> occupation = null;
            Join<Candidate, CandidateJobExperience> candidateJobExperiences = null;
            Join<Candidate, CandidateSkill> candidateSkills = null;
            Join<Candidate, CandidateAttachment> candidateAttachments = null;

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

                List<Order> orders = CandidateSpecificationUtil.getOrderByOrders(request, candidate, cb,
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
                candidateJobExperiences = candidateJobExperiences != null ? candidateJobExperiences : candidate.join("candidateJobExperiences", JoinType.LEFT);
                candidateSkills = candidateSkills != null ? candidateSkills : candidate.join("candidateSkills", JoinType.LEFT);
                candidateAttachments = candidateAttachments != null ? candidateAttachments : candidate.join("candidateAttachments", JoinType.LEFT);
                candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;
                candidateOccupations = candidate.join("candidateOccupations", JoinType.LEFT);
                occupation = candidateOccupations.join("occupation", JoinType.LEFT);

                for (String s : splittedText) {
                    String likeMatchTerm = "%" + s + "%";
                    conjunction = cb.and(conjunction,
                        cb.or(
                            cb.like(cb.lower(candidate.get("candidateNumber")), lowerCaseMatchTerm),
                            cb.like(cb.lower(user.get("firstName")), likeMatchTerm),
                            cb.like(cb.lower(user.get("lastName")), likeMatchTerm),
                            cb.like(cb.lower(user.get("email")), likeMatchTerm),
                            cb.like(cb.lower(candidate.get("phone")), lowerCaseMatchTerm),
                            cb.like(cb.lower(candidate.get("whatsapp")), lowerCaseMatchTerm),
                            cb.like(cb.lower(candidate.get("additionalInfo")), likeMatchTerm),
                            cb.like(cb.lower(candidateJobExperiences.get("description")), likeMatchTerm),
                            cb.like(cb.lower(candidateJobExperiences.get("role")), likeMatchTerm),
                            cb.like(cb.lower(candidateEducations.get("courseName")), likeMatchTerm),
                            cb.like(cb.lower(candidateOccupations.get("migrationOccupation")), likeMatchTerm),
                            cb.like(cb.lower(candidateSkills.get("skill")), likeMatchTerm),
                            cb.like(cb.lower(occupation.get("name")), likeMatchTerm)
                        )
                    );
                }
            }

            // STATUS SEARCH
            if (!Collections.isEmpty(request.getStatuses())) {
                List<CandidateStatus> statuses = request.getStatuses();
                conjunction = cb.and(conjunction,
                    cb.isTrue(candidate.get("status").in(statuses)));
            }

            // Occupations SEARCH
            if (!Collections.isEmpty(request.getOccupationIds())) {
                candidateOccupations = candidateOccupations != null ? candidateOccupations: candidate.join("candidateOccupations", JoinType.LEFT);
                occupation = occupation != null ? occupation : candidateOccupations.join("occupation", JoinType.LEFT);

                conjunction = cb.and(conjunction,
                        occupation.get("id").in(request.getOccupationIds())
                );

                //Min / Max Age
                if (request.getMinYrs() != null) {
                    Integer minYrs = request.getMinYrs();
                    conjunction = cb.and(conjunction, cb.and(
                            cb.greaterThanOrEqualTo(candidateOccupations.get("yearsExperience"), minYrs),
                            cb.isTrue(occupation.get("id").in(request.getOccupationIds()))));
                }

                if (request.getMaxYrs() != null) {
                    Integer maxYrs = request.getMaxYrs();
                    conjunction = cb.and(conjunction, cb.and(
                            cb.lessThanOrEqualTo(candidateOccupations.get("yearsExperience"), maxYrs),
                            cb.isTrue(occupation.get("id").in(request.getOccupationIds()))));
                }
            }

            // EXCLUDED CANDIDATES (eg from Review Status)
            if (excludedCandidates != null && !excludedCandidates.isEmpty()) {
                conjunction = cb.and(conjunction,
                    candidate.in(excludedCandidates).not());
            }

            // NATIONALITY SEARCH
            if (!Collections.isEmpty(request.getNationalityIds())) {
                if (request.getNationalitySearchType() == null || SearchType.or.equals(request.getNationalitySearchType())) {
                    conjunction = cb.and(conjunction,
                            cb.isTrue(candidate.get("nationality").get("id").in(request.getNationalityIds()))
                    );
                } else {
                    conjunction = cb.and(conjunction, candidate.get("nationality").get("id").in(request.getNationalityIds()).not()
                    );
                }
            }

            // COUNTRY SEARCH - taking into account user source country limitations
            // If request ids is NOT EMPTY we can just accept them because the options
            // presented to the user will be limited to the allowed source countries
            if (!Collections.isEmpty(request.getCountryIds())) {
                if (request.getCountrySearchType() == null || SearchType.or.equals(request.getCountrySearchType())) {
                    conjunction = cb.and(conjunction,
                        cb.isTrue(candidate.get("country").get("id").in(request.getCountryIds()))
                    );
                } else {
                    conjunction = cb.and(conjunction, candidate.get("country").get("id").in(request.getCountryIds()).not());
                }
            // If request ids IS EMPTY only show source countries
            } else if (loggedInUser != null &&
                    !Collections.isEmpty(loggedInUser.getSourceCountries())) {
                conjunction = cb.and(conjunction,
                        cb.isTrue(candidate.get("country").in(loggedInUser.getSourceCountries()))
                );
            }

            // PARTNER SEARCH
            if (!Collections.isEmpty(request.getPartnerIds())) {
                conjunction = cb.and(conjunction,
                    cb.isTrue(user.get("partner").get("id").in(request.getPartnerIds()))
                );
            }

            // SURVEY TYPE SEARCH
            if (!Collections.isEmpty(request.getSurveyTypeIds())) {
                conjunction = cb.and(conjunction,
                        cb.isTrue(candidate.get("surveyType").in(request.getSurveyTypeIds()))
                );
            }

            // REFERRER
            if (request.getRegoReferrerParam() != null &&
                !request.getRegoReferrerParam().trim().isEmpty()) {
                conjunction = cb.and(conjunction,
                        cb.like(cb.lower(candidate.get("regoReferrerParam")),
                            request.getRegoReferrerParam().toLowerCase())
                );
            }

            // GENDER SEARCH
            if (request.getGender() != null) {
                conjunction = cb.and(conjunction,
                        cb.equal(candidate.get("gender"), request.getGender())
                );
            }


            //Modified From
            if (request.getLastModifiedFrom() != null) {
                conjunction = cb.and(conjunction,
                    cb.greaterThanOrEqualTo(candidate.get("updatedDate"),getOffsetDateTime(
                            request.getLastModifiedFrom(), LocalTime.MIN, request.getTimezone())));
            }

            if (request.getLastModifiedTo() != null) {
                conjunction = cb.and(conjunction,
                    cb.lessThanOrEqualTo(candidate.get("updatedDate"), getOffsetDateTime(
                        request.getLastModifiedTo(), LocalTime.MAX, request.getTimezone())));
            }

            //Min / Max Age
            if (request.getMinAge() != null) {
                LocalDate minDob = LocalDate.now().minusYears(request.getMinAge() + 1);
                conjunction = cb.and(conjunction, cb.or(cb.lessThanOrEqualTo(candidate.get("dob"), minDob), cb.isNull(candidate.get("dob"))));
            }

            if (request.getMaxAge() != null) {
                LocalDate maxDob = LocalDate.now().minusYears(request.getMaxAge() + 1);

                conjunction = cb.and(conjunction, cb.or(cb.greaterThan(candidate.get("dob"), maxDob), cb.isNull(candidate.get("dob"))));
            }

            // UNHCR STATUSES
            if (!Collections.isEmpty(request.getUnhcrStatuses())) {
                List<UnhcrStatus> statuses = request.getUnhcrStatuses();
                conjunction = cb.and(conjunction,
                    cb.isTrue(candidate.get("unhcrStatus").in(statuses))
                );
            }

            // EDUCATION LEVEL SEARCH
            if (request.getMinEducationLevel() != null) {
                Join<Candidate, EducationLevel> educationLevel = candidate.join("maxEducationLevel", JoinType.LEFT);
                conjunction = cb.and(conjunction,
                        cb.greaterThanOrEqualTo(educationLevel.get("level"), request.getMinEducationLevel())
                );
            }

            // MINI INTAKE COMPLETE
            if (request.getMiniIntakeCompleted() != null) {
                if(request.getMiniIntakeCompleted()) {
                    conjunction = cb.and(conjunction,
                        cb.isNotNull(candidate.get("miniIntakeCompletedDate")));
                } else {
                    conjunction = cb.and(conjunction,
                        cb.isNull(candidate.get("miniIntakeCompletedDate")));
                }
            }

            // FULL INTAKE COMPLETE
            if (request.getFullIntakeCompleted() != null) {
                if(request.getFullIntakeCompleted()) {
                    conjunction = cb.and(conjunction,
                        cb.isNotNull(candidate.get("fullIntakeCompletedDate")));
                } else {
                    conjunction = cb.and(conjunction,
                        cb.isNull(candidate.get("fullIntakeCompletedDate")));
                }
            }

            // POTENTIAL DUPLICATE
            if (request.getPotentialDuplicate() != null) {
                conjunction = cb.and(
                    conjunction,
                    cb.equal(candidate.get("potentialDuplicate"), request.getPotentialDuplicate())
                );
            }

            // MAJOR SEARCH
            if (!Collections.isEmpty(request.getEducationMajorIds())) {
                candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;
                Join<Candidate, EducationMajor> major = candidateEducations.join("educationMajor", JoinType.LEFT);
                Join<Candidate, EducationMajor> migrationMajor = candidate.join("migrationEducationMajor", JoinType.LEFT);

                conjunction = cb.and(conjunction, cb.or(
                        cb.isTrue(major.get("id").in(request.getEducationMajorIds())),
                        cb.isTrue(migrationMajor.get("id").in(request.getEducationMajorIds())))
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
                    conjunction = cb.and(conjunction, cb.and(cb.equal(cb.lower(language.get("name")), "english"),
                            cb.greaterThanOrEqualTo(writtenLevel.get("level"), request.getEnglishMinWrittenLevel()),
                            cb.greaterThanOrEqualTo(spokenLevel.get("level"), request.getEnglishMinSpokenLevel())));
                } else if (request.getEnglishMinWrittenLevel() != null) {
                    conjunction = cb.and(conjunction, cb.and(cb.equal(cb.lower(language.get("name")), "english"),
                            cb.greaterThanOrEqualTo(writtenLevel.get("level"), request.getEnglishMinWrittenLevel())));
                } else if (request.getEnglishMinSpokenLevel() != null) {
                    conjunction = cb.and(conjunction, cb.and(cb.equal(cb.lower(language.get("name")), "english"),
                            cb.greaterThanOrEqualTo(spokenLevel.get("level"), request.getEnglishMinSpokenLevel())));
                }
                if (request.getOtherLanguageId() != null) {
                    if (request.getOtherMinSpokenLevel() != null && request.getOtherMinWrittenLevel() != null) {
                        conjunction = cb.and(conjunction, cb.and(cb.equal(language.get("id"), request.getOtherLanguageId()),
                                cb.greaterThanOrEqualTo(writtenLevel.get("level"), request.getOtherMinWrittenLevel()),
                                cb.greaterThanOrEqualTo(spokenLevel.get("level"), request.getOtherMinSpokenLevel())));
                    } else if (request.getOtherMinSpokenLevel() != null) {
                        conjunction = cb.and(conjunction, cb.and(cb.equal(language.get("id"), request.getOtherLanguageId()),
                                cb.greaterThanOrEqualTo(spokenLevel.get("level"), request.getOtherMinSpokenLevel())));
                    } else if (request.getOtherMinWrittenLevel() != null) {
                        conjunction = cb.and(conjunction, cb.and(cb.equal(language.get("id"), request.getOtherLanguageId()),
                                cb.greaterThanOrEqualTo(writtenLevel.get("level"), request.getOtherMinWrittenLevel())));
                    }
                }


            }

            //LIST ANY
            /* where candidate in
                (select candidate from candidateSavedList
                    where savedList.id in (saved lists ids))
            */
            SearchType listAnySearchType = request.getListAnySearchType();
            final List<Long> listAnyIds = request.getListAnyIds();
            if (!Collections.isEmpty(listAnyIds)) {
                Subquery<Candidate> sq = query.subquery(Candidate.class);
                Root<CandidateSavedList> csl = sq.from(CandidateSavedList.class);
                sq.select(csl.get("candidate")).where(
                    cb.in(csl.get("savedList").get("id")).value(listAnyIds)
                );

                //Compute the predicate depending on whether it is negated
                conjunction = cb.and(conjunction,
                     SearchType.not.equals(listAnySearchType)
                        ? cb.in(candidate).value(sq).not()
                        : cb.in(candidate).value(sq)
                );
            }

            //LIST ALL
            /* where
            candidate in (select candidate from candidateSavedList where savedList.id = id1)
            and
            candidate in (select candidate from candidateSavedList where savedList.id = id2)
            and
              ...
            */
            SearchType listAllSearchType = request.getListAllSearchType();
            final List<Long> listAllIds = request.getListAllIds();
            if (!Collections.isEmpty(listAllIds)) {
                for (Long listAllId : listAllIds) {
                    Subquery<Candidate> sq = query.subquery(Candidate.class);
                    Root<CandidateSavedList> csl = sq.from(CandidateSavedList.class);
                    sq.select(csl.get("candidate")).where(
                        cb.equal(csl.get("savedList").get("id"), listAllId)
                    );

                    //Compute the predicate depending on whether it is negated
                    //TODO JC If NOT then shouldn't the cb.and be a cb or else apply the NOT at the end.
                    conjunction = cb.and(conjunction,
                        SearchType.not.equals(listAllSearchType)
                            ? cb.in(candidate).value(sq).not()
                            : cb.in(candidate).value(sq)
                    );
                }
            }

            //CANDIDATE OPPORTUNITIES
            // Not currently in use as of Jun '24 - preserved for now in case of reinstatement.
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
                Predicate oppsWhereClauses = cb.conjunction();

                //Create the Select subquery which will return the opportunity count as a Long
                Subquery<Long> sq = query.subquery(Long.class);
                Root<CandidateOpportunity> opp = sq.from(CandidateOpportunity.class);

                //This where clause is always there: candidate_id = candidate.id
                oppsWhereClauses = cb.and(oppsWhereClauses,
                    cb.equal(opp.get("candidate").get("id"), candidate.get("id")));

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
                        oppsWhereClauses = cb.and(oppsWhereClauses,
                            cb.equal(opp.get("closed"), closedClauseValue));
                    }
                    if (relocatedOpps != null) {
                        //Add the where clause checking whether the integer value associated with
                        //opps stage is before (ie less than) the relocated stage or not.
                        //ie the clause is effectively "stage < relocated" or "stage >= relocated"
                        int relocatedStageOrder = CandidateOpportunityStage.relocated.ordinal();
                        Predicate relocatedPredicate = relocatedOpps ?
                            cb.greaterThanOrEqualTo(opp.get("stageOrder"), relocatedStageOrder) :
                            cb.lessThan(opp.get("stageOrder"), relocatedStageOrder);
                        oppsWhereClauses = cb.and(oppsWhereClauses, relocatedPredicate);
                    }
                }

                //Do the subquery select - ie do the opportunity count, by "and-ing" together all
                //the where clauses we have added to oppsConjunction
                sq.select(cb.count(opp)).where(oppsWhereClauses);

                conjunction = cb.and(conjunction,
                    //Check for zero or non-zero opportunity count depending on the above logic.
                    countMustBeNonZero ? cb.greaterThan(sq, 0L) : cb.equal(sq, 0L)
                );
            }

            return conjunction;
        };
    }

}
