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

package org.tctalent.server.data;

import java.time.LocalDateTime;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;

public class DuolingoTestData {

    public static DuolingoCoupon getDuolingoCoupon() {
        DuolingoCoupon coupon = new DuolingoCoupon();
        coupon.setId(1L);
        coupon.setCouponCode("COUPON123");
        coupon.setExpirationDate(LocalDateTime.now().plusDays(30));
        coupon.setDateSent(LocalDateTime.now().minusDays(1));
        coupon.setCouponStatus(DuolingoCouponStatus.AVAILABLE);

        return coupon;
    }
}
