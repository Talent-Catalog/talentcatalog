/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.repository.db.read.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Getter
@Setter
@SqlTable(name="candidate_dependant", alias = "cdep")
@SqlDefaults(mapUnannotatedColumns = true)
public class CandidateDependantReadDto {
    private LocalDate dob;
    private String gender;
    private Boolean healthConcern;
    private String healthNotes;
    private Long id;
    private String name;
    private String registered;
    private String registeredNotes;
    private String registeredNumber;
    private String relation;
}
