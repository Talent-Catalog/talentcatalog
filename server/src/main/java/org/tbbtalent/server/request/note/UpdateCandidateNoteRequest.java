package org.tbbtalent.server.request.note;

import javax.validation.constraints.NotBlank;

public class UpdateCandidateNoteRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String comment;

    public UpdateCandidateNoteRequest() {
    }

    public UpdateCandidateNoteRequest(@NotBlank String title, @NotBlank String comment) {
        this.title = title;
        this.comment = comment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
