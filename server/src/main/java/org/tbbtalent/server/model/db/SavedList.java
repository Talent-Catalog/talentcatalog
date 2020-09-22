/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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

@Entity
@Table(name = "saved_list")
@SequenceGenerator(name = "seq_gen", sequenceName = "saved_list_id_seq", allocationSize = 1)
public class SavedList extends AbstractCandidateSource {
    private static final Logger log = LoggerFactory.getLogger(SavedList.class);
    
    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_search_id")
    private SavedSearch savedSearch;

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
     * @param candidates Candidates to add to this SavedList.
     */
    public void addCandidates(Set<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            addCandidate(candidate);
        }
    }

    /**
     * Add the given candidate to this list
     * @param candidate Candidate to add
     */
    public void addCandidate(Candidate candidate) {
        final CandidateSavedList csl = 
                new CandidateSavedList(candidate, this);
        //Add candidate to the collection of candidates in this list
        getCandidateSavedLists().add(csl);
        //Also update other side of many to many relationship, adding this 
        //list to the candidate's collection of lists that they belong to.
        candidate.getCandidateSavedLists().add(csl);
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
