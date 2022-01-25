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

package org.tbbtalent.server.service.db;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.model.db.task.Task;
import org.tbbtalent.server.model.db.task.TaskAssignment;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

// TODO: Notes for Caroline: The methods and documentation are taken from your "TDD Operations Tasks"
// design document - with a bit more detail added.
// This documentation is also used to define the testing that needs to be carries out in
// TaskAssignmentServiceTest

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
     * @param dueDate - Custom due date (can be null, which case the days to complete will be used to set)
     * @return Newly created task assignment associated with candidate and task
     */
    TaskAssignmentImpl assignTaskToCandidate(User user, TaskImpl task, Candidate candidate, LocalDate dueDate);

    /**
     * A user may want to assign a task to a list of candidates. For example if there’s a list of
     * candidates shortlisted for a job opportunity, they might all be required to complete some
     * pre-offer tasks via a task list.
     * <p/>
     * A new active TaskAssignment object for each candidate in the list, associated with the given
     * task.
     * <p/>
     * Note that if the task is composed of subtasks, each subtask is assigned to the candidate.
     * <p/>
     * This is effectively multiple applications of {@link #assignTaskToCandidate} for each
     * candidate in the list. See above doc for that method.
     *
     * @param task - Task to be associated with the newly created TaskAssignment
     * @param list - List of candidates to whom the task should be assigned
     */
    void assignTaskToList(Task task, SavedList list);

    /**
     * Get the TaskAssignment with the given id.
     * @param taskAssignmentId ID of TaskAssignment to get
     * @return TaskAssigment
     * @throws NoSuchObjectException if there is no TaskAssignment with this id.
     */
    @NonNull
    TaskAssignment get(long taskAssignmentId) throws NoSuchObjectException;

    /**
     * Fetch task assignments for a given candidate by Status (eg active or inactive)
     *
     * @param candidate - Candidate whose task assignments we want
     * @param status    - Status (active or inactive) of task assignments. If null all task
     *                  assignments are fetched.
     * @return Task assignments associated with candidate
     */
    List<TaskAssignment> getCandidateTaskAssignments(Candidate candidate, @Nullable Status status);

    // TODO: 22/1/22 Doc
    void completeTaskAssignment(TaskAssignment ta);

    // TODO: 22/1/22 Doc
    void completeUploadTaskAssignment(TaskAssignment ta, MultipartFile file) throws IOException;
}
