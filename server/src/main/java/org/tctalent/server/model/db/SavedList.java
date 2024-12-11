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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.service.db.CandidateSavedListService;

/**
 * There are two kinds of SavedList:
 * <ul>
 *     <li>
 *         Normal lists. These are not directly associated with any
 *         saved search - so the savedSearch attribute will always be null.
 *     </li>
 *     <li>
 *         Selection lists. These are "hidden" lists associated with a saved
 *         search - indicated by the savedSearch attribute. They are used
 *         to record a user's items selected from the results of a saved search.
 *         So each selection list is associated with a saved search and a
 *         particular user. The sfJoblink of a selection list is copied at
 *         creation from its associated saved search.
 *         The name of a selection list is automatically created from the
 *         user and saved search id's.
 *     </li>
 * </ul>
 */
@Entity
@Table(name = "saved_list")
@SequenceGenerator(name = "seq_gen", sequenceName = "saved_list_id_seq", allocationSize = 1)
@Getter
@Setter
@Slf4j
public class SavedList extends AbstractCandidateSource {

    /**
     * Tasks associated with this list.
     * <p/>
     * Candidates assigned to this list will be automatically assigned these tasks.
     * <p/>
     * Empty set if no tasks associated.
     */
    @NonNull
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "task_saved_list",
        joinColumns = @JoinColumn(name = "saved_list_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private Set<TaskImpl> tasks = new HashSet<>();

    /**
     * Name of job description file, if one exists
     */
    @Nullable
    private String fileJdName;

    /**
     * Url link to job description file, if one exists
     */
    @Nullable
    private String fileJdLink;

    /**
     * Name of job opportunity intakefile, if one exists
     */
    @Nullable
    private String fileJoiName;

    /**
     * Url link to job opportunity intake file, if one exists
     */
    @Nullable
    private String fileJoiLink;

    /**
     * Name of interview guidance file, if one exists
     */
    @Nullable
    private String fileInterviewGuidanceName;

    /**
     * Url link to interview guidance file, if one exists
     */
    @Nullable
    private String fileInterviewGuidanceLink;

    /**
     * Name of signed MOU file, if one exists
     */
    @Nullable
    private String fileMouName;

    /**
     * Url link to signed MOU file, if one exists
     */
    @Nullable
    private String fileMouLink;

    /**
     * Url link to corresponding list folder on Google Drive, if one exists.
     * <p/>
     * This is the alpha named folder beneath the folder numerically named folder taken from the
     * lists's id: eg 12345/NameOfList12345
     */
    @Nullable
    private String folderlink;

    /**
     * Url link to corresponding Job Description folder on Google Drive, if one exists.
     * <p/>
     * This is subfolder of the folder pointed to by {@link #folderlink}
     * eg 12345/NameOfList12345/JobDescription
     */
    @Nullable
    private String folderjdlink;

    /**
     * Url link to published list doc, if one exists.
     */
    @Nullable
    private String publishedDocLink;

    /**
     * This is the suffix to used to construct a TBB hosted published url link, if one exists.
     * <p/>
     * For example in this public link...
     *   https://tctalent.org/published/physios
     * probably associated with a list of physiotherapists, the short name is "physios"
     */
    @Nullable
    private String tbbShortName;

    /**
     * If true defines the list as the "submission list" for a job.
     * <p/>
     * If true, this list is associated with a "registered" job. See the Angular "New Job" menu
     * item. A link to the job record on Salesforce is in {@link #getSfJobOpp()}.
     * There should only be one list registered to a particular job, as defined by its sfJobOpp.
     */
    @NonNull
    private Boolean registeredJob = false;

    /**
     * Non null if this is the selection list for the given saved search.
     * <p/>
     * For "normal" saved lists (ie not selection lists) this will always be
     * null.
     * <p/>
     * Note that a saved search may be shared between multiple users and each
     * user will have their own personal selection list for that saved search.
     * So to find a specific selection list you need to specify both the saved
     * search and a user.
     */
    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_search_id")
    private SavedSearch savedSearch;

    /**
     * If not null this is a saved search that contributed to the candidates
     * in the list. Some of those candidates were selected from that search.
     */
    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_search_source_id")
    private SavedSearch savedSearchSource;

    /**
     * This is the set of all CandidateSavedList entities associated with this
     * SavedList. There is one of these for each candidate in the list.
     * @see #getCandidates()
     * <p/>
     * We would prefer CascadeType.ALL with 'orphanRemoval' so that
     * removing from the candidateSavedLists collection would automatically
     * cascade down to delete the corresponding entry in the
     * candidate_saved_list table.
     * However we get Hibernate errors with that set up which it seems can only
     * be fixed by setting CascadeType.MERGE.
     * <p/>
     * See
     * https://stackoverflow.com/questions/16246675/hibernate-error-a-different-object-with-the-same-identifier-value-was-already-a
     * <p/>
     * A very good simple explanation of why JPA does not automatically delete the previous
     * contents of a collection is here:
     * https://stackoverflow.com/a/2011546/929968
     * <p/>
     *
     * This means that we have to manually manage all deletions. That has been
     * moved into {@link CandidateSavedListService} which is used to manage all
     * those deletions, also making sure that the corresponding
     * candidateSavedLists collections are kept up to date.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "savedList", cascade = CascadeType.MERGE)
    private Set<CandidateSavedList> candidateSavedLists = new HashSet<>();

    //Note use of Set rather than List as strongly recommended for Many to Many
    //relationships here:
    // https://thoughts-on-java.org/best-practices-for-many-to-many-associations-with-hibernate-and-jpa/
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "sharedLists", cascade = CascadeType.MERGE)
    private Set<User> users = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "savedList", cascade = CascadeType.ALL)
    @OrderBy("index ASC")
    @Nullable
    private List<ExportColumn> exportColumns;

    public void setExportColumns(@Nullable List<ExportColumn> exportColumns) {
        if (this.exportColumns != null) {
            this.exportColumns.clear();
        }
        modifyColumnIndices(exportColumns);
        this.exportColumns = exportColumns;
    }

    public void setRegisteredJob(Boolean registeredJob) {
        if (registeredJob != null) {
            this.registeredJob = registeredJob;
        }
    }

    /**
     * Salesforce Job opportunity account country. Can be retrieved from Salesforce to be displayed
     * when a list has a non null sfJobLink
     *
     */
    @Nullable
    public String getSfJobCountry() {
        final SalesforceJobOpp sfJobOpp = getSfJobOpp();
        Country country = sfJobOpp == null ? null : sfJobOpp.getCountry();
        return country == null ? null : country.getName();
    }

    /**
     * Salesforce Job opportunity stage. Can be retrieved from Salesforce to be displayed
     * when a list has a non null sfJobLink
     */
    @Nullable
    public JobOpportunityStage getSfJobStage() {
        final SalesforceJobOpp sfJobOpp = getSfJobOpp();
        return sfJobOpp == null ? null : sfJobOpp.getStage();
    }

    /**
     * True if the associated Salesforce job opportunity - specified by sfJobLink is closed.
     */
    public boolean isSfOppIsClosed() {
        final SalesforceJobOpp sfJobOpp = getSfJobOpp();
        return sfJobOpp == null ? false : sfJobOpp.isClosed();
    }

    /**
     * Get all candidates in this list.
     * @return Set of candidates in this list (a candidate cannot appear more
     * than once in a given list).
     */
    @Transient
    public Set<Candidate> getCandidates() {
        Set<Candidate> candidates = new HashSet<>();
        for (CandidateSavedList candidateSavedList : candidateSavedLists) {
            candidates.add(candidateSavedList.getCandidate());
        }
        return candidates;
    }

    @Override
    public Set<SavedList> getUsersCollection(User user) {
        return user.getSharedLists();
    }
}
