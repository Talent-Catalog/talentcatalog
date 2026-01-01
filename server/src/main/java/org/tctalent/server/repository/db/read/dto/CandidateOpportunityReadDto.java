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
import java.time.OffsetDateTime;
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
@SqlTable(name="candidate_opportunity", alias = "cop")
@SqlDefaults(mapUnannotatedColumns = true)
public class CandidateOpportunityReadDto {
    @JsonOneToOne(joinColumn = "candidate_id")
    private ShortCandidateReadDto candidate;
    private String closingComments;
    private String closingCommentsForCandidate;
    @JsonOneToOne(joinColumn = "created_by")
    private UserReadDto createdBy;
    private OffsetDateTime createdDate;
    private String employerFeedback;
    private Long id;
    @JsonOneToOne(joinColumn = "job_opp_id")
    private JobOppReadDto jobOpp;
    private String lastActiveStage;
    private String name;
    private String nextStep;
    private LocalDate nextStepDueDate;
    private String relocatingDependantIds;
    private String sfId;
    private String stage;
    @JsonOneToOne(joinColumn = "updated_by")
    private UserReadDto updatedBy;
    private OffsetDateTime updatedDate;
}
