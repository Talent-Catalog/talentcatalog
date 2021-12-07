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
import java.util.List;

@Entity
@Table(name = "todo_task")
@SequenceGenerator(name = "seq_gen", sequenceName = "todo_task_id_seq", allocationSize = 1)
public class TodoTask extends AbstractAuditableDomainObject<Long> {

    private String name;

    private String description;

    /**
     * If admin is true, this is a candidateTodo to be completed by admin and not the candidate (e.g. Intakes)
     * Use boolean rather than Boolean so that default value is false, not null.
     * Null is not allowed in Db definition
     */
    private boolean admin;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<TodoItem> todoItems;

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public boolean isAdmin() {return admin;}

    public void setAdmin(boolean admin) {this.admin = admin;}

    public List<TodoItem> getTodoItems() {return todoItems;}

    public void setTodoItems(List<TodoItem> todoItems) {this.todoItems = todoItems;}
}
