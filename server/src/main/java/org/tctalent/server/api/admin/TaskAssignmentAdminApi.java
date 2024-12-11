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
import org.springframework.web.bind.annotation.*;
import org.tctalent.server.request.task.CreateTaskAssignmentRequest;
import org.tctalent.server.request.task.TaskListRequest;
import org.tctalent.server.request.task.UpdateTaskAssignmentRequestAdmin;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskDtoHelper;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;

@RestController()
@RequestMapping("/api/admin/task-assignment")
public class TaskAssignmentAdminApi implements
        ITableApi<TaskListRequest, CreateTaskAssignmentRequest, UpdateTaskAssignmentRequestAdmin> {

    private final AuthService authService;
    private final CandidateService candidateService;
    private final SavedListService savedListService;
    private final TaskAssignmentService taskAssignmentService;
    private final TaskService taskService;

    @Autowired
    public TaskAssignmentAdminApi(
        AuthService authService, CandidateService candidateService,
        SavedListService savedListService,
        TaskAssignmentService taskAssignmentService,
        TaskService taskService) {
        this.authService = authService;
        this.candidateService = candidateService;
        this.savedListService = savedListService;
        this.taskAssignmentService = taskAssignmentService;
        this.taskService = taskService;
    }

    /**
     * Returns all task assignments matching the request.
     * @param request Defines which task assignments should be returned (belonging to what list and what task).
     * @return All matching TaskAssignmentImpls
     */
    @Override
    public @NotNull List<Map<String, Object>> search(@Valid TaskListRequest request) {
        List<TaskAssignmentImpl> taskAssignments = taskAssignmentService.listTaskAssignments(request);
        return TaskDtoHelper.getTaskAssignmentDto().buildList(taskAssignments);
    }

    @Override
    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateTaskAssignmentRequest request)
        throws EntityExistsException, NoSuchObjectException {

        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        TaskImpl task = taskService.get(request.getTaskId());
        Candidate candidate = candidateService.getCandidate(request.getCandidateId());
        TaskAssignmentImpl taskAssignment =
            taskAssignmentService.assignTaskToCandidate(user, task, candidate, null, request.getDueDate());

        return TaskDtoHelper.getTaskAssignmentDto().build(taskAssignment);
    }

    @Override
    public Map<String, Object> update(long id, UpdateTaskAssignmentRequestAdmin request)
        throws EntityExistsException, NoSuchObjectException {
        TaskAssignmentImpl ta = taskAssignmentService.get(id);

        TaskAssignmentImpl taskAssignment = taskAssignmentService.update(
            ta, request.isCompleted(), request.isAbandoned(), request.getCandidateNotes(),
            request.getDueDate());

        return TaskDtoHelper.getTaskAssignmentDto().build(taskAssignment);
    }

    @Override
    public boolean delete(long id) throws EntityReferencedException, InvalidRequestException {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return this.taskAssignmentService.deleteTaskAssignment(user, id);
    }

    @PutMapping("assign-to-list")
    public void assignTaskToList(@Valid @RequestBody TaskListRequest request)
        throws EntityExistsException, NoSuchObjectException {

        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        TaskImpl task = taskService.get(request.getTaskId());
        SavedList savedList = savedListService.get(request.getSavedListId());

        savedListService.associateTaskWithList(user, task, savedList);
    }

    @PutMapping("remove-from-list")
    public void removeTaskFromList(@Valid @RequestBody TaskListRequest request)
        throws EntityExistsException, NoSuchObjectException {

        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        TaskImpl task = taskService.get(request.getTaskId());
        SavedList savedList = savedListService.get(request.getSavedListId());

        savedListService.deassociateTaskFromList(user, task, savedList);
    }

}
