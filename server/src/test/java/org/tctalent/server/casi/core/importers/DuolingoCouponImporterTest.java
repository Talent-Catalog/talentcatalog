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

package org.tctalent.server.casi.core.importers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.tctalent.server.casi.application.providers.duolingo.DuolingoCouponImporter;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.ImportFailedException;

class DuolingoCouponImporterTest {

  private static final ServiceProvider PROVIDER = ServiceProvider.DUOLINGO;

  @Mock
  private ServiceResourceRepository serviceResourceRepository;

  @InjectMocks
  private DuolingoCouponImporter importer;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("import file successfully imports coupons")
  void importFileSucceeds() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status,Test Status
        code1,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE,
        code2,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE,
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "code1")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "code2")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(2, couponList.size());
      assertEquals("code1", couponList.get(0).getResourceCode());
      assertEquals(LocalDateTime.of(2024, 12, 31, 23, 59, 59), couponList.get(0).getExpiresAt());
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(0).getStatus());
      assertEquals("code2", couponList.get(1).getResourceCode());
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(1).getStatus());
      return true;
    }));
  }

  // Header Validation Tests

  @Test
  @DisplayName("import file fails when CSV header is missing")
  void importFileFailsWhenHeaderMissing() {
    // Arrange
    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", "".getBytes(StandardCharsets.UTF_8)
    );

    // Act & Assert
    assertThatThrownBy(() -> importer.importFile(file, ServiceCode.TEST_NON_PROCTORED))
        .isInstanceOf(ImportFailedException.class)
        .hasRootCauseMessage("CSV header is missing");
  }

  @Test
  @DisplayName("import file fails when required column Coupon Code is missing")
  void importFileFailsWhenCouponCodeColumnMissing() {
    // Arrange
    String csvContent = """
        Assignee Email,Expiration Date,Date Sent,Coupon Status
        ,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // Act & Assert
    assertThatThrownBy(() -> importer.importFile(file, ServiceCode.TEST_NON_PROCTORED))
        .isInstanceOf(ImportFailedException.class)
        .hasRootCauseMessage("Missing required column: coupon code");
  }

  @Test
  @DisplayName("import file fails when required column Expiration Date is missing")
  void importFileFailsWhenExpirationDateColumnMissing() {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Date Sent,Coupon Status
        code1,,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // Act & Assert
    assertThatThrownBy(() -> importer.importFile(file, ServiceCode.TEST_NON_PROCTORED))
        .isInstanceOf(ImportFailedException.class)
        .hasRootCauseMessage("Missing required column: expiration date");
  }

  @Test
  @DisplayName("import file succeeds with case-insensitive column names")
  void importFileSucceedsWithCaseInsensitiveColumns() throws Exception {
    // Arrange
    String csvContent = """
        COUPON CODE,Assignee Email,EXPIRATION DATE,DATE SENT,COUPON STATUS,Test Status
        code1,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE,
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "code1")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(1, couponList.size());
      assertEquals("code1", couponList.get(0).getResourceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file succeeds with BOM in header")
  void importFileSucceedsWithBOMInHeader() throws Exception {
    // Arrange
    String csvContent = "\uFEFFCoupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status\n"
        + "code1,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "code1")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(any());
  }

  // Date Parsing Tests

  @Test
  @DisplayName("import file parses date with seconds format")
  void importFileParsesDateWithSeconds() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        code1,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "code1")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(LocalDateTime.of(2024, 12, 31, 23, 59, 59), couponList.get(0).getExpiresAt());
      return true;
    }));
  }

  @Test
  @DisplayName("import file parses date without seconds format")
  void importFileParsesDateWithoutSeconds() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        code1,,2024/12/31 23:59,2024/12/01 10:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "code1")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(LocalDateTime.of(2024, 12, 31, 23, 59), couponList.get(0).getExpiresAt());
      return true;
    }));
  }

  @Test
  @DisplayName("import file handles empty date field")
  void importFileHandlesEmptyDateField() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        code1,,,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "code1")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertNull(couponList.get(0).getExpiresAt());
      return true;
    }));
  }

  @Test
  @DisplayName("import file fails when date format is invalid")
  void importFileFailsWhenDateFormatInvalid() {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        code1,,2024-12-31,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "code1")).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> importer.importFile(file, ServiceCode.TEST_NON_PROCTORED))
        .isInstanceOf(ImportFailedException.class)
        .hasRootCauseMessage("Invalid date format: 2024-12-31");
  }


}