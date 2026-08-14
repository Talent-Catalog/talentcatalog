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

package org.tctalent.server.casi.application.providers.pifi;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.casi.application.support.RelevantCountryResolver;
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
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.SavedListService;

/**
 * CASI service for PiFi country signposting links ({@code PIFI::HELP_SITE_LINK}).
 * <p>
 * Currently uses first-country match only (adequate while we support a single country).
 * May later be extended to multiple country matches if needed.
 *
 * @author sadatmalik
 */
@Service
public class PifiService extends AbstractCandidateAssistanceService {

  private final ResourceAllocator pifiAllocator;
  private final RelevantCountryResolver countryResolver;

  public PifiService(
      ServiceAssignmentRepository assignmentRepo,
      ServiceResourceRepository resourceRepo,
      AssignmentEngine assignmentEngine,
      SavedListService savedListService,
      RelevantCountryResolver countryResolver,
      @Qualifier("pifiHelpSiteLinkAllocator") ResourceAllocator allocator) {
    super(assignmentRepo, resourceRepo, assignmentEngine, savedListService);
    this.countryResolver = countryResolver;
    this.pifiAllocator = allocator;
  }

  @Override
  public ServiceProvider provider() {
    return ServiceProvider.PIFI;
  }

  @Override
  public ServiceCode serviceCode() {
    return ServiceCode.HELP_SITE_LINK;
  }

  @Override
  protected ResourceAllocator allocator() {
    return pifiAllocator;
  }

  @Override
  @Transactional(readOnly = true)
  public ServiceAssignment getCurrentAssignment(Long candidateId) {
    String firstCountry = firstCountryWithConfiguredLink(candidateId);
    if (firstCountry == null) {
      return null;
    }

    return getAssignmentsForCandidate(candidateId).stream()
        .filter(a -> a.getResource() != null
            && a.getResource().getStatus() == ResourceStatus.AVAILABLE
            && firstCountry.equals(normalizeIso(a.getResource().getCountryIsoCode())))
        .findFirst()
        .orElse(null);
  }

  @Override
  @Transactional
  public ServiceAssignment assignToCandidate(Long candidateId, User user) {
    String firstCountry = firstCountryWithConfiguredLink(candidateId);
    if (firstCountry == null) {
      throw new NoSuchObjectException("No HELP_SITE_LINK resources are configured for this candidate.");
    }

    boolean hasSameCountryAssigned = getAssignmentsForCandidate(candidateId).stream()
        .anyMatch(a -> a.getStatus() == AssignmentStatus.ASSIGNED
            && a.getResource() != null
            && firstCountry.equals(normalizeIso(a.getResource().getCountryIsoCode())));

    if (hasSameCountryAssigned) {
      throw new EntityExistsException(AssignmentStatus.ASSIGNED.name() + " " + serviceCode()
          + " resource", "for this candidate");
    }

    return assignmentEngine.assign(allocator(), candidateId, user);
  }

  // First matching relevant country with an AVAILABLE PiFi link.
  // Multi-country matching can be added later if required.
  private String firstCountryWithConfiguredLink(Long candidateId) {
    for (String isoCode : countryResolver.resolveCountryIsoCodes(candidateId)) {
      if (resourceRepository.countAvailableByProviderServiceAndCountry(provider(), serviceCode(), isoCode) > 0) {
        return isoCode;
      }
    }
    return null;
  }

  private String normalizeIso(String isoCode) {
    if (isoCode == null || isoCode.isBlank()) {
      return null;
    }
    return isoCode.trim().toUpperCase(Locale.ROOT);
  }
}
