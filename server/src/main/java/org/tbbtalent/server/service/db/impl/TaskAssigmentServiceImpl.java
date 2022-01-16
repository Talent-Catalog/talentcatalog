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

import java.util.List;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.task.Task;
import org.tbbtalent.server.model.db.task.TaskAssignment;
import org.tbbtalent.server.service.db.TaskAssignmentService;

// TODO: Note for Caroline:  that none of the methods are completed.
// They are the default implementations that I have configured Intellij to provide when I do a
// Command I - or Ctrl N (Code | Generate menu) and ask Intellij to provide method implementations.
//
// Nevertheless, I can write and run all the tests in TaskAssignmentServiceTest. They all fail
// (because this class is not fully coded) - but that is TDD.
// Note also that the tests run immediately and very quickly - because they don't require the
// Spring framework.
//
// Once I have written the code to make all the tests pass, theoretically this class is complete.
// I then just have to add the Spring notation and attach a repository to it so that stuff
// is saved in our data base.
//
// Note that the tests should still be able to run quickly by mocking up the Spring dependencies,
// but let's not worry about that for now. Let's get the basic logic working - without worrying
// about Spring and the database initially.

/**
 * Default implementation of a TaskAssignmentService
 *
 * @author John Cameron
 */
public class TaskAssigmentServiceImpl implements TaskAssignmentService {

    @Override
    public TaskAssignment assignTaskToCandidate(Task task, Candidate candidate) {
        //TODO JC Implement assignTaskToCandidate
        throw new UnsupportedOperationException("assignTaskToCandidate not implemented");
    }

    @Override
    public void assignTaskToList(Task task, SavedList list) {
        //TODO JC Implement assignTaskToList
        throw new UnsupportedOperationException("assignTaskToList not implemented");
    }

    @Override
    public List<TaskAssignment> getCandidateTaskAssignments(Candidate candidate, Status status) {
        //TODO JC Implement getCandidateTaskAssignments
        throw new UnsupportedOperationException("getCandidateTaskAssignments not implemented");
    }
}
