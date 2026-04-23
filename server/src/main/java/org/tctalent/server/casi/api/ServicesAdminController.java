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

package org.tctalent.server.casi.api;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.casi.api.dto.CountResponseDto;
import org.tctalent.server.casi.api.dto.ImportResponseDto;
import org.tctalent.server.casi.api.dto.ServiceAssignmentDto;
import org.tctalent.server.casi.api.dto.ServiceListDto;
import org.tctalent.server.casi.api.dto.ServiceResourceDto;
import org.tctalent.server.casi.api.request.ServiceListActionRequest;
import org.tctalent.server.casi.api.request.UpdateServiceResourceStatusRequest;
import org.tctalent.server.casi.core.services.CandidateAssistanceService;
import org.tctalent.server.casi.core.services.CandidateServiceRegistry;
import org.tctalent.server.casi.core.services.CandidateServicesQueryService;
import org.tctalent.server.casi.domain.mappers.ServiceAssignmentMapper;
import org.tctalent.server.casi.domain.mappers.ServiceResourceMapper;
import org.tctalent.server.casi.domain.persistence.ServiceListEntity;
import org.tctalent.server.casi.domain.persistence.ServiceListRepository;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.User;
import org.tctalent.server.security.AuthService;


/**
 * REST controller for managing candidate assistance services such as Duolingo. Candidate assistance
 * services may offer resources (e.g., coupons, subscriptions) that can be assigned to candidates.
 * <p>
 * Provides endpoints for importing resources, assigning services to candidates, updating resource
 * status, and querying service assignments and resources.
 * <p>
 * Note: Endpoints are secured by the URL-based rules in SecurityConfiguration. Sensitive inventory
 * management endpoints are additionally restricted to ADMIN and SYSTEMADMIN via method-level
 * {@code @PreAuthorize}.
 */
@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@Slf4j
public class ServicesAdminController {

  private static final String ADMIN_ONLY =
      "hasAnyAuthority('ROLE_ADMIN', 'ROLE_SYSTEMADMIN')";

  private final AuthService authService;
  private final CandidateServiceRegistry services;
  private final CandidateServicesQueryService queryService; // consolidated view
  private final ServiceListRepository serviceListRepository;

