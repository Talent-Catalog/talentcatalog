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

package org.tctalent.server.request.candidate;

import static org.tctalent.server.util.locale.LocaleHelper.getOffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateFilterByOpps;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.PagedSearchRequest;
import org.tctalent.server.util.CandidateSearchUtils;

/*
  TODO Fix this whole messy confusion around the relationships between SavedSearch
  SavedSearchRequest and what is transmitted up to the browser and received from the browser
  when transferring those objects in each direction.

  Here are some of the messiest parts of the code:

  * All the Transient fields in SavedSearch - which are populated by the getSavedSearch method
    in SavedSearchServiceImpl. Instead of all that copying and Transient fields, the SavedSearch
    should not be storing ids, but other entities. Ids can be transferred in the Dto
    (if necessary for performance reasons)
    and populated using browser look ups of id to values from those static lists (eg of countries).
  * Static lists should be uploaded to the browser once on login, then accessed in services.
    On the rare occasions when static lists change - eg new occupation, users are asked to logout
    and in again. Or - a failed local lookup could trigger an automatic reload before failing hard.
  * The addition of the above transient fields in the savedSearchDtoExtended method of SavedSearchAdminApi
  * convertToSearchCandidateRequest method on SavedSearchServiceImpl, and its cloned version
    in CandidateServiceImpl
  * The only partially successful attempt to impose some type checking on the browser by including
    a SavedSearch type which extends SearchCandidateRequest. The problem being that
    SavedSearchRequest data sent from the browser is different to SavedSearchRequest data
    sent up to the browser.
  * One of the key anomalies at the moment is the difference between the server and browser versions
    of the SearchCandidateRequest class. On the server, it has minimal fields - reflecting the
    data that is actually stored in the data base - as fields on the SavedSearch class (entity).
    On the browser the class has a whole bunch of redundant fields corresponding to the
    transient fields of SavedSearch.
  * On the browser, the SavedSearch class extends the SearchCandidateRequest class, but on the
    server it doesn't. Instead SavedSearch should contain SearchCandidateRequest as a property
    (@Embedded in the @Entity) in both browser and server. That avoids a lot of pointless field
    copying.
  * The redundant fields are on the browser version of SearchCandidateRequest because the
    inheriting browser SavedSearch makes use of those redundant fields (names instead of ids).
    But the browser version of SearchCandidateRequest probably doesn't need them in the Define
    Search screen for example. It would be good to find another way of supplying those extra fields
    to SavedSearch - maybe as methods which fetch names by looking up from the SS id through
    a local browser service. Or a subclassed SavedSearchEnriched object which adds them.
    Ideally we would have a basic SavedSearch object that extended a basic SearchCandidateRequest
    identical on both server and browser. Then add extra fields as needed as subclasses or
    methods.

  Approach:

    1. Figure out exactly what data needs to travel in both directions
    2. Make that data transfer type safe and consistent on browser and server.
    3. Do all the extra creation of redundant data (eg names from ids etc) in a single logical
       place - eg in DTO code on the server, or lookup services on the browser.

 */

@Getter
@Setter
@ToString(callSuper = true)
public class SearchCandidateRequest extends PagedSearchRequest {

    private boolean pgOnlySqlSearch;

    private String simpleQueryString;
    @NotNull
    private Long savedSearchId;
    private String keyword;
    private List<CandidateStatus> statuses;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private List<Long> occupationIds;
    private Integer minYrs;
    private Integer maxYrs;
    private List<Long> partnerIds;
    private List<Long> nationalityIds;
    private SearchType nationalitySearchType;
    private List<Long> countryIds;
    private SearchType countrySearchType;
    private List<Long> surveyTypeIds;
    private Long exclusionListId;
    private List<Long> listAllIds;
    private SearchType listAllSearchType;
    private List<Long> listAnyIds;
    private SearchType listAnySearchType;

    private Integer englishMinWrittenLevel;
    private Integer englishMinSpokenLevel;
    private Long otherLanguageId;
    private Integer otherMinWrittenLevel;
    private Integer otherMinSpokenLevel;
    private List<UnhcrStatus> unhcrStatuses;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private LocalDate lastModifiedFrom;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private LocalDate lastModifiedTo;

    @NotNull
    private String timezone;
    private Integer minAge;
    private Integer maxAge;
    private Integer minEducationLevel;
    private Integer maxEducationLevel;
    private List<Long> educationMajorIds;

