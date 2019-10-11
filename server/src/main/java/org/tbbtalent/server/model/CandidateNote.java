package org.tbbtalent.server.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "candidate_note")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_note_id_seq", allocationSize = 1)
public class CandidateNote extends AbstractDomainObject<Long>  {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    private String subject;
    private String comment;
    private LocalDate createdDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public CandidateNote() {
    }

    public Candidate getCandidate() { return candidate; }

    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public String getSubject() { return subject; }

    public void setSubject(String subject) { this.subject = subject; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getCreatedDate() { return createdDate; }

    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public User getUser() { return user; }

    public void setUser(User user) {  this.user = user; }
}
