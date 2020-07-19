package org.tbbtalent.server.request.note;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateCandidateNoteRequest {

    @NotNull
    private Long candidateId;

    @NotBlank
    private String title;

    @NotBlank
    private String comment;

    public CreateCandidateNoteRequest() {
    }

    public CreateCandidateNoteRequest(@NotNull Long candidateId, @NotBlank String title, @NotBlank String comment) {
        this.candidateId = candidateId;
        this.title = title;
        this.comment = comment;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
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
