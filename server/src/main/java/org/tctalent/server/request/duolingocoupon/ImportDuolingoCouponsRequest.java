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

package org.tctalent.server.request.duolingocoupon;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.DuolingoCoupon;
/**
 * Represents a request to import a list of Duolingo coupons.
 * This request contains a list of {@link DuolingoCoupon} objects
 * that are validated before being processed.
 */
@Getter
@Setter
public class ImportDuolingoCouponsRequest {
  /**
   * A list of Duolingo coupons to be imported.
   * This list must not be empty and each coupon must be valid.
   */
  @NotEmpty(message = "Coupon list must not be empty")
  @Valid
  private List<DuolingoCoupon> coupons;

}
