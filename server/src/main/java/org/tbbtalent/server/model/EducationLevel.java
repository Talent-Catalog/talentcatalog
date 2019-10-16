package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "education_level")
@SequenceGenerator(name = "seq_gen", sequenceName = "education_level_id_seq", allocationSize = 1)
public class EducationLevel extends AbstractDomainObject<Long> {

    private String name;
    private int level;
    @Enumerated(EnumType.STRING)
    private Status status;

    public EducationLevel() {
    }

    public EducationLevel(String name, Status status, int level) {
        this.name = name;
        this.status = status;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
