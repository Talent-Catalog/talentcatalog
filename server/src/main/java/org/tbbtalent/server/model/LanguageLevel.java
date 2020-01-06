package org.tbbtalent.server.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "language_level")
@SequenceGenerator(name = "language_level_gen", sequenceName = "language_level_id_seq", allocationSize = 1)
public class LanguageLevel extends AbstractTranslatableDomainObject<Long> {

    private int level;
    @Enumerated(EnumType.STRING)
    private Status status;

    public LanguageLevel() {
    }

    public LanguageLevel(String name, Status status, int level) {
        setName(name);
        this.status = status;
        this.level = level;
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
