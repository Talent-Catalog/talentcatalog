package org.tbbtalent.server.request.search;

import javax.validation.constraints.NotBlank;

import org.tbbtalent.server.model.SavedSearchSubtype;
import org.tbbtalent.server.model.SavedSearchType;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

public class UpdateSavedSearchRequest {

    @NotBlank
    private String name;

    private SavedSearchType savedSearchType;
    private SavedSearchSubtype savedSearchSubtype;

    private SearchCandidateRequest searchCandidateRequest;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SavedSearchType getSavedSearchType() {
        return savedSearchType;
    }

    public void setSavedSearchType(SavedSearchType savedSearchType) {
        this.savedSearchType = savedSearchType;
    }

    public SavedSearchSubtype getSavedSearchSubtype() {
        return savedSearchSubtype;
    }

    public void setSavedSearchSubtype(SavedSearchSubtype savedSearchSubtype) {
        this.savedSearchSubtype = savedSearchSubtype;
    }

    public SearchCandidateRequest getSearchCandidateRequest() {
        return searchCandidateRequest;
    }

    public void setSearchCandidateRequest(SearchCandidateRequest searchCandidateRequest) {
        this.searchCandidateRequest = searchCandidateRequest;
    }

}
