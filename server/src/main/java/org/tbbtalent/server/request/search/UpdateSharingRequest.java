package org.tbbtalent.server.request.search;

import javax.validation.constraints.NotNull;

public class UpdateSharingRequest {

    @NotNull
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
