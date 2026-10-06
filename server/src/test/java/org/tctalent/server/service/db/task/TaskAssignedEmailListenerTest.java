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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.configuration.properties.TaskAssignmentEmailProperties;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.TaskAssignedEvent;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class TaskAssignedEmailListenerTest {

  @Mock private EmailHelper emailHelper;

  private TaskAssignedEmailListener listener;
  private MutableTicker ticker;

  private TaskAssignmentImpl taskAssignment;
  private Candidate candidate;
  private User user;

  @BeforeEach
  void setUp() {
    TaskImpl task = new TaskImpl();
    task.setId(100L);
    task.setName("testTask");
    task.setDisplayName("Complete your profile");
    task.setNotifyOnAssignment(true);

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

    TaskAssignmentEmailProperties properties = new TaskAssignmentEmailProperties();
    properties.setCooldown(Duration.ofMinutes(15));
    ticker = new MutableTicker();
    listener = new TaskAssignedEmailListener(emailHelper, properties, ticker);
  }

  @Test
  @DisplayName("onAssigned sends email when candidate user and email exist")
  void onAssignedSendsEmailWhenCandidateDetailsExist() {
    listener.onAssigned(new TaskAssignedEvent(taskAssignment));

    verify(emailHelper).sendTaskAssignedEmail(user, "Complete your profile");
  }

  @Test
  @DisplayName("onAssigned suppresses a second email within the candidate cooldown")
  void onAssignedSuppressesSecondEmailWithinCooldown() {
    listener.onAssigned(new TaskAssignedEvent(taskAssignment));
    listener.onAssigned(new TaskAssignedEvent(taskAssignment));

    verify(emailHelper).sendTaskAssignedEmail(user, "Complete your profile");
  }

  @Test
  @DisplayName("onAssigned sends another email after the candidate cooldown expires")
  void onAssignedSendsAgainAfterCooldown() {
    listener.onAssigned(new TaskAssignedEvent(taskAssignment));
    ticker.advance(Duration.ofMinutes(15));
    listener.onAssigned(new TaskAssignedEvent(taskAssignment));

    verify(emailHelper, times(2)).sendTaskAssignedEmail(user, "Complete your profile");
  }

  @Test
  @DisplayName("onAssigned applies cooldowns independently per candidate")
  void onAssignedAppliesCooldownPerCandidate() {
    User otherUser = new User();
    otherUser.setId(201L);
    otherUser.setEmail("other@example.com");

    Candidate otherCandidate = new Candidate();
    otherCandidate.setId(301L);
    otherCandidate.setUser(otherUser);

    TaskAssignmentImpl otherAssignment = new TaskAssignmentImpl();
    otherAssignment.setId(401L);
    otherAssignment.setTask(taskAssignment.getTask());
    otherAssignment.setCandidate(otherCandidate);

    listener.onAssigned(new TaskAssignedEvent(taskAssignment));
    listener.onAssigned(new TaskAssignedEvent(otherAssignment));

    verify(emailHelper).sendTaskAssignedEmail(user, "Complete your profile");
    verify(emailHelper).sendTaskAssignedEmail(otherUser, "Complete your profile");
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
  @DisplayName("onAssigned releases the cooldown when email sending fails")
  void onAssignedReleasesCooldownAfterEmailSendFailure() {
    doThrow(new RuntimeException("smtp down"))
        .doNothing()
        .when(emailHelper)
        .sendTaskAssignedEmail(any(User.class), anyString());

    assertDoesNotThrow(() -> listener.onAssigned(new TaskAssignedEvent(taskAssignment)));
    listener.onAssigned(new TaskAssignedEvent(taskAssignment));

    verify(emailHelper, times(2)).sendTaskAssignedEmail(user, "Complete your profile");
  }

  private static final class MutableTicker implements Ticker {

    private long elapsedNanos;

    @Override
    public long read() {
      return elapsedNanos;
    }

    void advance(Duration duration) {
      elapsedNanos += duration.toNanos();
    }
  }
}
