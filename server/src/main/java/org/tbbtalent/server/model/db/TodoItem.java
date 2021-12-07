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
import java.time.OffsetDateTime;

@Entity
@Table(name = "todo_item")
@SequenceGenerator(name = "seq_gen", sequenceName = "todo_item_id_seq", allocationSize = 1)
public class TodoItem extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_task_id")
    private TodoTask todoTask;

    /**
     * This tracks the status of the TodoItem defaulting to false.
     * Use boolean rather than Boolean so that default value is false, not null.
     * Null is not allowed in Db definition
     */
    private boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private User completedBy;

    @Column(name = "completed_date")
    private OffsetDateTime completedDate;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    public Candidate getCandidate() {return candidate;}

    public void setCandidate(Candidate candidate) {this.candidate = candidate;}

    public TodoTask getTodoTask() {return todoTask;}

    public void setTodoTask(TodoTask todoTask) {this.todoTask = todoTask;}

    public boolean isCompleted() {return completed;}

    public void setCompleted(boolean completed) {this.completed = completed;}

    public User getCompletedBy() {return completedBy;}

    public void setCompletedBy(User completedBy) {this.completedBy = completedBy;}

    public OffsetDateTime getCompletedDate() {return completedDate;}

    public void setCompletedDate(OffsetDateTime completedDate) {this.completedDate = completedDate;}

    public OffsetDateTime getDueDate() {return dueDate;}

    public void setDueDate(OffsetDateTime dueDate) {this.dueDate = dueDate;}
}
