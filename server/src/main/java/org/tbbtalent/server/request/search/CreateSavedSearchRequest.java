package org.tbbtalent.server.request.search;

import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

import javax.validation.constraints.NotBlank;

public class CreateSavedSearchRequest {

    @NotBlank
    private String name;

    private SearchCandidateRequest searchCandidateRequest;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SearchCandidateRequest getSearchCandidateRequest() {
        return searchCandidateRequest;
    }

    public void setSearchCandidateRequest(SearchCandidateRequest searchCandidateRequest) {
        this.searchCandidateRequest = searchCandidateRequest;
    }
}
