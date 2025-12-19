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

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.task.TaskAssignment;
import org.tctalent.server.request.task.TaskListRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;

/**
 * Service for managing {@link TaskAssignment}s.
 *
 * @author John Cameron
 */
public interface TaskAssignmentService {

    /**
     * An active TaskAssignment object is added to the candidate's task assignments.
     * <p/>
     * A user may want to assign a task to a candidate. For example if a candidate has to complete a
     * task uniquely for a particular position they’ve accepted, a user might assign a newly created
     * unique task to this candidate.
     * <p/>
     * Note that if the task is composed of subtasks, each subtask is assigned to the candidate.
     *
     * @param user      - User who made assignment
     * @param task      - Task to be associated with the newly created TaskAssignment
     * @param candidate - Candidate associated with the newly created TaskAssignment
     * @param relatedList - If not null, indicates that the assignemtn is related to the given list
     * @param dueDate   - Custom due date (can be null, which case the days to complete will be used
     *                  to set)
     * @return Newly created task assignment associated with candidate and task
     */
    TaskAssignmentImpl assignTaskToCandidate(
        User user, TaskImpl task, Candidate candidate, @Nullable SavedList relatedList,
        @Nullable LocalDate dueDate);

    /**
     * Get the TaskAssignment with the given id.
     *
     * @param taskAssignmentId ID of TaskAssignment to get
     * @return TaskAssigment
     * @throws NoSuchObjectException if there is no TaskAssignment with this id.
     */
    @NonNull
    TaskAssignmentImpl get(long taskAssignmentId) throws NoSuchObjectException;

    /**
     * Update the given upload task assignment.
     *
     * @param taskAssignment   TaskAssignment to update
     * @param abandoned The task assignment is marked as abandoned or not (by setting the abandonedDate).
     * @param notes If not null, sets the candidateNotes associated with the task assignment
     * @param nonDefaultDueDate If not null, sets a non default task assignment dueDate (otherwise
     *                          the due date is set automatically based on the task's
     *                          daysToComplete field.
     * @return Updated Task Assignment
     */
    @NonNull
    TaskAssignmentImpl updateUploadTaskAssignment(@NonNull TaskAssignmentImpl taskAssignment,
        boolean abandoned, @Nullable String notes, @Nullable LocalDate nonDefaultDueDate);

    /**
     * Update the given task assignment.
     *
     * @param taskAssignment   TaskAssignment to update
     * @param completed If null, this is not processed at all. Otherwise the task assignment is
     *                  marked as completed or not - by setting completedDate
     * @param abandoned The task assignment is marked as abandoned (by setting the abandonedDate).
     * @param notes If not null, sets the candidateNotes associated with the task assignment
     * @param nonDefaultDueDate If not null, sets a non default task assignment dueDate (otherwise
     *                          the due date is set automatically based on the task's
     *                          daysToComplete field.
     * @return Updated Task Assignment
     */
    @NonNull
    TaskAssignmentImpl update(@NonNull TaskAssignmentImpl taskAssignment, @Nullable Boolean completed,
        boolean abandoned, @Nullable String notes, @Nullable LocalDate nonDefaultDueDate);

    /**
     * Deactivate the task assignment.
     *
     * @param loggedInUser     - the logged in admin user who deactivated the task assignment
     * @param taskAssignmentId - Task Assignment to be deactivated.
     * @throws NoSuchObjectException If no such task assignment exists with that id
     */
    void deactivateTaskAssignment(User loggedInUser, long taskAssignmentId)
        throws NoSuchObjectException;

    /**
     * Delete the task assignment.
     * <p/>
     * Note that the difference between deleting and deactivating is that a deactivated task
     * assignment can still be displayed to users - where a deleted task assignment is as if
     * it never existed (although it is in fact still kept on the database).
     * <p/>
     * Typically we delete task assignments when they never should have been assigned in the first
     * place.
     *
     * @param loggedInUser     - the logged in admin user who deleted the task assignment
     * @param taskAssignmentId - Task Assignment to be deleted.
     * @return true/false depending on success
     * @throws NoSuchObjectException If no such task assignment exists with that id
     */
    boolean deleteTaskAssignment(User loggedInUser, long taskAssignmentId)
        throws NoSuchObjectException;

    /**
     * Marks the given task assignment as completed.
     * @param ta Task assignment
     */
    void completeTaskAssignment(TaskAssignment ta);

    /**
     * Marks the given upload task assignment as completed if the given file is successfully
     * uploaded according to the UploadTask's upload attributes.
     * @param ta Task assignment
     * @throws IOException if the upload fails
     * @throws ClassCastException if the task assignment is not for an UploadTask
     */
    void completeUploadTaskAssignment(TaskAssignment ta, MultipartFile file)
        throws IOException, ClassCastException;

    /**
     * Return all Task Assignment's that match the given request.
     * <p/>
     * @param request Defines which TaskAssignment's to return (in what saved list, and what task)
     * @return Matching Task Assignment's
     */
    List<TaskAssignmentImpl> listTaskAssignments(TaskListRequest request);

    /**
     * Finds task assignments by task ID, candidate ID, and status.
     *
     * @param taskId The ID of the task.
     * @param candidateId The ID of the candidate.
     * @param status The status of the task assignment.
     * @return A list of matching TaskAssignmentImpl objects.
     */
    List<TaskAssignmentImpl> findByTaskIdAndCandidateIdAndStatus(Long taskId, Long candidateId, Status status);

}
