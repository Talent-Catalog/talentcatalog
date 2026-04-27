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
import org.tctalent.server.casi.api.dto.ServiceResourceDto;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;

class ServiceResourceMapperTest {

  private static final Long ID = 42L;
  private static final String RESOURCE_CODE = "PROC-ABC-123";
  private static final OffsetDateTime SENT_AT = OffsetDateTime.now().minusDays(1);
  private static final OffsetDateTime EXPIRES_AT = OffsetDateTime.now().plusDays(30);

  @Nested
  @DisplayName("null-safety")
  class NullSafety {

    @Test
    @DisplayName("toModel(entity) returns null when entity is null")
    void toModelFromEntity_nullInput_returnsNull() {
      assertThat(ServiceResourceMapper.toModel((ServiceResourceEntity) null)).isNull();
    }

    @Test
    @DisplayName("toModel(dto) returns null when dto is null")
    void toModelFromDto_nullInput_returnsNull() {
      assertThat(ServiceResourceMapper.toModel((ServiceResourceDto) null)).isNull();
    }

    @Test
    @DisplayName("toDto(model) returns null when model is null")
    void toDto_nullInput_returnsNull() {
      assertThat(ServiceResourceMapper.toDto(null)).isNull();
    }
  }

  @Nested
  @DisplayName("entity to model")
  class EntityToModel {

    @Test
    @DisplayName("maps all fields correctly from entity to model")
    void mapsAllFieldsCorrectly() {
      ServiceResourceEntity entity = new ServiceResourceEntity();
      entity.setId(ID);
      entity.setProvider(ServiceProvider.DUOLINGO);
      entity.setServiceCode(ServiceCode.TEST_PROCTORED);
      entity.setResourceCode(RESOURCE_CODE);
      entity.setStatus(ResourceStatus.AVAILABLE);
      entity.setSentAt(SENT_AT);
      entity.setExpiresAt(EXPIRES_AT);

      ServiceResource model = ServiceResourceMapper.toModel(entity);

      assertThat(model).isNotNull();
      assertThat(model.getId()).isEqualTo(ID);
      assertThat(model.getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
      assertThat(model.getServiceCode()).isEqualTo(ServiceCode.TEST_PROCTORED);
      assertThat(model.getResourceCode()).isEqualTo(RESOURCE_CODE);
      assertThat(model.getStatus()).isEqualTo(ResourceStatus.AVAILABLE);
      assertThat(model.getSentAt()).isEqualTo(SENT_AT);
      assertThat(model.getExpiresAt()).isEqualTo(EXPIRES_AT);
    }

    @Test
    @DisplayName("maps entity with null optional fields (sentAt, expiresAt)")
    void mapsEntityWithNullOptionalFields() {
      ServiceResourceEntity entity = new ServiceResourceEntity();
      entity.setId(ID);
      entity.setProvider(ServiceProvider.DUOLINGO);
      entity.setServiceCode(ServiceCode.TEST_NON_PROCTORED);
      entity.setResourceCode(RESOURCE_CODE);
      entity.setStatus(ResourceStatus.SENT);
      entity.setSentAt(null);
      entity.setExpiresAt(null);

      ServiceResource model = ServiceResourceMapper.toModel(entity);

      assertThat(model).isNotNull();
      assertThat(model.getSentAt()).isNull();
      assertThat(model.getExpiresAt()).isNull();
    }
  }

  @Nested
  @DisplayName("dto to model")
  class DtoToModel {

    @Test
    @DisplayName("maps all fields correctly from dto to model")
    void mapsAllFieldsCorrectly() {
      ServiceResourceDto dto = ServiceResourceDto.builder()
          .id(ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_PROCTORED)
          .resourceCode(RESOURCE_CODE)
          .status(ResourceStatus.REDEEMED)
          .sentAt(SENT_AT)
          .expiresAt(EXPIRES_AT)
          .build();

      ServiceResource model = ServiceResourceMapper.toModel(dto);

      assertThat(model).isNotNull();
      assertThat(model.getId()).isEqualTo(ID);
      assertThat(model.getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
      assertThat(model.getServiceCode()).isEqualTo(ServiceCode.TEST_PROCTORED);
      assertThat(model.getResourceCode()).isEqualTo(RESOURCE_CODE);
      assertThat(model.getStatus()).isEqualTo(ResourceStatus.REDEEMED);
      assertThat(model.getSentAt()).isEqualTo(SENT_AT);
      assertThat(model.getExpiresAt()).isEqualTo(EXPIRES_AT);
    }
  }

