package org.tbbtalent.server.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "candidate_certification")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_certification_id_seq", allocationSize = 1)
public class CandidateCertification extends AbstractDomainObject<Long>  {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    private String name;
    private String institution;
    private LocalDate dateCompleted;

    public CandidateCertification() {
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getInstitution() { return institution; }

    public void setInstitution(String institution) { this.institution = institution; }

    public LocalDate getDateCompleted() { return dateCompleted; }

    public void setDateCompleted(LocalDate dateCompleted) { this.dateCompleted = dateCompleted; }
}
