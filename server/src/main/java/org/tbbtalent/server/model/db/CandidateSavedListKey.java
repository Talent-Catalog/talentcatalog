/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Primary key for {@link CandidateSavedList}. 
 * See doc for that class.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Embeddable
public class CandidateSavedListKey implements Serializable {
    
    @Column(name = "candidate_id")
    private Long candidateId;

    @Column(name = "saved_list_id")
    private Long savedListId;

    public CandidateSavedListKey() {
    }

    public CandidateSavedListKey(Long candidateId, Long savedListId) {
        this.candidateId = candidateId;
        this.savedListId = savedListId;
    }
}
