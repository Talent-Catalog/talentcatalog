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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.files.UploadType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.QuestionTaskAssignmentImpl;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.UploadTaskAssignmentImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.QuestionTask;
import org.tctalent.server.model.db.task.QuestionTaskAssignment;
import org.tctalent.server.model.db.task.Task;
import org.tctalent.server.model.db.task.TaskAssignment;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.model.db.task.UploadTask;
import org.tctalent.server.repository.db.TaskAssignmentRepository;
import org.tctalent.server.request.task.TaskListRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateAttachmentService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

/**
 * Default implementation of a TaskAssignmentService
 *
 * @author John Cameron
 */
@Service
public class TaskAssigmentServiceImpl implements TaskAssignmentService {
    private final CandidateAttachmentService candidateAttachmentService;
    private final CandidatePropertyService candidatePropertyService;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskService taskService;
    private final AuthService authService;

    public TaskAssigmentServiceImpl(
        CandidateAttachmentService candidateAttachmentService,
        CandidatePropertyService candidatePropertyService,
        TaskAssignmentRepository taskAssignmentRepository,
        AuthService authService,
        TaskService taskService) {
        this.candidateAttachmentService = candidateAttachmentService;
        this.candidatePropertyService = candidatePropertyService;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.authService = authService;
        this.taskService = taskService;
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
                if ("claimCouponButton".equals(taskAssignment.getTask().getName())) {
                    // Fetch the "duolingoTest" task
                    TaskImpl duolingoTestTask = taskService.getByName("duolingoTest");
                    int daysToComplete = duolingoTestTask.getDaysToComplete() != null ? duolingoTestTask.getDaysToComplete() : 0;

                    // Assign the duolingoTest task to the candidate
                    assignTaskToCandidate(getLoggedInUser(), duolingoTestTask, taskAssignment.getCandidate(), null, LocalDate.now().plusDays(daysToComplete));
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

    private String computeUploadFileName(Candidate candidate, String taskName, String baseFileName) {
        return candidate.getCandidateNumber() + "_" + taskName + "-" + baseFileName;
    }

    @Override
    public void completeUploadTaskAssignment(TaskAssignment ta, MultipartFile file)
        throws IOException, ClassCastException {
        UploadTask uploadTask = (UploadTask) ta.getTask();

        Candidate candidate = ta.getCandidate();
        UploadType uploadType = uploadTask.getUploadType();
        String taskName = uploadTask.getName();
        String uploadedName = computeUploadFileName(candidate, taskName, file.getOriginalFilename());
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

    @Override
    public List<TaskAssignmentImpl> findByTaskIdAndCandidateIdAndStatus(
        Long taskId, Long candidateId, Status status) {
        return taskAssignmentRepository.findByTask_IdAndCandidate_IdAndStatus(taskId, candidateId, status);
    }

    private User getLoggedInUser() {
        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return user;
    }

    @Override
    public void populateTransientTaskAssignmentFields(List<TaskAssignmentImpl> taskAssignments) {
        for (TaskAssignment taskAssignment : taskAssignments) {
            taskService.populateTransientFields(taskAssignment.getTask());

            //If task is completed, see if there is any transient data to be populated - eg the
            //answer on a question task
            if (taskAssignment.getCompletedDate() != null) {
                if (taskAssignment instanceof QuestionTaskAssignmentImpl) {
                    QuestionTaskAssignment qta = (QuestionTaskAssignmentImpl) taskAssignment;
                    String answer = fetchCandidateTaskAnswer(qta);
                    qta.setAnswer(answer);
                }
            }
        }
    }

    /**
     * Retrieves the answer, if any, of the give question task assignment.
     * @param questionTaskAssignment Question task assignment
     * @return Candidate's answer to the question
     * @throws NoSuchObjectException if the answer could not be retrieved because the answer has
     * been specified as being located in a non existent candidate field or property.
     */
    @Nullable
    private String fetchCandidateTaskAnswer(QuestionTaskAssignment questionTaskAssignment)
        throws NoSuchObjectException {
        String answer;
        Task task = questionTaskAssignment.getTask();
        if (task instanceof QuestionTask) {
            String answerField = ((QuestionTask) task).getCandidateAnswerField();
            Candidate candidate = questionTaskAssignment.getCandidate();
            if (answerField == null) {
                //Get answer from candidate property
                String propertyName = task.getName();
                final CandidateProperty property =
                    candidatePropertyService.findProperty(candidate, propertyName);
                answer = property != null ? property.getValue() : null;
            } else {
                //Get answer from candidate field

                //TODO JC This is a bit of a hack - we need a better way of processing things like candidateExams
                String candidateExamsFieldName = "candidateExams";
                if (answerField.startsWith(candidateExamsFieldName + ".")) {
                    //Special code for candidate exams
                    //Extract the exam name from the answerField
                    String examName = answerField.substring(candidateExamsFieldName.length() + 1);
                    Exam examType = Exam.valueOf(examName);

                    //See if we already have an entry for this exam
                    Optional<CandidateExam> examO = Optional.empty();
                    final List<CandidateExam> candidateExams = candidate.getCandidateExams();
                    if (candidateExams != null) {
                        examO = candidateExams.stream().filter(
                            e -> e.getExam().equals(examType)).findFirst();
                    }

                    //Fetch the answer
                    CandidateExam exam;
                    if (examO.isPresent()) {
                        exam = examO.get();
                        answer = exam.getScore();
                    } else {
                        answer = null;
                    }
                } else {
                    try {
                        Object value = PropertyUtils.getProperty(candidate, answerField);
                        answer = value != null ? value.toString() : null;
                    } catch (IllegalAccessException e) {
                        throw new InvalidRequestException("Unable to access '" + answerField
                            + "' field of candidate");
                    } catch (InvocationTargetException e) {
                        throw new InvalidRequestException("Error while accessing '" + answerField
                            + "' field of candidate");
                    } catch (NoSuchMethodException e) {
                        throw new NoSuchObjectException(
                            "Answer not found to " + task.getDisplayName()
                                + ". No such candidate field: " + answerField);
                    }
                }
            }
        } else {
            throw new InvalidRequestException("Task is not a QuestionTask: " + task.getName());
        }

        return answer;
    }

}
