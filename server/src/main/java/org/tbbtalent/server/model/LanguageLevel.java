package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "language_level")
@SequenceGenerator(name = "language_level_gen", sequenceName = "language_level_id_seq", allocationSize = 1)
public class LanguageLevel extends AbstractDomainObject<Long> {

    private String level;
    private int sortOrder;
    @Enumerated(EnumType.STRING)
    private Status status;

    public LanguageLevel() {
    }

    public LanguageLevel(String level, Status status) {
        this.level = level;
        this.status = status;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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
