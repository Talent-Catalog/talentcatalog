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

package org.tctalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.api.dto.DtoType;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.TaskDtoHelper;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.request.task.SearchTaskRequest;
import org.tctalent.server.request.task.UpdateTaskRequest;
import org.tctalent.server.service.db.TaskService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/task")
public class TaskAdminApi implements
        ITableApi<SearchTaskRequest, UpdateTaskRequest, UpdateTaskRequest> {

    private final TaskService taskService;

    @Autowired
    public TaskAdminApi(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public @NotNull List<Map<String, Object>> list() {
        List<TaskImpl> tasks = taskService.listTasks();
        return TaskDtoHelper.getTaskDto().buildList(tasks);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(
        @Valid SearchTaskRequest request) {
        Page<TaskImpl> tasks = this.taskService.searchTasks(request);
        return TaskDtoHelper.getTaskDto().buildPage(tasks);
    }

    @Override
    public @NotNull Map<String, Object> get(long id, DtoType dtoType) throws NoSuchObjectException {
        TaskImpl task = this.taskService.get(id);
        return TaskDtoHelper.getTaskDto().build(task);
    }

    @Override
    public @NotNull Map<String, Object> update(
            long id, @Valid UpdateTaskRequest request)
            throws EntityExistsException, NoSuchObjectException {
        TaskImpl task = this.taskService.update(id, request);
        return TaskDtoHelper.getTaskDto().build(task);
    }

}
