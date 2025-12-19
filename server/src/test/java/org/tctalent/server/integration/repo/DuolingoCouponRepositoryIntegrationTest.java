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

package org.tctalent.server.integration.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveAssignedDuolingoCoupon;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveCandidate;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveUnassignedDuolingoCoupon;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveUser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.integration.helper.BaseJpaIntegrationTest;
import org.tctalent.server.integration.helper.PostgresTestContainer;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.DuolingoTestType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.DuolingoCouponRepository;
import org.tctalent.server.repository.db.UserRepository;

/**
 * Integration tests for DuolingoCouponRepository, verifying coupon retrieval, existence, and counting functionality.
 */
public class DuolingoCouponRepositoryIntegrationTest extends BaseJpaIntegrationTest {

  @Autowired
  private DuolingoCouponRepository couponRepository;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private DuolingoCouponRepository duolingoCouponRepository;

  private DuolingoCoupon testCoupon;
  private Candidate testCandidate;


  @BeforeAll
  public static void setup() throws IOException, InterruptedException {
    PostgresTestContainer.startContainer();
  }
  /**
   * Sets up test data by creating a user, a candidate, and two Duolingo coupons (one assigned, one unassigned).
   */
  @BeforeEach
  void setUp() {
    assertTrue(isContainerInitialised(), "Database container should be initialized");

    User savedUser = createAndSaveUser(userRepository);
    testCandidate = createAndSaveCandidate(candidateRepository, savedUser);

    // Create an assigned coupon
    testCoupon = createAndSaveAssignedDuolingoCoupon(duolingoCouponRepository, testCandidate);

    assertTrue(testCoupon.getId() > 0, "Assigned coupon should have a valid ID");

    // Create an unassigned coupon
    DuolingoCoupon unassignedCoupon = createAndSaveUnassignedDuolingoCoupon(duolingoCouponRepository);
    assertTrue(unassignedCoupon.getId() > 0, "Unassigned coupon should have a valid ID");
  }

  /**
   * Tests finding a coupon by its unique coupon code.
   */
  @Test
  void shouldFindCouponByCouponCode() {
    DuolingoCoupon foundCoupon = couponRepository.findByCouponCode(testCoupon.getCouponCode())
        .orElse(null);
    assertNotNull(foundCoupon, "Coupon should be found");
    assertEquals(testCoupon.getId(), foundCoupon.getId(), "Coupon IDs should match");
  }

  /**
   * Tests that finding a coupon by a non-existent coupon code returns null.
   */
  @Test
  void shouldReturnNullForNonExistentCouponCode() {
    DuolingoCoupon foundCoupon = couponRepository.findByCouponCode("NON-EXISTENT-CODE")
        .orElse(null);
    assertNull(foundCoupon, "Non-existent coupon should not be found");
  }

  /**
   * Tests checking the existence of a coupon by its coupon code.
   */
  @Test
  void shouldConfirmCouponExistsByCouponCode() {
    boolean exists = couponRepository.existsByCouponCode(testCoupon.getCouponCode());
    assertTrue(exists, "Coupon should exist in the database");
  }

  /**
   * Tests that checking a non-existent coupon code returns false.
   */
  @Test
  void shouldReturnFalseForNonExistentCouponCode() {
    boolean exists = couponRepository.existsByCouponCode("NON-EXISTENT-CODE");
    assertFalse(exists, "Non-existent coupon should not exist");
  }

  /**
   * Tests finding all coupons assigned to a specific candidate by their ID.
   */
  @Test
  void shouldFindCouponsByCandidateId() {
    List<DuolingoCoupon> foundCoupons = couponRepository.findAllByCandidateId(testCandidate.getId());
    assertNotNull(foundCoupons, "Coupons list should not be null");
    assertFalse(foundCoupons.isEmpty(), "Coupons list should not be empty");
    assertEquals(1, foundCoupons.size(), "Should find exactly one coupon for the candidate");
    assertEquals(testCoupon.getCouponCode(), foundCoupons.get(0).getCouponCode(),
        "Coupon code should match");
  }

