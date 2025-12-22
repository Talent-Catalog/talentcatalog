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
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Country;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Getter
@Setter
public class CandidateJobExperienceDto {
    private Long id;
    private String companyName;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean fullTime;
    private Boolean paid;
    private String description;
    private Country country;
    private CandidateOccupation candidateOccupation;


}
