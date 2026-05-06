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

package org.tctalent.server.casi.application.providers.linkedin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ListRole;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceListEntity;
import org.tctalent.server.casi.domain.persistence.ServiceListRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;

@ExtendWith(MockitoExtension.class)
class LinkedInServiceTest {

  private static final Long CANDIDATE_ID = 123L;
  private static final Long ISSUE_REPORT_LIST_ID = 12623L;
  private static final Long ASSIGNMENT_FAILURE_LIST_ID = 12625L;
  private static final Long ELIGIBLE_LIST_ID = 12608L;
  private static final Long NON_ELIGIBLE_LIST_ID = 99999L;
  private static final String RESOURCE_CODE =
      "https://www.linkedin.com/premium/redeem/promo?coupon=ABC123";

  @Mock private ServiceAssignmentRepository assignmentRepository;
  @Mock private ServiceResourceRepository resourceRepository;
  @Mock private AssignmentEngine assignmentEngine;
  @Mock private SavedListService savedListService;
  @Mock private CandidateService candidateService;
  @Mock private ServiceListRepository serviceListRepo;
  @Mock private FileInventoryImporter linkedInImporter;
  @Mock private ResourceAllocator linkedInAllocator;

  @InjectMocks private LinkedInService linkedInService;

  private Candidate candidate;
  private ServiceAssignment reservedAssignment;
  private ServiceAssignmentEntity reservedAssignmentEntity;
  private ServiceAssignmentEntity redeemedAssignmentEntity;
  private SavedList issueReportList;
  private SavedList assignmentFailureList;
  private ServiceListEntity issueReportEntity;
  private ServiceListEntity assignmentFailureEntity;

  @BeforeEach
  void setUp() {
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");

    candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);
    candidate.setCandidateNumber("C12345");

    ServiceResourceEntity resourceEntity = new ServiceResourceEntity();
    resourceEntity.setId(1L);
    resourceEntity.setProvider(ServiceProvider.LINKEDIN);
    resourceEntity.setServiceCode(ServiceCode.PREMIUM_MEMBERSHIP);
    resourceEntity.setResourceCode(RESOURCE_CODE);
    resourceEntity.setStatus(ResourceStatus.RESERVED);

      ServiceResource reservedResource = ServiceResource.builder()
          .id(1L)
          .provider(ServiceProvider.LINKEDIN)
          .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
          .resourceCode(RESOURCE_CODE)
          .status(ResourceStatus.RESERVED)
          .build();

    reservedAssignment = ServiceAssignment.builder()
        .id(1L)
        .provider(ServiceProvider.LINKEDIN)
        .serviceCode(ServiceCode.PREMIUM_MEMBERSHIP)
        .resource(reservedResource)
        .candidateId(CANDIDATE_ID)
        .actorId(user.getId())
        .status(AssignmentStatus.ASSIGNED)
        .build();

    reservedAssignmentEntity = new ServiceAssignmentEntity();
    reservedAssignmentEntity.setId(1L);
    reservedAssignmentEntity.setProvider(ServiceProvider.LINKEDIN);
    reservedAssignmentEntity.setServiceCode(ServiceCode.PREMIUM_MEMBERSHIP);
    reservedAssignmentEntity.setResource(resourceEntity);
    reservedAssignmentEntity.setCandidate(candidate);
    reservedAssignmentEntity.setStatus(AssignmentStatus.ASSIGNED);

    ServiceResourceEntity redeemedResourceEntity = new ServiceResourceEntity();
    redeemedResourceEntity.setId(2L);
    redeemedResourceEntity.setProvider(ServiceProvider.LINKEDIN);
    redeemedResourceEntity.setServiceCode(ServiceCode.PREMIUM_MEMBERSHIP);
    redeemedResourceEntity.setResourceCode(RESOURCE_CODE);
    redeemedResourceEntity.setStatus(ResourceStatus.REDEEMED);

    redeemedAssignmentEntity = new ServiceAssignmentEntity();
    redeemedAssignmentEntity.setId(2L);
    redeemedAssignmentEntity.setProvider(ServiceProvider.LINKEDIN);
    redeemedAssignmentEntity.setServiceCode(ServiceCode.PREMIUM_MEMBERSHIP);
    redeemedAssignmentEntity.setResource(redeemedResourceEntity);
    redeemedAssignmentEntity.setCandidate(candidate);
    redeemedAssignmentEntity.setStatus(AssignmentStatus.ASSIGNED);

    issueReportList = new SavedList();
    issueReportList.setId(ISSUE_REPORT_LIST_ID);

    assignmentFailureList = new SavedList();
    assignmentFailureList.setId(ASSIGNMENT_FAILURE_LIST_ID);

    issueReportEntity = new ServiceListEntity();
    issueReportEntity.setSavedList(issueReportList);

