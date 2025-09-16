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

package org.tctalent.server.candidateservices.infrastructure.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.domain.events.ServiceAssignedEvent;
import org.tctalent.server.candidateservices.domain.events.ServiceExpiredEvent;
import org.tctalent.server.candidateservices.domain.events.ServiceRedeemedEvent;
import org.tctalent.server.candidateservices.domain.policy.TaskPolicyRegistry;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

@Component
@RequiredArgsConstructor
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

  // todo
  @TransactionalEventListener
  public void onRedeemed(ServiceRedeemedEvent event) { // e.g. "duolingoTest"
    var policy = policyRegistry.forProvider(event.assignment().getProvider());
    policy.handleOnRedeemed(event); // e.g., close/advance tasks
  }

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
  // This is sufficient for task associations without needing to fetch the full entity.
  private User user(ServiceAssignment a) {
    var u = new User();
    u.setId(a.getActorId());
    return u;
  }

}
