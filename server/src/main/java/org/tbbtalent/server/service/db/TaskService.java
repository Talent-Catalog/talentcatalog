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
import org.tbbtalent.server.model.db.task.Task;
import org.tbbtalent.server.request.task.SearchTaskRequest;

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
     * Get the tasks assigned to a list of candidates (related list id)
     * @param listId the list which we want the assigned tasks associated with
     * @return List of tasks
     */
    List<TaskImpl> listTasksAssignedToList(long listId);

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
}
