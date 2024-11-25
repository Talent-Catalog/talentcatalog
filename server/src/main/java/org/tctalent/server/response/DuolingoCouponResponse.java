/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.CouponStatus;

/**
 * Represents a response object for Duolingo coupons.
 * This class contains details about a Duolingo coupon, such as its code,
 * expiration date, and status, which are returned to the client.
 */
@Getter
@Setter
public class DuolingoCouponResponse {

  /** The unique identifier of the Duolingo coupon. */
  private Long id;

  /** The unique code of the Duolingo coupon. */
  private String couponCode;

  /** The expiration date and time of the coupon. */
  private LocalDateTime expirationDate;

  /** The date and time when the coupon was sent to the assignee, if applicable. */
  private LocalDateTime dateSent;

  /** The current status of the coupon (e.g., "Active", "Redeemed", "Expired"). */
  private CouponStatus couponStatus;

  /** The test status associated with the coupon (e.g., "Test Started", "Test Completed"). */
  private String testStatus;

  /**
   * Constructs a new DuolingoCouponResponse object with the specified details.
   *
   * @param id            the unique identifier of the coupon
   * @param couponCode    the unique code of the coupon
   * @param expirationDate the expiration date and time of the coupon
   * @param dateSent      the date and time when the coupon was sent
   * @param couponStatus  the current status of the coupon
   * @param testStatus    the test status associated with the coupon
   */
  public DuolingoCouponResponse(
      Long id,
      String couponCode,
      LocalDateTime expirationDate,
      LocalDateTime dateSent,
      CouponStatus couponStatus,
      String testStatus) {
    this.id = id;
    this.couponCode = couponCode;
    this.expirationDate = expirationDate;
    this.dateSent = dateSent;
    this.couponStatus = couponStatus;
    this.testStatus = testStatus;
  }
}
