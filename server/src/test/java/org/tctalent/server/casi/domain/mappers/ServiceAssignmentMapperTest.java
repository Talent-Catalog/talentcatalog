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

package org.tctalent.server.casi.domain.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.tctalent.server.casi.api.dto.ServiceAssignmentDto;
import org.tctalent.server.casi.api.dto.ServiceResourceDto;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;

class ServiceAssignmentMapperTest {

  private static final Long ASSIGNMENT_ID = 10L;
  private static final Long RESOURCE_ID = 20L;
  private static final Long CANDIDATE_ID = 100L;
  private static final Long ACTOR_ID = 200L;
  private static final String RESOURCE_CODE = "PROC-XYZ-456";
  private static final OffsetDateTime ASSIGNED_AT = OffsetDateTime.now().minusDays(2);

  @Nested
  @DisplayName("null-safety")
  class NullSafety {

    @Test
    @DisplayName("toModel(entity) returns null when entity is null")
    void toModelFromEntity_nullInput_returnsNull() {
      assertThat(ServiceAssignmentMapper.toModel((ServiceAssignmentEntity) null)).isNull();
    }

    @Test
    @DisplayName("toModel(dto) returns null when dto is null")
    void toModelFromDto_nullInput_returnsNull() {
      assertThat(ServiceAssignmentMapper.toModel((ServiceAssignmentDto) null)).isNull();
    }

