package org.tbbtalent.server.request.education.level;

import org.tbbtalent.server.model.Status;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateEducationLevelRequest {

    @NotBlank
    private String name;

    @NotNull
    private Status status;

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
