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

package org.tctalent.server.service.db.impl;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.*;
import org.tctalent.server.repository.db.TaskAssignmentRepository;
import org.tctalent.server.request.task.TaskListRequest;
import org.tctalent.server.service.db.CandidateAttachmentService;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.tctalent.server.model.db.task.Task;
import org.tctalent.server.model.db.task.TaskAssignment;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.model.db.task.UploadTask;
import org.tctalent.server.model.db.task.UploadType;

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
    private final DuolingoCouponService couponService;

    public TaskAssigmentServiceImpl(
            CandidateAttachmentService candidateAttachmentService,
            TaskAssignmentRepository taskAssignmentRepository,
            TaskService taskService, DuolingoCouponService couponService) {
        this.candidateAttachmentService = candidateAttachmentService;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskService = taskService;
        this.couponService = couponService;
    }

    @Override
    public TaskAssignmentImpl assignTaskToCandidate(
        User user, TaskImpl task, Candidate candidate, @Nullable SavedList savedList,
        @Nullable LocalDate dueDate) {

        TaskAssignmentImpl taskAssignment;

        TaskType taskType = task.getTaskType();
        switch (taskType) {
            case Question:
                taskAssignment = new QuestionTaskAssignmentImpl();
                break;

            case Upload:
                taskAssignment = new UploadTaskAssignmentImpl();
                break;

            default:
                taskAssignment = new TaskAssignmentImpl();
                if (task.getName().equals("duolingoTest")) {
                    couponService.assignCouponToCandidate(candidate.getId());
                }
                break;
        }

        taskAssignment.setTask(task);
        taskAssignment.setActivatedBy(user);
        taskAssignment.setActivatedDate(OffsetDateTime.now());
        taskAssignment.setCandidate(candidate);
        taskAssignment.setStatus(Status.active);
        taskAssignment.setRelatedList(savedList);
        // If no due date given in assignment, set due date to the days to complete from today.
        if (dueDate == null && task.getDaysToComplete() != null) {
            dueDate = LocalDate.now().plusDays(task.getDaysToComplete());
        }
        taskAssignment.setDueDate(dueDate);
        return taskAssignmentRepository.save(taskAssignment);
    }

    @NonNull
    @Override
    public TaskAssignmentImpl get(long taskAssignmentId) throws NoSuchObjectException {
        final TaskAssignmentImpl taskAssignment = taskAssignmentRepository.findById(
                taskAssignmentId)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, taskAssignmentId));
        return taskAssignment;
    }

    @NonNull
    @Override
    public TaskAssignmentImpl updateUploadTaskAssignment(@NonNull TaskAssignmentImpl taskAssignment,
        boolean abandoned, @Nullable String notes, @Nullable LocalDate nonDefaultDueDate) {
        return update(taskAssignment, null, abandoned, notes, nonDefaultDueDate);
    }

    @NonNull
    @Override
    public TaskAssignmentImpl update(
        @NonNull TaskAssignmentImpl taskAssignment, @Nullable Boolean completed,
        boolean abandoned, @Nullable String notes, @Nullable LocalDate nonDefaultDueDate) {

        if (nonDefaultDueDate != null) {
            taskAssignment.setDueDate(nonDefaultDueDate);
        }
        if (notes != null) {
            taskAssignment.setCandidateNotes(notes);
        }

        if (completed != null) {
            if (completed) {
                // Only set the completed date if it's a completed task and a date hasn't already been set.
                if (taskAssignment.getCompletedDate() == null) {
                    taskAssignment.setCompletedDate(OffsetDateTime.now());
                }
            } else {
                taskAssignment.setCompletedDate(null);
            }
        }

        if (abandoned) {
            // If the task is abandoned and the TA doesn't have an abandoned date, set to now.
            // Otherwise keep the existing abandoned date.
            if (taskAssignment.getAbandonedDate() == null) {
                taskAssignment.setAbandonedDate(OffsetDateTime.now());
            }
        } else {
            taskAssignment.setAbandonedDate(null);
        }


        taskAssignment = taskAssignmentRepository.save(taskAssignment);

        taskService.populateTransientFields(taskAssignment.getTask());

        return taskAssignment;
    }

    @Override
    public void deactivateTaskAssignment(User user, long taskAssignmentId) throws NoSuchObjectException {
        TaskAssignmentImpl taskAssignment = taskAssignmentRepository.findById(taskAssignmentId)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, taskAssignmentId));

        taskAssignment.setDeactivatedBy(user);
        taskAssignment.setDeactivatedDate(OffsetDateTime.now());
        taskAssignment.setStatus(Status.inactive);
        taskAssignmentRepository.save(taskAssignment);
    }

    @Override
    public boolean deleteTaskAssignment(User user, long taskAssignmentId) throws NoSuchObjectException {
        TaskAssignmentImpl taskAssignment = taskAssignmentRepository.findById(taskAssignmentId)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, taskAssignmentId));

        //Note that we use the deactivatedBy and Date fields to record deletions.
        taskAssignment.setDeactivatedBy(user);
        taskAssignment.setDeactivatedDate(OffsetDateTime.now());
        taskAssignment.setStatus(Status.deleted);
        taskAssignmentRepository.save(taskAssignment);
        return true;
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

    @Override
    public List<TaskAssignmentImpl> listTaskAssignments(TaskListRequest request) {
        List<TaskAssignmentImpl> taskAssignments = this.taskAssignmentRepository.findByTaskAndList(
                request.getTaskId(), request.getSavedListId());
        return taskAssignments;
    }

}
