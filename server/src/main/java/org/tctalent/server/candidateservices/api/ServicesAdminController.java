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

package org.tctalent.server.candidateservices.api;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.candidateservices.api.dto.ServiceAssignmentDto;
import org.tctalent.server.candidateservices.application.CandidateService;
import org.tctalent.server.candidateservices.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.candidateservices.core.services.CandidateServicesQueryService;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.security.AuthService;

@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@Slf4j
public class ServicesAdminController {

  private final Map<String, CandidateService> services;              // bean names: "duolingo", "credly", ...
  private final AuthService authService;
  private final ServiceResourceRepository resourceRepo;              // generic inventory
  private final CandidateServicesQueryService queryService;          // consolidated view

  // Endpoint to import inventory from file data
  @PostMapping(
      path = "/{provider}/{serviceCode}/import",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, Object> importInventory(@PathVariable String provider,
      @PathVariable String serviceCode,
      @RequestParam("file") MultipartFile file) {
    try {
      // Call the service to import coupons
      service(provider).importInventory(file, serviceCode);

      // Log success message
      LogBuilder.builder(log)
          .action("importCouponsFromCsv")
          .message("Coupons imported successfully from CSV")
          .logInfo();

      return Map.of("status", "success", "message", "Coupons imported successfully.");
    } catch (RuntimeException e) {
      LogBuilder.builder(log)
          .action("importCouponsFromCsv")
          .message("Failed to import coupons from CSV")
          .logError();
      // Return error response
      return Map.of("status", "failure", "message", "Failed to import coupons from CSV file.");
    }
  }

  // Consolidated “what services does this candidate have?” (all providers)
  @GetMapping("/assignments/candidate/{candidateId}")
  public List<ServiceAssignmentDto> getAssignments(@PathVariable Long candidateId) {
    return queryService.listForCandidate(candidateId).stream()
        .map(ServiceAssignmentDto::from).toList();
  }

//  // Generic list of resources for a candidate (any provider)
//  @GetMapping("/{provider}/resources/candidate/{candidateId}")
//  public List<ServiceResourceDto> listProviderResourcesForCandidate(@PathVariable String provider,
//      @PathVariable Long candidateId) {
//    return resourceRepo
//        .findAllByProviderAndCandidateIdOrderByAssignedAtDesc(provider.toUpperCase(), candidateId)
//        .stream().map(ServiceResourceDto::from).toList();
//  }
//
//
//
//
//
//
//
//
//
//
//
//
//  // --- Assign to a single candidate (generic response) ---
//  @PostMapping("/{provider}/{serviceCode}/assign/candidate/{candidateId}")
//  public ServiceAssignment assignToCandidate(@PathVariable String provider,
//      @PathVariable String serviceCode,
//      @PathVariable Long candidateId) {
//    User actor = authService.getLoggedInUser()
//        .orElseThrow(() -> new InvalidSessionException("Not logged in"));
//    return service(provider).assignToCandidate(candidateId, serviceCode, actor);
//  }
//
//  // --- Assign to a list of candidates ---
//  @PostMapping("/{provider}/{serviceCode}/assign/list/{listId}")
//  public List<ServiceAssignment> assignToList(@PathVariable String provider,
//      @PathVariable String serviceCode,
//      @PathVariable Long listId) {
//    User actor = authService.getLoggedInUser()
//        .orElseThrow(() -> new InvalidSessionException("Not logged in"));
//    return service(provider).assignToList(listId, serviceCode, actor);
//  }
//
//  // --- Count available inventory for a service ---
//  @GetMapping("/{provider}/{serviceCode}/available/count")
//  public Map<String, Object> countAvailable(@PathVariable String provider,
//      @PathVariable String serviceCode) {
//    int count = resourceRepo.countByProviderAndServiceCodeAndStatus(
//        provider.toUpperCase(), serviceCode, ResourceStatus.AVAILABLE);
//    return Map.of("status", "success", "count", count);
//  }
//
//  // --- List available inventory (lightweight DTO) ---
//  @GetMapping("/{provider}/{serviceCode}/available")
//  public List<ServiceResourceDto> listAvailable(@PathVariable String provider,
//      @PathVariable String serviceCode) {
//    return resourceRepo.findTop200ByProviderAndServiceCodeAndStatusOrderByIdAsc(
//            provider.toUpperCase(), serviceCode, ResourceStatus.AVAILABLE)
//        .stream().map(ServiceResourceDto::from).toList();
//  }
//
//  // --- Update resource status (generic) ---
//  @PutMapping("/{provider}/resource/{resourceCode}/status")
//  public void updateResourceStatus(@PathVariable String provider,
//      @PathVariable String resourceCode,
//      @RequestBody @Valid UpdateResourceStatusRequest req) {
//    ServiceResourceEntity r = resourceRepo
//        .findByProviderAndResourceCode(provider.toUpperCase(), resourceCode)
//        .orElseThrow(() -> new NoSuchObjectException("Resource not found"));
//    r.setStatus(req.status());
//    resourceRepo.save(r);
//  }
//
//  // --- Find one resource by code (generic) ---
//  @GetMapping("/{provider}/resource/{resourceCode}")
//  public ServiceResourceDto getResource(@PathVariable String provider,
//      @PathVariable String resourceCode) {
//    ServiceResourceEntity r = resourceRepo
//        .findByProviderAndResourceCode(provider.toUpperCase(), resourceCode)
//        .orElseThrow(() -> new NoSuchObjectException("Resource not found"));
//    return ServiceResourceDto.from(r);
//  }
//
//  // --- Consolidated “what services does this candidate have?” ---
//  @GetMapping("/assignments/candidate/{candidateId}")
//  public List<ServiceAssignment> getAssignmentsForCandidate(@PathVariable Long candidateId) {
//    return queryService.listForCandidate(candidateId);
//  }
//
  private CandidateService service(String provider) {
    CandidateService svc = services.get(provider.toLowerCase());
    if (svc == null) {
      throw new NoSuchObjectException("Unknown provider: " + provider);
    }
    return svc;
  }
}