    assignmentFailureEntity = new ServiceListEntity();
    assignmentFailureEntity.setSavedList(assignmentFailureList);
  }

  // Provider Key Test

  @Test
  @DisplayName("providerKey returns correct format")
  void providerKeyReturnsCorrectFormat() {
    assertThat(linkedInService.providerKey()).isEqualTo("LINKEDIN::PREMIUM_MEMBERSHIP");
  }

  // isEligible Tests

  @Test
  @DisplayName("isEligible returns true when candidate is on an eligible list")
  void isEligibleReturnsTrueWhenCandidateIsOnEligibleList() {
    SavedList eligibleList = mock(SavedList.class);
    when(eligibleList.getId()).thenReturn(ELIGIBLE_LIST_ID);
    ServiceListEntity eligibleEntity = new ServiceListEntity();
    eligibleEntity.setSavedList(eligibleList);
    when(serviceListRepo.findByProviderAndServiceCodeAndRole(ServiceProvider.LINKEDIN,
        ServiceCode.PREMIUM_MEMBERSHIP, ListRole.SERVICE_ELIGIBILITY))
        .thenReturn(List.of(eligibleEntity));
    when(savedListService.search(eq(CANDIDATE_ID), any(SearchSavedListRequest.class)))
        .thenReturn(List.of(eligibleList));

    assertThat(linkedInService.isEligible(CANDIDATE_ID)).isTrue();
  }

  @Test
  @DisplayName("isEligible returns false when candidate is not on any eligible list")
  void isEligibleReturnsFalseWhenCandidateIsNotOnEligibleList() {
    SavedList nonEligibleList = mock(SavedList.class);
    when(nonEligibleList.getId()).thenReturn(NON_ELIGIBLE_LIST_ID);
    when(savedListService.search(eq(CANDIDATE_ID), any(SearchSavedListRequest.class)))
        .thenReturn(List.of(nonEligibleList));

    assertThat(linkedInService.isEligible(CANDIDATE_ID)).isFalse();
  }

  @Test
  @DisplayName("isEligible returns false when candidate has no lists")
  void isEligibleReturnsFalseWhenCandidateHasNoLists() {
    when(savedListService.search(eq(CANDIDATE_ID), any(SearchSavedListRequest.class)))
        .thenReturn(Collections.emptyList());

    assertThat(linkedInService.isEligible(CANDIDATE_ID)).isFalse();
  }

  // findAssignmentWithReservedOrRedeemedResource Tests

  @Test
  @DisplayName("findAssignment returns RESERVED assignment when one exists alongside REDEEMED")
  void findAssignmentReturnsReservedAssignmentFirst() {
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP))
        .thenReturn(List.of(reservedAssignmentEntity, redeemedAssignmentEntity));

    ServiceAssignment result =
        linkedInService.findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID);

    assertThat(result).isNotNull();
    assertThat(result.getResource().getStatus()).isEqualTo(ResourceStatus.RESERVED);
  }

  @Test
  @DisplayName("findAssignment returns REDEEMED assignment when no RESERVED one exists")
  void findAssignmentReturnsRedeemedWhenNoReserved() {
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP))
        .thenReturn(List.of(redeemedAssignmentEntity));

    ServiceAssignment result =
        linkedInService.findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID);

    assertThat(result).isNotNull();
    assertThat(result.getResource().getStatus()).isEqualTo(ResourceStatus.REDEEMED);
  }

  @Test
  @DisplayName("findAssignment returns null when neither RESERVED nor REDEEMED assignment exists")
  void findAssignmentReturnsNullWhenNeitherExists() {
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP))
        .thenReturn(Collections.emptyList());

    ServiceAssignment result =
        linkedInService.findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID);

    assertThat(result).isNull();
  }

  private void stubIssueReportList() {
    when(serviceListRepo.findFirstByProviderAndServiceCodeAndRole(
        ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP, ListRole.USER_ISSUE_REPORT))
        .thenReturn(Optional.of(issueReportEntity));
  }

  private void stubAssignmentFailureList() {
    when(serviceListRepo.findFirstByProviderAndServiceCodeAndRole(
        ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP, ListRole.ASSIGNMENT_FAILURE))
        .thenReturn(Optional.of(assignmentFailureEntity));
  }

  // addCandidateToIssueReportList Tests

  @Test
  @DisplayName("addCandidateToIssueReportList calls expected service methods")
  void addCandidateToIssueReportListCallsExpectedServiceMethods() {
    String comment = "The coupon link was broken.";
    stubIssueReportList();
    when(candidateService.getCandidate(CANDIDATE_ID)).thenReturn(candidate);

    linkedInService.addCandidateToIssueReportList(reservedAssignment, comment);

    verify(candidateService).getCandidate(CANDIDATE_ID);
    verify(savedListService).addCandidateToList(eq(issueReportList), eq(candidate), any(String.class));
    verify(savedListService).saveIt(issueReportList);
  }

  @Test
  @DisplayName("addCandidateToIssueReportList note contains resource code and comment")
  void addCandidateToIssueReportListNoteContainsDetails() {
    String comment = "The coupon link was broken.";
    stubIssueReportList();
    when(candidateService.getCandidate(CANDIDATE_ID)).thenReturn(candidate);

    linkedInService.addCandidateToIssueReportList(reservedAssignment, comment);

    verify(savedListService).addCandidateToList(
        eq(issueReportList),
        eq(candidate),
        org.mockito.ArgumentMatchers.<String>argThat(note ->
            note.contains(RESOURCE_CODE) && note.contains(comment)
        )
    );
  }

  // addCandidateToAssignmentFailureList Tests

  @Test
  @DisplayName("addCandidateToAssignmentFailureList calls expected service methods")
  void addCandidateToAssignmentFailureListCallsExpectedServiceMethods() {
    Exception ex = new RuntimeException("Test exception");
    stubAssignmentFailureList();
    when(candidateService.getCandidate(CANDIDATE_ID)).thenReturn(candidate);

    linkedInService.addCandidateToAssignmentFailureList(CANDIDATE_ID, ex);

    verify(candidateService).getCandidate(CANDIDATE_ID);
    verify(savedListService).addCandidateToList(
        eq(assignmentFailureList), eq(candidate), eq(ex.toString()));
    verify(savedListService).saveIt(assignmentFailureList);
  }

  // isOnIssueReportList Tests

  @Test
  @DisplayName("isOnIssueReportList returns true when candidate is on issue report list")
  void isOnIssueReportListReturnsTrueWhenOnList() {
    stubIssueReportList();
    SavedList list = mock(SavedList.class);
    when(list.getId()).thenReturn(ISSUE_REPORT_LIST_ID);
    when(savedListService.search(eq(CANDIDATE_ID), any(SearchSavedListRequest.class)))
        .thenReturn(List.of(list));

    assertThat(linkedInService.isOnIssueReportList(CANDIDATE_ID)).isTrue();
  }

  @Test
  @DisplayName("isOnIssueReportList returns false when candidate is not on issue report list")
  void isOnIssueReportListReturnsFalseWhenNotOnList() {
    stubIssueReportList();
    SavedList list = mock(SavedList.class);
    when(list.getId()).thenReturn(NON_ELIGIBLE_LIST_ID);
    when(savedListService.search(eq(CANDIDATE_ID), any(SearchSavedListRequest.class)))
        .thenReturn(List.of(list));

    assertThat(linkedInService.isOnIssueReportList(CANDIDATE_ID)).isFalse();
  }

  @Test
  @DisplayName("isOnIssueReportList returns false when candidate has no lists")
  void isOnIssueReportListReturnsFalseWhenNoLists() {
    stubIssueReportList();
    when(savedListService.search(eq(CANDIDATE_ID), any(SearchSavedListRequest.class)))
        .thenReturn(Collections.emptyList());

    assertThat(linkedInService.isOnIssueReportList(CANDIDATE_ID)).isFalse();
  }

  // isOnAssignmentFailureList Tests

  @Test
  @DisplayName("isOnAssignmentFailureList returns true when candidate is on failure list")
  void isOnAssignmentFailureListReturnsTrueWhenOnList() {
    stubAssignmentFailureList();
    SavedList list = mock(SavedList.class);
    when(list.getId()).thenReturn(ASSIGNMENT_FAILURE_LIST_ID);
    when(savedListService.search(eq(CANDIDATE_ID), any(SearchSavedListRequest.class)))
        .thenReturn(List.of(list));

    assertThat(linkedInService.isOnAssignmentFailureList(CANDIDATE_ID)).isTrue();
  }

  @Test
  @DisplayName("isOnAssignmentFailureList returns false when candidate is not on failure list")
  void isOnAssignmentFailureListReturnsFalseWhenNotOnList() {
    stubAssignmentFailureList();
    SavedList list = mock(SavedList.class);
    when(list.getId()).thenReturn(NON_ELIGIBLE_LIST_ID);
    when(savedListService.search(eq(CANDIDATE_ID), any(SearchSavedListRequest.class)))
        .thenReturn(List.of(list));

    assertThat(linkedInService.isOnAssignmentFailureList(CANDIDATE_ID)).isFalse();
  }

  @Test
  @DisplayName("isOnAssignmentFailureList returns false when candidate has no lists")
  void isOnAssignmentFailureListReturnsFalseWhenNoLists() {
    stubAssignmentFailureList();
    when(savedListService.search(eq(CANDIDATE_ID), any(SearchSavedListRequest.class)))
        .thenReturn(Collections.emptyList());

    assertThat(linkedInService.isOnAssignmentFailureList(CANDIDATE_ID)).isFalse();
  }
}
