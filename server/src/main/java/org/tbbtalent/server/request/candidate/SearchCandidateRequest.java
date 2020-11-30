package org.tbbtalent.server.request.candidate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.tbbtalent.server.model.db.CandidateStatus;
import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.model.db.ReviewStatus;
import org.tbbtalent.server.model.db.SearchType;
import org.tbbtalent.server.request.PagedSearchRequest;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

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
    private List<Long> verifiedOccupationIds;
    private SearchType verifiedOccupationSearchType;
    private List<Long> nationalityIds;
    private SearchType nationalitySearchType;
    private List<Long> countryIds;
    private Integer englishMinWrittenLevel;
    private Integer englishMinSpokenLevel;
    private Long otherLanguageId;
    private Integer otherMinWrittenLevel;
    private Integer otherMinSpokenLevel;

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
    private List<ReviewStatus> reviewStatusFilter;
    private boolean includeNew;
    private Boolean includeDraftAndDeleted;
    private Boolean includeUploadedFiles;
    private LocalDate fromDate;
    private List<SearchJoinRequest> searchJoinRequests;
    private static final Logger log = LoggerFactory.getLogger(SearchCandidateRequest.class);

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
        log.info("SearchCandidateRequest - Have finished merging request. " + " page number:" + request.getPageNumber() + " page size:" + request.getPageSize());
    }
}

