/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "language_level")
//The generator must be named seq_gen to match the inherited @Id. Hibernate 6 resolved the misnamed
//"language_level_gen" generator to Language's language_id_seq, so ids have come from that
//sequence. Kept until the ids are moved to language_level_id_seq - TC-1189.
@SequenceGenerator(name = "seq_gen", sequenceName = "language_id_seq", allocationSize = 1)
@NoArgsConstructor
public class LanguageLevel extends AbstractTranslatableDomainObject<Long> {

    private int level;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private CefrLevel cefrLevel;

    public LanguageLevel(String name, Status status, int level, CefrLevel cefrLevel) {
        setName(name);
        this.status = status;
        this.level = level;
        this.cefrLevel = cefrLevel;
    }

}
