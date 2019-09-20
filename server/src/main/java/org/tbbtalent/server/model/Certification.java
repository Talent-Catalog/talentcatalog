package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "certification")
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "certification_gen")
    @SequenceGenerator(name = "certification_gen", sequenceName = "certification_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    private String name;
    private String institution;
    private String dateCompleted;

    public Certification() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
