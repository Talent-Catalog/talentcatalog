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

package org.tctalent.server.casi.application.providers.duolingo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.SavedListService;

@ExtendWith(MockitoExtension.class)
class DuolingoServiceTest {

  private static final Long CANDIDATE_ID = 123L;
  private static final String CANDIDATE_NUMBER = "C12345";
  private static final Long LIST_ID = 456L;
  private static final String RESOURCE_CODE = "COUPON123";

  @Mock private ServiceAssignmentRepository assignmentRepository;
  @Mock private ServiceResourceRepository resourceRepository;
  @Mock private AssignmentEngine assignmentEngine;
  @Mock private SavedListService savedListService;
  @Mock private FileInventoryImporter duolingoImporter;
  @Mock private ResourceAllocator duolingoAllocator;

  @InjectMocks private DuolingoService duolingoService;

  private User user;
  private Candidate candidate;
  private ServiceResource resource;
  private ServiceResourceEntity resourceEntity;
  private ServiceAssignment assignment;
  private ServiceAssignmentEntity assignmentEntity;
  private SavedList savedList;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setUsername("testuser");

    candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);
    candidate.setCandidateNumber(CANDIDATE_NUMBER);

    resource = ServiceResource.builder()
        .id(1L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resourceCode(RESOURCE_CODE)
        .status(ResourceStatus.AVAILABLE)
        .build();

    resourceEntity = new ServiceResourceEntity();
    resourceEntity.setId(1L);
    resourceEntity.setProvider(ServiceProvider.DUOLINGO);
    resourceEntity.setServiceCode(ServiceCode.TEST_PROCTORED);
    resourceEntity.setResourceCode(RESOURCE_CODE);
    resourceEntity.setStatus(ResourceStatus.AVAILABLE);

    assignment = ServiceAssignment.builder()
        .id(1L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resource(resource)
        .candidateId(CANDIDATE_ID)
        .actorId(user.getId())
        .status(AssignmentStatus.ASSIGNED)
        .build();

    assignmentEntity = new ServiceAssignmentEntity();
    assignmentEntity.setId(1L);
    assignmentEntity.setProvider(ServiceProvider.DUOLINGO);
    assignmentEntity.setServiceCode(ServiceCode.TEST_PROCTORED);
    assignmentEntity.setResource(resourceEntity);
    assignmentEntity.setCandidate(candidate);
    assignmentEntity.setStatus(AssignmentStatus.ASSIGNED);

    savedList = mock(SavedList.class);
    // Use lenient() for stubbings that are not used by all tests.
    // Without lenient(), Mockito would throw UnnecessaryStubbingException
    // for tests that don't use savedList (e.g., providerKeyReturnsCorrectFormat).
    lenient().when(savedList.getId()).thenReturn(LIST_ID);
    lenient().when(savedList.getCandidates()).thenReturn(Set.of(candidate));
  }

  // Provider Key Generation Tests

  @Test
  @DisplayName("providerKey returns correct format")
  void providerKeyReturnsCorrectFormat() {
    // Act
    String providerKey = duolingoService.providerKey();

    // Assert
    assertThat(providerKey).isEqualTo("DUOLINGO::TEST_PROCTORED");
  }


}

