/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "country")
@SequenceGenerator(name = "seq_gen", sequenceName = "country_id_seq", allocationSize = 1)
public class Country extends AbstractTranslatableDomainObject<Long> {

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "sourceCountries")
    Set<User> users = new HashSet<User>();

    public Country() {
    }

    public Country(String name, Status status) {
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
