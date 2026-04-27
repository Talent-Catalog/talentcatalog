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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Represents a Duolingo coupon that can be assigned to candidates for accessing Duolingo tests.
 * These coupons are managed and tracked by the system to ensure their proper usage and expiration
 * handling.
 */
@Getter
@Setter
@Entity
@Table(name = "duolingo_coupon")
@SequenceGenerator(name = "seq_gen", sequenceName = "duolingo_coupon_id_seq", allocationSize = 1)
public class DuolingoCoupon extends AbstractDomainObject<Long> {
  /**
   * Unique code representing the Duolingo coupon. This code is used by candidates to redeem the
   * coupon.
   */
  @Column(name = "coupon_code", unique = true)
  private String couponCode;
  /**
   * The candidate to whom this coupon is assigned. Can be null if the coupon is not yet assigned.
   */
  @Nullable
  @ManyToOne
  @JoinColumn(name = "candidate_id")
  private Candidate candidate;
  /**
   * The date and time when the coupon expires. After this date, the coupon becomes invalid.
   */
  @Column(name = "expiration_date")
  private LocalDateTime expirationDate;
  /**
   * The date and time when the coupon was sent to the candidate. Can be null if the coupon has not
   * been sent yet.
   */
  @Nullable
  @Column(name = "date_sent")
  private LocalDateTime dateSent;
  /**
   * Current status of the coupon (e.g., "Available", "Assigned", "Redeemed"). This field tracks the
   * lifecycle of the coupon.
   */
  @Column(name = "coupon_status")
  @Enumerated(EnumType.STRING)
  private DuolingoCouponStatus couponStatus;

  /**
   * The type of Duolingo test.
   * Stored as a string in the database.
   */
  @Column(name = "test_type")
  @Enumerated(EnumType.STRING)
  private DuolingoTestType testType;
}
