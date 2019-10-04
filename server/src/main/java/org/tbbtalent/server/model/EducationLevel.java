package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "education_level")
@SequenceGenerator(name = "seq_gen", sequenceName = "education_level_id_seq", allocationSize = 1)
public class EducationLevel extends AbstractDomainObject<Long> {

    private Long id;
    private String name;
    private int sortOrder;
    @Enumerated(EnumType.STRING)
    private Status status;

    public EducationLevel() {
    }

    public EducationLevel(String name, Status status) {
        this.name = name;
        this.status = status;
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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
