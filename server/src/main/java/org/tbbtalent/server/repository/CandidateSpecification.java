package org.tbbtalent.server.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.model.CandidateJobExperience;
import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.model.CandidateShortlistItem;
import org.tbbtalent.server.model.CandidateSkill;
import org.tbbtalent.server.model.CandidateStatus;
import org.tbbtalent.server.model.EducationLevel;
import org.tbbtalent.server.model.EducationMajor;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.model.LanguageLevel;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.model.SearchType;
import org.tbbtalent.server.model.ShortlistStatus;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

import io.jsonwebtoken.lang.Collections;
import static org.tbbtalent.server.repository.CandidateSpecificationUtil.getOrderByOrders;

public class CandidateSpecification {

    public static Specification<Candidate> buildSearchQuery(
            final SearchCandidateRequest request, @Nullable User loggedInUser) {
        return (candidate, query, builder) -> {
            
            //To better understand this code, look at the simpler but similar
            //SavedListGetQuery. JC - The Programmer's Friend.
            
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
            
            if (BooleanUtils.isNotTrue(request.getIncludeDraftAndDeleted())) {
                List<CandidateStatus> statuses = new ArrayList(Arrays.asList(CandidateStatus.draft, CandidateStatus.deleted) );
                conjunction.getExpressions().add(
                        candidate.get("status").in(statuses).not()
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
                if (CollectionUtils.isNotEmpty(request.getShortlistStatus())) {
                    Predicate pendingPredicate = null;
                    if (request.getShortlistStatus().contains(ShortlistStatus.pending)) {
                        Subquery<Candidate> sq = query.subquery(Candidate.class);
                        Root<Candidate> subCandidate = sq.from(Candidate.class);
                        /*
                        select Candidate from Candidate join 
                        CandidateShortListItems join 
                        SavedSearch where savedSearchid = requestid
                         pendingPredicate = candidate id not in the result of the query
                         
                         Is this just defaulting everything else as pending? 
                         */
                        Join<Object, Object> subShortList = subCandidate.join("candidateShortlistItems");
                        Join<Object, Object> subSavedSearch = subShortList.join("savedSearch");
                        sq.select(subCandidate).where(builder.equal(subSavedSearch.get("id"), request.getSavedSearchId()));
                        pendingPredicate = builder.not(builder.in(candidate.get("id")).value(sq));
                    }

                    Subquery<Candidate> sq = query.subquery(Candidate.class);
                    Root<Candidate> subCandidate = sq.from(Candidate.class);
                    Join<Object, Object> subShortList = subCandidate.join("candidateShortlistItems");
                    Join<Object, Object> subSavedSearch = subShortList.join("savedSearch");
                    sq.select(subCandidate).where(
                            builder.and(
                                    builder.equal(subSavedSearch.get("id"), request.getSavedSearchId()), 
                                    subShortList.get("shortlistStatus").in(request.getShortlistStatus())
                            )
                    );
                    
                    Predicate statusPredicate = builder.in(candidate.get("id")).value(sq);

                    if (pendingPredicate != null) {
                        conjunction.getExpressions().add(builder.and(builder.or(pendingPredicate, statusPredicate)));
                    } else {
                        conjunction.getExpressions().add(statusPredicate);
                    }
                } else if (query.getResultType().equals(Candidate.class)) {
                    Fetch<Candidate, CandidateShortlistItem> candidateShortlistItem = candidate.fetch("candidateShortlistItems", JoinType.LEFT);
                    Fetch<CandidateShortlistItem, SavedSearch> savedSearch = candidateShortlistItem.fetch("savedSearch", JoinType.LEFT);
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

                    // This is adding an OR statement IF the keyword search includes uploaded files. May be a better way to do this, but it works searching textExtracts based on IF statement
                    if(BooleanUtils.isTrue(request.getIncludeUploadedFiles())) {
                        predicates.get(0).getExpressions().add(builder.like(builder.lower(candidateAttachments.get("textExtract")), likeMatchTerm)
                        );
                    }
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
                if (BooleanUtils.isTrue(request.getIncludeDraftAndDeleted())) {
                    statuses.add(CandidateStatus.draft);
                    statuses.add(CandidateStatus.deleted);
                }
                conjunction.getExpressions().add(
                    builder.isTrue(candidate.get("status").in(statuses))
            );
            }

            // occupations SEARCH


            if (!Collections.isEmpty(request.getVerifiedOccupationIds())
                    || !Collections.isEmpty(request.getOccupationIds())
                    || !StringUtils.isBlank(request.getOrProfileKeyword())
            ) {
                candidateOccupations = candidateOccupations != null ? candidateOccupations: candidate.join("candidateOccupations", JoinType.LEFT);
                occupation = occupation != null ? occupation : candidateOccupations.join("occupation", JoinType.LEFT);

                if (!Collections.isEmpty(request.getVerifiedOccupationIds())) {
                    if (SearchType.not.equals(request.getVerifiedOccupationSearchType())) {
                        conjunction.getExpressions().add(occupation.get("id").in(request.getVerifiedOccupationIds()).not());
                    } else {
                        conjunction.getExpressions().add(builder.and(builder.isTrue(candidateOccupations.get("verified")),
                                builder.isTrue(occupation.get("id").in(request.getVerifiedOccupationIds()))
                        ));
                    }
                }

                if (!Collections.isEmpty(request.getOccupationIds()) || !StringUtils.isBlank(request.getOrProfileKeyword())) {
                    if (StringUtils.isBlank(request.getOrProfileKeyword())) {
                        conjunction.getExpressions().add(
                                builder.isTrue(occupation.get("id").in(request.getOccupationIds()))
                        );
                    } else {
                        candidateJobExperiences = candidateJobExperiences != null ? 
                                candidateJobExperiences : candidate.join
                                ("candidateJobExperiences", JoinType.LEFT);
                        candidateSkills = candidateSkills != null ? 
                                candidateSkills : 
                                candidate.join("candidateSkills", JoinType.LEFT);
                        candidateEducations = candidateEducations == null ? 
                                candidate.join("candidateEducations", JoinType.LEFT) : 
                                candidateEducations;

                        String lowerCaseMatchTerm = request.getOrProfileKeyword().toLowerCase();
                        String[] orText = lowerCaseMatchTerm.split("\\s*,\\s*");
                        List<Predicate> orPredicates = new ArrayList<>();
                        
                        //For each comma separated term
                        for (String orTerm : orText) {

                            //Extract the space separated words
                            //Each of these words must appear in a field for a match
                            String[] words = orTerm.split("\\s+");

                            //We are going to construct the "and" condition for 
                            //each checked data base field in this array
                            List<List<Predicate>> fieldAndPredicates = new ArrayList<>();
                            ArrayList<Predicate> additionalInfoAnds = new ArrayList<>();
                            fieldAndPredicates.add(additionalInfoAnds);
                            ArrayList<Predicate> descriptionAnds = new ArrayList<>();
                            fieldAndPredicates.add(descriptionAnds);
                            ArrayList<Predicate> roleAnds = new ArrayList<>();
                            fieldAndPredicates.add(roleAnds);
                            ArrayList<Predicate> courseNameAnds = new ArrayList<>();
                            fieldAndPredicates.add(courseNameAnds);
                            ArrayList<Predicate> migrationOccupationAnds = new ArrayList<>();
                            fieldAndPredicates.add(migrationOccupationAnds);
                            ArrayList<Predicate> occupationNameAnds = new ArrayList<>();
                            fieldAndPredicates.add(occupationNameAnds);
                            ArrayList<Predicate> skillAnds = new ArrayList<>();
                            fieldAndPredicates.add(skillAnds);
                            
                            for (String word : words) {
                                //Create each field LIKE word for all words.
                                String likeMatchTerm = "%" + word + "%";
                                additionalInfoAnds.add(
                                        builder.like(builder.lower(
                                                candidate.get("additionalInfo")), likeMatchTerm));
                                descriptionAnds.add(
                                        builder.like(builder.lower(
                                                candidateJobExperiences.get("description")), likeMatchTerm));
                                roleAnds.add(
                                        builder.like(builder.lower(
                                                candidateJobExperiences.get("role")), likeMatchTerm));
                                courseNameAnds.add(
                                        builder.like(builder.lower(
                                                candidateEducations.get("courseName")), likeMatchTerm));
                                migrationOccupationAnds.add(
                                        builder.like(builder.lower(
                                                candidateOccupations.get("migrationOccupation")), likeMatchTerm));
                                occupationNameAnds.add(
                                        builder.like(builder.lower(
                                                occupation.get("name")), likeMatchTerm));
                                skillAnds.add(
                                        builder.like(builder.lower(
                                                candidateSkills.get("skill")), likeMatchTerm));
                            }
                            
                            //For each field create a predicate that "AND"s 
                            //together all the field LIKE word predicates.
                            //Then add that predicate to our list of predicates
                            //which are going to be OR'd together.
                            for (List<Predicate> predicates: fieldAndPredicates) {
                                Predicate andTerm = builder.and(predicates.toArray(new Predicate[0])); 
                                orPredicates.add(andTerm);
                            }                            
                        }
                        if (Collections.isEmpty(request.getOccupationIds())) {
                            conjunction.getExpressions().add(builder.and(
                                    builder.or(orPredicates.toArray(new Predicate[0]))));
                        } else {
                            conjunction.getExpressions().add(builder.and(
                            builder.or(
                                    builder.isTrue(occupation.get("id").in(request.getOccupationIds())),
                                    builder.or(orPredicates.toArray(new Predicate[0])))
                            ));
                        }
                    }
                }
            }


            // UN REGISTERED SEARCH
            if (request.getUnRegistered() != null) {
                conjunction.getExpressions().add(
                        builder.equal(candidate.get("registeredWithUN"), request.getUnRegistered())
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
