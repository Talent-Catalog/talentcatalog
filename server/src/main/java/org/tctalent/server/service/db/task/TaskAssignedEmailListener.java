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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.TaskAssignedEvent;
import org.tctalent.server.service.db.email.EmailHelper;

/**
 * Listens for task assignment events and sends notification emails to candidates.
 *
 * @author sadatmalik
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskAssignedEmailListener {

    private final EmailHelper emailHelper;

    @Async
    @TransactionalEventListener
    public void onAssigned(TaskAssignedEvent event) {
        TaskAssignmentImpl taskAssignment = event.taskAssignment();
        String action = "TaskAssignedEmailListener:onAssigned: " + taskAssignment.getTask().getName();

        Candidate candidate = taskAssignment.getCandidate();
        if (candidate == null) {
            LogBuilder.builder(log)
                .action(action)
                .message("Task assignment " + taskAssignment.getId() + " has no candidate")
                .logWarn();
            return;
        }

        User user = candidate.getUser();
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            LogBuilder.builder(log)
                .action(action)
                .message("Candidate " + candidate.getId() + " has no associated user email")
                .logWarn();
            return;
        }

        try {
            emailHelper.sendTaskAssignedEmail(user, taskAssignment.getTask().getDisplayName());
        } catch (Exception ex) {
            LogBuilder.builder(log)
                .action(action)
                .message("Failed sending task assignment email for task assignment " + taskAssignment.getId()
                    + " to candidate " + candidate.getId())
                .logWarn(ex);
        }
    }
}
