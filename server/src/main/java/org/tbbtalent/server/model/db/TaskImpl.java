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
import org.tbbtalent.server.model.db.task.TaskType;

/**
 * Base implementation of all tasks.
 * <p/>
 * Note our Angular code only sees these base attributes tasks. Angular doesn't see the attributes
 * of subclasses. That is because our simple DTO processing and JSON loses that type specific
 * information. It doesn't matter because these attributes are all our Angular code needs in order
 * to work.
 * <p/>
 * However the Angular code does need to distinguish between different types of tasks, because they
 * will be processed differently. So for example, the Angular does need to know whether a task
 * is an upload task. That task type information is encoded in {@link #getTaskType()}.
 */
@Getter
@Setter
public class TaskImpl extends AbstractAuditableDomainObject<Long> implements Task {
    private boolean admin;
    private Integer daysToComplete;
    private String description;
    private String helpLink;
    private String name;
    private boolean optional;
    private List<Task> subtasks;

    /**
     * Type of task - this encodes the class type - so {@link TaskType#Simple} for a simple task,
     * {@link TaskType#Upload} for an UploadTask etc.
     * This allows the class type information of any task to be passed to Angular through JSON
     * serialization. Otherwise, we lose that type information when tasks objects are returned
     * through our REST Api to Angular.
     * <p/>
     * This value should be overridden by subclasses, to provide their related type.
     */
    protected TaskType taskType = TaskType.Simple;

}
