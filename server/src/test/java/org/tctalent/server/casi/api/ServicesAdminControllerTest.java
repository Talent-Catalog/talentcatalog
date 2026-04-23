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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.api.admin.ApiTestBase;
import org.tctalent.server.casi.api.request.ServiceListActionRequest;
import org.tctalent.server.casi.api.request.UpdateServiceResourceStatusRequest;
import org.tctalent.server.casi.core.services.CandidateAssistanceService;
import org.tctalent.server.casi.core.services.CandidateServiceRegistry;
import org.tctalent.server.casi.core.services.CandidateServicesQueryService;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ListAction;
import org.tctalent.server.casi.domain.model.ListRole;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceListEntity;
import org.tctalent.server.casi.domain.persistence.ServiceListRepository;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.security.AuthService;

@WebMvcTest(ServicesAdminController.class)
@AutoConfigureMockMvc
class ServicesAdminControllerTest extends ApiTestBase {

  private static final String BASE_PATH = "/api/admin/services";
  private static final String PROVIDER = "DUOLINGO";
  private static final String SERVICE_CODE = "TEST_PROCTORED";
  private static final Long CANDIDATE_ID = 123L;
  private static final Long LIST_ID = 456L;
  private static final Long SAVED_LIST_ID = 789L;
  private static final Long SERVICE_LIST_ID = 99L;
  private static final String RESOURCE_CODE = "COUPON123";
  private static final String CANDIDATE_NUMBER = "12345";

  @MockBean private AuthService authService;
  @MockBean private CandidateServiceRegistry candidateServiceRegistry;
  @MockBean private CandidateServicesQueryService queryService;
  @MockBean private CandidateAssistanceService candidateAssistanceService;
  @MockBean private ServiceListRepository serviceListRepository;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ServicesAdminController controller;

  private User testUser;
  private ServiceResource testResource;
  private ServiceAssignment testAssignment;

    @BeforeEach
  void setUp() {
    configureAuthentication();
    testUser = user;

    testResource = ServiceResource.builder()
        .id(1L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resourceCode(RESOURCE_CODE)
        .status(ResourceStatus.AVAILABLE)
        .expiresAt(OffsetDateTime.now().plusDays(30))
        .build();

    testAssignment = ServiceAssignment.builder()
        .id(1L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resource(testResource)
        .candidateId(CANDIDATE_ID)
        .actorId(testUser.getId())
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now())
        .build();

    SavedList savedList = new SavedList();
    savedList.setId(SAVED_LIST_ID);

    ServiceListEntity testServiceList = new ServiceListEntity();
    testServiceList.setId(SERVICE_LIST_ID);
    testServiceList.setProvider(ServiceProvider.LINKEDIN);
    testServiceList.setServiceCode(ServiceCode.PREMIUM_MEMBERSHIP);
    testServiceList.setListRole(ListRole.USER_ISSUE_REPORT);
    testServiceList.setPermittedActions(Set.of(ListAction.REASSIGN));
    testServiceList.setSavedList(savedList);

    given(authService.getLoggedInUser()).willReturn(Optional.of(testUser));
    given(candidateServiceRegistry.forProviderAndServiceCode(PROVIDER, SERVICE_CODE))
        .willReturn(candidateAssistanceService);
    given(candidateServiceRegistry.forProviderAndServiceCode(
        ServiceProvider.LINKEDIN.name(), ServiceCode.PREMIUM_MEMBERSHIP.name()))
        .willReturn(candidateAssistanceService);
    given(serviceListRepository.findBySavedListId(SAVED_LIST_ID))
        .willReturn(Optional.of(testServiceList));
    given(serviceListRepository.findById(SERVICE_LIST_ID))
        .willReturn(Optional.of(testServiceList));
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(controller).isNotNull();
  }

  @Test
  @DisplayName("import inventory succeeds")
  void importInventorySucceeds() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", "test content".getBytes()
    );

    mockMvc.perform(multipart(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/import")
            .file(file)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.message", is("Service resources imported successfully.")));

    verify(candidateAssistanceService).importInventory(any());
  }

  @Test
  @DisplayName("import inventory fails with exception")
  void importInventoryFails() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", "test content".getBytes()
    );

