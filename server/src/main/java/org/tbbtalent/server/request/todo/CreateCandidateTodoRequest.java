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

import javax.validation.constraints.NotNull;

public class CreateCandidateTodoRequest {
    @NotNull
    private Long candidateId;
    @NotNull
    private String type;

    private boolean completed;

    public Long getCandidateId() {return candidateId;}

    public void setCandidateId(Long candidateId) {this.candidateId = candidateId;}

    public String getType() {return type;}

    public void setType(String type) {this.type = type;}

    public boolean isCompleted() {return completed;}

    public void setCompleted(boolean completed) {this.completed = completed;}
}
