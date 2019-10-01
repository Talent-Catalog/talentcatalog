package org.tbbtalent.server.request.search;

import javax.validation.constraints.NotBlank;

public class UpdateSavedSearchRequest extends CreateSavedSearchRequest {

    @NotBlank
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
