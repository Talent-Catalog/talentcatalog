package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.model.db.SearchType;

public class SearchJoinRequest {

    private Long savedSearchId;
    private String name;
    private SearchType searchType;

    public SearchJoinRequest() {
    }

    public SearchJoinRequest(Long savedSearchId, String name, SearchType searchType) {
        this.savedSearchId = savedSearchId;
        this.name = name;
        this.searchType = searchType;
    }

    public Long getSavedSearchId() {
        return savedSearchId;
    }

    public void setSavedSearchId(Long savedSearchId) {
        this.savedSearchId = savedSearchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }
}

