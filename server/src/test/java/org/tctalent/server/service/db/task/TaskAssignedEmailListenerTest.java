/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.service.db.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.TaskAssignedEvent;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class TaskAssignedEmailListenerTest {

    @Mock
    private EmailHelper emailHelper;

    @InjectMocks
    private TaskAssignedEmailListener listener;

    private TaskAssignmentImpl taskAssignment;
    private Candidate candidate;
    private User user;

    @BeforeEach
    void setUp() {
        TaskImpl task = new TaskImpl();
        task.setId(100L);
        task.setName("testTask");
        task.setDisplayName("Complete your profile");

        user = new User();
        user.setId(200L);
        user.setEmail("candidate@example.com");
        user.setFirstName("Test");
        user.setLastName("Candidate");

        candidate = new Candidate();
        candidate.setId(300L);
        candidate.setUser(user);

        taskAssignment = new TaskAssignmentImpl();
        taskAssignment.setId(400L);
        taskAssignment.setTask(task);
        taskAssignment.setCandidate(candidate);
    }

    @Test
    @DisplayName("onAssigned sends email when candidate user and email exist")
    void onAssignedSendsEmailWhenCandidateDetailsExist() {
        listener.onAssigned(new TaskAssignedEvent(taskAssignment));

        verify(emailHelper).sendTaskAssignedEmail(user, "Complete your profile");
    }

    @Test
    @DisplayName("onAssigned skips email when candidate is missing")
    void onAssignedSkipsEmailWhenCandidateMissing() {
        taskAssignment.setCandidate(null);

        listener.onAssigned(new TaskAssignedEvent(taskAssignment));

        verify(emailHelper, never()).sendTaskAssignedEmail(any(User.class), anyString());
    }

    @Test
    @DisplayName("onAssigned skips email when candidate user is missing")
    void onAssignedSkipsEmailWhenUserMissing() {
        candidate.setUser(null);

        listener.onAssigned(new TaskAssignedEvent(taskAssignment));

        verify(emailHelper, never()).sendTaskAssignedEmail(any(User.class), anyString());
    }

    @Test
    @DisplayName("onAssigned skips email when candidate email is missing")
    void onAssignedSkipsEmailWhenEmailMissing() {
        user.setEmail(null);

        listener.onAssigned(new TaskAssignedEvent(taskAssignment));

        verify(emailHelper, never()).sendTaskAssignedEmail(any(User.class), anyString());
    }

    @Test
    @DisplayName("onAssigned catches email send failures")
    void onAssignedHandlesEmailSendFailure() {
        doThrow(new RuntimeException("smtp down"))
            .when(emailHelper).sendTaskAssignedEmail(any(User.class), anyString());

        assertDoesNotThrow(() -> listener.onAssigned(new TaskAssignedEvent(taskAssignment)));

        verify(emailHelper).sendTaskAssignedEmail(user, "Complete your profile");
    }
}