    @Test
    @DisplayName("toDto(model) returns null when model is null")
    void toDto_nullInput_returnsNull() {
      assertThat(ServiceAssignmentMapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("toModel(dto) with null resource maps resource as null")
    void toModelFromDto_nullResource_resourceIsNull() {
      ServiceAssignmentDto dto = ServiceAssignmentDto.builder()
          .id(ASSIGNMENT_ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_PROCTORED)
          .resource(null)
          .candidateId(CANDIDATE_ID)
          .actorId(ACTOR_ID)
          .status(AssignmentStatus.ASSIGNED)
          .assignedAt(ASSIGNED_AT)
          .build();

      ServiceAssignment model = ServiceAssignmentMapper.toModel(dto);

      assertThat(model).isNotNull();
      assertThat(model.getResource()).isNull();
    }

    @Test
    @DisplayName("toDto(model) with null resource maps resource as null")
    void toDto_modelWithNullResource_resourceIsNull() {
      ServiceAssignment model = ServiceAssignment.builder()
          .id(ASSIGNMENT_ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_PROCTORED)
          .resource(null)
          .candidateId(CANDIDATE_ID)
          .actorId(ACTOR_ID)
          .status(AssignmentStatus.ASSIGNED)
          .assignedAt(ASSIGNED_AT)
          .build();

      ServiceAssignmentDto dto = ServiceAssignmentMapper.toDto(model);

      assertThat(dto).isNotNull();
      assertThat(dto.getResource()).isNull();
    }
  }

  @Nested
  @DisplayName("entity to model")
  class EntityToModel {

    @Test
    @DisplayName("maps all fields correctly from entity to model")
    void mapsAllFieldsCorrectly() {
      ServiceResourceEntity resourceEntity = new ServiceResourceEntity();
      resourceEntity.setId(RESOURCE_ID);
      resourceEntity.setProvider(ServiceProvider.DUOLINGO);
      resourceEntity.setServiceCode(ServiceCode.TEST_PROCTORED);
      resourceEntity.setResourceCode(RESOURCE_CODE);
      resourceEntity.setStatus(org.tctalent.server.casi.domain.model.ResourceStatus.SENT);

      Candidate candidate = new Candidate();
      candidate.setId(CANDIDATE_ID);
      User actor = new User();
      actor.setId(ACTOR_ID);

      ServiceAssignmentEntity entity = new ServiceAssignmentEntity();
      entity.setId(ASSIGNMENT_ID);
      entity.setProvider(ServiceProvider.DUOLINGO);
      entity.setServiceCode(ServiceCode.TEST_PROCTORED);
      entity.setResource(resourceEntity);
      entity.setCandidate(candidate);
      entity.setActor(actor);
      entity.setStatus(AssignmentStatus.ASSIGNED);
      entity.setAssignedAt(ASSIGNED_AT);

      ServiceAssignment model = ServiceAssignmentMapper.toModel(entity);

      assertThat(model).isNotNull();
      assertThat(model.getId()).isEqualTo(ASSIGNMENT_ID);
      assertThat(model.getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
      assertThat(model.getServiceCode()).isEqualTo(ServiceCode.TEST_PROCTORED);
      assertThat(model.getCandidateId()).isEqualTo(CANDIDATE_ID);
      assertThat(model.getActorId()).isEqualTo(ACTOR_ID);
      assertThat(model.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
      assertThat(model.getAssignedAt()).isEqualTo(ASSIGNED_AT);
      assertThat(model.getResource()).isNotNull();
      assertThat(model.getResource().getId()).isEqualTo(RESOURCE_ID);
      assertThat(model.getResource().getResourceCode()).isEqualTo(RESOURCE_CODE);
    }

    @Test
    @DisplayName("maps entity with null actor to model with null actorId")
    void mapsEntityWithNullActor_actorIdIsNull() {
      ServiceResourceEntity resourceEntity = new ServiceResourceEntity();
      resourceEntity.setId(RESOURCE_ID);
      resourceEntity.setProvider(ServiceProvider.DUOLINGO);
      resourceEntity.setServiceCode(ServiceCode.TEST_PROCTORED);
      resourceEntity.setResourceCode(RESOURCE_CODE);
      resourceEntity.setStatus(org.tctalent.server.casi.domain.model.ResourceStatus.AVAILABLE);

      Candidate candidate = new Candidate();
      candidate.setId(CANDIDATE_ID);

      ServiceAssignmentEntity entity = new ServiceAssignmentEntity();
      entity.setId(ASSIGNMENT_ID);
      entity.setProvider(ServiceProvider.DUOLINGO);
      entity.setServiceCode(ServiceCode.TEST_PROCTORED);
      entity.setResource(resourceEntity);
      entity.setCandidate(candidate);
      entity.setActor(null);
      entity.setStatus(AssignmentStatus.ASSIGNED);
      entity.setAssignedAt(ASSIGNED_AT);

      ServiceAssignment model = ServiceAssignmentMapper.toModel(entity);

      assertThat(model).isNotNull();
      assertThat(model.getActorId()).isNull();
    }
  }

  @Nested
  @DisplayName("dto to model")
  class DtoToModel {

    @Test
    @DisplayName("maps all fields correctly from dto to model")
    void mapsAllFieldsCorrectly() {
      ServiceResourceDto resourceDto = ServiceResourceDto.builder()
          .id(RESOURCE_ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_PROCTORED)
          .resourceCode(RESOURCE_CODE)
          .status(org.tctalent.server.casi.domain.model.ResourceStatus.REDEEMED)
          .build();

      ServiceAssignmentDto dto = ServiceAssignmentDto.builder()
          .id(ASSIGNMENT_ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_NON_PROCTORED)
          .resource(resourceDto)
          .candidateId(CANDIDATE_ID)
          .actorId(ACTOR_ID)
          .status(AssignmentStatus.REDEEMED)
          .assignedAt(ASSIGNED_AT)
          .build();

      ServiceAssignment model = ServiceAssignmentMapper.toModel(dto);

      assertThat(model).isNotNull();
      assertThat(model.getId()).isEqualTo(ASSIGNMENT_ID);
      assertThat(model.getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
      assertThat(model.getServiceCode()).isEqualTo(ServiceCode.TEST_NON_PROCTORED);
      assertThat(model.getCandidateId()).isEqualTo(CANDIDATE_ID);
      assertThat(model.getActorId()).isEqualTo(ACTOR_ID);
      assertThat(model.getStatus()).isEqualTo(AssignmentStatus.REDEEMED);
      assertThat(model.getAssignedAt()).isEqualTo(ASSIGNED_AT);
      assertThat(model.getResource()).isNotNull();
      assertThat(model.getResource().getId()).isEqualTo(RESOURCE_ID);
      assertThat(model.getResource().getResourceCode()).isEqualTo(RESOURCE_CODE);
    }

    @Test
    @DisplayName("maps dto with null actorId")
    void mapsDtoWithNullActorId() {
      ServiceAssignmentDto dto = ServiceAssignmentDto.builder()
          .id(ASSIGNMENT_ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_PROCTORED)
          .resource(null)
          .candidateId(CANDIDATE_ID)
          .actorId(null)
          .status(AssignmentStatus.ASSIGNED)
          .assignedAt(ASSIGNED_AT)
          .build();

      ServiceAssignment model = ServiceAssignmentMapper.toModel(dto);

      assertThat(model).isNotNull();
      assertThat(model.getActorId()).isNull();
    }
  }

  @Nested
  @DisplayName("model to dto")
  class ModelToDto {

    @Test
    @DisplayName("maps all fields correctly from model to dto")
    void mapsAllFieldsCorrectly() {
      ServiceResource resource = ServiceResource.builder()
          .id(RESOURCE_ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_PROCTORED)
          .resourceCode(RESOURCE_CODE)
          .status(org.tctalent.server.casi.domain.model.ResourceStatus.EXPIRED)
          .build();

      ServiceAssignment model = ServiceAssignment.builder()
          .id(ASSIGNMENT_ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_NON_PROCTORED)
          .resource(resource)
          .candidateId(CANDIDATE_ID)
          .actorId(ACTOR_ID)
          .status(AssignmentStatus.EXPIRED)
          .assignedAt(ASSIGNED_AT)
          .build();

      ServiceAssignmentDto dto = ServiceAssignmentMapper.toDto(model);

      assertThat(dto).isNotNull();
      assertThat(dto.getId()).isEqualTo(ASSIGNMENT_ID);
      assertThat(dto.getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
      assertThat(dto.getServiceCode()).isEqualTo(ServiceCode.TEST_NON_PROCTORED);
      assertThat(dto.getCandidateId()).isEqualTo(CANDIDATE_ID);
      assertThat(dto.getActorId()).isEqualTo(ACTOR_ID);
      assertThat(dto.getStatus()).isEqualTo(AssignmentStatus.EXPIRED);
      assertThat(dto.getAssignedAt()).isEqualTo(ASSIGNED_AT);
      assertThat(dto.getResource()).isNotNull();
      assertThat(dto.getResource().getId()).isEqualTo(RESOURCE_ID);
      assertThat(dto.getResource().getResourceCode()).isEqualTo(RESOURCE_CODE);
    }
  }

  @Nested
  @DisplayName("round-trip")
  class RoundTrip {

    @Test
    @DisplayName("entity -> model -> dto preserves all fields including nested resource")
    void entityToModelToDto_preservesFields() {
      ServiceResourceEntity resourceEntity = new ServiceResourceEntity();
      resourceEntity.setId(RESOURCE_ID);
      resourceEntity.setProvider(ServiceProvider.DUOLINGO);
      resourceEntity.setServiceCode(ServiceCode.TEST_PROCTORED);
      resourceEntity.setResourceCode(RESOURCE_CODE);
      resourceEntity.setStatus(org.tctalent.server.casi.domain.model.ResourceStatus.AVAILABLE);

      Candidate candidate = new Candidate();
      candidate.setId(CANDIDATE_ID);
      User actor = new User();
      actor.setId(ACTOR_ID);

      ServiceAssignmentEntity entity = new ServiceAssignmentEntity();
      entity.setId(ASSIGNMENT_ID);
      entity.setProvider(ServiceProvider.DUOLINGO);
      entity.setServiceCode(ServiceCode.TEST_PROCTORED);
      entity.setResource(resourceEntity);
      entity.setCandidate(candidate);
      entity.setActor(actor);
      entity.setStatus(AssignmentStatus.ASSIGNED);
      entity.setAssignedAt(ASSIGNED_AT);

      ServiceAssignment model = ServiceAssignmentMapper.toModel(entity);
      ServiceAssignmentDto dto = ServiceAssignmentMapper.toDto(model);

      assertThat(dto.getId()).isEqualTo(entity.getId());
      assertThat(dto.getProvider()).isEqualTo(entity.getProvider());
      assertThat(dto.getServiceCode()).isEqualTo(entity.getServiceCode());
      assertThat(dto.getCandidateId()).isEqualTo(entity.getCandidate().getId());
      assertThat(dto.getActorId()).isEqualTo(entity.getActor().getId());
      assertThat(dto.getStatus()).isEqualTo(entity.getStatus());
      assertThat(dto.getAssignedAt()).isEqualTo(entity.getAssignedAt());
      assertThat(dto.getResource().getId()).isEqualTo(entity.getResource().getId());
      assertThat(dto.getResource().getResourceCode()).isEqualTo(entity.getResource().getResourceCode());
    }
  }
}