  // Endpoint to import service resources from a file -- e.g. coupons from CSV data
  @PreAuthorize(ADMIN_ONLY)
  @PostMapping(
      path = "/{provider}/{serviceCode}/import",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ImportResponseDto importInventory(@PathVariable String provider,
      @PathVariable String serviceCode,
      @RequestParam("file") MultipartFile file) {
    try {
      // Call the service to import coupons
      serviceFor(provider, serviceCode).importInventory(file);

      // Log success message
      LogBuilder.builder(log)
          .action("importInventory")
          .message(provider + "::" + serviceCode + " - resources imported successfully from file")
          .logInfo();

      return ImportResponseDto.builder()
          .status("success")
          .message("Service resources imported successfully.")
          .build();
    } catch (NoSuchObjectException e) {
      // Let NoSuchObjectException propagate so ErrorHandler can return 404
      throw e;
    } catch (RuntimeException e) {
      LogBuilder.builder(log)
          .action("importInventory")
          .message("Failed to import service resources from file")
          .logError();
      // Return error response
      return ImportResponseDto.builder()
          .status("failure")
          .message("Failed to import service resources from file.")
          .build();
    }
  }

  // Consolidated “what services does this candidate have?” (all providers)
  @GetMapping("/assignments/candidate/{candidateId}")
  public List<ServiceAssignmentDto> getAssignments(@PathVariable Long candidateId) {
    return queryService.listForCandidate(candidateId).stream()
        .map(ServiceAssignmentMapper::toDto).toList();
  }

  // Endpoint to retrieve all resources assigned to a candidate
  @GetMapping("/{provider}/{serviceCode}/resources/candidate/{candidateId}")
  public List<ServiceResourceDto> listProviderResourcesForCandidate(@PathVariable String provider,
      @PathVariable String serviceCode,
      @PathVariable("candidateId") Long candidateId) {
    return serviceFor(provider, serviceCode)
        .getResourcesForCandidate(candidateId)
        .stream().map(ServiceResourceMapper::toDto)
        .toList();
  }

  // Endpoint to update the status of a specific resource
  @PreAuthorize(ADMIN_ONLY)
  @PutMapping("/{provider}/{serviceCode}/resources/status")
  public void updateResourceStatus(@PathVariable String provider,
      @PathVariable String serviceCode,
      @Valid @RequestBody UpdateServiceResourceStatusRequest request) {
    serviceFor(provider, serviceCode)
        .updateResourceStatus(request.getResourceCode(), request.getStatus());
  }

  // Endpoint to list all available resources for a specific service
  @GetMapping("/{provider}/{serviceCode}/available")
  public List<ServiceResourceDto> listAvailable(@PathVariable String provider,
      @PathVariable String serviceCode) {
    return serviceFor(provider, serviceCode)
        .getAvailableResources()
        .stream().map(ServiceResourceMapper::toDto)
        .toList();
  }

  // Endpoint to get a single resource by its code for a specific service
  @GetMapping("/{provider}/{serviceCode}/resource/{resourceCode}")
  public ServiceResourceDto getResource(@PathVariable String provider,
      @PathVariable String serviceCode,
      @PathVariable String resourceCode) {

    var r = serviceFor(provider, serviceCode)
        .getResourceForResourceCode(resourceCode);

    return ServiceResourceMapper.toDto(r);
  }

  // Assign to a single candidate
  @PreAuthorize(ADMIN_ONLY)
  @PostMapping("/{provider}/{serviceCode}/assign/candidate/{candidateId}")
  public ServiceAssignmentDto assignToCandidate(@PathVariable String provider,
      @PathVariable String serviceCode,
      @PathVariable("candidateId") Long candidateId)
      throws InvalidSessionException, NoSuchObjectException, EntityExistsException {

    User user = authService.getLoggedInUser()
        .orElseThrow(() -> new InvalidSessionException("Not logged in"));

    var a = serviceFor(provider, serviceCode)
        .assignToCandidate(candidateId, user);

    return ServiceAssignmentMapper.toDto(a);
  }

  // Assign to a list
  @PreAuthorize(ADMIN_ONLY)
  @PostMapping("/{provider}/{serviceCode}/assign/list/{listId}")
  public List<ServiceAssignmentDto> assignToList(@PathVariable String provider,
      @PathVariable String serviceCode,
      @PathVariable Long listId) throws NoSuchObjectException, InvalidSessionException {

    User user = authService.getLoggedInUser()
        .orElseThrow(() -> new InvalidSessionException("Not logged in"));

    return serviceFor(provider, serviceCode)
        .assignToList(listId, user)
        .stream().map(ServiceAssignmentMapper::toDto)
        .toList();
  }

  // Endpoint to count the available inventory for a provider and service
  @GetMapping("/{provider}/{serviceCode}/available/count")
  public CountResponseDto countAvailable(@PathVariable String provider,
      @PathVariable String serviceCode) {
    long count = serviceFor(provider, serviceCode)
        .countAvailableForProviderAndService();

    return CountResponseDto.builder()
        .count(count)
        .build();
  }

  // Returns the service list associated with the given saved list, or 404 if none exists
  @GetMapping("/list/{savedListId}")
  public ServiceListDto getServiceList(@PathVariable Long savedListId) {
    return serviceListRepository.findBySavedListId(savedListId)
        .map(e -> ServiceListDto.builder()
            .id(e.getId())
            .provider(e.getProvider())
            .serviceCode(e.getServiceCode())
            .listRole(e.getListRole())
            .permittedActions(e.getPermittedActions())
            .build())
        .orElseThrow(() -> new NoSuchObjectException(
            "No service list for saved list id: " + savedListId));
  }

  // Perform a ListAction on a set of candidates within a service list.
  //
  // DEVELOPER NOTE: when adding a new ListAction enum value, you MUST add a corresponding case
  // to the switch expression below. The compiler will flag any missing cases — do not add a
  // default branch, as that would suppress this safety check.
  @PreAuthorize(ADMIN_ONLY)
  @PostMapping("/list/{serviceListId}/action")
  public void performServiceListAction(
      @PathVariable Long serviceListId,
      @Valid @RequestBody ServiceListActionRequest request)
      throws InvalidSessionException, NoSuchObjectException {

    User actor = authService.getLoggedInUser()
        .orElseThrow(() -> new InvalidSessionException("Not logged in"));

    ServiceListEntity serviceList = serviceListRepository.findById(serviceListId)
        .orElseThrow(() -> new NoSuchObjectException("No service list with id: " + serviceListId));

    CandidateAssistanceService service =
        serviceFor(serviceList.getProvider().name(), serviceList.getServiceCode().name());

    // Exhaustive switch — compiler enforces a case for every ListAction value.
    // Do NOT add a default branch: missing cases should be a compile error, not silently ignored.
    switch (request.getAction()) {
      case REASSIGN -> {
        for (String candidateNumber : request.getCandidateNumbers()) {
          service.reassignForCandidate(candidateNumber, actor);
        }
      }
    }
  }

  private CandidateAssistanceService serviceFor(String provider, String serviceCode) {
    // Registry throws NoSuchObjectException when service not found, which ErrorHandler maps to 404
    return services.forProviderAndServiceCode(provider, serviceCode);
  }
}

