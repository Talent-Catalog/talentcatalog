package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "language_level")
@SequenceGenerator(name = "language_level_gen", sequenceName = "language_level_id_seq", allocationSize = 1)
public class LanguageLevel extends AbstractDomainObject<Long> {

    private String name;
    private int level;
    @Enumerated(EnumType.STRING)
    private Status status;

    public LanguageLevel() {
    }

    public LanguageLevel(String name, Status status, int level) {
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
