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

package org.tctalent.server.casi.api;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.casi.api.dto.ServiceAssignmentDto;
import org.tctalent.server.casi.api.request.UpdateServiceResourceStatusRequest;
import org.tctalent.server.casi.application.policy.EligibilityPolicyRegistry;
import org.tctalent.server.casi.core.services.CandidateAssistanceService;
import org.tctalent.server.casi.core.services.CandidateServiceRegistry;
import org.tctalent.server.casi.domain.mappers.ServiceAssignmentMapper;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.UserService;

/**
 * Candidate-facing generic CASI endpoints.
 * This controller always scopes actions to the logged-in candidate.
 *
 * @author sadatmalik
 */
@RestController
@RequestMapping("/api/portal/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ServicesPortalController {

  private final AuthService authService;
  private final UserService userService;
  private final CandidateServiceRegistry services;
  private final EligibilityPolicyRegistry eligibilityPolicies;

  @GetMapping("/{provider}/{serviceCode}/eligibility")
  public boolean isEligible(@PathVariable String provider, @PathVariable String serviceCode) {
    ServiceProvider providerEnum = providerFor(provider, serviceCode);
    return eligibilityPolicies.isEligible(providerEnum, candidateIdFromSession());
  }

  @GetMapping("/{provider}/{serviceCode}/assignment")
  public ServiceAssignmentDto getCurrentAssignment(@PathVariable String provider,
      @PathVariable String serviceCode) {
    CandidateAssistanceService service = serviceFor(provider, serviceCode);
    ServiceAssignment assignment = service.getCurrentAssignment(candidateIdFromSession());

    return ServiceAssignmentMapper.toDto(assignment);
  }

  @PostMapping("/{provider}/{serviceCode}/assign")
  public ServiceAssignmentDto assign(@PathVariable String provider, @PathVariable String serviceCode) {
    ServiceProvider providerEnum = providerFor(provider, serviceCode);
    Long candidateId = candidateIdFromSession();
    if (!eligibilityPolicies.isEligible(providerEnum, candidateId)) {
      throw new ServiceException("candidate_not_eligible",
          "Candidate is not eligible for this service.");
    }

    var assignment = serviceFor(provider, serviceCode)
        .assignToCandidate(candidateId, userService.getSystemAdminUser());
    return ServiceAssignmentMapper.toDto(assignment);
  }

  @PutMapping("/{provider}/{serviceCode}/resources/status")
  public void updateResourceStatus(@PathVariable String provider,
      @PathVariable String serviceCode,
      @RequestBody UpdateServiceResourceStatusRequest request) {

    CandidateAssistanceService service = serviceFor(provider, serviceCode);
    Long candidateId = candidateIdFromSession();

    boolean ownsResource = service.getResourcesForCandidate(candidateId).stream()
        .anyMatch(resource -> request.getResourceCode().equals(resource.getResourceCode()));

    if (!ownsResource) {
      throw new NoSuchObjectException("Resource with code " + request.getResourceCode() + " not found");
    }

    service.updateResourceStatus(request.getResourceCode(), request.getStatus());
  }

  private CandidateAssistanceService serviceFor(String provider, String serviceCode) {
    return services.forProviderAndServiceCode(provider, serviceCode);
  }

  private ServiceProvider providerFor(String provider, String serviceCode) {
    // Reuse service registry lookup as endpoint-level provider/service validation.
    serviceFor(provider, serviceCode);
    return ServiceProvider.valueOf(provider.trim().toUpperCase(Locale.ROOT));
  }

  private Long candidateIdFromSession() {
    Long candidateId = authService.getLoggedInCandidateId();
    if (candidateId == null) {
      throw new InvalidSessionException("Not logged in as a candidate");
    }
    return candidateId;
  }
}
