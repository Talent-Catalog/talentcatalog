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

package org.tctalent.server.casi.domain.model;

/**
 * Status of a resource (e.g., a coupon code).
 *
 * @author sadatmalik
 */
public enum ResourceStatus {
  AVAILABLE,   // unallocated, ready to use
  RESERVED,    // allocated to a candidate, not yet sent
  SENT,        // sent/communicated to candidate
  REDEEMED,    // used by candidate
  EXPIRED,     // no longer valid
  DISABLED     // manually disabled/blocked

}
