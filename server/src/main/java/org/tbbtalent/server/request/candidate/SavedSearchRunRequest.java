package org.tbbtalent.server.request.candidate;

import java.util.List;

import org.tbbtalent.server.model.ShortlistStatus;
import org.tbbtalent.server.request.SearchRequest;

public class SavedSearchRunRequest extends SearchRequest {
    private Long savedSearchId;
    private List<ShortlistStatus> shortlistStatus;

    public SavedSearchRunRequest() {
    }

    public Long getSavedSearchId() {
        return savedSearchId;
    }

    public void setSavedSearchId(Long savedSearchId) {
        this.savedSearchId = savedSearchId;
    }

    public List<ShortlistStatus> getShortlistStatus() {
        return shortlistStatus;
    }

    public void setShortlistStatus(List<ShortlistStatus> shortlistStatus) {
        this.shortlistStatus = shortlistStatus;
    }
}
