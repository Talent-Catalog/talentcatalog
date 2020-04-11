package org.tbbtalent.server.request.user;

import javax.validation.constraints.NotNull;

public class UpdateSharingRequest {

    @NotNull
    private Long savedSearchId;

    public Long getSavedSearchId() {
        return savedSearchId;
    }

    public void setSavedSearchId(Long savedSearchId) {
        this.savedSearchId = savedSearchId;
    }
}
