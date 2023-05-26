/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * This is a candidate opportunity to be recruited for a particular job.
 * It is backed up by a corresponding Salesforce Candidate Opportunity.
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "candidate_opportunity")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_opportunity_id_seq", allocationSize = 1)
public class CandidateOpportunity extends AbstractOpportunity {

    /**
     * Associated candidate
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    /**
     * Closing comments as presented to candidates.
     */
    @Nullable
    private String closingCommentsForCandidate;

    /**
     * Employer feedback on candidate
     */
    @Nullable
    private String employerFeedback;

    /**
     * Associated job opportunity
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opp_id")
    SalesforceJobOpp jobOpp;

    /**
     * Current stage of opportunity.
     */
    @Enumerated(EnumType.STRING)
    CandidateOpportunityStage stage;

    /**
     * Override standard setStage to automatically also update stageOrder
     * @param stage New job opportunity stage
     */
    public void setStage(@NonNull CandidateOpportunityStage stage) {
        this.stage = stage;
        setStageOrder(stage.ordinal());
    }

}
