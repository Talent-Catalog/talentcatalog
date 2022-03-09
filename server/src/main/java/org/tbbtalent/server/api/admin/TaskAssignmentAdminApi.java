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

package org.tbbtalent.server.api.admin;

import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.TaskAssignmentImpl;
import org.tbbtalent.server.model.db.TaskDtoHelper;
import org.tbbtalent.server.model.db.TaskImpl;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.task.TaskListRequest;
import org.tbbtalent.server.request.task.CreateTaskAssignmentRequest;
import org.tbbtalent.server.request.task.UpdateTaskAssignmentRequestAdmin;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.TaskAssignmentService;
import org.tbbtalent.server.service.db.TaskService;

@RestController()
@RequestMapping("/api/admin/task-assignment")
public class TaskAssignmentAdminApi implements
        ITableApi<
            //todo replace with search request
            CreateTaskAssignmentRequest,

            CreateTaskAssignmentRequest, UpdateTaskAssignmentRequestAdmin> {

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
