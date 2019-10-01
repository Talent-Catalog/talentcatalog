package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.model.SearchType;

public class SearchJoinRequest {

    private Long savedSearchId;
    private SearchType searchType;

    public SearchJoinRequest() {
    }

    public SearchJoinRequest(Long savedSearchId, SearchType searchType) {
        this.savedSearchId = savedSearchId;
        this.searchType = searchType;
    }

    public Long getSavedSearchId() {
        return savedSearchId;
    }

    public void setSavedSearchId(Long savedSearchId) {
        this.savedSearchId = savedSearchId;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }
}

