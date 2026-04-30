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

package org.tctalent.server.casi.core.services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.casi.domain.model.ListRole;
import org.tctalent.server.casi.domain.persistence.ServiceListEntity;
import org.tctalent.server.casi.domain.persistence.ServiceListRepository;
import org.tctalent.server.configuration.SystemAdminConfiguration;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.UserService;

/**
 * Creates and registers service lists for all {@link CandidateAssistanceService} implementations
 * at application startup.
 * <p>
 * Each service declares its required lists via {@link CandidateAssistanceService#serviceListSpecs()}.
 * This service processes those declarations idempotently — lists and their
 * {@link ServiceListEntity} records are only created if they do not already exist.
 * <p>
 * Runs after {@link SystemAdminConfiguration} (Order 2 vs Order 1) to ensure the system admin
 * user is available for list creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceListSetupService {

  private final List<CandidateAssistanceService> allServices;
  private final ServiceListRepository serviceListRepository;
  private final SavedListService savedListService;
  private final UserService userService;

  @EventListener(ApplicationReadyEvent.class)
  @Order(2)
  @Transactional
  public void setupAllServiceLists() {
    for (CandidateAssistanceService service : allServices) {
      try {
        setupListsFor(service);
      } catch (Exception e) {
        LogBuilder.builder(log)
            .action("setupAllServiceLists")
            .message("Failed to set up service lists for " + service.providerKey()
                + ": " + e.getMessage())
            .logError(e);
      }
    }
  }

  /**
   * Triggers setup for a single service by its provider key. Useful for on-demand re-setup
   * without restarting the application.
   */
  @Transactional
  public void setupListsFor(String providerKey) {
    allServices.stream()
        .filter(s -> s.providerKey().equals(providerKey))
        .findFirst()
        .ifPresent(this::setupListsFor);
  }

  private void setupListsFor(CandidateAssistanceService service) {
    List<ServiceListSpec> specs = service.serviceListSpecs();
    if (specs.isEmpty()) {
      return;
    }

    validateSpecs(service, specs);

    User systemAdmin = userService.getSystemAdminUser();

    for (ServiceListSpec spec : specs) {
      boolean exists = spec.role().isAllowsMultiple()
          ? serviceListRepository.existsByProviderAndServiceCodeAndRoleAndName(
              service.provider(), service.serviceCode(), spec.role(), spec.listName())
          : serviceListRepository.existsByProviderAndServiceCodeAndRole(
              service.provider(), service.serviceCode(), spec.role());

      if (!exists) {
        UpdateSavedListInfoRequest req = new UpdateSavedListInfoRequest();
        req.setName(spec.listName());
        SavedList savedList = savedListService.createSavedList(systemAdmin, req);

        ServiceListEntity entity = new ServiceListEntity();
        entity.setSavedList(savedList);
        entity.setProvider(service.provider());
        entity.setServiceCode(service.serviceCode());
        entity.setListRole(spec.role());
        entity.setPermittedActions(new HashSet<>(spec.permittedActions()));
        serviceListRepository.save(entity);

        LogBuilder.builder(log)
            .action("setupListsFor")
            .message("Created service list '" + spec.listName() + "' ("
                + spec.role() + ") for " + service.providerKey())
            .logInfo();
      }
    }
  }

  private void validateSpecs(CandidateAssistanceService service, List<ServiceListSpec> specs) {
    Map<ListRole, Long> countByRole = specs.stream()
        .collect(Collectors.groupingBy(ServiceListSpec::role, Collectors.counting()));

    countByRole.forEach((role, count) -> {
      if (!role.isAllowsMultiple() && count > 1) {
        throw new IllegalStateException(
            "Service " + service.providerKey() + " declares " + count
                + " specs for role " + role + " but this role does not allow multiple lists");
      }
    });
  }
}
