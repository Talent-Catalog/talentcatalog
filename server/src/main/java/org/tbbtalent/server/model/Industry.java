package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "industry")
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "industry_gen")
    @SequenceGenerator(name = "industry_gen", sequenceName = "industry_id_seq", allocationSize = 1)
    private Long id;

    private String name;

    public Industry() {
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
