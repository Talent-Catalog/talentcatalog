package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "occupation")
@SequenceGenerator(name = "seq_gen", sequenceName = "occupation_id_seq", allocationSize = 1)
public class Occupation extends AbstractDomainObject<Long> {

    private String name;
    
    @Enumerated(EnumType.STRING)
    private Status status;

    public Occupation() {
    }

    public Occupation(String name, Status status) {
        this.name = name;
        this.status = status;
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
