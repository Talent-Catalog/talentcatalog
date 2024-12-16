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

package org.tctalent.server.model.db;

/**
 * Represents the various statuses that a Duolingo coupon can have.
 * These statuses reflect the lifecycle and state of a coupon within the system.
 */
public enum DuolingoCouponStatus {

  /**
   * The coupon is available for assignment but has not been allocated to any candidate.
   */
  AVAILABLE,

  /**
   * The coupon has been assigned to a candidate.
   */
  ASSIGNED,

  /**
   * The coupon has been sent to the candidate via email or other communication methods.
   */
  SENT,

  /**
   * The coupon has been redeemed by the candidate.
   */
  REDEEMED,

  /**
   * The coupon is no longer valid due to reaching its expiration date or other conditions.
   */
  EXPIRED;
}

