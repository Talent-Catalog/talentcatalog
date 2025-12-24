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
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Getter
@Setter
@SqlTable(name="candidate_job_experience", alias = "cje")
@SqlDefaults(mapUnannotatedColumns = true)
public class CandidateJobExperienceReadDto {
    @JsonOneToOne(joinLeftColumn = "candidate_occupation_id")
    private CandidateOccupationReadDto candidateOccupation;
    private String companyName;
    @JsonOneToOne(joinLeftColumn = "country_id")
    private CountryReadDto country;
    private String description;
    private LocalDate endDate;
    private String fullTime;
    private Long id;
    private String paid;
    private String role;
    private LocalDate startDate;
}
