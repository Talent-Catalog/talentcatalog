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

package org.tctalent.server.candidateservices.application.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentMapper;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentRepository;

@Service
@RequiredArgsConstructor
public class CandidateServicesQueryService {
  private final ServiceAssignmentRepository repo;

  @Transactional(readOnly = true)
  public List<ServiceAssignment> listForCandidate(Long candidateId) {
    return repo.findByCandidateIdOrderByAssignedAtDesc(candidateId).stream()
        .map(ServiceAssignmentMapper::toDomain)
        .toList();
  }

}
