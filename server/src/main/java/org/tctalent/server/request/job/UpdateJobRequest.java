/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.request.job;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.request.candidate.opportunity.OpportunityParams;

@Getter
@Setter
@ToString
public class UpdateJobRequest extends OpportunityParams {

    @Nullable
    private Long contactUserId;

    /**
     * If present indicates that this job is an evergreen job.
     */
    @Nullable
    private Boolean evergreen;

    /**
     * Name of the role associated with the job - for example "Senior programmer"
     */
    @Nullable
    private String roleName;

    /**
     * Id of associated  Salesforce job opportunity - which will need to be updated in sync with
     * the TC Job. If null, a new Salesforce job opportunity needs to be created to match the
     * TC job.
     */
    @Nullable
    private String sfId;

    /**
     * Url link to Salesforce Job opportunity. Only used when creating a job from an existing
     * Salesforce job opportunity
     */
    @Nullable
    private String sfJoblink;

    /**
     * If present indicates whether candidate search can be skipped
     */
    @Nullable
    private Boolean skipCandidateSearch;

    @Nullable
    private JobOpportunityStage stage;

    @Nullable
    private LocalDate submissionDueDate;

    /**
     * If present, the job is to have certain fields copied from the job belonging to this job id.
     */
    @Nullable
    private Long jobToCopyId;

    /**
     * If present, the name has been edited by the user who created the Job, or a System Admin.
     */
    @Nullable
    private String jobName;
}
