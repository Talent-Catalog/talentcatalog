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

package org.tctalent.server.model.db;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Represents a candidate associated with an {@link OfferToAssist} together with an optional
 * coupon code associated with the candidate.
 * @param candidate Candidate associated with an {@link OfferToAssist}
 * @param couponCode Optional coupon code associated with candidate
 * @author John Cameron
 */
public record CandidateCoupon(@NonNull Candidate candidate, @Nullable String couponCode) {}

