package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "language")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "language_gen")
    @SequenceGenerator(name = "language_gen", sequenceName = "language_id_seq", allocationSize = 1)
    private Long id;

    private String name;

    public Language() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
