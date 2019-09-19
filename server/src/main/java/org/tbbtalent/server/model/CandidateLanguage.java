package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "candidate_language")
public class CandidateLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidate_language_gen")
    @SequenceGenerator(name = "candidate_language_gen", sequenceName = "candidate_language_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    private String readWrite;
    private String speak;

    public CandidateLanguage() {
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

    public Language getLanguage() { return language; }

    public void setLanguage(Language language) { this.language = language; }

    public String getReadWrite() { return readWrite; }

    public void setReadWrite(String readWrite) { this.readWrite = readWrite; }

    public String getSpeak() { return speak; }

    public void setSpeak(String speak) { this.speak = speak; }
}
