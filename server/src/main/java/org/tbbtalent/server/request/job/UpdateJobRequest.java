/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.request.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.JobOpportunityStage;
import org.tbbtalent.server.request.candidate.opportunity.OpportunityParams;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class UpdateJobRequest extends OpportunityParams {

    /**
     * Url link to Salesforce EmployerJob opportunity
     */
    @Nullable
    private String sfJoblink;

    @Nullable
    private JobOpportunityStage stage;

    @Nullable
    private LocalDate submissionDueDate;
}