  @Nested
  @DisplayName("model to dto")
  class ModelToDto {

    @Test
    @DisplayName("maps all fields correctly from model to dto")
    void mapsAllFieldsCorrectly() {
      ServiceResource model = ServiceResource.builder()
          .id(ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_NON_PROCTORED)
          .resourceCode(RESOURCE_CODE)
          .status(ResourceStatus.EXPIRED)
          .sentAt(SENT_AT)
          .expiresAt(EXPIRES_AT)
          .build();

      ServiceResourceDto dto = ServiceResourceMapper.toDto(model);

      assertThat(dto).isNotNull();
      assertThat(dto.getId()).isEqualTo(ID);
      assertThat(dto.getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
      assertThat(dto.getServiceCode()).isEqualTo(ServiceCode.TEST_NON_PROCTORED);
      assertThat(dto.getResourceCode()).isEqualTo(RESOURCE_CODE);
      assertThat(dto.getStatus()).isEqualTo(ResourceStatus.EXPIRED);
      assertThat(dto.getSentAt()).isEqualTo(SENT_AT);
      assertThat(dto.getExpiresAt()).isEqualTo(EXPIRES_AT);
    }
  }

  @Nested
  @DisplayName("round-trip")
  class RoundTrip {

    @Test
    @DisplayName("entity -> model -> dto preserves all fields")
    void entityToModelToDto_preservesFields() {
      ServiceResourceEntity entity = new ServiceResourceEntity();
      entity.setId(ID);
      entity.setProvider(ServiceProvider.DUOLINGO);
      entity.setServiceCode(ServiceCode.TEST_PROCTORED);
      entity.setResourceCode(RESOURCE_CODE);
      entity.setStatus(ResourceStatus.AVAILABLE);
      entity.setSentAt(SENT_AT);
      entity.setExpiresAt(EXPIRES_AT);

      ServiceResource model = ServiceResourceMapper.toModel(entity);
      ServiceResourceDto dto = ServiceResourceMapper.toDto(model);

      assertThat(dto.getId()).isEqualTo(entity.getId());
      assertThat(dto.getProvider()).isEqualTo(entity.getProvider());
      assertThat(dto.getServiceCode()).isEqualTo(entity.getServiceCode());
      assertThat(dto.getResourceCode()).isEqualTo(entity.getResourceCode());
      assertThat(dto.getStatus()).isEqualTo(entity.getStatus());
      assertThat(dto.getSentAt()).isEqualTo(entity.getSentAt());
      assertThat(dto.getExpiresAt()).isEqualTo(entity.getExpiresAt());
    }

    @Test
    @DisplayName("dto -> model -> dto preserves all fields")
    void dtoToModelToDto_preservesFields() {
      ServiceResourceDto original = ServiceResourceDto.builder()
          .id(ID)
          .provider(ServiceProvider.DUOLINGO)
          .serviceCode(ServiceCode.TEST_NON_PROCTORED)
          .resourceCode(RESOURCE_CODE)
          .status(ResourceStatus.DISABLED)
          .sentAt(SENT_AT)
          .expiresAt(EXPIRES_AT)
          .build();

      ServiceResource model = ServiceResourceMapper.toModel(original);
      ServiceResourceDto roundTripped = ServiceResourceMapper.toDto(model);

      assertThat(roundTripped.getId()).isEqualTo(original.getId());
      assertThat(roundTripped.getProvider()).isEqualTo(original.getProvider());
      assertThat(roundTripped.getServiceCode()).isEqualTo(original.getServiceCode());
      assertThat(roundTripped.getResourceCode()).isEqualTo(original.getResourceCode());
      assertThat(roundTripped.getStatus()).isEqualTo(original.getStatus());
      assertThat(roundTripped.getSentAt()).isEqualTo(original.getSentAt());
      assertThat(roundTripped.getExpiresAt()).isEqualTo(original.getExpiresAt());
    }
  }
}
