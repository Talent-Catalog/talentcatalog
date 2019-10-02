package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "language")
@SequenceGenerator(name = "seq_gen", sequenceName = "language_id_seq", allocationSize = 1)
public class Language  extends AbstractDomainObject<Long> {


    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Language() {
    }

    public Language(String name, Status status) {
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
