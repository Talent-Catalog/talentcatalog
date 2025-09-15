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

package org.tctalent.server.candidateservices.infrastructure.importers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.opencsv.exceptions.CsvValidationException;
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
import org.tctalent.server.candidateservices.domain.model.ResourceStatus;
import org.tctalent.server.candidateservices.domain.model.ServiceCode;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceEntity;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceRepository;

class DuolingoCouponImporterTest {

  private static final String PROVIDER = "DUOLINGO";

  @Mock
  private ServiceResourceRepository serviceResourceRepository;

  @InjectMocks
  private DuolingoCouponImporter importer;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("importCoupons - successfully imports coupons")
  void testImportCoupons() throws IOException, CsvValidationException {
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
    when(serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER,"code2")).thenReturn(false);

    // Act
    importer.importFile(file, ServiceCode.DUOLINGO_TEST_NON_PROCTORED.name());

    // Assert
    verify(serviceResourceRepository, times(1)).saveAll(argThat(coupons -> {
      // Convert Iterable to List for easier assertions
      List<ServiceResourceEntity> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();

      // Verify the size and contents
      assertEquals(2, couponList.size());
      assertEquals("code1", couponList.get(0).getResourceCode());
      assertEquals(LocalDateTime.of(2024, 12, 31, 23, 59, 59), couponList.get(0).getExpiresAt());
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(0).getStatus());

      assertEquals("code2", couponList.get(1).getResourceCode());
      assertEquals(ResourceStatus.AVAILABLE, couponList.get(1).getStatus());

      return true;
    }));
  }
}