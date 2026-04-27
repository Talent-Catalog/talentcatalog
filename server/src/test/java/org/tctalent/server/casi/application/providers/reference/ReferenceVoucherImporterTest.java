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

package org.tctalent.server.casi.application.providers.reference;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.ImportFailedException;

class ReferenceVoucherImporterTest {

  @Mock
  private ServiceResourceRepository resources;

  @InjectMocks
  private ReferenceVoucherImporter importer;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("imports reference vouchers from CSV")
  void importFileSucceeds() throws Exception {
    String csv = """
        voucher_code,expires_at
        REF-001,2026-12-01
        REF-002,2026-12-31
        """;
    MockMultipartFile file = new MockMultipartFile(
        "file", "reference.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

    when(resources.existsByProviderAndResourceCode(ServiceProvider.REFERENCE, "REF-001")).thenReturn(false);
    when(resources.existsByProviderAndResourceCode(ServiceProvider.REFERENCE, "REF-002")).thenReturn(false);

    importer.importFile(file, ServiceCode.VOUCHER);

    verify(resources, times(1)).saveAll(argThat(saved -> {
      List<ServiceResourceEntity> list = StreamSupport.stream(saved.spliterator(), false).toList();
      assertEquals(2, list.size());
      assertEquals(ServiceProvider.REFERENCE, list.get(0).getProvider());
      assertEquals(ServiceCode.VOUCHER, list.get(0).getServiceCode());
      assertEquals(ResourceStatus.AVAILABLE, list.get(0).getStatus());
      assertEquals(OffsetDateTime.of(2026, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC), list.get(0).getExpiresAt());
      return true;
    }));
  }

  @Test
  @DisplayName("fails when required columns are missing")
  void importFileFailsWhenColumnsMissing() {
    String csv = """
        wrong,expires_at
        REF-001,2026-12-01
        """;
    MockMultipartFile file = new MockMultipartFile(
        "file", "reference.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

    assertThatThrownBy(() -> importer.importFile(file, ServiceCode.VOUCHER))
        .isInstanceOf(ImportFailedException.class)
        .hasMessageContaining("Missing required column: voucher_code");
  }

  @Test
  @DisplayName("skips duplicates and existing voucher codes")
  void importFileSkipsDuplicatesAndExistingCodes() throws Exception {
    String csv = """
        voucher_code,expires_at
        REF-001,2026-12-01
        REF-001,2026-12-01
        REF-002,2026-12-31
        """;
    MockMultipartFile file = new MockMultipartFile(
        "file", "reference.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

    when(resources.existsByProviderAndResourceCode(ServiceProvider.REFERENCE, "REF-001")).thenReturn(false);
    when(resources.existsByProviderAndResourceCode(ServiceProvider.REFERENCE, "REF-002")).thenReturn(true);

    importer.importFile(file, ServiceCode.VOUCHER);

    verify(resources, times(1)).saveAll(argThat(saved -> {
      List<ServiceResourceEntity> list = StreamSupport.stream(saved.spliterator(), false).toList();
      assertEquals(1, list.size());
      assertEquals("REF-001", list.get(0).getResourceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("does not save when all rows are invalid")
  void importFileSkipsAllInvalidRows() throws Exception {
    String csv = """
        voucher_code,expires_at
        ,2026-12-01
        ,2026-12-02
        """;
    MockMultipartFile file = new MockMultipartFile(
        "file", "reference.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

    importer.importFile(file, ServiceCode.VOUCHER);

    verify(resources, never()).saveAll(any());
  }
}
