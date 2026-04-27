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

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Name/Value associated with a candidate.
 * <p/>
 * The property name is unique for the candidate. ie a candidate can't have two properties with
 * the same name.
 * <p/>
 * Primary key is {@link CandidatePropertyKey} - needed because primary key is composite, made
 * up from candidate and property name.
 */
@Entity
@Table(name = "candidate_property")
@Getter
@Setter
public class CandidateProperty {

    @EmbeddedId
    CandidatePropertyKey id;

    /**
     * Candidate associated with this property
     */
    @NonNull
    @ManyToOne
    @MapsId("candidateId")
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    /**
     * The name of the property.
     * <p/>
     * Column annotation is needed here because the "name" field is already declared in the
     * embedded id.
     */
    @Column(insertable = false, updatable = false)
    @NonNull
    private String name;

    /**
     * The value of the property - can be null
     */
    @Nullable
    private String value;

    /**
     * Task assignment associated with this property - if any. May be null.
     * <p/>
     * If not null, the task assignment will be related to a question task assigned to the candidate
     * and the candidate's answer to that question is stored in this property.
     */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_task_assignment_id")
    private TaskAssignmentImpl relatedTaskAssignment;
}
