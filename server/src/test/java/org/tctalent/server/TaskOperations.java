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

package org.tctalent.server;

// TODO Note for Caroline: I don't this is ever going to be a real class. Probably delete.

public class TaskOperations {

    // TASKS (CRUD) //

    /**
     * Create a standard task, with name, description, time frame, admin only boolean.
     * Returns created/saved task object.
     */
    void createStandardTask(){};

    /**
     * Create a question task, with a question and response type.
     * Returns created/saved task object.
     */
    void createQuestionTask(){};

    /**
     * Create an upload task, with kind of file, help link.
     * Returns created/saved task object.
     */
    void createUploadTask(){};

    /**
     * Create a task list, with name, description, time frame, task list boolean true.
     * Returns created/saved task object.
     */
    void createTaskList(){};

    /**
     * Create a task and insert into a task list, with name, description, time frame, parent list/subtask relationship.
     * Returns created/saved task object.
     */
    void createSubTask(){};

    /**
     * Get the task object, including name, description, time frame etc.
     * Returns the task object with fields.
     */
    void getTask(){};

    /**
     * Update task name, description, time frame.
     * If I update a tasks time frame, does it update the due date for all the task assigments?
     * Returns the updated task object.
     */
    void updateTask(){};

    /**
     * Update task list by adding an existing task to list.
     * Returns the updated task object.
     */
    void updateTaskAddSubTask(){};

    /**
     * Delete a task.
     * What happens to task assignments when a task is deleted? Change the status of them to inactive?
     * Returns null.
     */
    void deleteTask(){}


    // TASK ASSIGNMENTS (CRUD) //

    /**
     * TASK -> CANDIDATE
     * Assigning a task to a candidate.
     * Create a task assignment, with a task object, candidate object, due date set, activated by fields.
     * Returns the task assignment (1).
     */
    void createTaskAssignmentToCandidate(){};

    /**
     * TASK LIST -> CANDIDATE
     * Assigning a task list to a candidate.
     * Create multiple task assignments, with subtask objects, candidate object, due dates set, activated by fields.
     * Returns the task assignments (multiple - as many as in the task list).
     */
    void createTaskListAssignmentToCandidate(){};

    /**
     * TASK -> LIST CANDIDATES
     * Assign a task to a list of candidates.
     * Create multiple task assignments, with a task object, candidate objects, due dates set, activated by fields.
     * Returns the task assignments (multiple - as many as in the candidate list).
     */
    void createTaskAssignmentToList(){};

    /**
     * TASK LIST -> LIST CANDIDATES
     * Assign a task list to a list of candidates.
     * Create multiple task assignments, with task objects, candidate objects, due dates, activiated by fields.
     * Returns the task assignments (multiple - # tasks x # candidates)
     */
    void createTaskListAssignmentToList(){};

    /**
     * Get the task assignment including all fields.
     * Return task assignment.
     */
    void getTaskAssignment(){};

    /**
     * Get the ACTIVE task assignments belonging to a candidate.
     * Need the candidate number or candidate object to fetch the task assignments.
     * Return active task assignment/s.
     */
    void getCandidateTaskAssignments(){};

    /**
     * Get the ACTIVE task assignments belonging to a list.
     * Need the list number or list object to fetch the task assignments.
     * Return active task assignment/s.
     */
    void getListTaskAssignments(){};

    /**
     * Update a candidate's task assignment.
     * Admin: change due date, change description.
     * Candidate: updates a task assignment, e.g. adds a note, uploads the file, answers the question.
     * Return updated task assignment.
     */
    void updateTaskAssignment(){};

    /**
     * Delete a candidate's task assignment, set to deactivated.
     * Return null.
     */
    void deleteTaskAssignment(){};

    // CANDIDATES //

    /**
     * Candidate to complete a task assignment. Set the completed date, completed by.
     * Do we set task to inactive now? Also include when admin completes task.
     * Return completed task assignment;
     */
    void completeTaskAssignment(){};

    /**
     * Adding a candidate to a list with tasks associated. Same as if assigning a candidate to those tasks.
     * If there are double ups of the task, which task assignment do we keep?
     * Return candidate w/ new tasks assigned.
     */
    void addCandidateToListWithTasks(){};

    /**
     * Removing a candidate from a list with tasks associated. Same as if removing a candidate from those tasks.
     * Do we set these removed tasks as inactive? Or deleted?
     * What are completed tasks set as? Inactive? If so inactive will include completed and deleted...
     * Return candidate w/ tasks removed.
     */
    void deleteCandidateFromListWithTasks(){};

}
