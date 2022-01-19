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
import org.tbbtalent.server.model.db.task.QuestionTask;
import org.tbbtalent.server.model.db.task.Task;
import org.tbbtalent.server.model.db.task.TaskType;
import org.tbbtalent.server.model.db.task.UploadInfo;
import org.tbbtalent.server.model.db.task.UploadTask;

/**
 * Implementation of all Task interfaces - flattened out into a single implementation rather
 * than subclasses. Necessary given our given code base and the need to stream lists of tasks
 * to the Angular where each of task can be a different type - eg simple task, question task,
 * upload task.
 * <p/>
 * What this means is that all special attributes - such as upload info for upload tasks, or
 * questions for question tasks - are all always present (although they be null) in every task
 * object - even though they may not be relevant.
 * <p/>
 * To replace a normal class hierarchy, the actual type of a task is determined by {@link #getType()}.
 */
@Getter
@Setter
public class TaskImpl extends AbstractAuditableDomainObject<Long>
    implements Task, QuestionTask, UploadTask {
    private boolean admin;
    private Integer daysToComplete;
    private String description;
    private String helpLink;
    private String name;
    private boolean optional;
    private String question;
    private List<Task> subtasks;
    private TaskType type;

    private UploadInfo uploadInfo;
}
