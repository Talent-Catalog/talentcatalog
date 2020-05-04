/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "saved_list")
@SequenceGenerator(name = "seq_gen", sequenceName = "saved_list_id_seq", allocationSize = 1)
public class SavedList extends AbstractCandidateSource {
    private static final Logger log = LoggerFactory.getLogger(SavedList.class);

    //Note use of Set rather than List as strongly recommended for Many to Many
    //relationships here: 
    // https://thoughts-on-java.org/best-practices-for-many-to-many-associations-with-hibernate-and-jpa/
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "savedLists", cascade = CascadeType.MERGE)
    private Set<Candidate> candidates = new HashSet<>();

    //Note use of Set rather than List as strongly recommended for Many to Many
    //relationships here: 
    // https://thoughts-on-java.org/best-practices-for-many-to-many-associations-with-hibernate-and-jpa/
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "sharedLists", cascade = CascadeType.MERGE)
    private Set<User> users = new HashSet<>();

    public Set<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(Set<Candidate> candidates) {
        this.candidates.clear();
        for (Candidate candidate : candidates) {
            addCandidate(candidate);
        }
    }

    /**
     * Add the given candidate to this list
     * @param candidate Candidate to add
     */
    public void addCandidate(Candidate candidate) {
        //Add candidate to the collection of candidates in this list
        getCandidates().add(candidate);
        //Also update other side of many to many relationship, adding this 
        //list to the candidate's collection of lists that they belong to.
        candidate.getSavedLists().add(this);
    }

    /**
     * Remove the given candidate from this list
     * @param candidate Candidate to remove
     */
    public void removeCandidate(Candidate candidate) {
        //Add candidate to the collection of candidates in this list
        getCandidates().remove(candidate);
        //Also update other side of many to many relationship, adding this 
        //list to the candidate's collection of lists that they belong to.
        candidate.getSavedLists().remove(this);
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
