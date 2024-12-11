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

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

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
