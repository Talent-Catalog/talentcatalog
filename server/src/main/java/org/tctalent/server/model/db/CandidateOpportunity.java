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

package org.tctalent.server.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opp_id")
    SalesforceJobOpp jobOpp;

    /**
     * Last active stage of opportunity - it is there for closed opportunities to know how far a candidate opportunity
     * got before it was closed. Defaults to prospect.
     */
    @Enumerated(EnumType.STRING)
    CandidateOpportunityStage lastActiveStage = CandidateOpportunityStage.prospect;

    /**
     * Current stage of opportunity.
     */
    @Enumerated(EnumType.STRING)
    CandidateOpportunityStage stage;

    /**
     * Url link to job offer contract file, if one exists
     */
    @Nullable
    private String fileOfferLink;

    /**
     * Name of job offer contract file, if one exists
     */
    @Nullable
    private String fileOfferName;

    /**
     * String of the ids of the candidate dependants that are relocating as part of the visa job check.
     * This is a simple string of ids to avoid a lengthy and unnecessary many-to-many relationship,
     * as we don't need to track the inverse relationship of dependants and their associated visa job checks.
     * It is a string as opposed to a List of ids due to the error: ''Basic' attribute type should not be a container'
     */
    private String relocatingDependantIds;

    /**
     * Get the string of relocating dependant ids and convert to a comma separated list of ids(long).
     * @return List of candidate dependant ids
     */
    public List<Long> getRelocatingDependantIds() {
        return relocatingDependantIds != null ?
            Stream.of(relocatingDependantIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList()) : null;
    }

    /**
     * Set the list of ids (long) to a string of ids comma separated to save to database.
     */
    public void setRelocatingDependantIds(List<Long> relocatingDependantIds) {
        this.relocatingDependantIds = !CollectionUtils.isEmpty(relocatingDependantIds) ?
            relocatingDependantIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }

    /**
     * Override standard setStage to automatically also update stageOrder and also closed
     * @param stage New job opportunity stage
     */
    public void setStage(@NonNull CandidateOpportunityStage stage) {
        this.stage = stage;
        setStageOrder(stage.ordinal());
        setClosed(this.stage.isClosed());
        setWon(this.stage.isWon());
        if (!stage.isClosed()) {
            setLastActiveStage(stage);
        }
    }

}
