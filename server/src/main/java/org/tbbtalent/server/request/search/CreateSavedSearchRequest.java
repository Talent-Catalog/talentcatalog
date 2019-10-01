package org.tbbtalent.server.request.search;

import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateSavedSearchRequest {

    @NotBlank
    private String name;

    @NotNull
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
