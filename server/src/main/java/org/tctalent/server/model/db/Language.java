/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "language")
@SequenceGenerator(name = "seq_gen", sequenceName = "language_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
public class Language  extends AbstractTranslatableDomainObject<Long> {

    /**
     * ISO code for this language
     */
    private String isoCode;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Language(String name, Status status) {
        setName(name);
        this.status = status;
    }

    @Override
    public String toString() {
        return "Language{" + "name='" + getName() +
            "', isoCode='" + isoCode + '\'' +
            '}';
    }
}
