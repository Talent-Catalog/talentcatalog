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

package org.tctalent.server.api.portal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.service.db.DuolingoCouponService;

import java.util.List;

@RestController
@RequestMapping("/api/portal/coupon")
@Slf4j
public class DuolingoCouponPortalApi {

    private final DuolingoCouponService couponService;

    @Autowired
    public DuolingoCouponPortalApi(DuolingoCouponService couponService) {
        this.couponService = couponService;
    }

    // Endpoint to retrieve all coupons assigned to a candidate
    @GetMapping("{candidateId}")
    public List<DuolingoCouponResponse> getCouponsForCandidate(
            @PathVariable("candidateId") Long candidateId) {
        return couponService.getCouponsForCandidate(candidateId);
    }

}
