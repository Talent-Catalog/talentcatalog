import { TestBed } from '@angular/core/testing';

import { DuolingoCouponService } from './duolingo-coupon.service';

describe('DuolingoCouponService', () => {
  let service: DuolingoCouponService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DuolingoCouponService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
