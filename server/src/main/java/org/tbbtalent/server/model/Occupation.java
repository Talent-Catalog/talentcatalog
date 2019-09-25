package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "occupation")
@SequenceGenerator(name = "seq_gen", sequenceName = "occupation_id_seq", allocationSize = 1)
public class Occupation extends AbstractDomainObject<Long> {

    private Long id;

    private String name;
    @Enumerated(EnumType.STRING)
    private Status status;

    public Occupation() {
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
