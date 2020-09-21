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

@Entity
@Table(name = "saved_list")
@SequenceGenerator(name = "seq_gen", sequenceName = "saved_list_id_seq", allocationSize = 1)
public class SavedList extends AbstractCandidateSource {
    private static final Logger log = LoggerFactory.getLogger(SavedList.class);
    
    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_search_id")
    private SavedSearch savedSearch;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "savedList", cascade = CascadeType.ALL, orphanRemoval = true)
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
     * Clears all candidates from SavedList
     */
    private void clear() {
        //Remove this list from all candidate entities
        for (CandidateSavedList csl : this.candidateSavedLists) {
            csl.getCandidate().getCandidateSavedLists().remove(csl);
        }
        //Clear set of candidates
        this.candidateSavedLists.clear();
    }

    /**
     * Replaces any existing candidates with the given set of candidates
     * or sets contents of list to empty if set is empty or null.
     * <p/>
     * See also {@link #addCandidates}
     * @param candidates New set of candidates belonging to this SavedList. 
     */
    public void setCandidates(@Nullable Set<Candidate> candidates) {
        clear();
        if (candidates != null) {
            addCandidates(candidates);
        }
    }

    /**
     * Add the given candidates to this SavedList - merging them in with any
     * existing candidates in the list (no duplicates - if a candidate is
     * already present it will still only appear once).
     * <p/>
     * See also {@link #setCandidates}
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

    public void removeCandidates(Set<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            removeCandidate(candidate);
        }
    }

    /**
     * Remove the given candidate from this list
     * @param candidate Candidate to remove
     */
    public void removeCandidate(Candidate candidate) {
        final CandidateSavedList csl =
                new CandidateSavedList(candidate, this);
        //Add candidate to the collection of candidates in this list
        getCandidateSavedLists().remove(csl);
        //Also update other side of many to many relationship, adding this 
        //list to the candidate's collection of lists that they belong to.
        candidate.getCandidateSavedLists().remove(csl);
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
