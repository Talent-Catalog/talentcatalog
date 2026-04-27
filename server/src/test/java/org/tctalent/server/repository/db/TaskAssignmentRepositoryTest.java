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

package org.tctalent.server.repository.db;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.UserService;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
class TaskAssignmentRepositoryTest {

    private Candidate assignedCandidate;
    private User activatingUser;
    private TaskImpl task;
    private TaskAssignmentImpl taskAssignment;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        activatingUser = userService.getSystemAdminUser();
        assignedCandidate = candidateService.getTestCandidate();

        task = new TaskImpl();

        task.setName("Sample Simple Task");
        task.setCreatedBy(activatingUser);
        task.setCreatedDate(OffsetDateTime.now());

        taskRepository.save(task);
    }

    @AfterEach
    void tearDown() {
        taskAssignmentRepository.delete(taskAssignment);
        taskRepository.delete(task);
    }

    @Test
    void create() {
        taskAssignment = new TaskAssignmentImpl();
        taskAssignment.setTask(task);
        taskAssignment.setActivatedBy(activatingUser);
        taskAssignment.setActivatedDate(OffsetDateTime.now());
        taskAssignment.setCandidate(assignedCandidate);
        taskAssignment.setStatus(Status.active);
        taskAssignmentRepository.save(taskAssignment);
    }
}
