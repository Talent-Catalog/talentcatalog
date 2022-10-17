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

package org.tbbtalent.server.model.db;

import java.time.OffsetDateTime;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

/**
 * This is a copy of an Employer Job Opportunity on Salesforce
 * <p/>
 * Job Opps are intended to only be used for the monitoring open job opps.
 * They are not intended to completely duplicate what is on SF - eg history
 * <p/>
 * Approach to keeping in sync with Salesforce
 * ===========================================
 * - Only job opps that have been registered in TC are updated from Salesforce. There will be
 * opportunities on Salesforce that are not reflected on the TC.
 * - Once a day local open opportunities on the TC are updated from Salesforce
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "salesforce_job_opp")
public class SalesforceJobOpp {

    /**
     * ID of copied Salesforce job opportunity is also used as id of this copy.
     */
    @Id
    @javax.persistence.Id
    @Column(name = "id")
    private String id;


    @SequenceGenerator(name = "seq_gen", sequenceName = "tc_job_id_seq", allocationSize = 1)
    private Long tcJobId;

    /**
     * Salesforce id of account (ie employer) associated with opportunity
     */
    private String accountId;

    /**
     * True if opportunity is closed
     */
    private boolean closed;

    /**
     * Name of country where job is located
     */
    private String country;

    /**
     * Name of employer - maps to Account name on Salesforce
     */
    private String employer;

    /**
     * Name of opportunity - maps to Opportunity name on Salesforce
     */
    private String name;

    /**
     * Salesforce id of owner of opportunity
     */
    private String ownerId;

    /**
     * Stage of job opportunity
     */
    @Enumerated(EnumType.STRING)
    private JobOpportunityStage stage;

    /**
     * Stage of job opportunity expressed as number - 0 being first stage.
     * <p/>
     * Used for sorting by stage.
     * <p/>
     * This is effectively a computed field, computed by calling the ordinal() method of the
     * {@link #stage} enum.
     */
    private int stageOrder;

    /**
     * Last time that this was updated from Salesforce (which holds the master copy)
     */
    private OffsetDateTime lastUpdate;


    /**
     * Date that submission of candidates to employer is due.
     */
    @Nullable
    private OffsetDateTime submissionDueDate;

    /**
     * This is the official list of candidates which will be submitted to the employer for
     * their consideration.
     * <p/>
     * SubmissionList should be a registeredJob associated with sfJobOpp
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_list_id")
    private SavedList submissionList;

    /**
     * Override standard setStage to automatically also update stageOrder
     * @param stage New job opportunity stage
     */
    public void setStage(JobOpportunityStage stage) {
        this.stage = stage;
        setStageOrder(stage.ordinal());
    }
}
