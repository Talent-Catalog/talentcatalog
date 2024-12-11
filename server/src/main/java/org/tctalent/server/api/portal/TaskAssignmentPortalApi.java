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

package org.tctalent.server.api.portal;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.QuestionTaskAssignmentImpl;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskDtoHelper;
import org.tctalent.server.model.db.task.TaskAssignment;
import org.tctalent.server.request.task.UpdateQuestionTaskAssignmentRequestCandidate;
import org.tctalent.server.request.task.UpdateTaskAssignmentCommentRequest;
import org.tctalent.server.request.task.UpdateTaskAssignmentRequestCandidate;
import org.tctalent.server.request.task.UpdateUploadTaskAssignmentRequestCandidate;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.TaskAssignmentService;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;

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
     * This is to update the upload tasks other fields (if it is abandoned and/or if there are candidate notes.
     * The upload is processed separately see method above. So to update the other fields requires a separate API call.
     * @param id Id of the task assignment to be updated.
     * @param request A request that contains if task is abandoned and or candidate notes.
     * @return The details of the updated task assignment
     * @throws NoSuchObjectException
     * @throws UnauthorisedActionException
     */
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
        } else {
            // If the request has been abandoned, we want to set the completed value as false.
            completed = false;
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

    /**
     * Completes a candidate's simple task assignment.
     * @param id Task assignment id
     * @return The details of the updated task assignment, is completed, is abandoned, and candidate notes.
     * @throws NoSuchObjectException if no task assignment is found with that id
     * @throws UnauthorisedActionException if the task assignment does not belong to logged in candidate
     * */
    @PutMapping("{id}")
    public Map<String, Object> updateTaskAssignment(@PathVariable("id") long id,
        @Valid @RequestBody UpdateTaskAssignmentRequestCandidate request)
        throws NoSuchObjectException, UnauthorisedActionException {

        boolean completed;
        boolean abandoned = request.isAbandoned();

        // If the request has been abandoned, we want to override the completed value as false.
        if (abandoned) {
            completed = false;
        } else {
            // If not abandoned, we want to set the completed value to whatever is in the request.
            completed = request.isCompleted();
        }

        TaskAssignmentImpl ta = taskAssignmentService.get(id);

        checkAuthorisation(ta);

        ta = taskAssignmentService.update(ta, completed, abandoned,
            request.getCandidateNotes(), null);

        return TaskDtoHelper.getTaskAssignmentDto().build(ta);
    }

    /**
     * Completes a task assignment's comment only.
     * @param id Task assignment id
     * @return The candidate notes of the task assignment
     * @throws NoSuchObjectException if no task assignment is found with that id
     * @throws UnauthorisedActionException if the task assignment does not belong to logged in candidate
     * */
    @PutMapping("{id}/comment")
    public Map<String, Object> updateTaskComment(@PathVariable("id") long id,
                                                    @Valid @RequestBody UpdateTaskAssignmentCommentRequest request)
            throws NoSuchObjectException, UnauthorisedActionException {

        TaskAssignmentImpl ta = taskAssignmentService.get(id);

        checkAuthorisation(ta);

        boolean completed = ta.getCompletedDate() != null;
        boolean abandoned = ta.getAbandonedDate() != null;

        ta = taskAssignmentService.update(ta, completed, abandoned,
                request.getCandidateNotes(), null);

        return TaskDtoHelper.getTaskAssignmentDto().build(ta);
    }

}
