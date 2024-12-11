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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

/**
 * This represents an Employer Job Opportunity.
 * <p/>
 * They are backed by equivalent Employer Job Opportunity objects on Salesforce.
 * <p/>
 * Job Opps are intended to only be used for the monitoring open job opps.
 * They are not intended to completely duplicate what is on SF - eg history
 * <p/>
 * Approach to keeping in sync with Salesforce
 * ===========================================
 * - Only job opps that have been registered in TC are updated from Salesforce. There will be
 * opportunities on Salesforce that are not reflected on the TC.
 * - Once a day local open opportunities on the TC are updated from Salesforce
 * (see JobService.updateOpenJobs)
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "salesforce_job_opp")
@SequenceGenerator(name = "seq_gen", sequenceName = "salesforce_job_opp_tc_job_id_seq", allocationSize = 1)
public class SalesforceJobOpp extends AbstractOpportunity {

    //TODO JC Redundant
    /**
     * Salesforce id of account (ie employer) associated with opportunity
     */
    private String accountId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "jobOpp", cascade = CascadeType.MERGE)
    private Set<CandidateOpportunity> candidateOpportunities = new HashSet<>();

    /**
     * TC user responsible for this job - will normally be "destination" staff located in the same
     * region as the {@link #employer}
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_user_id")
    private User contactUser;

    /**
     * References country object on database (set using the country name that comes from SF)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_object_id")
    private Country country;

    /**
     * Description given to job in job intake.
     */
    private String description;

    //todo This will be redundant when we have switched to using employerEntity below.
    //todo Problem is where Job opps are copied from opps on SF. Then we rely on special
    //todo computed fields on SF like AccountName
    /**
     * Name of employer - maps to Account name on Salesforce
     */
    private String employer;

    /**
     * Link to employer associated with job.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private Employer employerEntity;

    /**
     * True if this an evergreen job - ie a job that automatically replicates when it gets
     * past the recruitment stage. A new copy of the original job is created so that new
     * candidates matching the job's requirements can continue to apply.
     */
    private boolean evergreen;

    /**
     * Evergreen child of this job.
     * <p/>
     * A job can only have one child. The primary purpose of this field is just as a flag
     * indicating that this job already has a child. This can be used to avoid a job spawning
     * more than one child - if, for example, a job's stage is set to Recruitment, spawning a
     * child, then the stage is set back to Prospect, then back to Recruitment again. That second
     * time it is moved to Recruitment will not spawn another child because this field indicates
     * that a child already exists.
     */
    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evergreen_child_id")
    private SalesforceJobOpp evergreenChild;

    /**
     * Optional exclusion list associated with job.
     * <p/>
     * Used to exclude people who have already been seen and rejected for this job from future
     * searches (see {@link SavedSearch#getExclusionList()}).
     */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exclusion_list_id")
    private SavedList exclusionList;

    /**
     * Summary describing job
     */
    private String jobSummary;

    /**
     * Name of opportunity - maps to Opportunity name on Salesforce
     */
    private String name;

    /**
     * Salesforce id of owner of opportunity
     */
    private String ownerId;

    /**
     * User that published job on the TC
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    /**
     * Time that this job was published on the TC.
     * <p/>
     * A null publishedDate indicates that the job has not been published.
     * <p/>
     * Note that for old jobs that predated the concept of "publishing" a job, publishedDate
     * should equal createdDate.
     */
    @Nullable
    private OffsetDateTime publishedDate;

    /**
     * Partner responsible for this job.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_partner_id")
    private PartnerImpl jobCreator;

    /**
     * True if no candidate search is required. The candidates to be considered have already
     * been added to the submission list.
     */
    private boolean skipCandidateSearch;

    /**
     * Stage of job opportunity
     */
    @Enumerated(EnumType.STRING)
    private JobOpportunityStage stage;

    //Note use of Set rather than List as strongly recommended for Many to Many
    //relationships here:
    // https://thoughts-on-java.org/best-practices-for-many-to-many-associations-with-hibernate-and-jpa/
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "user_job",
        joinColumns = @JoinColumn(name = "tc_job_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> starringUsers = new HashSet<>();

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
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "job_suggested_saved_search",
        joinColumns = @JoinColumn(name = "tc_job_id"),
        inverseJoinColumns = @JoinColumn(name = "saved_search_id"))
    private Set<SavedSearch> suggestedSearches = new HashSet<>();

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opp_intake_id")
    private JobOppIntake jobOppIntake;

    /**
     * Salesforce field: hiring commitment of job opportunity
     * As of 22/5/23 this may change to a text field, stored in database as text but currently a number from SF.
     */
    private Long hiringCommitment;

    /**
     * Salesforce field: the website of the employer
     * (On SF exists on Account, but copied to Opportunity and fetched with Opportunity object)
     */
    private String employerWebsite;

    /**
     * Salesforce field: if the employer has hired internationally before
     * (On SF exists on Account, but copied to Opportunity and fetched on Opportunity object)
     */
    private String employerHiredInternationally;

    /**
     * Salesforce field: opportunity score of employer job opportunity
     */
    private String opportunityScore;

    /**
     * Salesforce field: description of employer from account
     */
    private String employerDescription;

    public void addStarringUser(User user) {
        starringUsers.add(user);
    }

    public void removeStarringUser(User user) {
        starringUsers.remove(user);
    }

    /**
     * Override standard setStage to automatically also update stageOrder
     * @param stage New job opportunity stage
     */
    public void setStage(JobOpportunityStage stage) {
        this.stage = stage;
        setStageOrder(stage.ordinal());

        //Set redundant closed and won fields.
        setClosed(this.stage.isClosed());
        setWon(this.stage.isWon());
    }
}