    doThrow(new ImportFailedException("Invalid file format"))
        .when(candidateAssistanceService).importInventory(any());

    mockMvc.perform(multipart(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/import")
            .file(file)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("failure")))
        .andExpect(jsonPath("$.message", is("Failed to import service resources from file.")));
  }

  @Test
  @DisplayName("import inventory fails when invalid provider/service code")
  void importInventoryFailsWithInvalidProviderServiceCode() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", "test content".getBytes()
    );

    given(candidateServiceRegistry.forProviderAndServiceCode("INVALID", "INVALID"))
        .willThrow(new NoSuchObjectException("Unknown candidate service for provider: INVALID, serviceCode: INVALID"));

    mockMvc.perform(multipart(BASE_PATH + "/INVALID/INVALID/import")
            .file(file)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")))
        .andExpect(jsonPath("$.message", containsString("Unknown candidate service")));
  }

  @Test
  @DisplayName("assign to candidate succeeds")
  void assignToCandidateSucceeds() throws Exception {
    given(candidateAssistanceService.assignToCandidate(CANDIDATE_ID, testUser))
        .willReturn(testAssignment);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/candidate/" + CANDIDATE_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.candidateId", is(CANDIDATE_ID.intValue())))
        .andExpect(jsonPath("$.status", is("ASSIGNED")));

    verify(candidateAssistanceService).assignToCandidate(CANDIDATE_ID, testUser);
  }

  @Test
  @DisplayName("assign to candidate fails when not logged in")
  void assignToCandidateFailsWhenNotLoggedIn() throws Exception {
    given(authService.getLoggedInUser()).willReturn(Optional.empty());

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/candidate/" + CANDIDATE_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code", is("invalid_session")))
        .andExpect(jsonPath("$.message", is("Not logged in")));

    verify(candidateAssistanceService, never()).assignToCandidate(anyLong(), any());
  }

  @Test
  @DisplayName("assign to candidate fails with duplicate assignment")
  void assignToCandidateFailsWithDuplicateAssignment() throws Exception {
    doThrow(new EntityExistsException("Service", "already assigned"))
        .when(candidateAssistanceService).assignToCandidate(CANDIDATE_ID, testUser);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/candidate/" + CANDIDATE_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("assign to candidate fails when candidate not found")
  void assignToCandidateFailsWhenCandidateNotFound() throws Exception {
    doThrow(new NoSuchObjectException("Candidate with ID " + CANDIDATE_ID + " not found"))
        .when(candidateAssistanceService).assignToCandidate(CANDIDATE_ID, testUser);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/candidate/" + CANDIDATE_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("assign to candidate fails when service throws exception")
  void assignToCandidateFailsWithServiceException() throws Exception {
    doThrow(new RuntimeException("Database error"))
        .when(candidateAssistanceService).assignToCandidate(CANDIDATE_ID, testUser);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/candidate/" + CANDIDATE_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("assign to candidate fails when invalid provider/service code")
  void assignToCandidateFailsWithInvalidProviderServiceCode() throws Exception {
    given(candidateServiceRegistry.forProviderAndServiceCode("INVALID", "INVALID"))
        .willThrow(new NoSuchObjectException("Unknown candidate service for provider: INVALID, serviceCode: INVALID"));

    mockMvc.perform(post(BASE_PATH + "/INVALID/INVALID/assign/candidate/" + CANDIDATE_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")))
        .andExpect(jsonPath("$.message", containsString("Unknown candidate service")));
  }

  @Test
  @DisplayName("assign to list succeeds")
  void assignToListSucceeds() throws Exception {
    List<ServiceAssignment> assignments = List.of(testAssignment);
    given(candidateAssistanceService.assignToList(LIST_ID, testUser))
        .willReturn(assignments);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/list/" + LIST_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id", is(1)));

    verify(candidateAssistanceService).assignToList(LIST_ID, testUser);
  }

  @Test
  @DisplayName("assign to list fails with insufficient resources")
  void assignToListFailsWithInsufficientResources() throws Exception {
    doThrow(new NoSuchObjectException("Not enough resources"))
        .when(candidateAssistanceService).assignToList(LIST_ID, testUser);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/list/" + LIST_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("assign to list fails when list not found")
  void assignToListFailsWhenListNotFound() throws Exception {
    doThrow(new NoSuchObjectException(SavedList.class, LIST_ID))
        .when(candidateAssistanceService).assignToList(LIST_ID, testUser);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/list/" + LIST_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("assign to list succeeds with partial assignments")
  void assignToListSucceedsWithPartialAssignments() throws Exception {
    // Create a second assignment for a different candidate
    ServiceAssignment assignment2 = ServiceAssignment.builder()
        .id(2L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resource(testResource)
        .candidateId(789L) // Different candidate
        .actorId(testUser.getId())
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now())
        .build();

    // simulate partial success: only 2 assignments returned (some candidates already had assignments)
    List<ServiceAssignment> partialAssignments = List.of(testAssignment, assignment2);
    given(candidateAssistanceService.assignToList(LIST_ID, testUser))
        .willReturn(partialAssignments);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/list/" + LIST_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()", is(2))) // Only 2 assignments (partial success)
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[1].id", is(2)));

    verify(candidateAssistanceService).assignToList(LIST_ID, testUser);
  }

  @Test
  @DisplayName("assign to list fails when service throws exception")
  void assignToListFailsWithServiceException() throws Exception {
    doThrow(new RuntimeException("Database error"))
        .when(candidateAssistanceService).assignToList(LIST_ID, testUser);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/assign/list/" + LIST_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("assign to list fails when invalid provider/service code")
  void assignToListFailsWithInvalidProviderServiceCode() throws Exception {
    given(candidateServiceRegistry.forProviderAndServiceCode("INVALID", "INVALID"))
        .willThrow(new NoSuchObjectException("Unknown candidate service for provider: INVALID, serviceCode: INVALID"));

    mockMvc.perform(post(BASE_PATH + "/INVALID/INVALID/assign/list/" + LIST_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")))
        .andExpect(jsonPath("$.message", containsString("Unknown candidate service")));
  }

  @Test
  @DisplayName("get assignments for candidate succeeds")
  void getAssignmentsForCandidateSucceeds() throws Exception {
    List<ServiceAssignment> assignments = List.of(testAssignment);
    given(queryService.listForCandidate(CANDIDATE_ID))
        .willReturn(assignments);

    mockMvc.perform(get(BASE_PATH + "/assignments/candidate/" + CANDIDATE_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id", is(1)));

    verify(queryService).listForCandidate(CANDIDATE_ID);
  }

  @Test
  @DisplayName("get assignments for candidate returns empty list when no assignments")
  void getAssignmentsForCandidateReturnsEmptyList() throws Exception {
    given(queryService.listForCandidate(CANDIDATE_ID))
        .willReturn(Collections.emptyList());

    mockMvc.perform(get(BASE_PATH + "/assignments/candidate/" + CANDIDATE_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(queryService).listForCandidate(CANDIDATE_ID);
  }

  @Test
  @DisplayName("get assignments for candidate fails when query service throws exception")
  void getAssignmentsForCandidateFailsWithException() throws Exception {
    doThrow(new RuntimeException("Database error"))
        .when(queryService).listForCandidate(CANDIDATE_ID);

    mockMvc.perform(get(BASE_PATH + "/assignments/candidate/" + CANDIDATE_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("list available resources succeeds")
  void listAvailableResourcesSucceeds() throws Exception {
    List<ServiceResource> resources = List.of(testResource);
    given(candidateAssistanceService.getAvailableResources())
        .willReturn(resources);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/available")
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].resourceCode", is(RESOURCE_CODE)));

    verify(candidateAssistanceService).getAvailableResources();
  }

  @Test
  @DisplayName("list available resources returns empty list")
  void listAvailableResourcesReturnsEmpty() throws Exception {
    given(candidateAssistanceService.getAvailableResources())
        .willReturn(Collections.emptyList());

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/available")
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @DisplayName("list available resources fails when service throws exception")
  void listAvailableResourcesFailsWithException() throws Exception {
    doThrow(new RuntimeException("Database error"))
        .when(candidateAssistanceService).getAvailableResources();

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/available")
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("list available resources fails when invalid provider/service code")
  void listAvailableResourcesFailsWithInvalidProviderServiceCode() throws Exception {
    given(candidateServiceRegistry.forProviderAndServiceCode("INVALID", "INVALID"))
        .willThrow(new NoSuchObjectException("Unknown candidate service for provider: INVALID, serviceCode: INVALID"));

    mockMvc.perform(get(BASE_PATH + "/INVALID/INVALID/available")
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")))
        .andExpect(jsonPath("$.message", containsString("Unknown candidate service")));
  }

  @Test
  @DisplayName("get resource by code succeeds")
  void getResourceByCodeSucceeds() throws Exception {
    given(candidateAssistanceService.getResourceForResourceCode(RESOURCE_CODE))
        .willReturn(testResource);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/resource/" + RESOURCE_CODE)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.resourceCode", is(RESOURCE_CODE)));

    verify(candidateAssistanceService).getResourceForResourceCode(RESOURCE_CODE);
  }

  @Test
  @DisplayName("get resource by code fails when not found")
  void getResourceByCodeFailsWhenNotFound() throws Exception {
    doThrow(new NoSuchObjectException("Resource not found"))
        .when(candidateAssistanceService).getResourceForResourceCode(RESOURCE_CODE);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/resource/" + RESOURCE_CODE)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("get resource by code fails when service throws exception")
  void getResourceByCodeFailsWithException() throws Exception {
    doThrow(new RuntimeException("Database error"))
        .when(candidateAssistanceService).getResourceForResourceCode(RESOURCE_CODE);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/resource/" + RESOURCE_CODE)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("get resource by code fails when invalid provider/service code")
  void getResourceByCodeFailsWithInvalidProviderServiceCode() throws Exception {
    given(candidateServiceRegistry.forProviderAndServiceCode("INVALID", "INVALID"))
        .willThrow(new NoSuchObjectException("Unknown candidate service for provider: INVALID, serviceCode: INVALID"));

    mockMvc.perform(get(BASE_PATH + "/INVALID/INVALID/resource/" + RESOURCE_CODE)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")))
        .andExpect(jsonPath("$.message", containsString("Unknown candidate service")));
  }

  @Test
  @DisplayName("update resource status succeeds")
  void updateResourceStatusSucceeds() throws Exception {
    UpdateServiceResourceStatusRequest request = new UpdateServiceResourceStatusRequest();
    request.setResourceCode(RESOURCE_CODE);
    request.setStatus(ResourceStatus.DISABLED);

    mockMvc.perform(put(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/resources/status")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isOk());

    verify(candidateAssistanceService).updateResourceStatus(RESOURCE_CODE, ResourceStatus.DISABLED);
  }

  @Test
  @DisplayName("update resource status fails when resource not found")
  void updateResourceStatusFailsWhenResourceNotFound() throws Exception {
    UpdateServiceResourceStatusRequest request = new UpdateServiceResourceStatusRequest();
    request.setResourceCode(RESOURCE_CODE);
    request.setStatus(ResourceStatus.DISABLED);

    doThrow(new NoSuchObjectException("Resource with code " + RESOURCE_CODE + " not found"))
        .when(candidateAssistanceService).updateResourceStatus(RESOURCE_CODE, ResourceStatus.DISABLED);

    mockMvc.perform(put(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/resources/status")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")))
        .andExpect(jsonPath("$.message", is("Resource with code " + RESOURCE_CODE + " not found")));
  }

  @Test
  @DisplayName("update resource status fails when service throws exception")
  void updateResourceStatusFailsWithException() throws Exception {
    UpdateServiceResourceStatusRequest request = new UpdateServiceResourceStatusRequest();
    request.setResourceCode(RESOURCE_CODE);
    request.setStatus(ResourceStatus.DISABLED);

    doThrow(new RuntimeException("Database error"))
        .when(candidateAssistanceService).updateResourceStatus(RESOURCE_CODE, ResourceStatus.DISABLED);

    mockMvc.perform(put(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/resources/status")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("update resource status fails when invalid provider/service code")
  void updateResourceStatusFailsWithInvalidProviderServiceCode() throws Exception {
    UpdateServiceResourceStatusRequest request = new UpdateServiceResourceStatusRequest();
    request.setResourceCode(RESOURCE_CODE);
    request.setStatus(ResourceStatus.DISABLED);

    given(candidateServiceRegistry.forProviderAndServiceCode("INVALID", "INVALID"))
        .willThrow(new NoSuchObjectException("Unknown candidate service for provider: INVALID, serviceCode: INVALID"));

    mockMvc.perform(put(BASE_PATH + "/INVALID/INVALID/resources/status")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")))
        .andExpect(jsonPath("$.message", containsString("Unknown candidate service")));
  }

  @Test
  @DisplayName("count available resources succeeds")
  void countAvailableResourcesSucceeds() throws Exception {
    given(candidateAssistanceService.countAvailableForProviderAndService())
        .willReturn(5L);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/available/count")
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.count", is(5)));

    verify(candidateAssistanceService).countAvailableForProviderAndService();
  }

  @Test
  @DisplayName("count available resources fails with service exception")
  void countAvailableResourcesFailsWithServiceException() throws Exception {
    doThrow(new ServiceException("count_failed", "Failed to count available resources"))
        .when(candidateAssistanceService).countAvailableForProviderAndService();

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/available/count")
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("count_failed")))
        .andExpect(jsonPath("$.message", is("Failed to count available resources")));
  }

  @Test
  @DisplayName("count available resources fails when invalid provider/service code")
  void countAvailableResourcesFailsWithInvalidProviderServiceCode() throws Exception {
    given(candidateServiceRegistry.forProviderAndServiceCode("INVALID", "INVALID"))
        .willThrow(new NoSuchObjectException("Unknown candidate service for provider: INVALID, serviceCode: INVALID"));

    mockMvc.perform(get(BASE_PATH + "/INVALID/INVALID/available/count")
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")))
        .andExpect(jsonPath("$.message", containsString("Unknown candidate service")));
  }

  @Test
  @DisplayName("list provider resources for candidate succeeds")
  void listProviderResourcesForCandidateSucceeds() throws Exception {
    List<ServiceResource> resources = List.of(testResource);
    given(candidateAssistanceService.getResourcesForCandidate(CANDIDATE_ID))
        .willReturn(resources);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/resources/candidate/" + CANDIDATE_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].resourceCode", is(RESOURCE_CODE)));

    verify(candidateAssistanceService).getResourcesForCandidate(CANDIDATE_ID);
  }

  @Test
  @DisplayName("list provider resources for candidate returns empty list")
  void listProviderResourcesForCandidateReturnsEmptyList() throws Exception {
    given(candidateAssistanceService.getResourcesForCandidate(CANDIDATE_ID))
        .willReturn(Collections.emptyList());

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/resources/candidate/" + CANDIDATE_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(candidateAssistanceService).getResourcesForCandidate(CANDIDATE_ID);
  }

  @Test
  @DisplayName("list provider resources for candidate fails with service exception")
  void listProviderResourcesForCandidateFailsWithServiceException() throws Exception {
    doThrow(new RuntimeException("Database error"))
        .when(candidateAssistanceService).getResourcesForCandidate(CANDIDATE_ID);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE
            + "/resources/candidate/" + CANDIDATE_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("list provider resources for candidate fails with invalid provider/service code")
  void listProviderResourcesForCandidateFailsWithInvalidProviderServiceCode() throws Exception {
    given(candidateServiceRegistry.forProviderAndServiceCode("INVALID", "INVALID"))
        .willThrow(new NoSuchObjectException("Unknown candidate service for provider: INVALID, serviceCode: INVALID"));

    mockMvc.perform(get(BASE_PATH + "/INVALID/INVALID/resources/candidate/" + CANDIDATE_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")))
        .andExpect(jsonPath("$.message", containsString("Unknown candidate service")));
  }

  @Test
  @DisplayName("get service list returns DTO for known saved list")
  void getServiceListSucceeds() throws Exception {
    mockMvc.perform(get(BASE_PATH + "/list/" + SAVED_LIST_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(SERVICE_LIST_ID.intValue())))
        .andExpect(jsonPath("$.provider", is("LINKEDIN")))
        .andExpect(jsonPath("$.serviceCode", is("PREMIUM_MEMBERSHIP")))
        .andExpect(jsonPath("$.listRole", is("USER_ISSUE_REPORT")))
        .andExpect(jsonPath("$.permittedActions[0]", is("REASSIGN")));
  }

  @Test
  @DisplayName("get service list returns 404 when saved list has no service list")
  void getServiceListReturns404WhenNotFound() throws Exception {
    given(serviceListRepository.findBySavedListId(SAVED_LIST_ID))
        .willReturn(Optional.empty());

    mockMvc.perform(get(BASE_PATH + "/list/" + SAVED_LIST_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")));
  }

  @Test
  @DisplayName("perform service list action succeeds for REASSIGN")
  void performServiceListActionSucceeds() throws Exception {
    ServiceListActionRequest request = new ServiceListActionRequest();
    request.setAction(ListAction.REASSIGN);
    request.setCandidateNumbers(List.of(CANDIDATE_NUMBER));

    mockMvc.perform(post(BASE_PATH + "/list/" + SERVICE_LIST_ID + "/action")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isOk());

    verify(candidateAssistanceService).reassignForCandidate(CANDIDATE_NUMBER, testUser);
  }

  @Test
  @DisplayName("perform service list action returns 404 when service list not found")
  void performServiceListActionReturns404WhenServiceListNotFound() throws Exception {
    given(serviceListRepository.findById(SERVICE_LIST_ID))
        .willReturn(Optional.empty());

    ServiceListActionRequest request = new ServiceListActionRequest();
    request.setAction(ListAction.REASSIGN);
    request.setCandidateNumbers(List.of(CANDIDATE_NUMBER));

    mockMvc.perform(post(BASE_PATH + "/list/" + SERVICE_LIST_ID + "/action")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("missing_object")));
  }

  @Test
  @DisplayName("perform service list action returns 401 when not logged in")
  void performServiceListActionFailsWhenNotLoggedIn() throws Exception {
    given(authService.getLoggedInUser()).willReturn(Optional.empty());

    ServiceListActionRequest request = new ServiceListActionRequest();
    request.setAction(ListAction.REASSIGN);
    request.setCandidateNumbers(List.of(CANDIDATE_NUMBER));

    mockMvc.perform(post(BASE_PATH + "/list/" + SERVICE_LIST_ID + "/action")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code", is("invalid_session")));

    verify(candidateAssistanceService, never()).reassignForCandidate(any(), any());
  }

  @Test
  @DisplayName("perform service list action returns 400 when candidate numbers are empty")
  void performServiceListActionFailsWithEmptyCandidateNumbers() throws Exception {
    ServiceListActionRequest request = new ServiceListActionRequest();
    request.setAction(ListAction.REASSIGN);
    request.setCandidateNumbers(List.of());

    mockMvc.perform(post(BASE_PATH + "/list/" + SERVICE_LIST_ID + "/action")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("perform service list action calls reassign for each candidate number")
  void performServiceListActionCallsReassignForEachCandidate() throws Exception {
    ServiceListActionRequest request = new ServiceListActionRequest();
    request.setAction(ListAction.REASSIGN);
    request.setCandidateNumbers(List.of("11111", "22222", "33333"));

    mockMvc.perform(post(BASE_PATH + "/list/" + SERVICE_LIST_ID + "/action")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isOk());

    verify(candidateAssistanceService).reassignForCandidate("11111", testUser);
    verify(candidateAssistanceService).reassignForCandidate("22222", testUser);
    verify(candidateAssistanceService).reassignForCandidate("33333", testUser);
  }

  @Test
  @DisplayName("case insensitive provider and service code lookup")
  void caseInsensitiveProviderServiceCodeLookup() throws Exception {
    List<ServiceResource> resources = List.of(testResource);
    given(candidateAssistanceService.getAvailableResources())
        .willReturn(resources);
    
    // Mock the registry call with lowercase values (registry normalizes internally)
    given(candidateServiceRegistry.forProviderAndServiceCode("duolingo", "test_proctored"))
        .willReturn(candidateAssistanceService);

    // Test with lowercase
    mockMvc.perform(get(BASE_PATH + "/duolingo/test_proctored/available")
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk());

    verify(candidateServiceRegistry).forProviderAndServiceCode("duolingo", "test_proctored");
  }
}

