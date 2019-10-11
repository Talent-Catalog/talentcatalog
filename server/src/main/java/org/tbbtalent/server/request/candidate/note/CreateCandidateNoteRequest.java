package org.tbbtalent.server.request.candidate.note;

import java.time.LocalDate;

public class CreateCandidateNoteRequest {

    private String subject;
    private String comment;
    private Long userId;
    private Long candidateId;
    private LocalDate createdDate;

    public String getSubject() { return subject; }

    public void setSubject(String subject) { this.subject = subject; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCandidateId() { return candidateId; }

    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public LocalDate getCreatedDate() { return createdDate; }

    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
}
