/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "country")
@SequenceGenerator(name = "seq_gen", sequenceName = "country_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
public class Country extends AbstractTranslatableDomainObject<Long> {

    /**
     * ISO code for this country
     */
    private String isoCode;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "sourceCountries")
    Set<User> users = new HashSet<>();

    public Country(String name, Status status) {
        setName(name);
        this.status = status;
    }

    @Override
    public String toString() {
        return "Country{" + "name='" + getName() +
            "', isoCode='" + isoCode + '\'' +
            '}';
    }
}
