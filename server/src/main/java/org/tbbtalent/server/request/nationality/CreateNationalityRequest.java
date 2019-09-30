package org.tbbtalent.server.request.nationality;

import javax.validation.constraints.NotBlank;

public class CreateNationalityRequest {

    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
