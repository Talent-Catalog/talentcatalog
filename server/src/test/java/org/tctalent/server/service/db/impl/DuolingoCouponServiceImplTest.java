/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.DuolingoCouponRepository;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.service.db.email.EmailHelper;

class DuolingoCouponServiceImplTest {

  @Mock
  private DuolingoCouponRepository couponRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private EmailHelper emailHelper;

  @InjectMocks
  private DuolingoCouponServiceImpl couponService;

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

    when(couponRepository.existsByCouponCode("code1")).thenReturn(false);
    when(couponRepository.existsByCouponCode("code2")).thenReturn(false);

    // Act
    couponService.importCoupons(file);

    // Assert
    verify(couponRepository, times(1)).saveAll(argThat(coupons -> {
      // Convert Iterable to List for easier assertions
      List<DuolingoCoupon> couponList = StreamSupport.stream(coupons.spliterator(), false).toList();

      // Verify the size and contents
      assertEquals(2, couponList.size());
      assertEquals("code1", couponList.get(0).getCouponCode());
      assertEquals(LocalDateTime.of(2024, 12, 31, 23, 59, 59), couponList.get(0).getExpirationDate());
      assertEquals(DuolingoCouponStatus.AVAILABLE, couponList.get(0).getCouponStatus());

      assertEquals("code2", couponList.get(1).getCouponCode());
      assertEquals(DuolingoCouponStatus.AVAILABLE, couponList.get(1).getCouponStatus());

      return true;
    }));
  }

  @Test
  @DisplayName("assignCouponToCandidate - assigns coupon successfully")
  void testAssignCouponToCandidate() {
    Candidate candidate = new Candidate();
    candidate.setId(1L);

    DuolingoCoupon coupon = new DuolingoCoupon();
    coupon.setCouponCode("code1");
    coupon.setCouponStatus(DuolingoCouponStatus.AVAILABLE);

    when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
    when(couponRepository.findTop1ByCandidateIsNullAndCouponStatus(DuolingoCouponStatus.AVAILABLE))
        .thenReturn(Optional.of(coupon));

    DuolingoCoupon assignedCoupon = couponService.assignCouponToCandidate(1L);

    assertNotNull(assignedCoupon);
    assertEquals("code1", assignedCoupon.getCouponCode());
    assertEquals(DuolingoCouponStatus.SENT, assignedCoupon.getCouponStatus());
    verify(couponRepository, times(1)).save(coupon);
    verify(emailHelper, times(1)).sendDuolingoCouponEmail(candidate.getUser());
  }

  @Test
  @DisplayName("assignCouponToCandidate - throws NoSuchObjectException if candidate not found")
  void testAssignCouponToCandidateThrowsForMissingCandidate() {
    when(candidateRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> couponService.assignCouponToCandidate(1L));
  }

  @Test
  @DisplayName("getCouponsForCandidate - returns coupons for a candidate")
  void testGetCouponsForCandidate() {
    DuolingoCoupon coupon = new DuolingoCoupon();
    coupon.setCouponCode("code1");

    when(couponRepository.findAllByCandidateId(1L)).thenReturn(List.of(coupon));

    List<DuolingoCouponResponse> responses = couponService.getCouponsForCandidate(1L);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    assertEquals("code1", responses.get(0).getCouponCode());
  }

  @Test
  @DisplayName("updateCouponStatus - updates coupon status")
  void testUpdateCouponStatus() {
    DuolingoCoupon coupon = new DuolingoCoupon();
    coupon.setCouponCode("code1");

    when(couponRepository.findByCouponCode("code1")).thenReturn(Optional.of(coupon));

    couponService.updateCouponStatus("code1", DuolingoCouponStatus.EXPIRED);

    assertEquals(DuolingoCouponStatus.EXPIRED, coupon.getCouponStatus());
    verify(couponRepository, times(1)).save(coupon);
  }

  @Test
  @DisplayName("getAvailableCoupons - returns available coupons")
  void testGetAvailableCoupons() {
    DuolingoCoupon coupon = new DuolingoCoupon();
    coupon.setCouponCode("code1");
    coupon.setCouponStatus(DuolingoCouponStatus.AVAILABLE);

    when(couponRepository.findByCandidateIsNullAndCouponStatus(DuolingoCouponStatus.AVAILABLE))
        .thenReturn(List.of(coupon));

    List<DuolingoCoupon> coupons = couponService.getAvailableCoupons();

    assertNotNull(coupons);
    assertEquals(1, coupons.size());
    assertEquals("code1", coupons.get(0).getCouponCode());
  }

  @Test
  @DisplayName("findByCouponCode - returns coupon by code")
  void testFindByCouponCode() {
    DuolingoCoupon coupon = new DuolingoCoupon();
    coupon.setCouponCode("code1");

    when(couponRepository.findByCouponCode("code1")).thenReturn(Optional.of(coupon));

    DuolingoCoupon result = couponService.findByCouponCode("code1");

    assertNotNull(result);
    assertEquals("code1", result.getCouponCode());
  }

  @Test
  @DisplayName("findByCouponCode - throws NoSuchObjectException if coupon not found")
  void testFindByCouponCodeThrowsForMissingCoupon() {
    when(couponRepository.findByCouponCode("code1")).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> couponService.findByCouponCode("code1"));
  }

  @Test
  @DisplayName("markCouponsAsExpired - updates expired coupons")
  void testMarkCouponsAsExpired() {
    // Arrange
    DuolingoCoupon coupon1 = new DuolingoCoupon();
    coupon1.setCouponCode("expired1");
    coupon1.setExpirationDate(LocalDateTime.now().minusDays(1));
    coupon1.setCouponStatus(DuolingoCouponStatus.AVAILABLE);

    DuolingoCoupon coupon2 = new DuolingoCoupon();
    coupon2.setCouponCode("expired2");
    coupon2.setExpirationDate(LocalDateTime.now().minusDays(2));
    coupon2.setCouponStatus(DuolingoCouponStatus.SENT);

    List<DuolingoCoupon> expiredCoupons = List.of(coupon1, coupon2);

    when(couponRepository.findAllByExpirationDateBeforeAndCouponStatusNotIn(
        any(LocalDateTime.class), anyList()))
        .thenReturn(expiredCoupons);

    // Act
    couponService.markCouponsAsExpired();

    // Assert
    assertEquals(DuolingoCouponStatus.EXPIRED, coupon1.getCouponStatus());
    assertEquals(DuolingoCouponStatus.EXPIRED, coupon2.getCouponStatus());
    verify(couponRepository, times(1)).saveAll(expiredCoupons);
  }

}
