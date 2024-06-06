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

package org.tctalent.server.request.candidate;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
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

    private String simpleQueryString;
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
    private List<Long> educationMajorIds;
    private Boolean miniIntakeCompleted;
    private Boolean fullIntakeCompleted;
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

