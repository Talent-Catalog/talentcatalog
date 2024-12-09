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

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';

import {DuolingoCouponService} from './duolingo-coupon.service';
import {environment} from '../../environments/environment';
import {DuolingoCouponResponse, Status} from '../model/duolingo-coupon';

describe('DuolingoCouponService', () => {
  let service: DuolingoCouponService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DuolingoCouponService]
    });
    service = TestBed.inject(DuolingoCouponService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#create', () => {
    it('should return an Observable DuolingoCouponResponse', () => {
      const mockResponse: DuolingoCouponResponse = {
        status: 'Success',
        coupon: {
          id: 1,
          couponCode: '123456',
          expirationDate: new Date(),
          dateSent: new Date(),
          duolingoCouponStatus: Status.AVAILABLE
        },
        message: 'Coupon created successfully'
      }

      service.create(1).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/coupon/1/assign`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });
  })
});
