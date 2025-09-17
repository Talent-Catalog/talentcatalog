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

package org.tctalent.server.candidateservices.application;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.candidateservices.application.alloc.ResourceAllocator;
import org.tctalent.server.candidateservices.domain.events.ServiceAssignedEvent;
import org.tctalent.server.candidateservices.domain.model.AssignmentStatus;
import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.domain.model.ServiceResource;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentEntity;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentRepository;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.service.db.email.EmailHelper;

@Component
@RequiredArgsConstructor
public class AssignmentEngine {

  private final CandidateRepository candidates;
  private final ServiceAssignmentRepository ledger;
  private final ServiceResourceRepository resourceRepo;
  private final ApplicationEventPublisher events;

  private final EmailHelper emailHelper; // TODO -- SM -- replace with NotificationListener later

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
    ServiceAssignment model = ServiceAssignment.from(e);

    emailHelper.sendDuolingoCouponEmail(c.getUser()); // TODO -- SM -- keep for now; later move to NotificationListener
    events.publishEvent(new ServiceAssignedEvent(model));
    return model;
  }

}
