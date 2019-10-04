package org.tbbtalent.server.request.language.level;

import org.tbbtalent.server.model.Status;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateLanguageLevelRequest {

    @NotBlank
    private String level;

    @NotNull
    private Status status;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
