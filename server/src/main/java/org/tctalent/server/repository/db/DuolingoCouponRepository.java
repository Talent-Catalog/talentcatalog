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

package org.tctalent.server.repository.db;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.DuolingoTestType;

/**
 * Repository interface for managing Coupon entities in the database.
 * <p/>
 * This interface extends JpaRepository, providing basic CRUD operations
 * and custom queries to retrieve Coupon records based on various conditions.
 */
@Repository
public interface DuolingoCouponRepository extends JpaRepository<DuolingoCoupon, Long> {

  /**
   * Finds a coupon by its unique coupon code.
   * <p/>
   * This method will return an Optional containing the coupon if found, or
   * an empty Optional if no coupon matches the provided code.
   *
   * @param couponCode the unique code identifying the coupon.
   * @return an Optional containing the Coupon if found, otherwise empty.
   */
  Optional<DuolingoCoupon> findByCouponCode(String couponCode);

  /**
   * Checks if a coupon exists in the database with the specified coupon code.
   * <p/>
   * This is useful to avoid duplicate entries based on the unique coupon code.
   *
   * @param couponCode the unique code identifying the coupon.
   * @return true if a coupon with the specified code exists, false otherwise.
   */
  boolean existsByCouponCode(String couponCode);

  /**
   * Finds all coupons assigned to a specific candidate by candidate ID.
   * <p/>
   * This method retrieves a list of all coupons associated with a given
   * candidate, identified by their unique candidate ID.
   *
   * @param candidateId the ID of the candidate to whom the coupons are assigned.
   * @return a List of Coupons assigned to the specified candidate.
   */
  List<DuolingoCoupon> findAllByCandidateId(Long candidateId);

  /**
   * Finds the first available coupon (if any) that has not been assigned to any candidate,
   * matches a specific coupon status, and is designated for a Proctored test.
   * <p/>
   * This method is useful for allocating unassigned coupons with a particular status and test type.
   *
   * @param couponStatus the status of the coupon (e.g., "AVAILABLE").
   * @param testType the type of Duolingo test (e.g., "PROCTORED").
   * @return an Optional containing the first unassigned Coupon matching the status and test type if found, or empty otherwise.
   */
  Optional<DuolingoCoupon> findTop1ByCandidateIsNullAndCouponStatusAndTestType(
      DuolingoCouponStatus couponStatus, DuolingoTestType testType);

  /**
   * Finds all available coupons that are not assigned to any candidate,
   * match a specific coupon status, and correspond to a particular test type.
   * <p/>
   * This method retrieves all coupons that meet the criteria for bulk processing or reporting.
   *
   * @param couponStatus the status of the coupons to retrieve (e.g., "ACTIVE").
   * @param testType the type of Duolingo test (e.g., "PROCTORED").
   * @return a List of Coupons that are unassigned and match the specified status and test type.
   */
  List<DuolingoCoupon> findByCandidateIsNullAndCouponStatusAndTestType(
      DuolingoCouponStatus couponStatus, DuolingoTestType testType);

  /**
   * Retrieves all coupons that have expired before the specified date
   * and are not in the given list of statuses.
   * <p>
   * This method is useful for identifying coupons that should be marked as expired,
   * while excluding those that are already expired or redeemed.
   *
   * @param expirationDate the expiration date to check against.
   * @param couponStatuses a list of statuses to exclude from the result (e.g., EXPIRED, REDEEMED).
   * @return a List of coupons that have expired and are not in the excluded statuses.
   */
  List<DuolingoCoupon> findAllByExpirationDateBeforeAndCouponStatusNotIn(
      LocalDateTime expirationDate, List<DuolingoCouponStatus> couponStatuses);

  /**
   * Counts the number of coupons that are available, meaning they are not assigned
   * to any candidate and have a specific coupon status.
   * <p/>
   * This method is useful for determining how many coupons are available for assignment,
   * based on the coupon status (e.g., "AVAILABLE").
   *
   * @param couponStatus the status of the coupons to count (e.g., "AVAILABLE").
   * @return the number of available coupons with the specified status.
   */
  int countByCandidateIsNullAndCouponStatus(DuolingoCouponStatus couponStatus);

  /**
   * Counts the number of available coupons that are not assigned to any candidate,
   * have the specified coupon status, and match the given test type.
   *
   * This method helps determine how many coupons are currently unassigned and
   * available for use based on their status (e.g., "AVAILABLE") and test type.
   *
   * @param couponStatus the status of the coupons to count (e.g., "AVAILABLE").
   * @param testType     the type of test associated with the coupons.
   * @return the number of unassigned coupons matching the specified status and test type.
   */
  int countByCandidateIsNullAndCouponStatusAndTestType(DuolingoCouponStatus couponStatus, DuolingoTestType testType);

}