    private Boolean miniIntakeCompleted;
    private Boolean fullIntakeCompleted;
    private Boolean potentialDuplicate;
    private String regoReferrerParam;
    private List<ReviewStatus> reviewStatusFilter;

    //TODO JC I don't think is used - possible Zombie code
    private Boolean includeUploadedFiles;
    private LocalDate fromDate;
    private List<SearchJoinRequest> searchJoinRequests;

    /**
     * If specified, requests display of candidates whose candidate opportunities (if any) match
     * the filter.
     * <p/>
     * This filter maps on to the following Boolean SavedSearch params:
     * <ul>
     *     <li>anyOpps</li>
     *     <li>closedOpps</li>
     *     <li>relocatedOpps</li>
     * </ul>
     *
     * Not currently in use as of Jun '24 - preserved for now in case of reinstatement.
     */
    private CandidateFilterByOpps candidateFilterByOpps;

    public SearchCandidateRequest() {
        super(Sort.Direction.DESC, new String[]{"id"});
    }

    /**
     * @see #extractSQL(User, Collection)
     */
    public String extractSQL() {
        return extractSQL(null, null);
    }

    /**
     * <p>
     * Extracts the where clause of database query SQL corresponding to the given search request.
     * </p>
     * <p>
     *     The where clause assumes that it is associated with a SELECT FROM candidate.
     *     In particular joins will assume that the candidate table is at the root.
     * </p>
     * <p>
     *     For example, if the SQL is being used to retrieve all candidate ids matching the query
     *     your code might look like this:
     * </p>
     * <p>
     * <code>
     *    String whereClauseSql = searchRequest.extractSQL();
     * </code>
     * </p>
     * <p>
     * <code>
     *    String querySql = "select distinct candidate.id from candidate" + whereClauseSql;
     * </code>
     * </p>
     * @param user User making the request. If not null, user-specific constraints are added to the
     *             generated SQL - for example, some users are restricted to seeing candidates
     *             located in certain countries.
     * @param excludedCandidates Candidates to be excluded from results - defaults to none if null
     * @return String containing the SQL or JPQL
     */
    public String extractSQL(
        @Nullable User user, @Nullable Collection<Candidate> excludedCandidates) {

        Set<String> joins = new LinkedHashSet<>();
        List<String> ands = new ArrayList<>();

        //Text search
        if (getSimpleQueryString() != null && !getSimpleQueryString().isEmpty()) {
            joins.add(CandidateSearchUtils.CANDIDATE__CANDIDATE_JOB_EXPERIENCE__JOIN__NATIVE);
            String tsquery = CandidateSearchUtils.buildTsQuerySQL(getSimpleQueryString());
            String clause = "candidate_job_experience.ts @@ to_tsquery('english', '" + tsquery + "')";
            ands.add(clause);
        }

        // STATUS SEARCH
        if (!ObjectUtils.isEmpty(getStatuses())) {
            String values = getStatuses().stream()
                .map(Enum::name).map(val -> "'" + val + "'").collect(Collectors.joining(","));
            ands.add("candidate.status in (" + values + ")");
        }

        // Occupations SEARCH
        if (!ObjectUtils.isEmpty(getOccupationIds())) {
            joins.add(CandidateSearchUtils.CANDIDATE__CANDIDATE_OCCUPATION__JOIN__NATIVE);
            String values = getOccupationIds().stream()
                .map(Objects::toString).collect(Collectors.joining(","));
            ands.add("candidate_occupation.occupation_id in (" + values + ")");

            if (getMinYrs() != null) {
                ands.add("candidate_occupation.years_experience >= " + getMinYrs());
            }
            if (getMaxYrs() != null) {
                ands.add("candidate_occupation.years_experience <= " + getMaxYrs());
            }
        }

        // EXCLUDED CANDIDATES (eg from Review Status)
        if (!ObjectUtils.isEmpty(excludedCandidates)) {
            String values = excludedCandidates.stream()
                .map(candidate -> candidate.getId().toString())
                .collect(Collectors.joining(","));
            ands.add("not candidate.id in (" + values + ")");
        }

        // NATIONALITY SEARCH
        if (!ObjectUtils.isEmpty(getNationalityIds())) {
            String values = getNationalityIds().stream()
                .map(Objects::toString).collect(Collectors.joining(","));
            if (getNationalitySearchType() == null || getNationalitySearchType().equals(SearchType.or)) {
                ands.add("candidate.nationality_id in (" + values + ")");
            } else {
                ands.add("candidate.nationality_id not in (" + values + ")");
            }
        }

        // COUNTRY SEARCH - taking into account user source country limitations
        // If request ids is NOT EMPTY we can just accept them because the options
        // presented to the user will be limited to the allowed source countries
        if (!ObjectUtils.isEmpty(getCountryIds())) {
            String values = getCountryIds().stream()
                .map(Objects::toString).collect(Collectors.joining(","));
            if (getCountrySearchType() == null || getCountrySearchType().equals(SearchType.or)) {
                ands.add("candidate.country_id in (" + values + ")");
            } else {
                ands.add("candidate.country_id not in (" + values + ")");
            }
        // If request's countryIds IS EMPTY only show user's source countries
        } else if (user != null && !ObjectUtils.isEmpty(user.getSourceCountries())) {
            String values = user.getSourceCountries().stream()
                .map(country -> country.getId().toString())
                .collect(Collectors.joining(","));
            ands.add("candidate.country_id in (" + values + ")");
        }

        // PARTNER SEARCH
        if (!ObjectUtils.isEmpty(getPartnerIds())) {
            joins.add(CandidateSearchUtils.CANDIDATE__USER__JOIN__NATIVE);
            String values = getPartnerIds().stream()
                .map(Objects::toString).collect(Collectors.joining(","));
            ands.add("users.partner_id in (" + values + ")");
        }

        // SURVEY TYPE SEARCH
        if (!ObjectUtils.isEmpty(getSurveyTypeIds())) {
            String values = getSurveyTypeIds().stream()
                .map(Objects::toString).collect(Collectors.joining(","));
            ands.add("candidate.survey_type_id in (" + values + ")");
        }

        // REFERRER
        final String referrerParam =
            getRegoReferrerParam() == null ? null : getRegoReferrerParam().trim().toLowerCase();
        if (referrerParam != null && !referrerParam.isEmpty()) {
            ands.add("lower(candidate.rego_referrer_param) like '" + referrerParam + "'");
        }

        // GENDER SEARCH
        if (getGender() != null) {
            ands.add("candidate.gender = '" + getGender().name() + "'");
        }

        //Modified From
        if (getLastModifiedFrom() != null) {
            ands.add("candidate.updated_date >= '" +
                getOffsetDateTime(getLastModifiedFrom(), LocalTime.MIN, getTimezone()) + "'");
        }

        //Modified To
        if (getLastModifiedTo() != null) {
            ands.add("candidate.updated_date <= '" +
                getOffsetDateTime(getLastModifiedTo(), LocalTime.MAX, getTimezone()) + "'");
        }

        //Min / Max Age
        if (getMinAge() != null) {
            LocalDate minDob = LocalDate.now().minusYears(getMinAge() + 1);
            ands.add("(candidate.dob <= '" + minDob + "' or candidate.dob is null)" );
        }
        if (getMaxAge() != null) {
            LocalDate maxDob = LocalDate.now().minusYears(getMaxAge() + 1);
            ands.add("(candidate.dob > '" + maxDob + "' or candidate.dob is null)" );
        }

        // UNHCR STATUSES
        if (!ObjectUtils.isEmpty(getUnhcrStatuses())) {
            String values = getUnhcrStatuses().stream()
                .map(Enum::name).map(val -> "'" + val + "'").collect(Collectors.joining(","));
            ands.add("candidate.unhcr_status in (" + values + ")");
        }

        // EDUCATION LEVEL SEARCH
        if (getMinEducationLevel() != null || getMaxEducationLevel() != null) {
            joins.add(CandidateSearchUtils.CANDIDATE__EDUCATION_LEVEL__JOIN__NATIVE);
            if (getMinEducationLevel() != null) {
                ands.add("education_level.level >= " + getMinEducationLevel());
            }
            if (getMaxEducationLevel() != null) {
                ands.add("education_level.level <= " + getMaxEducationLevel());
            }
        }

        // MINI INTAKE COMPLETE
        if (getMiniIntakeCompleted() != null) {
            boolean completed = getMiniIntakeCompleted();
            ands.add("mini_intake_completed_date " + (completed ? "is not null" : "is null"));
        }

        // FULL INTAKE COMPLETE
        if (getFullIntakeCompleted() != null) {
            boolean completed = getFullIntakeCompleted();
            ands.add("full_intake_completed_date " + (completed ? "is not null" : "is null"));
        }

        // POTENTIAL DUPLICATE
        if (getPotentialDuplicate() != null) {
            boolean potentialDuplicate = getPotentialDuplicate();
            ands.add("potentialDuplicate = " + potentialDuplicate);
        }

        // MAJOR SEARCH
        if (!ObjectUtils.isEmpty(getEducationMajorIds())) {
            String values = getEducationMajorIds().stream()
                .map(Objects::toString).collect(Collectors.joining(","));
            joins.add(CandidateSearchUtils.CANDIDATE__CANDIDATE_EDUCATION__JOIN__NATIVE);
            ands.add("(major_id in (" + values + ") "
                + "or migration_education_major_id in (" + values + "))");
        }

        // LANGUAGE SEARCH
        if (getEnglishMinSpokenLevel() != null || getEnglishMinWrittenLevel() != null
            || getOtherLanguageId() != null
            || getOtherMinSpokenLevel() != null || getOtherMinWrittenLevel() != null) {

            joins.add(CandidateSearchUtils.CANDIDATE__CANDIDATE_LANGUAGE__JOIN__NATIVE);
            joins.add(CandidateSearchUtils.CANDIDATE_LANGUAGE__LANGUAGE__JOIN_NATIVE);

            if (getEnglishMinSpokenLevel() != null && getEnglishMinWrittenLevel() != null) {
                joins.add(CandidateSearchUtils.CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_SPOKEN__JOIN_NATIVE);
                joins.add(CandidateSearchUtils.CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_WRITTEN__JOIN_NATIVE);
                ands.add("lower(language.name) = 'english'");
                ands.add("spoken_level.level >= " + getEnglishMinSpokenLevel());
                ands.add("written_level.level >= " + getEnglishMinWrittenLevel());
            } else if (getEnglishMinSpokenLevel() != null) {
                joins.add(CandidateSearchUtils.CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_SPOKEN__JOIN_NATIVE);
                ands.add("lower(language.name) = 'english'");
                ands.add("spoken_level.level >= " + getEnglishMinSpokenLevel());
            } else if (getEnglishMinWrittenLevel() != null) {
                joins.add(CandidateSearchUtils.CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_WRITTEN__JOIN_NATIVE);
                ands.add("lower(language.name) = 'english'");
                ands.add("written_level.level >= " + getEnglishMinWrittenLevel());
            }

            if (getOtherLanguageId() != null) {
                if (getOtherMinSpokenLevel() != null && getOtherMinWrittenLevel() != null) {
                    joins.add(CandidateSearchUtils.CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_SPOKEN__JOIN_NATIVE);
                    joins.add(CandidateSearchUtils.CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_WRITTEN__JOIN_NATIVE);
                    ands.add("candidate_language.language_id = " + getOtherLanguageId());
                    ands.add("spoken_level.level >= " + getOtherMinSpokenLevel());
                    ands.add("written_level.level >= " + getOtherMinWrittenLevel());
                } else if (getOtherMinSpokenLevel() != null) {
                    joins.add(CandidateSearchUtils.CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_SPOKEN__JOIN_NATIVE);
                    ands.add("candidate_language.language_id = " + getOtherLanguageId());
                    ands.add("spoken_level.level >= " + getOtherMinSpokenLevel());
                } else if (getOtherMinWrittenLevel() != null) {
                    joins.add(CandidateSearchUtils.CANDIDATE_LANGUAGE__LANGUAGE_LEVEL_WRITTEN__JOIN_NATIVE);
                    ands.add("candidate_language.language_id = " + getOtherLanguageId());
                    ands.add("written_level.level >= " + getOtherMinWrittenLevel());
                }
            }
        }

        String joinClause = String.join(" left join ", joins);
        String whereClause = String.join(" and ", ands);

        String query = "";
        if (!joinClause.isEmpty()) {
            query += " left join " + joinClause;
        }
        if (!whereClause.isEmpty()) {
            query += " where " + whereClause;
        }

        return query;
    }

    /**
     * Merge in a SavedSearchGetRequest - eg paging info
     * @param request Request to merge in.
     */
    public void merge(SavedSearchGetRequest request) {
        //Copy across the reviewStatusFilter
        setReviewStatusFilter(request.getReviewStatusFilter());

        //Copy paging request across to search request
        setPageNumber(request.getPageNumber());
        setPageSize(request.getPageSize());
        setSortDirection(request.getSortDirection());
        setSortFields(request.getSortFields());
    }
}

