package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "language")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "language_gen")
    @SequenceGenerator(name = "language_gen", sequenceName = "language_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    private String name;
    private String readWrite;
    private String speak;

    public Language() {
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

    public String getReadWrite() { return readWrite; }

    public void setReadWrite(String readWrite) { this.readWrite = readWrite; }

    public String getSpeak() { return speak; }

    public void setSpeak(String speak) { this.speak = speak; }
}
