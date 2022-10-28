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

import java.time.LocalDate;
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
    private String sfId;

    /**
     * Automatically generated unique numeric id for this job
     */
    @SequenceGenerator(name = "seq_gen", sequenceName = "tc_job_id_seq", allocationSize = 1)
    @Column(name = "tc_job_id")
    private Long id;

    /**
     * Salesforce id of account (ie employer) associated with opportunity
     */
    private String accountId;

    /**
     * True if opportunity is closed
     */
    private boolean closed;

    /**
     * Email to use for enquiries about this job.
     * <p/>
     * Should default to email of {@link #contactUser} - but can be different
     */
    private String contactEmail;

    /**
     * TC user responsible for this job - will normally be "destination" staff located in the same
     * region as the {@link #employer}
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_user_id")
    private User contactUser;

    /**
     * Name of country where job is located
     */
    private String country;

    /**
     * Name of employer - maps to Account name on Salesforce
     */
    private String employer;

    /**
     * Summary describing job
     */
    private String jobSummary;

    /**
     * Last time that this was updated from Salesforce (which holds the master copy)
     */
    private OffsetDateTime lastUpdate;

    /**
     * Name of opportunity - maps to Opportunity name on Salesforce
     */
    private String name;

    /**
     * Salesforce id of owner of opportunity
     */
    private String ownerId;

    /**
     * Recruiter partner responsible for this job.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_partner_id")
    private RecruiterPartnerImpl recruiterPartner;

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
     * Date that submission of candidates to employer is due.
     */
    @Nullable
    private LocalDate submissionDueDate;

    /**
     * This is the official list of candidates which will be submitted to the employer for
     * their consideration.
     * <p/>
     * This list should have the {@link SavedList#getRegisteredJob()} attribute set true.
     * That marks it as a special list associated with a single job.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_list_id")
    private SavedList submissionList;

    /**
     * Optional list containing candidates that the employer/recruiter thought looked right for the
     * job
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_list_id")
    private SavedList suggestedList;

    /**
     * Optional search(es) that the employer/recruiter thought would find candidates matching the
     * job requirements.
     */
//    //TODO JC
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//        name = "job_suggested_saved_search",
//        joinColumns = @JoinColumn(name = "tc_job_id", referencedColumnName = "tc_job_id"),
//        inverseJoinColumns = @JoinColumn(name = "saved_search_id"))
//    private Set<SavedSearch> suggestedSearches = new HashSet<>();

    /**
     * Override standard setStage to automatically also update stageOrder
     * @param stage New job opportunity stage
     */
    public void setStage(JobOpportunityStage stage) {
        this.stage = stage;
        setStageOrder(stage.ordinal());
    }
}
