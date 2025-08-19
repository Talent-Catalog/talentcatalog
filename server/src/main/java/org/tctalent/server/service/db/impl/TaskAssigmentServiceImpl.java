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

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.repository.db.TaskAssignmentRepository;
import org.tctalent.server.request.task.TaskListRequest;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskProcessor;
import org.tctalent.server.service.db.TaskService;
import org.tctalent.server.model.db.*;
import org.tctalent.server.model.db.task.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class TaskAssigmentServiceImpl implements TaskAssignmentService {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskService taskService;
    private final Map<TaskType, TaskProcessor> taskProcessors;

    @Autowired
    public TaskAssigmentServiceImpl(
        TaskAssignmentRepository taskAssignmentRepository,
        TaskService taskService,
        List<TaskProcessor> taskProcessors) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskService = taskService;
        this.taskProcessors = new HashMap<>();
        for (TaskProcessor processor : taskProcessors) {
            this.taskProcessors.put(processor.getTaskType(), processor);
        }
    }

    @Override
    public TaskAssignmentImpl assignTaskToCandidate(
        User user, TaskImpl task, @Nullable Candidate candidate, @Nullable SavedList savedList,
        @Nullable LocalDate dueDate) {
        List<TaskAssignmentImpl> assignments = new ArrayList<>();

        // If savedList is provided, assign the task to all candidates in the list
        if (savedList != null) {
            for (Candidate listCandidate : savedList.getCandidates()) {
                assignments.add(
                    createTaskAssignment(user, task, listCandidate, savedList, dueDate));
            }
            return assignments.isEmpty() ? null : assignments.get(0);
        }

        // If candidate is provided, assign the task to the single candidate
        if (candidate != null) {
            TaskAssignmentImpl assignment = createTaskAssignment(user, task, candidate, savedList,
                dueDate);
            assignments.add(assignment);
            return assignment;
        }

        throw new IllegalArgumentException("Either candidate or savedList must be provided");
    }

    private TaskAssignmentImpl createTaskAssignment(
        User user, TaskImpl task, Candidate candidate, @Nullable SavedList savedList,
        @Nullable LocalDate dueDate) {
        TaskProcessor processor = taskProcessors.get(task.getTaskType());
        if (processor == null) {
            throw new IllegalArgumentException(
                "No processor found for task type: " + task.getTaskType());
        }

        TaskAssignmentImpl taskAssignment = processor.createTaskAssignment();
        taskAssignment.setTask(task);
        taskAssignment.setActivatedBy(user);
        taskAssignment.setActivatedDate(OffsetDateTime.now());
        taskAssignment.setCandidate(candidate);
        taskAssignment.setStatus(Status.active);
        taskAssignment.setRelatedList(savedList);
        if (dueDate == null && task.getDaysToComplete() != null) {
            dueDate = LocalDate.now().plusDays(task.getDaysToComplete());
        }
        taskAssignment.setDueDate(dueDate);
        return taskAssignmentRepository.save(taskAssignment);
    }

    @NonNull
    @Override
    public TaskAssignmentImpl get(long taskAssignmentId) throws NoSuchObjectException {
        return taskAssignmentRepository.findById(taskAssignmentId)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, taskAssignmentId));
    }

    @NonNull
    @Override
    public TaskAssignmentImpl updateUploadTaskAssignment(
        @NonNull TaskAssignmentImpl taskAssignment, boolean abandoned,
        @Nullable String notes, @Nullable LocalDate nonDefaultDueDate) {
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
                if (taskAssignment.getCompletedDate() == null) {
                    taskAssignment.setCompletedDate(OffsetDateTime.now());
                }
                TaskProcessor processor = taskProcessors.get(
                    taskAssignment.getTask().getTaskType());
                processor.handleCompletion(taskAssignment);
            } else {
                taskAssignment.setCompletedDate(null);
            }
        }

        if (abandoned) {
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
    public void deactivateTaskAssignment(User user, long taskAssignmentId)
        throws NoSuchObjectException {
        TaskAssignmentImpl taskAssignment = get(taskAssignmentId);
        taskAssignment.setDeactivatedBy(user);
        taskAssignment.setDeactivatedDate(OffsetDateTime.now());
        taskAssignment.setStatus(Status.inactive);
        taskAssignmentRepository.save(taskAssignment);
    }

    @Override
    public boolean deleteTaskAssignment(User user, long taskAssignmentId)
        throws NoSuchObjectException {
        TaskAssignmentImpl taskAssignment = get(taskAssignmentId);
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

    @Override
    public void completeUploadTaskAssignment(TaskAssignment ta, MultipartFile file)
        throws IOException, ClassCastException {
        TaskProcessor processor = taskProcessors.get(ta.getTask().getTaskType());
        if (processor == null) {
            throw new IllegalArgumentException(
                "No processor found for task type: " + ta.getTask().getTaskType());
        }
        TaskAssignmentImpl taskAssignment = processor.completeTask((TaskAssignmentImpl) ta, new TaskCompletionContext().addFile(file));
        taskAssignmentRepository.save(taskAssignment);
    }

    @Override
    public TaskAssignment completeUploadTask(Long taskAssignmentId, MultipartFile[] files,
        Map<String, String> fieldAnswers)
        throws IOException, NoSuchObjectException {
        TaskAssignmentImpl assignment = get(taskAssignmentId);
        TaskProcessor processor = taskProcessors.get(assignment.getTask().getTaskType());
        if (processor == null) {
            throw new IllegalArgumentException(
                "No processor found for task type: " + assignment.getTask().getTaskType());
        }
        TaskCompletionContext context = new TaskCompletionContext()
            .addFiles(files)
            .addFieldAnswers(fieldAnswers);
        TaskAssignmentImpl taskAssignment = processor.completeTask(assignment, context);
        taskAssignmentRepository.save(taskAssignment);
        return taskAssignment;
    }

    @Override
    public List<TaskAssignmentImpl> listTaskAssignments(TaskListRequest request) {
        return taskAssignmentRepository.findByTaskAndList(
            request.getTaskId(), request.getSavedListId());
    }

    @Override
    public List<TaskAssignmentImpl> findByTaskIdAndCandidateIdAndStatus(
        Long taskId, Long candidateId, Status status) {
        return taskAssignmentRepository.findByTask_IdAndCandidate_IdAndStatus(taskId, candidateId,
            status);
    }
}