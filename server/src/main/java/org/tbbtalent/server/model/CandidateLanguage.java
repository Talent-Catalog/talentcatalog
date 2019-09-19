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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "read_write")
    private LanguageLevel readWrite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "speak")
    private LanguageLevel speak;

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

    public LanguageLevel getReadWrite() { return readWrite; }

    public void setReadWrite(LanguageLevel readWrite) { this.readWrite = readWrite; }

    public LanguageLevel getSpeak() { return speak; }

    public void setSpeak(LanguageLevel speak) { this.speak = speak; }
}
