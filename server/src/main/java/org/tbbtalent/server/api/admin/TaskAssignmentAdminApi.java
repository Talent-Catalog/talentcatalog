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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.request.task.CreateTaskAssignmentRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.TaskAssignmentService;
import org.tbbtalent.server.service.db.TaskService;

import java.util.Map;

@RestController()
@RequestMapping("/api/admin/task-assignment")
public class TaskAssignmentAdminApi implements
        ITableApi<
            //todo replace with search request
            CreateTaskAssignmentRequest,

            CreateTaskAssignmentRequest,

            //todo replace with update request
            CreateTaskAssignmentRequest> {

    private final AuthService authService;
    private final CandidateService candidateService;
    private final TaskAssignmentService taskAssignmentService;
    private final TaskService taskService;

    @Autowired
    public TaskAssignmentAdminApi(
        AuthService authService, CandidateService candidateService,
        TaskAssignmentService taskAssignmentService,
        TaskService taskService) {
        this.authService = authService;
        this.candidateService = candidateService;
        this.taskAssignmentService = taskAssignmentService;
        this.taskService = taskService;
    }

    @Override
    @PostMapping
    public Map<String, Object> create(CreateTaskAssignmentRequest request)
        throws EntityExistsException, NoSuchObjectException {

        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        TaskImpl task = taskService.get(request.getTaskId());
        Candidate candidate = candidateService.getCandidate(request.getCandidateId());
        TaskAssignmentImpl taskAssignment =
            taskAssignmentService.assignTaskToCandidate(user, task, candidate, request.getDueDate());

        return TaskDtoHelper.getTaskAssignmentDto().build(taskAssignment);
    }

}
