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

package org.tctalent.server.casi.application.providers.unhcr;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.services.AbstractCandidateAssistanceService;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;

/**
 * CASI service for country-specific UNHCR help site links.
 *
 * @author sadatmalik
 */
@Service
public class UnhcrService extends AbstractCandidateAssistanceService {

  private final ResourceAllocator unhcrAllocator;
  private final CandidateService candidateService;

  public UnhcrService(
      ServiceAssignmentRepository assignmentRepo,
      ServiceResourceRepository resourceRepo,
      AssignmentEngine assignmentEngine,
      SavedListService savedListService,
      CandidateService candidateService,
      @Qualifier("unhcrHelpSiteLinkAllocator") ResourceAllocator allocator) {
    super(assignmentRepo, resourceRepo, assignmentEngine, savedListService);
    this.unhcrAllocator = allocator;
    this.candidateService = candidateService;
  }

  @Override
  public ServiceProvider provider() {
    return ServiceProvider.UNHCR;
  }

  @Override
  public ServiceCode serviceCode() {
    return ServiceCode.HELP_SITE_LINK;
  }

  @Override
  protected ResourceAllocator allocator() {
    return unhcrAllocator;
  }

  // UNHCR links are country-specific shared resources; only return the assignment
  // that matches the candidate's current country.
  @Override
  @Transactional(readOnly = true)
  public ServiceAssignment getCurrentAssignment(Long candidateId) {
    String candidateCountryIsoCode = candidateCountryIsoCode(candidateId);
    if (candidateCountryIsoCode == null) {
      return null;
    }

    return getAssignmentsForCandidate(candidateId).stream()
        .filter(a -> a.getResource() != null
            && a.getResource().getStatus() == ResourceStatus.AVAILABLE
            && candidateCountryIsoCode.equals(normalizeIso(a.getResource().getCountryIsoCode())))
        .findFirst()
        .orElse(null);
  }

  // Assign UNHCR link if not already ASSIGNED for the candidate's current country.
  // The assignment allows tracking the assigned resource.
  @Override
  @Transactional
  public ServiceAssignment assignToCandidate(Long candidateId, User user) {
    String candidateCountryIsoCode = candidateCountryIsoCode(candidateId);

    boolean hasSameCountryAssigned = getAssignmentsForCandidate(candidateId).stream()
        .anyMatch(a -> a.getStatus() == AssignmentStatus.ASSIGNED
            && a.getResource() != null
            && candidateCountryIsoCode != null
            && candidateCountryIsoCode.equals(normalizeIso(a.getResource().getCountryIsoCode())));

    if (hasSameCountryAssigned) {
      throw new EntityExistsException(AssignmentStatus.ASSIGNED.name() + " " + serviceCode()
          + " resource", "for this candidate");
    }

    return assignmentEngine.assign(allocator(), candidateId, user);
  }

  private String candidateCountryIsoCode(Long candidateId) {
    Candidate candidate = candidateService.getCandidate(candidateId);
    return normalizeIso(candidate.getCountry() == null ? null : candidate.getCountry().getIsoCode());
  }

  private String normalizeIso(String isoCode) {
    if (isoCode == null || isoCode.isBlank()) {
      return null;
    }
    return isoCode.trim().toUpperCase(Locale.ROOT);
  }
}
