package org.tbbtalent.server.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "education_major")
@SequenceGenerator(name = "seq_gen", sequenceName = "education_major_id_seq", allocationSize = 1)
public class EducationMajor extends AbstractTranslatableDomainObject<Long> {

    @Enumerated(EnumType.STRING)
    private Status status;

    public EducationMajor() {
    }

    public EducationMajor(String name, Status status) {
        setName(name);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
