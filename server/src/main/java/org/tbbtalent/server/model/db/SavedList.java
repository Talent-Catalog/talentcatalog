/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.service.db.CandidateSavedListService;

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
public class SavedList extends AbstractCandidateSource {
    private static final Logger log = LoggerFactory.getLogger(SavedList.class);

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
     * Even though we would prefer CascadeType.ALL with 'orphanRemoval' so that 
     * removing from the candidateSavedLists collection would automatically
     * cascade down to delete the corresponding entry in the 
     * candidate_saved_list table.
     * However we get Hibernate errors with that set up which it seems can only 
     * be fixed by setting CascadeType.MERGE.
     * <p/>
     * See
     * https://stackoverflow.com/questions/16246675/hibernate-error-a-different-object-with-the-same-identifier-value-was-already-a
     * <p/>
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

    @Nullable
    public SavedSearch getSavedSearch() {
        return savedSearch;
    }

    public void setSavedSearch(@Nullable SavedSearch savedSearch) {
        this.savedSearch = savedSearch;
    }

    @Nullable
    public SavedSearch getSavedSearchSource() {
        return savedSearchSource;
    }

    public void setSavedSearchSource(@Nullable SavedSearch savedSearchSource) {
        this.savedSearchSource = savedSearchSource;
    }

    public Set<CandidateSavedList> getCandidateSavedLists() {
        return candidateSavedLists;
    }

    public void setCandidateSavedLists(Set<CandidateSavedList> candidateSavedLists) {
        this.candidateSavedLists = candidateSavedLists;
    }

    @Transient
    public Set<Candidate> getCandidates() {
        Set<Candidate> candidates = new HashSet<>();
        for (CandidateSavedList candidateSavedList : candidateSavedLists) {
            candidates.add(candidateSavedList.getCandidate());
        }
        return candidates;
    }

    /**
     * Add the given candidates to this SavedList - merging them in with any
     * existing candidates in the list (no duplicates - if a candidate is
     * already present it will still only appear once).
     * <p/>
     * If a source list is supplied, the original candidate context will be
     * copied across (eg contextNote).
     * @param candidates Candidates to add to this SavedList.
     * @param sourceList If not null, refers to the list where candidates came
     *                   from, so that context can be copied across.
     */
    public void addCandidates(
            Set<Candidate> candidates, @Nullable SavedList sourceList) {
        for (Candidate candidate : candidates) {
            addCandidate(candidate, sourceList);
        }
    }

    public void addCandidates(Set<Candidate> candidates) {
        addCandidates(candidates, null);
    }

    /**
     * Add the given candidate to this list
     * @param candidate Candidate to add
     * @param sourceList If not null, refers to the list where candidate came
     *                   from, so that context can be copied across.
     */
    public void addCandidate(
            Candidate candidate, @Nullable SavedList sourceList) {
        String contextNote = null;
        if (sourceList != null) {
            //Need to copy the context across from the source list.
            //Contract csl we are looking for
            CandidateSavedList targetCsl = 
                    new CandidateSavedList(candidate, sourceList);
            Set<CandidateSavedList> sourceCsls = 
                    candidate.getCandidateSavedLists();
            for (CandidateSavedList sourceCsl : sourceCsls) {
                if (sourceCsl.equals(targetCsl)) {
                    contextNote = sourceCsl.getContextNote();
                    break;
                }
            }
        }
        
        //Create new candidate/list link
        final CandidateSavedList csl = 
                new CandidateSavedList(candidate, this);
        //Copy across context
        if (contextNote != null) {
            csl.setContextNote(contextNote);
        }
        
        //Add candidate to the collection of candidates in this list
        getCandidateSavedLists().add(csl);
        //Also update other side of many to many relationship, adding this 
        //list to the candidate's collection of lists that they belong to.
        candidate.getCandidateSavedLists().add(csl);
    }
    
    public void addCandidate(Candidate candidate) {
        addCandidate(candidate, null);
    }

    @Override
    public Set<User> getUsers() {
        return users;
    }

    @Override
    public Set<SavedList> getUsersCollection(User user) {
        return user.getSharedLists();
    }
}
