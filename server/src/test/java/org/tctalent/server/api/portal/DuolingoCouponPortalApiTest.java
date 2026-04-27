package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.service.db.DuolingoCouponService;

class DuolingoCouponPortalApiTest {

  @Mock
  private DuolingoCouponService couponService;

  @InjectMocks
  private DuolingoCouponPortalApi duolingoCouponPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetCouponsForCandidate_Success() {
    Long candidateId = 1L;
    List<DuolingoCouponResponse> coupons = List.of(createSampleCouponResponse());
    when(couponService.getCouponsForCandidate(candidateId)).thenReturn(coupons);

    List<DuolingoCouponResponse> result = duolingoCouponPortalApi.getCouponsForCandidate(
        candidateId);

    assertNotNull(result);
    assertEquals(1, result.size());
    DuolingoCouponResponse coupon = result.get(0);
    assertEquals("COUPON123", coupon.getCouponCode());
    assertEquals(DuolingoCouponStatus.AVAILABLE, coupon.getDuolingoCouponStatus());
    verify(couponService).getCouponsForCandidate(candidateId);
  }

  @Test
  void testGetCouponsForCandidate_EmptyList() {
    Long candidateId = 1L;
    when(couponService.getCouponsForCandidate(candidateId)).thenReturn(Collections.emptyList());

    List<DuolingoCouponResponse> result = duolingoCouponPortalApi.getCouponsForCandidate(
        candidateId);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(couponService).getCouponsForCandidate(candidateId);
  }

  private DuolingoCouponResponse createSampleCouponResponse() {
    DuolingoCouponResponse coupon = new DuolingoCouponResponse(1L, "COUPON123",
        LocalDateTime.of(2025, 3, 1, 12, 0),
        LocalDateTime.now(), DuolingoCouponStatus.AVAILABLE);
    coupon.setCouponCode("COUPON123");
    coupon.setDuolingoCouponStatus(DuolingoCouponStatus.AVAILABLE);
    return coupon;
  }
}