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

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.DocumentStatus;
import org.tctalent.server.model.db.FamilyRelations;
import org.tctalent.server.model.db.RiskLevel;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;
import org.tctalent.server.repository.db.read.annotation.JsonOneToMany;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

@Getter
@Setter
@SqlTable(name="candidate_visa_check", alias = "cvisac")
@SqlDefaults(mapUnannotatedColumns = true)
public class CandidateVisaCheckReadDto {
    private YesNo characterAssessment;
    @JsonOneToOne(joinColumn = "country_id")
    private CountryReadDto country;
    private FamilyRelations destinationFamily;
    private YesNo englishThreshold;
    private YesNo healthAssessment;
    private Long id;
    private RiskLevel overallRisk;
    private YesNoUnsure pathwayAssessment;
    private YesNo protection;
    private YesNo securityRisk;
    private DocumentStatus validTravelDocs;
    @JsonOneToMany(joinColumn = "candidate_visa_check_id")
    private List<CandidateVisaJobCheckReadDto> candidateVisaJobChecks;
}
