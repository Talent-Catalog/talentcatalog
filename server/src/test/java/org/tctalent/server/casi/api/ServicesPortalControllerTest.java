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

package org.tctalent.server.casi.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.api.admin.ApiTestBase;
import org.tctalent.server.casi.api.request.UpdateServiceResourceStatusRequest;
import org.tctalent.server.casi.application.policy.EligibilityPolicyRegistry;
import org.tctalent.server.casi.core.services.CandidateAssistanceService;
import org.tctalent.server.casi.core.services.CandidateServiceRegistry;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.CounterpartyType;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.AgreementService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CounterpartyService;
import org.tctalent.server.service.db.TermsInfoService;
import org.tctalent.server.service.db.UserService;

@WebMvcTest(ServicesPortalController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServicesPortalControllerTest extends ApiTestBase {

  private static final String BASE_PATH = "/api/portal/services";
  private static final String PROVIDER = "LINKEDIN";
  private static final String SERVICE_CODE = "PREMIUM_MEMBERSHIP";
  private static final Long CANDIDATE_ID = 123L;
  private static final String RESOURCE_CODE = "CODE-123";
  private static final String TERMS_ID = "ReferenceServiceTermsV1";
  private static final String OPC_DPA_TERMS_ID = "OpcDataProcessingAgreementV1";

  @MockitoBean private AuthService authService;
  @MockitoBean private UserService userService;
  @MockitoBean private CandidateServiceRegistry candidateServiceRegistry;
  @MockitoBean private EligibilityPolicyRegistry eligibilityPolicyRegistry;
  @MockitoBean private CandidateAssistanceService candidateAssistanceService;
  @MockitoBean private AgreementService agreementService;
  @MockitoBean private CounterpartyService counterpartyService;
  @MockitoBean private CandidateService candidateService;
  @MockitoBean private TermsInfoService termsInfoService;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  private Candidate candidate;
  private Counterparty counterparty;
  private TermsInfo termsInfo;
  private TermsInfo opcDpaTermsInfo;

  @BeforeEach
  void setUp() {
    configureAuthentication();
    candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);
    counterparty = new Counterparty();
    counterparty.setId(99L);
    counterparty.setType(CounterpartyType.SERVICE_PROVIDER);
    counterparty.setServiceProvider(ServiceProvider.LINKEDIN);
    termsInfo = new TermsInfo(TERMS_ID, "/terms/ReferenceServiceTermsV1.html",
        TermsType.REFERENCE_SERVICE_TERMS, null);
    termsInfo.setContent("<p>Terms</p>");
    opcDpaTermsInfo = new TermsInfo(OPC_DPA_TERMS_ID, "/terms/OpcDataProcessingAgreement-20250831.html",
        TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT, null);
    opcDpaTermsInfo.setContent("<p>OPC DPA</p>");

    given(authService.getLoggedInCandidateId()).willReturn(CANDIDATE_ID);
    given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate));
    given(candidateServiceRegistry.forProviderAndServiceCode(PROVIDER, SERVICE_CODE))
        .willReturn(candidateAssistanceService);
    given(candidateAssistanceService.agreementTermsType()).willReturn(Optional.empty());
    given(candidateAssistanceService.opcDpaAcceptedTermsInfoId()).willReturn(Optional.empty());
  }

  @Test
  @DisplayName("eligibility endpoint delegates to eligibility policy registry")
  void eligibilityEndpointDelegatesToRegistry() throws Exception {
    given(eligibilityPolicyRegistry.isEligible(ServiceProvider.LINKEDIN, CANDIDATE_ID))
        .willReturn(true);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/eligibility")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(true)));
  }

  @Test
  @DisplayName("assignment endpoint prefers reserved assignment over redeemed")
  void assignmentEndpointPrefersReservedOverRedeemed() throws Exception {
    ServiceResource redeemedResource = ServiceResource.builder()
        .id(1L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resourceCode("REDEEMED")
        .status(ResourceStatus.REDEEMED)
        .expiresAt(OffsetDateTime.now().plusDays(30))
        .build();

    ServiceResource reservedResource = ServiceResource.builder()
        .id(2L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resourceCode("RESERVED")
        .status(ResourceStatus.RESERVED)
        .expiresAt(OffsetDateTime.now().plusDays(30))
        .build();

    ServiceAssignment redeemed = ServiceAssignment.builder()
        .id(11L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resource(redeemedResource)
        .candidateId(CANDIDATE_ID)
        .status(AssignmentStatus.REDEEMED)
        .assignedAt(OffsetDateTime.now().minusDays(2))
        .build();

    ServiceAssignment reserved = ServiceAssignment.builder()
        .id(12L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resource(reservedResource)
        .candidateId(CANDIDATE_ID)
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now().minusDays(1))
        .build();

    given(candidateAssistanceService.getCurrentAssignment(CANDIDATE_ID))
        .willReturn(reserved);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/assignment")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(12)))
        .andExpect(jsonPath("$.resource.resourceCode", is("RESERVED")));

    verify(candidateAssistanceService).getCurrentAssignment(CANDIDATE_ID);
  }

  @Test
  @DisplayName("assign endpoint fails when candidate is ineligible")
  void assignFailsWhenCandidateIsIneligible() throws Exception {
    given(eligibilityPolicyRegistry.isEligible(ServiceProvider.LINKEDIN, CANDIDATE_ID))
        .willReturn(false);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/assign")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("candidate_not_eligible")))
        .andExpect(jsonPath("$.message", containsString("not eligible")));

    verify(candidateAssistanceService, never()).assignToCandidate(anyLong(), ArgumentMatchers.any());
  }

  @Test
  @DisplayName("assign endpoint uses system admin actor when candidate is eligible")
  void assignUsesSystemAdminActorWhenEligible() throws Exception {
    given(eligibilityPolicyRegistry.isEligible(ServiceProvider.LINKEDIN, CANDIDATE_ID))
        .willReturn(true);
    given(userService.getSystemAdminUser()).willReturn(user);

    ServiceResource resource = ServiceResource.builder()
        .id(2L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resourceCode(RESOURCE_CODE)
        .status(ResourceStatus.RESERVED)
        .expiresAt(OffsetDateTime.now().plusDays(30))
        .build();

    ServiceAssignment assignment = ServiceAssignment.builder()
        .id(44L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resource(resource)
        .candidateId(CANDIDATE_ID)
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now())
        .build();

    given(candidateAssistanceService.assignToCandidate(CANDIDATE_ID, user)).willReturn(assignment);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/assign")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(44)))
        .andExpect(jsonPath("$.candidateId", is(CANDIDATE_ID.intValue())));

    verify(candidateAssistanceService).assignToCandidate(CANDIDATE_ID, user);
  }

  @Test
  @DisplayName("get agreement terms returns no content when service has no terms")
  void getAgreementTermsReturnsNoContentWhenNoTerms() throws Exception {
    given(candidateAssistanceService.agreementTermsType()).willReturn(Optional.empty());

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/terms")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("get OPC DPA terms returns no content when service has no OPC DPA configured")
  void getOpcDpaTermsReturnsNoContentWhenNoOpcDpaConfigured() throws Exception {
    given(candidateAssistanceService.opcDpaAcceptedTermsInfoId()).willReturn(Optional.empty());

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/opc-dpa/terms")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("get OPC DPA terms returns configured terms")
  void getOpcDpaTermsReturnsConfiguredTerms() throws Exception {
    given(candidateAssistanceService.opcDpaAcceptedTermsInfoId()).willReturn(Optional.of(OPC_DPA_TERMS_ID));
    given(termsInfoService.get(OPC_DPA_TERMS_ID)).willReturn(opcDpaTermsInfo);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/opc-dpa/terms")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(OPC_DPA_TERMS_ID)))
        .andExpect(jsonPath("$.content", is("<p>OPC DPA</p>")));
  }

  @Test
  @DisplayName("needs OPC DPA acceptance returns false when no OPC DPA configured")
  void needsOpcDpaReturnsFalseWhenNotConfigured() throws Exception {
    given(candidateAssistanceService.opcDpaAcceptedTermsInfoId()).willReturn(Optional.empty());

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/opc-dpa/needs-acceptance")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(false)));
  }

  @Test
  @DisplayName("needs OPC DPA acceptance delegates to agreement service when configured")
  void needsOpcDpaDelegatesToAgreementServiceWhenConfigured() throws Exception {
    given(candidateAssistanceService.opcDpaAcceptedTermsInfoId()).willReturn(Optional.of(OPC_DPA_TERMS_ID));
    given(counterpartyService.findOrCreateByTypeAndServiceProvider(
        CounterpartyType.SERVICE_PROVIDER, ServiceProvider.LINKEDIN)).willReturn(counterparty);
    given(agreementService.needsAcceptance(candidate, counterparty,
        TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT)).willReturn(true);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/opc-dpa/needs-acceptance")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(true)));
  }

  @Test
  @DisplayName("accept OPC DPA records agreement for configured terms")
  void acceptOpcDpaRecordsAgreementForConfiguredTerms() throws Exception {
    given(candidateAssistanceService.opcDpaAcceptedTermsInfoId()).willReturn(Optional.of(OPC_DPA_TERMS_ID));
    given(counterpartyService.findOrCreateByTypeAndServiceProvider(
        CounterpartyType.SERVICE_PROVIDER, ServiceProvider.LINKEDIN)).willReturn(counterparty);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/opc-dpa/accept")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(agreementService).recordAgreement(candidate, counterparty, OPC_DPA_TERMS_ID);
  }

  @Test
  @DisplayName("get agreement terms returns current terms when configured")
  void getAgreementTermsReturnsCurrentTermsWhenConfigured() throws Exception {
    given(candidateAssistanceService.agreementTermsType())
        .willReturn(Optional.of(TermsType.REFERENCE_SERVICE_TERMS));
    given(termsInfoService.getCurrentByType(TermsType.REFERENCE_SERVICE_TERMS)).willReturn(termsInfo);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/terms")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(TERMS_ID)))
        .andExpect(jsonPath("$.content", is("<p>Terms</p>")));
  }

  @Test
  @DisplayName("needs acceptance returns false when service has no terms")
  void needsAcceptanceReturnsFalseWhenNoTerms() throws Exception {
    given(candidateAssistanceService.agreementTermsType()).willReturn(Optional.empty());

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/needs-acceptance")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(false)));
  }

  @Test
  @DisplayName("needs acceptance delegates to agreement service when service has terms")
  void needsAcceptanceDelegatesToAgreementServiceWhenServiceHasTerms() throws Exception {
    given(candidateAssistanceService.agreementTermsType())
        .willReturn(Optional.of(TermsType.REFERENCE_SERVICE_TERMS));
    given(counterpartyService.findOrCreateByTypeAndServiceProvider(
        CounterpartyType.SERVICE_PROVIDER, ServiceProvider.LINKEDIN)).willReturn(counterparty);
    given(agreementService.needsAcceptance(candidate, counterparty, TermsType.REFERENCE_SERVICE_TERMS))
        .willReturn(true);

    mockMvc.perform(get(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/needs-acceptance")
            .header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(true)));
  }

  @Test
  @DisplayName("accept agreement records agreement for current terms")
  void acceptAgreementRecordsAgreementForCurrentTerms() throws Exception {
    given(candidateAssistanceService.agreementTermsType())
        .willReturn(Optional.of(TermsType.REFERENCE_SERVICE_TERMS));
    given(termsInfoService.getCurrentByType(TermsType.REFERENCE_SERVICE_TERMS)).willReturn(termsInfo);
    given(counterpartyService.findOrCreateByTypeAndServiceProvider(
        CounterpartyType.SERVICE_PROVIDER, ServiceProvider.LINKEDIN)).willReturn(counterparty);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/agreement/accept")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(agreementService).recordAgreement(candidate, counterparty, TERMS_ID);
  }

  @Test
  @DisplayName("assign endpoint fails when agreement acceptance is required")
  void assignFailsWhenAgreementAcceptanceIsRequired() throws Exception {
    given(eligibilityPolicyRegistry.isEligible(ServiceProvider.LINKEDIN, CANDIDATE_ID))
        .willReturn(true);
    given(candidateAssistanceService.agreementTermsType())
        .willReturn(Optional.of(TermsType.REFERENCE_SERVICE_TERMS));
    given(counterpartyService.findOrCreateByTypeAndServiceProvider(
        CounterpartyType.SERVICE_PROVIDER, ServiceProvider.LINKEDIN)).willReturn(counterparty);
    given(agreementService.needsAcceptance(candidate, counterparty, TermsType.REFERENCE_SERVICE_TERMS))
        .willReturn(true);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/assign")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("agreement_required")));

    verify(candidateAssistanceService, never()).assignToCandidate(anyLong(), ArgumentMatchers.any());
  }

  @Test
  @DisplayName("assign endpoint fails when OPC DPA acknowledgment is required before provider terms")
  void assignFailsWhenOpcDpaAcknowledgmentIsRequired() throws Exception {
    given(eligibilityPolicyRegistry.isEligible(ServiceProvider.LINKEDIN, CANDIDATE_ID))
        .willReturn(true);
    given(candidateAssistanceService.opcDpaAcceptedTermsInfoId()).willReturn(Optional.of(OPC_DPA_TERMS_ID));
    given(candidateAssistanceService.agreementTermsType())
        .willReturn(Optional.of(TermsType.REFERENCE_SERVICE_TERMS));
    given(counterpartyService.findOrCreateByTypeAndServiceProvider(
        CounterpartyType.SERVICE_PROVIDER, ServiceProvider.LINKEDIN)).willReturn(counterparty);
    given(agreementService.needsAcceptance(candidate, counterparty,
        TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT)).willReturn(true);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/assign")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("opc_dpa_acknowledgment_required")));

    verify(agreementService, never()).needsAcceptance(candidate, counterparty, TermsType.REFERENCE_SERVICE_TERMS);
    verify(candidateAssistanceService, never()).assignToCandidate(anyLong(), ArgumentMatchers.any());
  }

  @Test
  @DisplayName("assign endpoint succeeds when agreement is already accepted")
  void assignSucceedsWhenAgreementAlreadyAccepted() throws Exception {
    given(eligibilityPolicyRegistry.isEligible(ServiceProvider.LINKEDIN, CANDIDATE_ID))
        .willReturn(true);
    given(userService.getSystemAdminUser()).willReturn(user);
    given(candidateAssistanceService.agreementTermsType())
        .willReturn(Optional.of(TermsType.REFERENCE_SERVICE_TERMS));
    given(counterpartyService.findOrCreateByTypeAndServiceProvider(
        CounterpartyType.SERVICE_PROVIDER, ServiceProvider.LINKEDIN)).willReturn(counterparty);
    given(agreementService.needsAcceptance(candidate, counterparty, TermsType.REFERENCE_SERVICE_TERMS))
        .willReturn(false);

    ServiceResource resource = ServiceResource.builder()
        .id(2L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resourceCode(RESOURCE_CODE)
        .status(ResourceStatus.RESERVED)
        .expiresAt(OffsetDateTime.now().plusDays(30))
        .build();

    ServiceAssignment assignment = ServiceAssignment.builder()
        .id(45L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resource(resource)
        .candidateId(CANDIDATE_ID)
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now())
        .build();
    given(candidateAssistanceService.assignToCandidate(CANDIDATE_ID, user)).willReturn(assignment);

    mockMvc.perform(post(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/assign")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(45)));

    verify(candidateAssistanceService).assignToCandidate(CANDIDATE_ID, user);
  }

  @Test
  @DisplayName("resource status update is allowed for candidate-owned resource")
  void updateResourceStatusOnlyAllowsCandidateOwnedResource() throws Exception {
    ServiceResource owned = ServiceResource.builder()
        .id(1L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resourceCode(RESOURCE_CODE)
        .status(ResourceStatus.RESERVED)
        .expiresAt(OffsetDateTime.now().plusDays(30))
        .build();
    given(candidateAssistanceService.getResourcesForCandidate(CANDIDATE_ID)).willReturn(List.of(owned));

    UpdateServiceResourceStatusRequest request = new UpdateServiceResourceStatusRequest();
    request.setResourceCode(RESOURCE_CODE);
    request.setStatus(ResourceStatus.REDEEMED);

    mockMvc.perform(put(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/resources/status")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(candidateAssistanceService).updateResourceStatus(RESOURCE_CODE, ResourceStatus.REDEEMED);
  }

  @Test
  @DisplayName("resource status update returns not found for unowned resource")
  void updateResourceStatusReturnsNotFoundForUnownedResource() throws Exception {
    ServiceResource other = ServiceResource.builder()
        .id(1L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resourceCode("OTHER")
        .status(ResourceStatus.RESERVED)
        .expiresAt(OffsetDateTime.now().plusDays(30))
        .build();
    given(candidateAssistanceService.getResourcesForCandidate(CANDIDATE_ID)).willReturn(List.of(other));

    UpdateServiceResourceStatusRequest request = new UpdateServiceResourceStatusRequest();
    request.setResourceCode(RESOURCE_CODE);
    request.setStatus(ResourceStatus.REDEEMED);

    mockMvc.perform(put(BASE_PATH + "/" + PROVIDER + "/" + SERVICE_CODE + "/resources/status")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", containsString("not found")));

    verify(candidateAssistanceService, never()).updateResourceStatus(RESOURCE_CODE, ResourceStatus.REDEEMED);
  }

  @Test
  @DisplayName("resource status update propagates unknown provider as not found")
  void updateResourceStatusPropagatesUnknownProvider() throws Exception {
    given(candidateServiceRegistry.forProviderAndServiceCode("UNKNOWN", "UNKNOWN"))
        .willThrow(new NoSuchObjectException("Unknown candidate service for provider: UNKNOWN, serviceCode: UNKNOWN"));

    UpdateServiceResourceStatusRequest request = new UpdateServiceResourceStatusRequest();
    request.setResourceCode(RESOURCE_CODE);
    request.setStatus(ResourceStatus.REDEEMED);

    mockMvc.perform(put(BASE_PATH + "/UNKNOWN/UNKNOWN/resources/status")
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }
}
