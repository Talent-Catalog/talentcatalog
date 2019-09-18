package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "education_type")
public class EduType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "education_type_gen")
    @SequenceGenerator(name = "education_type_gen", sequenceName = "education_type_id_seq", allocationSize = 1)
    private Long id;

    private String name;

    public EduType() {
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
