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

package org.tctalent.server.casi.core.services;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.casi.domain.events.ServiceAssignedEvent;
import org.tctalent.server.casi.domain.events.ServiceReassignedEvent;
import org.tctalent.server.casi.domain.mappers.ServiceAssignmentMapper;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;


@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentEngine {

  private final CandidateRepository candidates;
  private final ServiceAssignmentRepository ledger;
  private final ServiceResourceRepository resourceRepo;
  private final ApplicationEventPublisher events;

  @Transactional
  public ServiceAssignment assign(ResourceAllocator allocator, Long candidateId, User actor) {

    Candidate c = candidates.findById(candidateId)
        .orElseThrow(() -> new NoSuchObjectException("Candidate with ID " + candidateId + " not found"));

    // todo -- pass in the PD here
    ServiceResource res = allocator.allocateFor(c); // provider-specific step

    // Write to central ledger
    ServiceAssignmentEntity e = new ServiceAssignmentEntity();
    e.setResource(resourceRepo.getReferenceById(res.getId()));
    e.setCandidate(c);
    e.setActor(actor);
    e.setStatus(AssignmentStatus.ASSIGNED);
    e.setAssignedAt(LocalDateTime.now());
    ledger.save(e);

    // Event + model
    ServiceAssignment model = ServiceAssignmentMapper.toModel(e);

    events.publishEvent(new ServiceAssignedEvent(model));
    return model;
  }

  @Transactional
  public ServiceAssignment reassign(ResourceAllocator allocator, String candidateNumber, User actor) {

    Candidate candidate = candidates.findByCandidateNumber(candidateNumber);
    if (candidate == null) {
      throw new NoSuchObjectException("Candidate with Number " + candidateNumber + " not found");
    }

    updatePreviousAssignments(allocator, candidate);

    // Make a new assignment for the candidate
    var assignment = assign(allocator, candidate.getId(), actor);

    // Log the reassignment
    LogBuilder.builder(log)
        .user(Optional.ofNullable(actor))
        .action("Reassigned: " + assignment.getProvider() + " " + assignment.getServiceCode())
        .message("Reassigned new coupon " + assignment.getResource().getResourceCode() + " to candidate " + candidate.getId())
        .logInfo();

    return assignment;
  }

  private void updatePreviousAssignments(ResourceAllocator allocator, Candidate candidate) {

    // Find and mark all existing assignments and resources for the candidate as reassigned/disabled
    ledger.findByCandidateAndProviderAndService(
        candidate.getId(), allocator.getProvider(), allocator.getServiceCode())
        .forEach(assignment -> {
          assignment.setStatus(AssignmentStatus.REASSIGNED);
          ledger.save(assignment);

          ServiceResourceEntity r = assignment.getResource();
          r.setStatus(ResourceStatus.DISABLED);
          resourceRepo.save(r);

          events.publishEvent(new ServiceReassignedEvent(ServiceAssignmentMapper.toModel(assignment)));
        });
  }

}
