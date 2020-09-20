/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "candidate_saved_list")
public class CandidateSavedList {

    @EmbeddedId
    CandidateSavedListKey id = new CandidateSavedListKey();
    
    @ManyToOne
    @MapsId("candidateId")
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne
    @MapsId("savedListId")
    @JoinColumn(name = "saved_list_id")
    private SavedList savedList;
    
    private String contextNote;

    public CandidateSavedList() {
    }

    public CandidateSavedList(Candidate candidate, SavedList savedList) {
        this.candidate = candidate;
        this.savedList = savedList;
        
        candidate.getCandidateSavedLists().add(this);
        savedList.getCandidateSavedLists().add(this);
    }
}
