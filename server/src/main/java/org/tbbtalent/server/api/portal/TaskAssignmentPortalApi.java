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

package org.tbbtalent.server.api.portal;

import java.io.IOException;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.UnauthorisedActionException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.QuestionTaskAssignmentImpl;
import org.tbbtalent.server.model.db.TaskAssignmentImpl;
import org.tbbtalent.server.model.db.TaskDtoHelper;
import org.tbbtalent.server.model.db.task.TaskAssignment;
import org.tbbtalent.server.request.task.UpdateQuestionTaskAssignmentRequestCandidate;
import org.tbbtalent.server.request.task.UpdateTaskAssignmentRequestCandidate;
import org.tbbtalent.server.request.task.UpdateUploadTaskAssignmentRequestCandidate;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.TaskAssignmentService;

/**
 * Candidate portal API for TaskAssignment operations.
 *
 * @author John Cameron
 */
@RestController()
@RequestMapping("/api/portal/task-assignment")
public class TaskAssignmentPortalApi {
    private final AuthService authService;
    private final CandidateService candidateService;
    private final TaskAssignmentService taskAssignmentService;

    public TaskAssignmentPortalApi(
        AuthService authService,
        CandidateService candidateService,
        TaskAssignmentService taskAssignmentService) {
        this.authService = authService;
        this.candidateService = candidateService;
        this.taskAssignmentService = taskAssignmentService;
    }

    private void checkAuthorisation(TaskAssignment ta)
        throws InvalidSessionException, UnauthorisedActionException {
        //Check that candidate associated with task assignment matches logged in candidate
        Long loggedInCandidateId = authService.getLoggedInCandidateId();
        if (loggedInCandidateId == null) {
            throw new InvalidSessionException("Not logged in");
        }
        Candidate candidate = ta.getCandidate();
        if (!candidate.getId().equals(loggedInCandidateId)) {
            throw new UnauthorisedActionException("completeTaskAssignment");
        }
    }

    /**
     * Completes a candidate's simple task assignment.
     * @param id Task assignment id
     * @return The details of the updated task assignment
     * @throws NoSuchObjectException if no task assignment is found with that id
     * @throws UnauthorisedActionException if the task assignment does not belong to logged in candidate
     */
    @PostMapping("{id}/complete")
    public Map<String, Object> completeSimpleTask(@PathVariable("id") long id)
        throws NoSuchObjectException, UnauthorisedActionException {

        TaskAssignment ta = taskAssignmentService.get(id);

        checkAuthorisation(ta);

        taskAssignmentService.completeTaskAssignment(ta);

        return TaskDtoHelper.getTaskAssignmentDto().build(ta);
    }

    /**
     * Attempts completion of a candidate's upload task assignment.
     * <p/>
     * The given file will be added as an attachment to the candidate associated with the given task
     * assignment according to the attributes of the task assignment.
     * <p/>
     * If the upload is successful, the task will be marked as complete.
     * @param id Task assignment id
     * @param file Attachment file
     * @return The details of the updated upload task assignment
     * @throws NoSuchObjectException if no task assignment is found with that id
     * @throws IOException if there was a problem uploading the file
     * @throws UnauthorisedActionException if the task assignment does not belong to logged in candidate
     */
    @PostMapping("{id}/complete-upload")
    public Map<String, Object> completeUploadTask(@PathVariable("id") long id,
        @RequestParam("file") MultipartFile file )
        throws IOException, NoSuchObjectException, UnauthorisedActionException {

        TaskAssignment ta = taskAssignmentService.get(id);

        checkAuthorisation(ta);

        taskAssignmentService.completeUploadTaskAssignment(ta, file);

        return TaskDtoHelper.getTaskAssignmentDto().build(ta);
    }

    /**
     * Updates the given task assignment based on the data entered by the candidate.
     *
     * @param id ID of task assigment to be updated
     * @param request Data entered by candidate - eg their answer to the question, or comments or
     *                if they have abandoned the task
     * @return Modified task assignment - maybe now completed etc
     * @throws InvalidRequestException If a non abandoned request has no answer
     * @throws NoSuchObjectException If the given id does not match a valid task assignment
     * @throws UnauthorisedActionException If the logged user is not authorized to do this
     */
    @PutMapping("{id}/question")
    public Map<String, Object> updateQuestionTask(@PathVariable("id") long id,
        @Valid @RequestBody UpdateQuestionTaskAssignmentRequestCandidate request)
        throws InvalidRequestException, NoSuchObjectException, UnauthorisedActionException {

        boolean completed = false;
        boolean abandoned = request.isAbandoned();
        String answer = request.getAnswer();

        //If the request has not been abandoned, we expect a non empty answer
        if (!abandoned) {
            if (answer == null || answer.trim().length() == 0) {
                throw new InvalidRequestException("Missing answer to question");
            }
            completed = true;
        }

        QuestionTaskAssignmentImpl ta = (QuestionTaskAssignmentImpl) taskAssignmentService.get(id);

        checkAuthorisation(ta);

        taskAssignmentService.update(ta, completed, abandoned,
            request.getCandidateNotes(), null);

        if (completed) {
            candidateService.storeCandidateTaskAnswer(ta, answer);
        }

        return TaskDtoHelper.getTaskAssignmentDto().build(ta);
    }

    @PutMapping("{id}/upload")
    public Map<String, Object> updateUploadTaskAssignment(@PathVariable("id") long id,
        @Valid @RequestBody UpdateUploadTaskAssignmentRequestCandidate request)
        throws NoSuchObjectException, UnauthorisedActionException {

        TaskAssignmentImpl ta = taskAssignmentService.get(id);

        checkAuthorisation(ta);

        ta = taskAssignmentService.updateUploadTaskAssignment(ta, request.isAbandoned(),
            request.getCandidateNotes(), null);

        return TaskDtoHelper.getTaskAssignmentDto().build(ta);
    }

    @PutMapping("{id}")
    public Map<String, Object> updateTaskAssignment(@PathVariable("id") long id,
        @Valid @RequestBody UpdateTaskAssignmentRequestCandidate request)
        throws NoSuchObjectException, UnauthorisedActionException {

        TaskAssignmentImpl ta = taskAssignmentService.get(id);

        checkAuthorisation(ta);

        ta = taskAssignmentService.update(ta, request.isCompleted(), request.isAbandoned(),
            request.getCandidateNotes(), null);

        return TaskDtoHelper.getTaskAssignmentDto().build(ta);
    }

}
