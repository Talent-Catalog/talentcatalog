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

import javax.persistence.*;

@Entity
@Table(name = "candidate_todo")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_todo_id_seq", allocationSize = 1)
public class CandidateTodo extends AbstractAuditableDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    /**
     * An enum to determine the category of candidateTodo eg. Visa Check, Visa Job Check, Getting Documents or 'Custom'.
     * This helps group candidateTodos and build lists of todos.
     */
    @Enumerated(EnumType.STRING)
    private TodoType type;

    /**
     * The name of the candidateTodo more specific than the type.
     * This is a unique value per candidate, to help avoid double ups and allow for changes to be made to completed (switch true to false for example).
     * Eg. only ONE with name VisaCheckAu, or in terms of Visa Job Check types there will only be ONE for each job name.
     */
    private String name;

    /**
     * This tracks the status of the candidateTodo defaulting to false.
     * Use boolean rather than Boolean so that default value is false, not null.
     * Null is not allowed in Db definition
     */
    private boolean completed;

    /**
     * If admin is true, this is a candidateTodo to be completed by admin and not the candidate (e.g. Intakes)
     * Use boolean rather than Boolean so that default value is false, not null.
     * Null is not allowed in Db definition
     */
    private boolean admin;

    public Candidate getCandidate() {return candidate;}

    public void setCandidate(Candidate candidate) {this.candidate = candidate;}

    public TodoType getType() {return type;}

    public void setType(TodoType type) {this.type = type;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public boolean isCompleted() {return completed;}

    public void setCompleted(boolean completed) {this.completed = completed;}

    public boolean isAdmin() {return admin;}

    public void setAdmin(boolean admin) {this.admin = admin;}
}
