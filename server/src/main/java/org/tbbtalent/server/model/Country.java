package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "country_gen")
    @SequenceGenerator(name = "country_gen", sequenceName = "country_id_seq", allocationSize = 1)
    private Long id;
    private String name;

    public Country() {
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
