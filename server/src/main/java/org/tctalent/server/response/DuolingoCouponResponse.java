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

@Getter
@Setter
public class DuolingoCouponResponse {
  private Long id;
  private String couponCode;
  private String assigneeEmail;
  private LocalDateTime expirationDate;
  private LocalDateTime dateSent;
  private String couponStatus;
  private String testStatus;
  // Constructor, getters, and setters
  public DuolingoCouponResponse(Long id,String couponCode, String assigneeEmail, LocalDateTime expirationDate,
      LocalDateTime dateSent, String couponStatus, String testStatus) {
    this.id = id;
    this.couponCode = couponCode;
    this.assigneeEmail = assigneeEmail;
    this.expirationDate = expirationDate;
    this.dateSent = dateSent;
    this.couponStatus = couponStatus;
    this.testStatus = testStatus;
  }

}


