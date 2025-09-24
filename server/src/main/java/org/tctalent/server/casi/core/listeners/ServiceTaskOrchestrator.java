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

package org.tctalent.server.casi.core.listeners;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tctalent.server.casi.domain.events.ServiceAssignedEvent;
import org.tctalent.server.casi.domain.events.ServiceExpiredEvent;
import org.tctalent.server.casi.domain.events.ServiceReassignedEvent;
import org.tctalent.server.casi.domain.events.ServiceRedeemedEvent;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.application.policy.TaskPolicyRegistry;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceTaskOrchestrator {

  private final TaskService taskService;
  private final TaskAssignmentService taskAssignmentService;
  private final TaskPolicyRegistry policyRegistry; // maps provider -> policy

  @TransactionalEventListener
  public void onAssigned(ServiceAssignedEvent event) {
    var policy = policyRegistry.forProvider(event.assignment().getProvider());
    for (String taskName : policy.tasksOnAssigned(event)) { // e.g. "claimCouponButton"
      TaskImpl task = taskService.getByName(taskName);
      taskAssignmentService.assignTaskToCandidate(
          user(event.assignment()), task,
          candidate(event.assignment()),
          null, null // no specific context or due date, TODO -- SM -- assignment or policy could provide these
      );
    }
  }

  @TransactionalEventListener
  public void onRedeemed(ServiceRedeemedEvent event) {
    var policy = policyRegistry.forProvider(event.assignment().getProvider());
    for (String taskName : policy.tasksOnRedeemed(event)) { // e.g. "duolingoTest"
      TaskImpl task = taskService.getByName(taskName);
      taskAssignmentService.assignTaskToCandidate(
          user(event.assignment()), task,
          candidate(event.assignment()),
          null, null // no specific context or due date, TODO -- SM -- assignment or policy could provide these
      );
    }
  }

  @TransactionalEventListener
  public void onReassigned(ServiceReassignedEvent event) {
    var policy = policyRegistry.forProvider(event.assignment().getProvider());
    for (String taskName : policy.tasksOnReassigned(event)) { // e.g. "duolingoTest"
      TaskImpl task = taskService.getByName(taskName);

      var existing = taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(
          task.getId(),
          event.assignment().getCandidateId(),
          Status.active
      );

      existing.forEach(ta -> {
        ta.setStatus(Status.inactive); // mark old assignments inactive
        taskAssignmentService.update(
            ta, null, true,
            "Marked inactive due to reassignment", null);

        LogBuilder.builder(log)
            .user(Optional.of(user(event.assignment())))
            .action("Reassigned: " + event.assignment().getProvider() + " " + event.assignment().getServiceCode())
            .message("Marked task assignment ID " + ta.getId() + " as inactive for candidate " + ta.getCandidate().getId() + " due to reassignment.")
            .logInfo();
      });
    }
  }

  // todo
  @TransactionalEventListener
  public void onExpired(ServiceExpiredEvent event) {
    var policy = policyRegistry.forProvider(event.assignment().getProvider());
    policy.handleOnExpired(event); // e.g., mark tasks inactive
  }

  // Returns a Candidate entity with only the ID set.
  // This is sufficient for task associations without needing to fetch the full entity.
  private Candidate candidate(ServiceAssignment a) {
    var c = new Candidate();
    c.setId(a.getCandidateId());
    return c;
  }

  // Returns a User entity with only the ID set.
  // This is sufficient for task associations and logging without needing to fetch the full entity.
  private User user(ServiceAssignment a) {
    var u = new User();
    u.setId(a.getActorId());
    return u;
  }

}
