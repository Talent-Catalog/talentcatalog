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

import {DuolingoCoupon, Status} from "./duolingo-coupon";

describe('DuolingoCoupon Interface', () => {
  it('should create a valid DuolingoCoupon object', () => {

    const expirationDate = new Date();
    const dateSent = new Date();

    // Mock data
    const duolingoCoupon: DuolingoCoupon = {
      id: 1,
      couponCode: '123456',
      expirationDate: expirationDate,
      dateSent: dateSent,
      duolingoCouponStatus: Status.SENT
    };

    // Assertions
    expect(duolingoCoupon.id).toEqual(1);
    expect(duolingoCoupon.couponCode).toEqual('123456');
    expect(duolingoCoupon.expirationDate).toEqual(expirationDate);
    expect(duolingoCoupon.dateSent).toEqual(dateSent);
    expect(duolingoCoupon.duolingoCouponStatus).toEqual(Status.SENT);
  });
});
