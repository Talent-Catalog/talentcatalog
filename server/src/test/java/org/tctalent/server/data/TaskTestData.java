/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;

public class TaskTestData {

    public static TaskImpl getTask() {
        TaskImpl task = new TaskImpl();
        task.setId(148L);
        task.setName("a test task");
        task.setDaysToComplete(7);
        task.setDescription("a test task description");
        task.setDisplayName("task display name");
        task.setOptional(false);
        task.setDocLink("http://help.link");
        return task;
    }

    public static List<TaskImpl> getListOfTasks() {
        TaskImpl task1 = getTask();
        TaskImpl task2 = getTask();
        task2.setName("test task 2");
        TaskImpl task3 = getTask();
        task3.setName("test task 3");
        return List.of(task1, task2, task3);
    }

    public static TaskAssignmentImpl getTaskAssignment() {
        TaskAssignmentImpl ta = new TaskAssignmentImpl();
        ta.setId(99L);
        ta.setTask(getTask());
        ta.setStatus(Status.active);
        ta.setDueDate(LocalDate.of(2025, 1, 1));
        return ta;
    }

    public static TaskAssignmentImpl getCompletedTaskAssignment() {
        TaskAssignmentImpl ta = new TaskAssignmentImpl();
        ta.setId(99L);
        ta.setTask(getTask());
        ta.setStatus(Status.active);
        ta.setDueDate(LocalDate.of(2025, 1, 1));
        ta.setCompletedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        ta.setAbandonedDate(OffsetDateTime.parse("2022-10-30T12:30:00+02:00"));
        ta.setCandidateNotes("These are candidate notes.");
        return ta;
    }

    public static List<TaskAssignmentImpl> getTaskAssignments() {
        TaskAssignmentImpl taskAssignment = getCompletedTaskAssignment();
        return List.of(
            taskAssignment
        );
    }

}
