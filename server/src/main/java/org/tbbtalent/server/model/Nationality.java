package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "nationality")
@SequenceGenerator(name = "seq_gen", sequenceName = "nationality_id_seq", allocationSize = 1)
public class Nationality  extends AbstractDomainObject<Long> {

    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Nationality() {
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
