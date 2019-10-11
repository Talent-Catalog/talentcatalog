package org.tbbtalent.server.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "candidate_note")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_note_id_seq", allocationSize = 1)
public class CandidateNote extends AbstractAuditableDomainObject<Long>  {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    private String title;
    private String comment;
    @Enumerated(EnumType.STRING)
    private NoteType noteType;

    public CandidateNote() {
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
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

    public NoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }
}
