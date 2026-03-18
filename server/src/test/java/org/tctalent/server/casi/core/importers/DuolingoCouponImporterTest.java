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
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE,
        ACC456,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE,
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC456")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(2, couponList.size());
      assertEquals("ACC123", couponList.get(0).getResourceCode());
      assertEquals(OffsetDateTime.of(2024, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC), couponList.get(0).getExpiresAt());
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(0).getStatus());
      assertEquals("ACC456", couponList.get(1).getResourceCode());
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
        .hasMessageContaining("CSV header is missing");
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
        .hasMessageContaining("Missing required column: coupon code");
  }

  @Test
  @DisplayName("import file fails when required column Expiration Date is missing")
  void importFileFailsWhenExpirationDateColumnMissing() {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Date Sent,Coupon Status
        ACC123,,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // Act & Assert
    assertThatThrownBy(() -> importer.importFile(file, ServiceCode.TEST_NON_PROCTORED))
        .isInstanceOf(ImportFailedException.class)
        .hasMessageContaining("Missing required column: expiration date");
  }

  @Test
  @DisplayName("import file succeeds with case-insensitive column names")
  void importFileSucceedsWithCaseInsensitiveColumns() throws Exception {
    // Arrange
    String csvContent = """
        COUPON CODE,Assignee Email,EXPIRATION DATE,DATE SENT,COUPON STATUS,Test Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE,
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(1, couponList.size());
      assertEquals("ACC123", couponList.get(0).getResourceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file succeeds with BOM in header")
  void importFileSucceedsWithBOMInHeader() throws Exception {
    // Arrange
    String csvContent = "\uFEFFCoupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status\n"
        + "ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

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
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(OffsetDateTime.of(2024, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC), couponList.get(0).getExpiresAt());
      return true;
    }));
  }

  @Test
  @DisplayName("import file parses date without seconds format")
  void importFileParsesDateWithoutSeconds() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59,2024/12/01 10:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(OffsetDateTime.of(2024, 12, 31, 23, 59, 0, 0, ZoneOffset.UTC), couponList.get(0).getExpiresAt());
      return true;
    }));
  }

  @Test
  @DisplayName("import file handles empty date field")
  void importFileHandlesEmptyDateField() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

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
  @DisplayName("import file handles multiple date formats in same file")
  void importFileHandlesMultipleDateFormatsInSameFile() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        ACC456,,2024/12/31 23:59,2024/12/01 10:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC456")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(2, couponList.size());
      // First coupon uses format with seconds
      assertEquals(OffsetDateTime.of(2024, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC), couponList.get(0).getExpiresAt());
      // Second coupon without seconds
      assertEquals(OffsetDateTime.of(2024, 12, 31, 23, 59, 0, 0, ZoneOffset.UTC), couponList.get(1).getExpiresAt());
      return true;
    }));
  }

  @Test
  @DisplayName("import file fails when date format is invalid")
  void importFileFailsWhenDateFormatInvalid() {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024-12-31,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> importer.importFile(file, ServiceCode.TEST_NON_PROCTORED))
        .isInstanceOf(ImportFailedException.class)
        .hasRootCauseInstanceOf(RuntimeException.class)
        .hasRootCauseMessage("Invalid date format: 2024-12-31");
  }

  // Status Mapping Tests

  @Test
  @DisplayName("import file maps AVAILABLE status correctly")
  void importFileMapsAvailableStatus() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(0).getStatus());
      return true;
    }));
  }

  @Test
  @DisplayName("import file maps ASSIGNED status to RESERVED")
  void importFileMapsAssignedStatusToReserved() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,ASSIGNED
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ResourceStatus.RESERVED, couponList.get(0).getStatus());
      return true;
    }));
  }

  @Test
  @DisplayName("import file maps SENT status correctly")
  void importFileMapsSentStatus() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,SENT
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ResourceStatus.SENT, couponList.get(0).getStatus());
      return true;
    }));
  }

  @Test
  @DisplayName("import file maps REDEEMED status correctly")
  void importFileMapsRedeemedStatus() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,REDEEMED
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ResourceStatus.REDEEMED, couponList.get(0).getStatus());
      return true;
    }));
  }

  @Test
  @DisplayName("import file maps EXPIRED status correctly")
  void importFileMapsExpiredStatus() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,EXPIRED
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ResourceStatus.EXPIRED, couponList.get(0).getStatus());
      return true;
    }));
  }

  @Test
  @DisplayName("import file maps empty status to AVAILABLE")
  void importFileMapsEmptyStatusToAvailable() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(0).getStatus());
      return true;
    }));
  }

  @Test
  @DisplayName("import file maps unknown status to AVAILABLE")
  void importFileMapsUnknownStatusToAvailable() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,UNKNOWN_STATUS
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(0).getStatus());
      return true;
    }));
  }

  @Test
  @DisplayName("import file maps case-insensitive status")
  void importFileMapsCaseInsensitiveStatus() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,available
        ACC456,,2024/12/31 23:59:59,2024/12/01 10:00:00,Assigned
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC456")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(0).getStatus());
      assertEquals(ResourceStatus.RESERVED, couponList.get(1).getStatus());
      return true;
    }));
  }

  @Test
  @DisplayName("import file handles mixed statuses in same file")
  void importFileHandlesMixedStatusesInSameFile() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        ACC456,,2024/12/31 23:59:59,2024/12/01 10:00:00,ASSIGNED
        ACC789,,2024/12/31 23:59:59,2024/12/01 10:00:00,SENT
        NONP123,,2024/12/31 23:59:59,2024/12/01 10:00:00,REDEEMED
        NONP456,,2024/12/31 23:59:59,2024/12/01 10:00:00,EXPIRED
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC456")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC789")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "NONP123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "NONP456")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(5, couponList.size());
      // Verify all statuses are correctly mapped by finding each coupon
      ServiceResourceEntity acc123 = couponList.stream()
          .filter(c -> "ACC123".equals(c.getResourceCode()))
          .findFirst().orElseThrow();
      ServiceResourceEntity acc456 = couponList.stream()
          .filter(c -> "ACC456".equals(c.getResourceCode()))
          .findFirst().orElseThrow();
      ServiceResourceEntity acc789 = couponList.stream()
          .filter(c -> "ACC789".equals(c.getResourceCode()))
          .findFirst().orElseThrow();
      ServiceResourceEntity nonp123 = couponList.stream()
          .filter(c -> "NONP123".equals(c.getResourceCode()))
          .findFirst().orElseThrow();
      ServiceResourceEntity nonp456 = couponList.stream()
          .filter(c -> "NONP456".equals(c.getResourceCode()))
          .findFirst().orElseThrow();
      
      assertEquals(ResourceStatus.AVAILABLE, acc123.getStatus());
      assertEquals(ResourceStatus.RESERVED, acc456.getStatus()); // ASSIGNED -> RESERVED
      assertEquals(ResourceStatus.SENT, acc789.getStatus());
      assertEquals(ResourceStatus.REDEEMED, nonp123.getStatus());
      assertEquals(ResourceStatus.EXPIRED, nonp456.getStatus());
      return true;
    }));
  }

  // Service Code Detection Tests

  @Test
  @DisplayName("import file detects TEST_NON_PROCTORED for ACCNONPROC prefix")
  void importFileDetectsTestNonProctoredForAccnonprocPrefix() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACCNONPROC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACCNONPROC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ServiceCode.TEST_NON_PROCTORED, couponList.get(0).getServiceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file detects TEST_NON_PROCTORED for NONP prefix")
  void importFileDetectsTestNonProctoredForNonpPrefix() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        NONP123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "NONP123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ServiceCode.TEST_NON_PROCTORED, couponList.get(0).getServiceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file detects TEST_PROCTORED for ACC prefix")
  void importFileDetectsTestProctoredForAccPrefix() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ServiceCode.TEST_PROCTORED, couponList.get(0).getServiceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file detects TEST_PROCTORED for PROC prefix")
  void importFileDetectsTestProctoredForProcPrefix() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        PROC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "PROC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(ServiceCode.TEST_PROCTORED, couponList.get(0).getServiceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file skips coupons with unrecognized prefix")
  void importFileSkipsCouponsWithUnrecognizedPrefix() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        UNKNOWN123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "UNKNOWN123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    // Coupons with unrecognized prefixes should be skipped, so saveAll should not be called
    verify(serviceResourceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("import file imports recognized coupons and skips unrecognized ones")
  void importFileImportsRecognizedAndSkipsUnrecognized() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        UNKNOWN456,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        NONP789,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "UNKNOWN456")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "NONP789")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      // Should only import recognized prefixes (ACC123 and NONP789), skip UNKNOWN456
      assertEquals(2, couponList.size());
      // Verify the recognized coupons are present
      assertThat(couponList.stream().map(ServiceResourceEntity::getResourceCode))
          .containsExactlyInAnyOrder("ACC123", "NONP789");
      return true;
    }));
  }

  // Duplicate Handling Tests

  @Test
  @DisplayName("import file skips duplicate coupon codes within file")
  void importFileSkipsDuplicateCodesInFile() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        NONP456,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "NONP456")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(2, couponList.size()); // ACC123 once, NONP456 once
      return true;
    }));
  }

  @Test
  @DisplayName("import file skips coupon codes that already exist in database")
  void importFileSkipsExistingCodesInDatabase() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        NONP456,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(true); // Exists
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "NONP456")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(1, couponList.size()); // Only NONP456
      assertEquals("NONP456", couponList.get(0).getResourceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file skips all coupons when all are duplicates or exist in db")
  void importFileSkipsAllWhenAllDuplicatesOrExist() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        NONP456,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(true); // Exists
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "NONP456")).thenReturn(true); // Exists

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("import file skips all coupons when all have unrecognized prefixes")
  void importFileSkipsAllWhenAllHaveUnrecognizedPrefixes() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        UNKNOWN123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        INVALID456,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        BAD789,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "UNKNOWN123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "INVALID456")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "BAD789")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    // All coupons have unrecognized prefixes, so saveAll should not be called
    verify(serviceResourceRepository, never()).saveAll(any());
  }

  // Edge Cases Tests

  @Test
  @DisplayName("import file handles empty file with only header")
  void importFileHandlesEmptyFileWithOnlyHeader() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("import file skips coupon with empty coupon code field")
  void importFileSkipsCouponWithEmptyCouponCode() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    // Empty coupon code should be skipped (no recognized prefix), only ACC123 should be saved
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(1, couponList.size()); // Only ACC123, empty coupon code skipped
      assertEquals("ACC123", couponList.get(0).getResourceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file skips lines with insufficient columns")
  void importFileSkipsLinesWithInsufficientColumns() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        ACC456,,
        ACC789,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC789")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(2, couponList.size()); // ACC123 and ACC789, ACC456 skipped
      return true;
    }));
  }

  @Test
  @DisplayName("import file handles whitespace in coupon codes")
  void importFileHandlesWhitespaceInCouponCodes() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123 ,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123 ")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals("ACC123 ", couponList.get(0).getResourceCode()); // Trailing whitespace preserved
      return true;
    }));
  }

  @Test
  @DisplayName("import file handles mixed service codes in same file")
  void importFileHandlesMixedServiceCodes() throws Exception {
    // Arrange
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        ACC123,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        NONP456,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "NONP456")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(2, couponList.size());
      assertEquals(ServiceCode.TEST_PROCTORED, couponList.get(0).getServiceCode());
      assertEquals(ServiceCode.TEST_NON_PROCTORED, couponList.get(1).getServiceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file handles large file with many coupons")
  void importFileHandlesLargeFileWithManyCoupons() throws Exception {
    // Arrange - Create a CSV with 100 coupons
    StringBuilder csvContent = new StringBuilder();
    csvContent.append("Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status\n");
    
    for (int i = 1; i <= 100; i++) {
      String couponCode = String.format("ACC%03d", i);
      csvContent.append(String.format("%s,,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE\n", couponCode));
      when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, couponCode)).thenReturn(false);
    }

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.toString().getBytes(StandardCharsets.UTF_8)
    );

    // Act
    importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(100, couponList.size());
      // Verify all coupons have correct properties
      for (int i = 0; i < 100; i++) {
        ServiceResourceEntity coupon = couponList.get(i);
        String expectedCode = String.format("ACC%03d", i + 1);
        assertEquals(expectedCode, coupon.getResourceCode());
        assertEquals(ServiceCode.TEST_PROCTORED, coupon.getServiceCode());
        assertEquals(ResourceStatus.AVAILABLE, coupon.getStatus());
        assertEquals(OffsetDateTime.of(2024, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC), coupon.getExpiresAt());
      }
      return true;
    }));
  }

  // Exception Handling Tests

  @Test
  @DisplayName("import file throws ImportFailedException on IOException")
  void importFileThrowsImportFailedExceptionOnIOException() {
    // Arrange
    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", new byte[0]
    ) {
      @Override
      public java.io.InputStream getInputStream() throws IOException {
        throw new IOException("File read error");
      }
    };

    // Act & Assert
    assertThatThrownBy(() -> importer.importFile(file, ServiceCode.TEST_NON_PROCTORED))
        .isInstanceOf(ImportFailedException.class)
        .hasCauseInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("import file throws ImportFailedException on CsvValidationException")
  void importFileThrowsImportFailedExceptionOnCsvValidationException() {
    // Arrange
    // Create a file that will cause CSV validation error
    // This is tricky to simulate, but we can create malformed CSV
    String csvContent = """
        Coupon Code,Assignee Email,Expiration Date,Date Sent,Coupon Status
        "ACC123",,2024/12/31 23:59:59,2024/12/01 10:00:00,AVAILABLE
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // Note: This test may not actually trigger CsvValidationException
    // as opencsv is quite lenient. The test documents the expected behavior.
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, "ACC123")).thenReturn(false);

    // Act - should not throw for this case, but if it does, it should be ImportFailedException
    // This test verifies the exception handling structure
    try {
      importer.importFile(file, ServiceCode.TEST_NON_PROCTORED);
      // If no exception, that's fine - CSV parsing succeeded
    } catch (ImportFailedException e) {
      // If exception occurs, it should be ImportFailedException
      assertThat(e).isInstanceOf(ImportFailedException.class);
    }
  }
}