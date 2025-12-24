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
@SqlTable(name="candidate_visa_check", alias = "cvisac")
@SqlDefaults(mapUnannotatedColumns = true)
public class CandidateVisaCheckReadDto {
    private String ageRequirement;
    private String characterAssessment;
    private CountryReadDto country;
    private String destinationFamily;
    private String englishThreshold;
    private String healthAssessment;
    private Long id;
    private String languagesRequired;
    private String languagesThresholdMet;
    private String overallRisk;
    private String pathwayAssessment;
    private String protection;
    private String securityRisk;
    private String validTravelDocs;
    private List<CandidateVisaJobCheckReadDto> candidateVisaJobChecks;
}
