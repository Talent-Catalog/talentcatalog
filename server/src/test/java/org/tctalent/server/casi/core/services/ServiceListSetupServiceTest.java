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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.domain.model.ListAction;
import org.tctalent.server.casi.domain.model.ListRole;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceListEntity;
import org.tctalent.server.casi.domain.persistence.ServiceListRepository;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.UserService;

@ExtendWith(MockitoExtension.class)
class ServiceListSetupServiceTest {

  @Mock private ServiceListRepository serviceListRepository;
  @Mock private SavedListService savedListService;
  @Mock private UserService userService;

  private CandidateAssistanceService linkedInService;
  private CandidateAssistanceService duolingoService;

  private User systemAdmin;
  private SavedList createdList;

  @BeforeEach
  void setUp() {
    linkedInService = mock(CandidateAssistanceService.class);
    duolingoService = mock(CandidateAssistanceService.class);

    systemAdmin = new User();
    systemAdmin.setId(1L);

    createdList = new SavedList();
    createdList.setId(10L);
  }

  // ── helpers ────────────────────────────────────────────────────────────────

  private ServiceListSetupService buildSetupService(CandidateAssistanceService... services) {
    return new ServiceListSetupService(
        List.of(services), serviceListRepository, savedListService, userService);
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("service with no specs skips all setup")
  void serviceWithNoSpecs_skipsAllSetup() {
    given(linkedInService.serviceListSpecs()).willReturn(List.of());

    buildSetupService(linkedInService).setupAllServiceLists();

    verify(serviceListRepository, never()).existsByProviderAndServiceCodeAndRole(any(), any(), any());
    verify(savedListService, never()).createSavedList(any(User.class), any());
    verify(serviceListRepository, never()).save(any());
  }

  @Test
  @DisplayName("creates saved list and entity when no list for that role exists yet")
  void createsListWhenRoleHasNoPriorList() {
    given(linkedInService.provider()).willReturn(ServiceProvider.LINKEDIN);
    given(linkedInService.serviceCode()).willReturn(ServiceCode.PREMIUM_MEMBERSHIP);
    given(linkedInService.serviceListSpecs()).willReturn(List.of(
        new ServiceListSpec("LinkedIn Issue Reports", ListRole.USER_ISSUE_REPORT, Set.of(ListAction.REASSIGN))
    ));
    given(userService.getSystemAdminUser()).willReturn(systemAdmin);
    given(serviceListRepository.existsByProviderAndServiceCodeAndRole(
        ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP, ListRole.USER_ISSUE_REPORT))
        .willReturn(false);
    given(savedListService.createSavedList(eq(systemAdmin), any(UpdateSavedListInfoRequest.class)))
        .willReturn(createdList);

    buildSetupService(linkedInService).setupAllServiceLists();

    verify(savedListService).createSavedList(eq(systemAdmin), any(UpdateSavedListInfoRequest.class));
    ArgumentCaptor<ServiceListEntity> captor = ArgumentCaptor.forClass(ServiceListEntity.class);
    verify(serviceListRepository).save(captor.capture());
    ServiceListEntity saved = captor.getValue();
    assertThat(saved.getProvider()).isEqualTo(ServiceProvider.LINKEDIN);
    assertThat(saved.getServiceCode()).isEqualTo(ServiceCode.PREMIUM_MEMBERSHIP);
    assertThat(saved.getListRole()).isEqualTo(ListRole.USER_ISSUE_REPORT);
    assertThat(saved.getPermittedActions()).containsExactly(ListAction.REASSIGN);
    assertThat(saved.getSavedList()).isEqualTo(createdList);
  }

  @Test
  @DisplayName("skips creation when a list for that role already exists")
  void skipsCreationWhenRoleAlreadyHasAList() {
    given(linkedInService.provider()).willReturn(ServiceProvider.LINKEDIN);
    given(linkedInService.serviceCode()).willReturn(ServiceCode.PREMIUM_MEMBERSHIP);
    given(linkedInService.serviceListSpecs()).willReturn(List.of(
        new ServiceListSpec("LinkedIn Issue Reports", ListRole.USER_ISSUE_REPORT, Set.of(ListAction.REASSIGN))
    ));
    given(serviceListRepository.existsByProviderAndServiceCodeAndRole(
        ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP, ListRole.USER_ISSUE_REPORT))
        .willReturn(true);

    buildSetupService(linkedInService).setupAllServiceLists();

    verify(savedListService, never()).createSavedList(any(User.class), any());
    verify(serviceListRepository, never()).save(any());
  }

  @Test
  @DisplayName("creates list when no list with that name exists yet")
  void createsListWhenNoListWithSameNameExists() {
    given(linkedInService.provider()).willReturn(ServiceProvider.LINKEDIN);
    given(linkedInService.serviceCode()).willReturn(ServiceCode.PREMIUM_MEMBERSHIP);
    given(linkedInService.serviceListSpecs()).willReturn(List.of(
        new ServiceListSpec("LinkedIn Eligible - APAC", ListRole.SERVICE_ELIGIBILITY, Set.of())
    ));
    given(userService.getSystemAdminUser()).willReturn(systemAdmin);
    given(serviceListRepository.existsByProviderAndServiceCodeAndRoleAndName(
        ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP,
        ListRole.SERVICE_ELIGIBILITY, "LinkedIn Eligible - APAC"))
        .willReturn(false);
    given(savedListService.createSavedList(eq(systemAdmin), any(UpdateSavedListInfoRequest.class)))
        .willReturn(createdList);

    buildSetupService(linkedInService).setupAllServiceLists();

    verify(savedListService).createSavedList(eq(systemAdmin), any(UpdateSavedListInfoRequest.class));
    verify(serviceListRepository).save(any(ServiceListEntity.class));
  }

  @Test
  @DisplayName("skips creation when a list with the same name already exists")
  void skipsCreationWhenListWithSameNameExists() {
    given(linkedInService.provider()).willReturn(ServiceProvider.LINKEDIN);
    given(linkedInService.serviceCode()).willReturn(ServiceCode.PREMIUM_MEMBERSHIP);
    given(linkedInService.serviceListSpecs()).willReturn(List.of(
        new ServiceListSpec("LinkedIn Eligible - APAC", ListRole.SERVICE_ELIGIBILITY, Set.of())
    ));
    given(serviceListRepository.existsByProviderAndServiceCodeAndRoleAndName(
        ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP,
        ListRole.SERVICE_ELIGIBILITY, "LinkedIn Eligible - APAC"))
        .willReturn(true);

    buildSetupService(linkedInService).setupAllServiceLists();

    verify(savedListService, never()).createSavedList(any(User.class), any());
    verify(serviceListRepository, never()).save(any());
  }

  @Test
  @DisplayName("creates a separate list for each distinct name declared")
  void createsSeparateListsForDistinctNames() {
    given(linkedInService.provider()).willReturn(ServiceProvider.LINKEDIN);
    given(linkedInService.serviceCode()).willReturn(ServiceCode.PREMIUM_MEMBERSHIP);
    given(linkedInService.serviceListSpecs()).willReturn(List.of(
        new ServiceListSpec("LinkedIn Eligible - APAC", ListRole.SERVICE_ELIGIBILITY, Set.of()),
        new ServiceListSpec("LinkedIn Eligible - EMEA", ListRole.SERVICE_ELIGIBILITY, Set.of())
    ));
    given(userService.getSystemAdminUser()).willReturn(systemAdmin);
    given(serviceListRepository.existsByProviderAndServiceCodeAndRoleAndName(any(), any(), any(), any()))
        .willReturn(false);
    given(savedListService.createSavedList(eq(systemAdmin), any(UpdateSavedListInfoRequest.class)))
        .willReturn(createdList);

    buildSetupService(linkedInService).setupAllServiceLists();

    verify(savedListService, times(2)).createSavedList(eq(systemAdmin), any(UpdateSavedListInfoRequest.class));
    verify(serviceListRepository, times(2)).save(any(ServiceListEntity.class));
  }

  @Test
  @DisplayName("setupAllServiceLists swallows exception and continues with remaining services")
  void continuesWhenOneServiceFails() {
    // linkedInService declares two specs for the same role — causes IllegalStateException.
    // providerKey() is called in the catch block when logging the failure.
    given(linkedInService.providerKey()).willReturn("LINKEDIN::PREMIUM_MEMBERSHIP");
    given(linkedInService.serviceListSpecs()).willReturn(List.of(
        new ServiceListSpec("List A", ListRole.USER_ISSUE_REPORT, Set.of()),
        new ServiceListSpec("List B", ListRole.USER_ISSUE_REPORT, Set.of())
    ));
    given(duolingoService.provider()).willReturn(ServiceProvider.DUOLINGO);
    given(duolingoService.serviceCode()).willReturn(ServiceCode.TEST_PROCTORED);
    given(duolingoService.serviceListSpecs()).willReturn(List.of(
        new ServiceListSpec("Duolingo Issue Reports", ListRole.USER_ISSUE_REPORT, Set.of())
    ));
    given(userService.getSystemAdminUser()).willReturn(systemAdmin);
    given(serviceListRepository.existsByProviderAndServiceCodeAndRole(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ListRole.USER_ISSUE_REPORT))
        .willReturn(false);
    given(savedListService.createSavedList(eq(systemAdmin), any(UpdateSavedListInfoRequest.class)))
        .willReturn(createdList);

    // Must not throw — exception for linkedInService is swallowed
    buildSetupService(linkedInService, duolingoService).setupAllServiceLists();

    // Duolingo list is still created despite LinkedIn failing
    verify(serviceListRepository).save(any(ServiceListEntity.class));
  }

  @Test
  @DisplayName("setupListsFor(providerKey) throws when service declares more lists than the role permits")
  void throwsWhenServiceDeclaresMoreListsThanRoleAllows() {
    given(linkedInService.providerKey()).willReturn("LINKEDIN::PREMIUM_MEMBERSHIP");
    given(linkedInService.serviceListSpecs()).willReturn(List.of(
        new ServiceListSpec("List A", ListRole.USER_ISSUE_REPORT, Set.of()),
        new ServiceListSpec("List B", ListRole.USER_ISSUE_REPORT, Set.of())
    ));

    assertThatThrownBy(() ->
        buildSetupService(linkedInService).setupListsFor("LINKEDIN::PREMIUM_MEMBERSHIP"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("USER_ISSUE_REPORT");
  }

  @Test
  @DisplayName("setupListsFor(providerKey) delegates to the matching service")
  void setupListsForProviderKey_delegatesToMatchingService() {
    given(linkedInService.providerKey()).willReturn("LINKEDIN::PREMIUM_MEMBERSHIP");
    given(linkedInService.provider()).willReturn(ServiceProvider.LINKEDIN);
    given(linkedInService.serviceCode()).willReturn(ServiceCode.PREMIUM_MEMBERSHIP);
    given(linkedInService.serviceListSpecs()).willReturn(List.of(
        new ServiceListSpec("LinkedIn Issue Reports", ListRole.USER_ISSUE_REPORT, Set.of(ListAction.REASSIGN))
    ));
    given(userService.getSystemAdminUser()).willReturn(systemAdmin);
    given(serviceListRepository.existsByProviderAndServiceCodeAndRole(
        ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP, ListRole.USER_ISSUE_REPORT))
        .willReturn(false);
    given(savedListService.createSavedList(eq(systemAdmin), any(UpdateSavedListInfoRequest.class)))
        .willReturn(createdList);

    buildSetupService(linkedInService, duolingoService).setupListsFor("LINKEDIN::PREMIUM_MEMBERSHIP");

    // Only LinkedIn's list is created; Duolingo is not touched
    verify(serviceListRepository).save(any(ServiceListEntity.class));
    verify(duolingoService, never()).serviceListSpecs();
  }

  @Test
  @DisplayName("setupListsFor(providerKey) is a no-op for an unknown key")
  void setupListsForProviderKey_unknownKey_isNoOp() {
    given(linkedInService.providerKey()).willReturn("LINKEDIN::PREMIUM_MEMBERSHIP");
    given(duolingoService.providerKey()).willReturn("DUOLINGO::TEST_PROCTORED");

    buildSetupService(linkedInService, duolingoService).setupListsFor("UNKNOWN::UNKNOWN");

    verify(serviceListRepository, never()).existsByProviderAndServiceCodeAndRole(any(), any(), any());
    verify(savedListService, never()).createSavedList(any(User.class), any());
    verify(serviceListRepository, never()).save(any());
  }
}
