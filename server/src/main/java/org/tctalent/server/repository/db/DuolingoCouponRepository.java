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

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;

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
   * Finds the first available coupon (if any) that has not been assigned to any candidate
   * and matches a specific coupon status.
   * <p/>
   * This method is useful for allocating unassigned coupons with a particular status.
   *
   * @param couponStatus the status of the coupon (e.g., "ACTIVE").
   * @return an Optional containing the first unassigned Coupon matching the status if found, or empty otherwise.
   */
  Optional<DuolingoCoupon> findTop1ByCandidateIsNullAndCouponStatus(
      DuolingoCouponStatus couponStatus);

  /**
   * Finds all available coupons that are not assigned to any candidate and match
   * a specific coupon status.
   * <p/>
   * This method retrieves all coupons that meet the criteria for bulk processing or reporting.
   *
   * @param couponStatus the status of the coupons to retrieve (e.g., "ACTIVE").
   * @return a List of Coupons that are unassigned and match the specified status.
   */
  List<DuolingoCoupon> findByCandidateIsNullAndCouponStatus(DuolingoCouponStatus couponStatus);
}
