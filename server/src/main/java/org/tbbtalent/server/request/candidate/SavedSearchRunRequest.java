package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.SearchRequest;

public class SavedSearchRunRequest extends SearchRequest {
    private Long savedSearchId;
    private String reviewStatus;

    public SavedSearchRunRequest() {
    }

    public Long getSavedSearchId() {
        return savedSearchId;
    }

    public void setSavedSearchId(Long savedSearchId) {
        this.savedSearchId = savedSearchId;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
}
