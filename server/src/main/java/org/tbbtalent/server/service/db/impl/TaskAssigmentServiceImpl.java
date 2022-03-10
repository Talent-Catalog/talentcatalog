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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.TaskAssignmentImpl;
import org.tbbtalent.server.model.db.TaskImpl;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.db.task.QuestionTask;
import org.tbbtalent.server.model.db.task.Task;
import org.tbbtalent.server.model.db.task.TaskAssignment;
import org.tbbtalent.server.model.db.task.UploadTask;
import org.tbbtalent.server.model.db.task.UploadType;
import org.tbbtalent.server.repository.db.TaskAssignmentRepository;
import org.tbbtalent.server.service.db.CandidateAttachmentService;
import org.tbbtalent.server.service.db.CandidatePropertyService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.TaskAssignmentService;

/**
 * Default implementation of a TaskAssignmentService
 *
 * @author John Cameron
 */
@Service
public class TaskAssigmentServiceImpl implements TaskAssignmentService {
    private final CandidateAttachmentService candidateAttachmentService;
    private final CandidatePropertyService candidatePropertyService;
    private final CandidateService candidateService;
    private final TaskAssignmentRepository taskAssignmentRepository;

    public TaskAssigmentServiceImpl(
        CandidateAttachmentService candidateAttachmentService,
        CandidatePropertyService candidatePropertyService,
        CandidateService candidateService,
        TaskAssignmentRepository taskAssignmentRepository) {
        this.candidateAttachmentService = candidateAttachmentService;
        this.candidatePropertyService = candidatePropertyService;
        this.candidateService = candidateService;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    @Override
    public TaskAssignmentImpl assignTaskToCandidate(
        User user, TaskImpl task, Candidate candidate, @Nullable SavedList savedList,
        @Nullable LocalDate dueDate) {
        TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();
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
        return taskAssignmentRepository.findById(taskAssignmentId)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, taskAssignmentId));
    }

    @NonNull
    @Override
    public TaskAssignmentImpl updateQuestionTaskAssignment(
        @NonNull TaskAssignmentImpl taskAssignment, @NonNull String answer, boolean completed,
        boolean abandoned, @Nullable String notes, @Nullable LocalDate nonDefaultDueDate) {
        if (!abandoned) {
             //Update answer
            storeCandidateAnswer(taskAssignment, answer);
        }
        return update(taskAssignment, completed, abandoned, notes, nonDefaultDueDate);
    }

    /**
     * Stores the given answer supplied for the given question task assignment.
     * @param questionTaskAssignment Question task assignment
     * @param answer Answer to question
     * @throws InvalidRequestException If the task associated with the given task assignment is not
     * a QuestionTask
     */
    private void storeCandidateAnswer(TaskAssignmentImpl questionTaskAssignment, String answer)
        throws InvalidRequestException {
        Task task = questionTaskAssignment.getTask();
        if (task instanceof QuestionTask) {
            String answerField = ((QuestionTask) task).getCandidateAnswerField();
            Candidate candidate = questionTaskAssignment.getCandidate();
            if (answerField == null) {
                //Store answer in a candidate property
                String propertyName = task.getName();
                candidatePropertyService.createOrUpdateProperty(
                    candidate, propertyName, answer, questionTaskAssignment);
            } else {
                //Store answer in the candidate field

                try {
                    PropertyUtils.setProperty(candidate, answerField, answer);
                } catch (IllegalAccessException e) {
                    throw new InvalidRequestException("Unable to access '" + answerField
                        + "' field of candidate");
                } catch (InvocationTargetException e) {
                    throw new InvalidRequestException("Error while accessing '" + answerField
                        + "' field of candidate");
                } catch (NoSuchMethodException e) {
                    throw new InvalidRequestException("Candidate field does not exist: '" + answerField
                        + "'");
                }

                candidateService.save(candidate, true);
            }
        } else {
            throw new InvalidRequestException("Task is not a QuestionTask: " + task.getName());
        }
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
        return taskAssignmentRepository.save(taskAssignment);
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

}
