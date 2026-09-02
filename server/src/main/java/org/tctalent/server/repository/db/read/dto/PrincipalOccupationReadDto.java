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

package org.tctalent.server.repository.db.read.dto;

import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

/**
 * Minimal view of a candidate's principal occupation - just enough to identify which occupation
 * it is, without the job experiences and audit user details that {@link CandidateOccupationReadDto}
 * carries. Used for {@link CandidateReadDto#getPrincipalOccupation()}, which is included on every
 * candidate in search results, so keeping this small matters for search performance.
 */
@Getter
@Setter
@SqlTable(name="candidate_occupation", alias = "pocc")
@SqlDefaults(mapUnannotatedColumns = true)
public class PrincipalOccupationReadDto {
    private Long id;
    @JsonOneToOne(joinColumn = "occupation_id")
    private OccupationReadDto occupation;
}
