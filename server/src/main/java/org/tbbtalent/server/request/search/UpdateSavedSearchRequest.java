package org.tbbtalent.server.request.search;

import javax.validation.constraints.NotBlank;

import org.tbbtalent.server.model.SavedSearchType;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

public class UpdateSavedSearchRequest {

    @NotBlank
    private String name;

    private SavedSearchType type;

    private SearchCandidateRequest searchCandidateRequest;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SavedSearchType getType() {
        return type;
    }

    public void setType(SavedSearchType type) {
        this.type = type;
    }

    public SearchCandidateRequest getSearchCandidateRequest() {
        return searchCandidateRequest;
    }

    public void setSearchCandidateRequest(SearchCandidateRequest searchCandidateRequest) {
        this.searchCandidateRequest = searchCandidateRequest;
    }

}
