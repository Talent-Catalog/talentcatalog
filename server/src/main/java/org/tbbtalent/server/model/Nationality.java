package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "nationality")
public class Nationality {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nationality_gen")
    @SequenceGenerator(name = "nationality_gen", sequenceName = "nationality_id_seq", allocationSize = 1)
    private Long id;

    private String name;

    public Nationality() {
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
