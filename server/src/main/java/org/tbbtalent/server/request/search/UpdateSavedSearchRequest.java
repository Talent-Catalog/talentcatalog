package org.tbbtalent.server.request.search;

import org.tbbtalent.server.model.SavedSearchSubtype;
import org.tbbtalent.server.model.SavedSearchType;
import org.tbbtalent.server.request.candidate.AbstractUpdateCandidateSourceRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

public class UpdateSavedSearchRequest extends AbstractUpdateCandidateSourceRequest {

    private Boolean reviewable;

    private SavedSearchType savedSearchType;
    private SavedSearchSubtype savedSearchSubtype;

    private SearchCandidateRequest searchCandidateRequest;

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

    public Boolean getReviewable() {
        return reviewable;
    }

    public void setReviewable(Boolean reviewable) {
        this.reviewable = reviewable;
    }
}
