import {TestBed} from '@angular/core/testing';

import {LinkedinPremiumCouponService} from './linkedin-premium-coupon.service';

describe('LinkedinPremiumCouponService', () => {
  let service: LinkedinPremiumCouponService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LinkedinPremiumCouponService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
