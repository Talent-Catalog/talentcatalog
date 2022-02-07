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

package org.tbbtalent.server.service.db.impl;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.model.db.task.Task;
import org.tbbtalent.server.model.db.task.TaskAssignment;
import org.tbbtalent.server.model.db.task.UploadTask;
import org.tbbtalent.server.model.db.task.UploadType;
import org.tbbtalent.server.repository.db.TaskAssignmentRepository;
import org.tbbtalent.server.request.task.UpdateTaskAssignmentRequest;
import org.tbbtalent.server.service.db.CandidateAttachmentService;
import org.tbbtalent.server.service.db.TaskAssignmentService;
import org.tbbtalent.server.service.db.TaskService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of a TaskAssignmentService
 *
 * @author John Cameron
 */
@Service
public class TaskAssigmentServiceImpl implements TaskAssignmentService {
    private final CandidateAttachmentService candidateAttachmentService;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskService taskService;

    public TaskAssigmentServiceImpl(
        CandidateAttachmentService candidateAttachmentService,
        TaskAssignmentRepository taskAssignmentRepository,
        TaskService taskService) {
        this.candidateAttachmentService = candidateAttachmentService;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskService = taskService;
    }

    @Override
    public TaskAssignmentImpl assignTaskToCandidate(
        User user, TaskImpl task, Candidate candidate, @Nullable LocalDate dueDate) {
        TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();
        taskAssignment.setTask(task);
        taskAssignment.setActivatedBy(user);
        taskAssignment.setActivatedDate(OffsetDateTime.now());
        taskAssignment.setCandidate(candidate);
        taskAssignment.setStatus(Status.active);
        // If no due date given in assignment, set due date to the days to complete from today.
        if (dueDate == null && task.getDaysToComplete() != null) {
            dueDate = LocalDate.now().plusDays(task.getDaysToComplete());
        }
        taskAssignment.setDueDate(dueDate);
        return taskAssignmentRepository.save(taskAssignment);
    }

    @Override
    public void assignTaskToList(
        User user, TaskImpl task, SavedList list, @Nullable LocalDate dueDate) {
        Set<Candidate> candidates = list.getCandidates();
        for (Candidate candidate : candidates) {
            assignTaskToCandidate(user, task, candidate, dueDate);
        }
    }

    @NonNull
    @Override
    public TaskAssignmentImpl get(long taskAssignmentId) throws NoSuchObjectException {
        return taskAssignmentRepository.findById(taskAssignmentId)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, taskAssignmentId));
    }

    @NonNull
    @Override
    public TaskAssignmentImpl update(long taskAssignmentId, UpdateTaskAssignmentRequest request) throws NoSuchObjectException {
        TaskAssignmentImpl taskAssignment = taskAssignmentRepository.findById(taskAssignmentId)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, taskAssignmentId));

        // A way to establish if coming from candidate or admin portal as the requests may be different?

        if (request.getDueDate() != null) {
            taskAssignment.setDueDate(request.getDueDate());
        }
        if (request.getCandidateNotes() != null) {
            taskAssignment.setCandidateNotes(request.getCandidateNotes());
        }

        if (request.isCompleted()) {
            // Only set the completed date if it's a completed task and a date hasn't already been set.
            if (taskAssignment.getCompletedDate() == null) {
                taskAssignment.setCompletedDate(OffsetDateTime.now());
            }
        } else {
            taskAssignment.setCompletedDate(null);
        }

        if (request.isAbandoned()) {
            // If the task is abandoned and the TA doesn't have an abandoned date, set to now.
            // Otherwise keep the existing abandoned date.
            if (taskAssignment.getAbandonedDate() == null) {
                taskAssignment.setAbandonedDate(OffsetDateTime.now());
            }
        } else {
            taskAssignment.setAbandonedDate(null);
        }
        return taskAssignmentRepository.save(taskAssignment);
    }

    @NonNull
    @Override
    public boolean removeTaskAssignment(User user, long taskAssignmentId) throws NoSuchObjectException {
        TaskAssignmentImpl taskAssignment = taskAssignmentRepository.findById(taskAssignmentId)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, taskAssignmentId));

        taskAssignment.setDeactivatedBy(user);
        taskAssignment.setDeactivatedDate(OffsetDateTime.now());
        taskAssignment.setStatus(Status.deleted);
        taskAssignmentRepository.save(taskAssignment);
        return true;
    }

    @Override
    public List<TaskAssignment> getCandidateTaskAssignments(Candidate candidate, Status status) {
        //TODO JC Implement getCandidateTaskAssignments
        throw new UnsupportedOperationException("getCandidateTaskAssignments not implemented");
    }

    @Override
    public void completeTaskAssignment(TaskAssignment ta) {
        ta.setCompletedDate(OffsetDateTime.now());
        taskAssignmentRepository.save((TaskAssignmentImpl) ta);
    }

    private String computeUploadFileName
        (Candidate candidate, UploadType uploadType, String baseFileName) {
        return candidate.getCandidateNumber() + "-" + uploadType + "-" + baseFileName;
    }

    @Override
    public void completeUploadTaskAssignment(TaskAssignment ta, MultipartFile file)
        throws IOException, ClassCastException {
        UploadTask uploadTask = (UploadTask) ta.getTask();

        Candidate candidate = ta.getCandidate();
        UploadType uploadType = uploadTask.getUploadType();
        String uploadedName = computeUploadFileName(candidate, uploadType, file.getOriginalFilename());
        String subFolderName = uploadTask.getUploadSubfolderName();
        candidateAttachmentService.uploadAttachment(candidate, uploadedName, subFolderName, file, uploadType);

        completeTaskAssignment(ta);
    }

}
