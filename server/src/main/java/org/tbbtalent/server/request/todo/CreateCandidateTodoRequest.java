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

package org.tbbtalent.server.request.todo;

import org.tbbtalent.server.model.db.TodoType;

import javax.validation.constraints.NotNull;

public class CreateCandidateTodoRequest {
    @NotNull
    private Long candidateId;
    private TodoType type;
    @NotNull
    private String name;
    private boolean completed;

    public Long getCandidateId() {return candidateId;}

    public void setCandidateId(Long candidateId) {this.candidateId = candidateId;}

    public TodoType getType() {return type;}

    public void setType(TodoType type) {this.type = type;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public boolean isCompleted() {return completed;}

    public void setCompleted(boolean completed) {this.completed = completed;}
}
