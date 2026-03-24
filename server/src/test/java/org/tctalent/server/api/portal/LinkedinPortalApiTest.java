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

package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.casi.api.request.IssueReportRequest;
import org.tctalent.server.casi.api.request.UpdateServiceResourceStatusRequest;
import org.tctalent.server.casi.application.providers.linkedin.LinkedInService;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.UserService;

class LinkedinPortalApiTest {

  private static final Long CANDIDATE_ID = 123L;
  private static final String RESOURCE_CODE =
      "https://www.linkedin.com/premium/redeem/promo?coupon=ABC123";

  @Mock
  private LinkedInService linkedInService;

  @Mock
  private UserService userService;

  @InjectMocks
  private LinkedinPortalApi linkedinPortalApi;

  private ServiceAssignment assignment;
  private User systemAdmin;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    systemAdmin = new User();
    systemAdmin.setId(1L);
    systemAdmin.setUsername("systemadmin");

      ServiceResource resource = ServiceResource.builder()
          .id(1L)
          .provider(ServiceProvider.LINKEDIN)
          .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
          .resourceCode(RESOURCE_CODE)
          .status(ResourceStatus.RESERVED)
          .build();

    assignment = ServiceAssignment.builder()
        .id(1L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resource(resource)
        .candidateId(CANDIDATE_ID)
        .actorId(systemAdmin.getId())
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now())
        .build();
  }

  // isEligible Tests

  @Test
  @DisplayName("isEligible returns true when candidate is eligible")
  void isEligibleReturnsTrueWhenEligible() {
    when(linkedInService.isEligible(CANDIDATE_ID)).thenReturn(true);

    Boolean result = linkedinPortalApi.isEligible(CANDIDATE_ID);

    assertTrue(result);
    verify(linkedInService).isEligible(CANDIDATE_ID);
  }

  @Test
  @DisplayName("isEligible returns false when candidate is not eligible")
  void isEligibleReturnsFalseWhenNotEligible() {
    when(linkedInService.isEligible(CANDIDATE_ID)).thenReturn(false);

    Boolean result = linkedinPortalApi.isEligible(CANDIDATE_ID);

    assertFalse(result);
    verify(linkedInService).isEligible(CANDIDATE_ID);
  }

  // findAssignment Tests

  @Test
  @DisplayName("findAssignment returns RESERVED assignment when one exists")
  void findAssignmentReturnsReservedAssignment() {
    when(linkedInService.findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID))
        .thenReturn(assignment);

    ServiceAssignment result =
        linkedinPortalApi.findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID);

    assertNotNull(result);
    assertEquals(CANDIDATE_ID, result.getCandidateId());
    assertEquals(ResourceStatus.RESERVED, result.getResource().getStatus());
    verify(linkedInService).findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID);
  }

  @Test
  @DisplayName("findAssignment returns null when no assignment exists")
  void findAssignmentReturnsNullWhenNoneExists() {
    when(linkedInService.findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID))
        .thenReturn(null);

    ServiceAssignment result =
        linkedinPortalApi.findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID);

    assertNull(result);
    verify(linkedInService).findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID);
  }

  // assign Tests

  @Test
  @DisplayName("assign returns assignment on success")
  void assignReturnsAssignmentOnSuccess() {
    when(userService.getSystemAdminUser()).thenReturn(systemAdmin);
    when(linkedInService.assignToCandidate(CANDIDATE_ID, systemAdmin)).thenReturn(assignment);

    ServiceAssignment result = linkedinPortalApi.assign(CANDIDATE_ID);

    assertNotNull(result);
    assertEquals(CANDIDATE_ID, result.getCandidateId());
    verify(userService).getSystemAdminUser();
    verify(linkedInService).assignToCandidate(CANDIDATE_ID, systemAdmin);
  }

  @Test
  @DisplayName("assign returns null and records failure when exception occurs")
  void assignReturnsNullOnException() {
    RuntimeException ex = new RuntimeException("Unexpected assignment error");
    when(userService.getSystemAdminUser()).thenReturn(systemAdmin);
    when(linkedInService.assignToCandidate(CANDIDATE_ID, systemAdmin)).thenThrow(ex);

    ServiceAssignment result = linkedinPortalApi.assign(CANDIDATE_ID);

    assertNull(result);
    verify(linkedInService).addCandidateToAssignmentFailureList(CANDIDATE_ID, ex);
  }

  // updateCouponStatus Tests

  @Test
  @DisplayName("updateCouponStatus delegates to service")
  void updateCouponStatusDelegatesToService() {
    UpdateServiceResourceStatusRequest request = new UpdateServiceResourceStatusRequest();
    request.setResourceCode(RESOURCE_CODE);
    request.setStatus(ResourceStatus.REDEEMED);

    linkedinPortalApi.updateCouponStatus(request);

    verify(linkedInService).updateResourceStatus(RESOURCE_CODE, ResourceStatus.REDEEMED);
  }

  // addCandidateToIssueReportList Tests

  @Test
  @DisplayName("addCandidateToIssueReportList delegates assignment and comment to service")
  void addCandidateToIssueReportListDelegatesToService() {
    IssueReportRequest request = new IssueReportRequest();
    request.setAssignment(assignment);
    request.setIssueComment("The coupon link did not work.");

    doNothing().when(linkedInService)
        .addCandidateToIssueReportList(assignment, "The coupon link did not work.");

    linkedinPortalApi.addCandidateToIssueReportList(request);

    verify(linkedInService).addCandidateToIssueReportList(assignment, "The coupon link did not work.");
  }

  // isOnIssueReportList Tests

  @Test
  @DisplayName("isOnIssueReportList returns true when candidate is on list")
  void isOnIssueReportListReturnsTrueWhenOnList() {
    when(linkedInService.isOnIssueReportList(CANDIDATE_ID)).thenReturn(true);

    Boolean result = linkedinPortalApi.isOnIssueReportList(CANDIDATE_ID);

    assertTrue(result);
    verify(linkedInService).isOnIssueReportList(CANDIDATE_ID);
  }

  @Test
  @DisplayName("isOnIssueReportList returns false when candidate is not on list")
  void isOnIssueReportListReturnsFalseWhenNotOnList() {
    when(linkedInService.isOnIssueReportList(CANDIDATE_ID)).thenReturn(false);

    Boolean result = linkedinPortalApi.isOnIssueReportList(CANDIDATE_ID);

    assertFalse(result);
    verify(linkedInService).isOnIssueReportList(CANDIDATE_ID);
  }

  // isOnAssignmentFailureList Tests

  @Test
  @DisplayName("isOnAssignmentFailureList returns true when candidate is on list")
  void isOnAssignmentFailureListReturnsTrueWhenOnList() {
    when(linkedInService.isOnAssignmentFailureList(CANDIDATE_ID)).thenReturn(true);

    Boolean result = linkedinPortalApi.isOnAssignmentFailureList(CANDIDATE_ID);

    assertTrue(result);
    verify(linkedInService).isOnAssignmentFailureList(CANDIDATE_ID);
  }

  @Test
  @DisplayName("isOnAssignmentFailureList returns false when candidate is not on list")
  void isOnAssignmentFailureListReturnsFalseWhenNotOnList() {
    when(linkedInService.isOnAssignmentFailureList(CANDIDATE_ID)).thenReturn(false);

    Boolean result = linkedinPortalApi.isOnAssignmentFailureList(CANDIDATE_ID);

    assertFalse(result);
    verify(linkedInService).isOnAssignmentFailureList(CANDIDATE_ID);
  }
}
