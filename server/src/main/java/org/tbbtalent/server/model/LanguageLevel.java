package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "language_level")
public class LanguageLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "language_level_gen")
    @SequenceGenerator(name = "language_level_gen", sequenceName = "language_level_id_seq", allocationSize = 1)
    private Long id;

    private String level;

    public LanguageLevel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
