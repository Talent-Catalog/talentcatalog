package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "certification")
@SequenceGenerator(name = "seq_gen", sequenceName = "certification_id_seq", allocationSize = 1)
public class CandidateCertification extends AbstractDomainObject<Long>  {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    private String name;
    private String institution;
    private String dateCompleted;

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

    public String getDateCompleted() { return dateCompleted; }

    public void setDateCompleted(String dateCompleted) { this.dateCompleted = dateCompleted; }
}