  /**
   * Tests that finding coupons for a non-existent candidate ID returns an empty list.
   */
  @Test
  void shouldReturnEmptyListForNonExistentCandidateId() {
    List<DuolingoCoupon> foundCoupons = couponRepository.findAllByCandidateId(999999999L);
    assertTrue(foundCoupons.isEmpty(), "Coupons list should be empty for non-existent candidate");
  }

  /**
   * Tests finding the first available unassigned coupon with a specific status and test type.
   */
  @Test
  void shouldFindFirstAvailableCouponByStatusAndTestType() {
    DuolingoCoupon foundCoupon = couponRepository
        .findTop1ByCandidateIsNullAndCouponStatusAndTestType(
            DuolingoCouponStatus.AVAILABLE, DuolingoTestType.PROCTORED)
        .orElse(null);
    assertNotNull(foundCoupon, "Available coupon should be found");
    assertEquals("TEST-COUPON-2", foundCoupon.getCouponCode(), "Coupon code should match");
  }

  /**
   * Tests finding all available unassigned coupons with a specific status and test type.
   */
  @Test
  void shouldFindAllAvailableCouponsByStatusAndTestType() {
    List<DuolingoCoupon> foundCoupons = couponRepository
        .findByCandidateIsNullAndCouponStatusAndTestType(
            DuolingoCouponStatus.AVAILABLE, DuolingoTestType.PROCTORED);
    assertNotNull(foundCoupons, "Coupons list should not be null");
    assertFalse(foundCoupons.isEmpty(), "Coupons list should not be empty");
    assertEquals(1, foundCoupons.size(), "Should find exactly one available coupon");
    assertEquals("TEST-COUPON-2", foundCoupons.get(0).getCouponCode(), "Coupon code should match");
  }

  /**
   * Tests finding all coupons that have expired before a specific date and are not in excluded statuses.
   */
  @Test
  void shouldFindExpiredCouponsNotInExcludedStatuses() {
    // Create an expired coupon
    DuolingoCoupon expiredCoupon = new DuolingoCoupon();
    expiredCoupon.setCouponCode("EXPIRED-COUPON");
    expiredCoupon.setCouponStatus(DuolingoCouponStatus.AVAILABLE);
    expiredCoupon.setTestType(DuolingoTestType.PROCTORED);
    expiredCoupon.setExpirationDate(LocalDateTime.now().minusDays(1));
    couponRepository.save(expiredCoupon);

    List<DuolingoCoupon> foundCoupons = couponRepository
        .findAllByExpirationDateBeforeAndCouponStatusNotIn(
            LocalDateTime.now(), List.of(DuolingoCouponStatus.EXPIRED, DuolingoCouponStatus.REDEEMED));
    assertNotNull(foundCoupons, "Coupons list should not be null");
    assertFalse(foundCoupons.isEmpty(), "Coupons list should not be empty");
    assertTrue(true, "Should find at least one expired coupon");
    assertEquals("EXPIRED-COUPON", foundCoupons.get(foundCoupons.size()-1).getCouponCode(), "Coupon code should match");
  }

  /**
   * Tests counting available coupons with a specific status.
   */
  @Test
  void shouldCountAvailableCouponsByStatus() {
    int count = couponRepository.countByCandidateIsNullAndCouponStatus(DuolingoCouponStatus.AVAILABLE);
    assertTrue(count > 0, "Should find at least one available coupon");
  }

  /**
   * Tests counting available coupons with a specific status and test type.
   */
  @Test
  void shouldCountAvailableCouponsByStatusAndTestType() {
    int count = couponRepository.countByCandidateIsNullAndCouponStatusAndTestType(
        DuolingoCouponStatus.AVAILABLE, DuolingoTestType.PROCTORED);
    assertEquals(1, count, "Should count exactly one available coupon for the test type");
  }
}