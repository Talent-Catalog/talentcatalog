/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db;

import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.task.Task;
import org.tctalent.server.request.task.SearchTaskRequest;
import org.tctalent.server.request.task.UpdateTaskRequest;

import java.util.List;

/**
 * Service for managing {@link Task}
 *
 * @author John Cameron
 */
public interface TaskService {

    /**
     * Get the Task with the given id.
     * @param taskId ID of TaskAssignment to get
     * @return Task
     * @throws NoSuchObjectException if there is no Task with this id.
     */
    @NonNull
    TaskImpl get(long taskId) throws NoSuchObjectException;

    /**
     * Get the Task with the given name.
     * @param name name of TaskAssignment to get
     * @return Task
     * @throws NoSuchObjectException if there is no Task with this name.
     */
    @NonNull
    TaskImpl getByName(String name) throws NoSuchObjectException;

    List<TaskImpl> listTasks();

    /**
     * Populate transient fields on given task
     * @param task Task to populate
     */
    void populateTransientFields(Task task);

    /**
     * Get the tasks as a paged search request
     * @param request - Paged Search Request
     * @return Page of tasks
     */
    Page<TaskImpl> searchTasks(SearchTaskRequest request);

    /**
     * Update the task with the given ID. Only allows admins to update a task display name and description at the moment.
     * @param id of task to update
     * @param request Request contains updated display name and description
     * @return Updated task
     * @throws EntityExistsException if updated name request already exists.
     * @throws NoSuchObjectException if there is not task with this id.
     */
    TaskImpl update(long id, UpdateTaskRequest request) throws EntityExistsException, NoSuchObjectException;
}
