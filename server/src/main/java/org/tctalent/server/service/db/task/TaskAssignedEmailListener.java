/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.task;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tctalent.server.configuration.properties.TaskAssignmentEmailProperties;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.TaskAssignedEvent;
import org.tctalent.server.service.db.email.EmailHelper;

/**
 * Listens for task assignment events and sends notification emails to candidates.
 *
 * <p>Successful notifications start a configurable, in-memory cooldown for the candidate so that
 * bulk task assignments do not produce a burst of emails. Failed sends release the cooldown so a
 * later assignment can retry.
 *
 * @author sadatmalik
 */
@Component
@Slf4j
public class TaskAssignedEmailListener {

  private final EmailHelper emailHelper;
  private final Cache<Long, Object> notifiedCandidates;

  @Autowired
  public TaskAssignedEmailListener(
      EmailHelper emailHelper, TaskAssignmentEmailProperties properties) {
    this(emailHelper, properties, Ticker.systemTicker());
  }

  TaskAssignedEmailListener(
      EmailHelper emailHelper, TaskAssignmentEmailProperties properties, Ticker ticker) {
    Duration cooldown = properties.getCooldown();
    if (cooldown.isZero() || cooldown.isNegative()) {
      throw new IllegalArgumentException("Task assignment email cooldown must be positive");
    }

    this.emailHelper = emailHelper;
    this.notifiedCandidates =
        Caffeine.newBuilder().expireAfterWrite(cooldown).ticker(ticker).build();
  }

  @Async
  @TransactionalEventListener
  public void onAssigned(TaskAssignedEvent event) {
    TaskAssignmentImpl taskAssignment = event.taskAssignment();
    String action = "TaskAssignedEmailListener:onAssigned: " + taskAssignment.getTask().getName();

    if (!taskAssignment.getTask().isNotifyOnAssignment()) {
      return;
    }

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

    Object notificationMarker = new Object();
    if (notifiedCandidates.asMap().putIfAbsent(candidate.getId(), notificationMarker) != null) {
      return;
    }

    try {
      emailHelper.sendTaskAssignedEmail(user, taskAssignment.getTask().getDisplayName());
    } catch (Exception ex) {
      notifiedCandidates.asMap().remove(candidate.getId(), notificationMarker);
      LogBuilder.builder(log)
          .action(action)
          .message(
              "Failed sending task assignment email for task assignment "
                  + taskAssignment.getId()
                  + " to candidate "
                  + candidate.getId())
          .logWarn(ex);
    }
  }
}
