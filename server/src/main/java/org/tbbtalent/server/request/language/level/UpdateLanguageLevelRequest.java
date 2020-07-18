package org.tbbtalent.server.request.language.level;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.tbbtalent.server.model.db.Status;

public class UpdateLanguageLevelRequest {

    @NotBlank
    private String name;
    @NotNull
    private Status status;

    private int level;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
