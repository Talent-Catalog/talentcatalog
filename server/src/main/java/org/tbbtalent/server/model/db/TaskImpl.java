/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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


import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tbbtalent.server.model.db.task.Task;

@Getter
@Setter
// todo should be extending from the interface?
// YES - renaming to TaskImpl (Impl = "Implmentation of an interface")
public class TaskImpl extends AbstractAuditableDomainObject<Long> implements Task {
    private String name;
    private String description;
    private String timeframe; //todo What is this? It does not appear in the Task interface and has no documentation on it
    private boolean admin;
    private boolean isList; //todo What is this? It does not appear in the Task interface and has no documentation on it

    private Integer daysToComplete;
    private String helpLink;
    private boolean optional;
    private List<Task> subtasks;
}
