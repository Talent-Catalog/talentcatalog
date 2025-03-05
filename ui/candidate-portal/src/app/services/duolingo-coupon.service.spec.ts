import {TestBed} from '@angular/core/testing';

import {DuolingoCouponService} from './duolingo-coupon.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {DuolingoCouponResponse, DuolingoCouponStatus} from "../model/duolingo-coupon";
import {environment} from "../../environments/environment";

describe('DuolingoCouponService', () => {
  let service: DuolingoCouponService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DuolingoCouponService],
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

  it('should retrieve coupons for a candidate', () => {
    const candidateId = 123;
    const mockCoupons: DuolingoCouponResponse[] = [
      {
        id: 1,
        couponCode: 'COUPON123',
        duolingoCouponStatus: DuolingoCouponStatus.ASSIGNED,
        expirationDate: '01/12/2024',
        dateSent: '01/10/2020'
      },
      {
        id: 2,
        couponCode: 'COUPON456',
        duolingoCouponStatus: DuolingoCouponStatus.AVAILABLE,
        expirationDate: '01/12/2024',
        dateSent: '01/10/2020'
      },
    ];

    service.getCouponsForCandidate(candidateId).subscribe(coupons => {
      expect(coupons).toEqual(mockCoupons);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/coupon/${candidateId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCoupons);
  });
});
