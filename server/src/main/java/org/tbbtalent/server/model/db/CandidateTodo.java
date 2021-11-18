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

    private String type;

    /**
     * Use boolean rather than Boolean so that default value is false, not null.
     * Null is not allowed in Db definition
     */
    private boolean completed;

    public Candidate getCandidate() {return candidate;}

    public void setCandidate(Candidate candidate) {this.candidate = candidate;}

    public String getType() {return type;}

    public void setType(String type) {this.type = type;}

    public boolean isCompleted() {return completed;}

    public void setCompleted(boolean completed) {this.completed = completed;}
}
