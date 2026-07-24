/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.request.candidate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Sort;
import org.tctalent.server.model.db.CandidateFilterByOpps;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.request.PagedSearchRequest;

@Getter
@Setter
@ToString(callSuper = true)
public class SearchCandidateRequest extends PagedSearchRequest {

    /**
     * This is text that describes in natural language the candidates that we are looking for.
     * In the context of job matching, it would correspond to the job description, but it could
     * also be used for other purposes. It is a textual representation of the requirements or
     * criteria that the candidates must meet.
     */
    private String requirementsDescription;

    /**
     * When a {@link #requirementsDescription} is present, it is used to filter the candidates
     * in two ways:
     * <ul>
     *     <li>
     *         Lexically: based on matched keywords in the description, in particular required
     *         skills, with each candidate's skills as they appear in their job experiences.
     *     </li>
     *     <li>
     *         Semantically: based on semantic similarity between the description
     *         and each candidate's various job experiences. Vector embeddings are used to detect
     *         that semantic similarity.
     *     </li>
     * </ul>
     * <p>
     *   This lexicalScoreProportion parameter defines how much weight to give to lexical and
     *   semantic matches.
     *   It is a float value between 0 and 1, where 0 means that the lexical score will not be used
     *   at all and 1 means that it will be used completely. The semantic score proportion will be
     *   1 minus the lexical score proportion.
     *   So a value of 0.5 means that lexical and semantic scores will be given equal weight.
     * </p>
     */
    private float lexicalScoreProportion;

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

    private Boolean includePendingTermsCandidates;
    private Boolean miniIntakeCompleted;
    private Boolean fullIntakeCompleted;
    private Boolean potentialDuplicate;
    private String regoReferrerParam;
    private List<ReviewStatus> reviewStatusFilter;
    private LocalDate fromDate;
    private List<SearchJoinRequest> searchJoinRequests;

    private String regoUtmCampaign;
    private String regoUtmSource;
    private String regoUtmMedium;

    private List<String> candidateNumbers;
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

