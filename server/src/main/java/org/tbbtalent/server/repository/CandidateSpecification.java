package org.tbbtalent.server.repository;

import io.jsonwebtoken.lang.Collections;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

import javax.persistence.criteria.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class CandidateSpecification {

    public static Specification<Candidate> buildSearchQuery(final SearchCandidateRequest request) {
        return (candidate, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            Join user = null;
            Join nationality = null;
            Join country = null;
            Join<Candidate, CandidateEducation> candidateEducations = null;
            Join<Candidate, CandidateOccupation> candidateOccupations = null;
            Join<CandidateOccupation, Occupation> occupation = null;
            Join<Candidate, CandidateJobExperience> candidateJobExperiences = null;
            Join<Candidate, CandidateSkill> candidateSkills = null;

            conjunction.getExpressions().add(
                    builder.notEqual(candidate.get("status"), CandidateStatus.draft)
            );

            if (query.getResultType().equals(Candidate.class)) {
                query.distinct(true);

                Fetch<Object, Object> userFetch = candidate.fetch("user", JoinType.INNER);
                user = (Join<Object, Object>) userFetch;

                Fetch<Object, Object> nationalityFetch = candidate.fetch("nationality", JoinType.INNER);
                nationality = (Join<Object, Object>) nationalityFetch;

                Fetch<Object, Object> countryFetch = candidate.fetch("country", JoinType.INNER);
                country = (Join<Object, Object>) countryFetch;

                String[] sort = request.getSortFields();
                List<Order> orders = new ArrayList<>();

                for (String property : sort) {

                    Join<Object, Object> join = null;
                    String subProperty = null;
                    if (property.startsWith("user.")) {
                        join = user;
                        subProperty = property.replaceAll("user.", "");
                    } else if (property.startsWith("nationality.")) {
                        join = nationality;
                        subProperty = property.replaceAll("nationality.", "");
                    } else if (property.startsWith("country.")) {
                        join = country;
                        subProperty = property.replaceAll("country.", "");
                    } else {
                        subProperty = property;
                    }

                    Path<Object> path = null;
                    if (join != null) {
                        path = join.get(subProperty);
                    } else {
                        path = candidate.get(subProperty);
                    }
                    orders.add(request.getSortDirection().equals(Sort.Direction.ASC) ? builder.asc(path) : builder.desc(path));
                }

                query.orderBy(orders);

            } else {
                user = candidate.join("user");
                nationality = candidate.join("nationality");
                country = candidate.join("country");
            }
            //Review status
            //Only saved searches support review status - ie has this candidate 
            //been reviewed as belonging in this saved search.
            if (request.getSavedSearchId() != null) {
                if (CollectionUtils.isNotEmpty(request.getShortlistStatus())) {
                    Predicate pendingPredicate = null;
                    if (request.getShortlistStatus().contains(ShortlistStatus.pending)) {
                        Subquery<Candidate> sq = query.subquery(Candidate.class);
                        Root<Candidate> subCandidate = sq.from(Candidate.class);
                        Join<Object, Object> subShortList = subCandidate.join("candidateShortlistItems");
                        Join<Object, Object> subSavedSearch = subShortList.join("savedSearch");
                        sq.select(subCandidate).where(builder.equal(subSavedSearch.get("id"), request.getSavedSearchId()));
                        pendingPredicate = builder.not(builder.in(candidate.get("id")).value(sq));
                    }

                    Subquery<Candidate> sq = query.subquery(Candidate.class);
                    Root<Candidate> subCandidate = sq.from(Candidate.class);
                    Join<Object, Object> subShortList = subCandidate.join("candidateShortlistItems");
                    Join<Object, Object> subSavedSearch = subShortList.join("savedSearch");
                    sq.select(subCandidate).where(builder.and(builder.equal(subSavedSearch.get("id"), request.getSavedSearchId()), subShortList.get("shortlistStatus").in(request.getShortlistStatus())));
                    Predicate statusPredicate = builder.in(candidate.get("id")).value(sq);

                    if (pendingPredicate != null) {
                        conjunction.getExpressions().add(builder.and(builder.or(pendingPredicate, statusPredicate)));
                    } else {
                        conjunction.getExpressions().add(statusPredicate);
                    }

                    //Fetch short lists todo only fetch for specific search
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
                }
                if (predicates.size() > 1) {
                    conjunction.getExpressions().add(builder.and(builder.and(predicates.toArray(new Predicate[0]))));
                } else {
                    conjunction.getExpressions().add(builder.and(predicates.toArray(new Predicate[0])));
                }


            }
            // STATUS SEARCH
            if (!Collections.isEmpty(request.getStatuses())) {
                conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("status").in(request.getStatuses()))
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
                        builder.not(occupation.get("id").in(request.getVerifiedOccupationIds()));
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
                    } else if (Collections.isEmpty(request.getOccupationIds())) {
                        candidateJobExperiences = candidateJobExperiences != null ? candidateJobExperiences : candidate.join("candidateJobExperiences", JoinType.LEFT);
                        candidateSkills = candidateSkills != null ? candidateSkills : candidate.join("candidateSkills", JoinType.LEFT);
                        candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;

                        String lowerCaseMatchTerm = request.getOrProfileKeyword().toLowerCase();
                        String[] splittedText = lowerCaseMatchTerm.split("\\s+|,\\s*|\\.\\s*");
                        List<Predicate> predicates = new ArrayList<>();
                        for (String s : splittedText) {
                            String likeMatchTerm = "%" + s + "%";
                            predicates.add(builder.or(
                                    builder.like(builder.lower(candidate.get("additionalInfo")), likeMatchTerm),
                                    builder.like(builder.lower(candidateJobExperiences.get("description")), likeMatchTerm),
                                    builder.like(builder.lower(candidateJobExperiences.get("role")), likeMatchTerm),
                                    builder.like(builder.lower(candidateEducations.get("courseName")), likeMatchTerm),
                                    builder.like(builder.lower(candidateOccupations.get("migrationOccupation")), likeMatchTerm),
                                    builder.like(builder.lower(occupation.get("name")), likeMatchTerm),
                                    builder.like(builder.lower(candidateSkills.get("skill")), likeMatchTerm)
                            ));
                        }
                        if (predicates.size() > 1) {
                            conjunction.getExpressions().add(builder.and(builder.or(predicates.toArray(new Predicate[0]))));
                        } else {
                            conjunction.getExpressions().add(builder.and(predicates.toArray(new Predicate[0])));
                        }

                    } else {
                        String lowerCaseMatchTerm = request.getOrProfileKeyword().toLowerCase();
                        candidateJobExperiences = candidateJobExperiences != null ? candidateJobExperiences : candidate.join("candidateJobExperiences", JoinType.LEFT);
                        candidateSkills = candidateSkills != null ? candidateSkills :candidate.join("candidateSkills", JoinType.LEFT);
                        candidateEducations = candidateEducations == null ? candidate.join("candidateEducations", JoinType.LEFT) : candidateEducations;

                        String[] splittedText = lowerCaseMatchTerm.split("\\s+|,\\s*|\\.\\s*");
                        List<Predicate> predicates = new ArrayList<>();
                        for (String s : splittedText) {
                            String likeMatchTerm = "%" + s + "%";
                            predicates.add(builder.or(
                                    builder.like(builder.lower(candidate.get("additionalInfo")), likeMatchTerm),
                                    builder.like(builder.lower(candidateJobExperiences.get("description")), likeMatchTerm),
                                    builder.like(builder.lower(candidateJobExperiences.get("role")), likeMatchTerm),
                                    builder.like(builder.lower(candidateEducations.get("courseName")), likeMatchTerm),
                                    builder.like(builder.lower(candidateOccupations.get("migrationOccupation")), likeMatchTerm),
                                    builder.like(builder.lower(candidateSkills.get("skill")), likeMatchTerm)
                            ));
                        }
                        if (predicates.size() > 1) {
                            conjunction.getExpressions().add(builder.or(
                                    builder.isTrue(occupation.get("id").in(request.getOccupationIds())),
                                    builder.or(builder.or(predicates.toArray(new Predicate[0])))));
                        } else {
                            conjunction.getExpressions().add(builder.or(
                                    builder.isTrue(occupation.get("id").in(request.getOccupationIds())),
                                    builder.or(predicates.toArray(new Predicate[0]))));
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
                    conjunction.getExpressions().add(
                            builder.isFalse(candidate.get("nationality").in(request.getNationalityIds()))
                    );
                }
            }

            // COUNTRY SEARCH
            if (!Collections.isEmpty(request.getCountryIds())) {
                conjunction.getExpressions().add(
                        builder.isTrue(candidate.get("country").in(request.getCountryIds()))
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

            //Registered From - change to registered
//            if (request.getRegisteredFrom() != null) {
//                conjunction.getExpressions().add(builder.greaterThanOrEqualTo(candidate.get("registeredDate"), getOffsetDateTime(request.getRegisteredFrom(), LocalTime.MIN, request.getTimezone())));
//            }
//
//            if (request.getRegisteredTo() != null) {
//                conjunction.getExpressions().add(builder.lessThanOrEqualTo(candidate.get("registeredDate"), getOffsetDateTime(request.getRegisteredTo(), LocalTime.MAX, request.getTimezone())));
//            }

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
