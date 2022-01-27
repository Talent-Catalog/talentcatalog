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

package org.tbbtalent.server.service.db;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.TaskImpl;
import org.tbbtalent.server.model.db.UploadTaskImpl;
import org.tbbtalent.server.request.PagedSearchRequest;
import org.tbbtalent.server.request.task.CreateTaskRequest;
import org.tbbtalent.server.request.task.CreateUploadTaskRequest;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public interface TaskService {
    TaskImpl createTask(CreateTaskRequest request);

    UploadTaskImpl createUploadTask(CreateUploadTaskRequest request);

    /**
     * Get the Task with the given id.
     * @param taskId ID of TaskAssignment to get
     * @return Task
     * @throws NoSuchObjectException if there is no Task with this id.
     */
    @NonNull
    TaskImpl get(long taskId) throws NoSuchObjectException;

    List<TaskImpl> listTasks();

    /**
     * Get the tasks assigned to a list of candidates (related list id)
     * @param listId the list which we want the assigned tasks associated with
     * @return List of tasks
     */
    List<TaskImpl> listTasksAssignedToList(long listId);

    /**
     * Get the tasks as a paged search request
     * @param request - Paged Search Request
     * @return Page of tasks
     */
    Page<TaskImpl> searchTasks(PagedSearchRequest request);
}
