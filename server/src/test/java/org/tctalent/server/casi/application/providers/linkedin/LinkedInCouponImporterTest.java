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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

class LinkedInCouponImporterTest {

  private static final ServiceProvider PROVIDER = ServiceProvider.LINKEDIN;
  private static final ServiceCode SERVICE_CODE = ServiceCode.PREMIUM_MEMBERSHIP;
  private static final String COUPON_001 =
      "https://www.linkedin.com/premium/redeem/promo?coupon=COUPON001";
  private static final String COUPON_002 =
      "https://www.linkedin.com/premium/redeem/promo?coupon=COUPON002";

  @Mock
  private ServiceResourceRepository serviceResourceRepository;

  @InjectMocks
  private LinkedInCouponImporter importer;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("import file successfully imports valid coupons with dd/MM/yyyy date")
  void importFileSucceedsWithDdMmYyyyDate() {
    String csvContent = "Serial #,Premium Code,Activate by\n"
        + "1001," + COUPON_001 + ",31/12/2026\n"
        + "1002," + COUPON_002 + ",31/12/2026\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, COUPON_001))
        .thenReturn(false);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, COUPON_002))
        .thenReturn(false);

    importer.importFile(file, SERVICE_CODE);

    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList =
          StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(2, couponList.size());
      assertEquals(COUPON_001, couponList.get(0).getResourceCode());
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(0).getStatus());
      assertEquals(SERVICE_CODE, couponList.get(0).getServiceCode());
      assertEquals(PROVIDER, couponList.get(0).getProvider());
      assertEquals(OffsetDateTime.of(2026, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC), couponList.get(0).getExpiresAt());
      assertEquals(COUPON_002, couponList.get(1).getResourceCode());
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(1).getStatus());
      return true;
    }));
  }

  @Test
  @DisplayName("import file succeeds with MM/dd/yyyy date format")
  void importFileSucceedsWithMmDdYyyyDate() {
    String csvContent = "Serial #,Premium Code,Activate by\n"
        + "1001," + COUPON_001 + ",12/31/2026\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(any(), any()))
        .thenReturn(false);

    importer.importFile(file, SERVICE_CODE);

    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList =
          StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(1, couponList.size());
      assertEquals(OffsetDateTime.of(2026, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC), couponList.get(0).getExpiresAt());
      return true;
    }));
  }

  @Test
  @DisplayName("import file throws ImportFailedException when header row is missing")
  void importFileThrowsWhenHeaderMissing() {
    MockMultipartFile file = new MockMultipartFile(
        "file", "empty.csv", "text/csv", new byte[0]
    );

    assertThatThrownBy(() -> importer.importFile(file, SERVICE_CODE))
        .isInstanceOf(ImportFailedException.class)
        .hasMessageContaining("CSV header is missing");

    verify(serviceResourceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("import file throws ImportFailedException when required column is missing")
  void importFileThrowsWhenRequiredColumnMissing() {
    String csvContent = """
        Serial #,Wrong Column,Activate by
        1001,COUPON001,31/12/2026
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    assertThatThrownBy(() -> importer.importFile(file, SERVICE_CODE))
        .isInstanceOf(ImportFailedException.class)
        .hasMessageContaining("Missing required column: premium code");

    verify(serviceResourceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("import file skips duplicate coupon codes within the file")
  void importFileSkipsDuplicatesWithinFile() {
    // Both rows have the same coupon code
    String csvContent = "Serial #,Premium Code,Activate by\n"
        + "1001," + COUPON_001 + ",31/12/2026\n"
        + "1002," + COUPON_001 + ",31/12/2026\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(any(), any()))
        .thenReturn(false);

    importer.importFile(file, SERVICE_CODE);

    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList =
          StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(1, couponList.size());
      return true;
    }));
  }

  @Test
  @DisplayName("import file skips coupons that already exist in the database")
  void importFileSkipsExistingCouponsInDatabase() {
    String csvContent = "Serial #,Premium Code,Activate by\n"
        + "1001," + COUPON_001 + ",31/12/2026\n"
        + "1002," + COUPON_002 + ",31/12/2026\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, COUPON_001))
        .thenReturn(true);
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, COUPON_002))
        .thenReturn(false);

    importer.importFile(file, SERVICE_CODE);

    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList =
          StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(1, couponList.size());
      assertEquals(COUPON_002, couponList.get(0).getResourceCode());
      return true;
    }));
  }

  @Test
  @DisplayName("import file does not call saveAll when all coupons already exist in database")
  void importFileDoesNotSaveWhenAllExist() {
    String csvContent = "Serial #,Premium Code,Activate by\n"
        + "1001," + COUPON_001 + ",31/12/2026\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(any(), any()))
        .thenReturn(true);

    importer.importFile(file, SERVICE_CODE);

    verify(serviceResourceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("import file handles BOM character in header")
  void importFileHandlesBomInHeader() {
    String csvContent = "\uFEFFSerial #,Premium Code,Activate by\n"
        + "1001," + COUPON_001 + ",31/12/2026\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(any(), any()))
        .thenReturn(false);

    importer.importFile(file, SERVICE_CODE);

    verify(serviceResourceRepository, times(1)).saveAll(any());
  }

  @Test
  @DisplayName("import file throws ImportFailedException for unrecognized date format")
  void importFileThrowsForInvalidDateFormat() {
    String csvContent = "Serial #,Premium Code,Activate by\n"
        + "1001," + COUPON_001 + ",2026-31-12\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(any(), any()))
        .thenReturn(false);

    assertThatThrownBy(() -> importer.importFile(file, SERVICE_CODE))
        .isInstanceOf(ImportFailedException.class);
  }

  @Test
  @DisplayName("import file does not call saveAll when file has only a header row")
  void importFileDoesNotSaveWhenOnlyHeader() {
    String csvContent = "Serial #,Premium Code,Activate by\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    importer.importFile(file, SERVICE_CODE);

    verify(serviceResourceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("import file stores null expiry when activate by date is empty")
  void importFileStoresNullExpiryForEmptyDate() {
    // Trailing comma ensures the empty field is still parsed as column 3
    String csvContent = "Serial #,Premium Code,Activate by\n"
        + "1001," + COUPON_001 + ",\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(any(), any()))
        .thenReturn(false);

    importer.importFile(file, SERVICE_CODE);

    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList =
          StreamSupport.stream(coupons.spliterator(), false).toList();
      assertEquals(1, couponList.size());
      assertNull(couponList.get(0).getExpiresAt());
      return true;
    }));
  }

  @Test
  @DisplayName("import file handles case-insensitive and whitespace-padded headers")
  void importFileHandlesPaddedHeaders() {
    String csvContent = " Serial # , Premium Code , Activate by \n"
        + "1001," + COUPON_001 + ",31/12/2026\n";

    MockMultipartFile file = new MockMultipartFile(
        "file", "coupons.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
    );

    when(serviceResourceRepository.existsByProviderAndResourceCode(any(), any()))
        .thenReturn(false);

    importer.importFile(file, SERVICE_CODE);

    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      List<ServiceResourceEntity> couponList =
          StreamSupport.stream(coupons.spliterator(), false).toList();
      assertThat(couponList).hasSize(1);
      return true;
    }));
  }
}
