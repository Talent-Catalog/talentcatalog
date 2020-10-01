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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity representing the Many to Many relationship between Candidates and
 * SavedLists.
 * <p/>
 * It is expressed as an entity so that it can have its own attributes
 * namely {@link #contextNote}.
 * <p/>
 * See 
 * https://www.baeldung.com/jpa-many-to-many
 * https://thorben-janssen.com/many-relationships-additional-properties/ 
 * https://stackoverflow.com/questions/52648330/spring-data-jpa-manytomany-relationship-with-extra-column
 * <p/>
 * Note that it is not explicitly stated in the above links (although it is
 * coded in the GitHub source of the Baeldung example) but this object
 * should also have equals and hashCode implementations which are 
 * uniquely defined by the {@link CandidateSavedListKey} id.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "candidate_saved_list")
public class CandidateSavedList {

    @EqualsAndHashCode.Include
    @EmbeddedId
    CandidateSavedListKey id;

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
        this.id = new CandidateSavedListKey(candidate.getId(), savedList.getId()); 
    }
}
