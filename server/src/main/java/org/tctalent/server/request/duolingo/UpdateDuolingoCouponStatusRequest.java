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

package org.tctalent.server.request.duolingo;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.DuolingoCouponStatus;

/**
 * Represents a request to update the status of a Duolingo coupon.
 * This request includes the coupon code and the new status to be assigned.
 */
@Getter
@Setter
public class UpdateDuolingoCouponStatusRequest {
  /**
   * The unique code of the Duolingo coupon to be updated.
   * This field must not be blank.
   */
  @NotBlank(message = "Coupon code must not be blank")
  private String couponCode;
  /**
   * The new status to be assigned to the Duolingo coupon.
   * This field must not be blank.
   */
  @NotBlank(message = "Status must not be blank")
  private DuolingoCouponStatus status;

}
