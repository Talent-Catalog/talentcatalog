import {TestBed} from '@angular/core/testing';

import {LinkedinPremiumCouponService} from './linkedin-premium-coupon.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {environment} from "../../environments/environment";

describe('LinkedinPremiumCouponService', () => {
  let service: LinkedinPremiumCouponService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LinkedinPremiumCouponService],
    });

    service = TestBed.inject(LinkedinPremiumCouponService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should import coupons from a CSV file', () => {
    const file = new File(['mock csv content'], 'coupons.csv', { type: 'text/csv' });
    const mockResponse = { success: true };

    service.importCoupons(file).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/services/LINKEDIN/PREMIUM_MEMBERSHIP/import`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('enctype')).toBe('multipart/form-data');
    req.flush(mockResponse);
  });

  it('should get the count of available coupons', () => {
    const mockCount = { count: 42 };

    service.countAvailableCoupons().subscribe(response => {
      expect(response).toEqual(mockCount);
    });

    const req = httpMock.expectOne(
      `${environment.apiUrl}/services/LINKEDIN/PREMIUM_MEMBERSHIP/available/count`
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockCount);
  });
});
