import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DuolingoCouponService } from './duolingo-coupon.service';
import { environment } from '../../environments/environment';
import {
  DuolingoCouponResponse,
  DuolingoCouponStatus,
  UpdateCouponStatusRequest
} from '../model/duolingo-coupon';

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

  it('should import coupons from a CSV file', () => {
    const file = new File(['mock csv content'], 'coupons.csv', { type: 'text/csv' });
    const mockResponse = { success: true };

    service.importCoupons(file).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/coupon/import`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('enctype')).toBe('multipart/form-data');
    req.flush(mockResponse);
  });

  it('should assign a coupon to a candidate', () => {
    const candidateId = 123;
    const mockResponse = { success: true };

    service.assignCouponToCandidate(candidateId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/coupon/${candidateId}/assign`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should retrieve coupons for a candidate', () => {
    const candidateId = 123;
    const mockCoupons: DuolingoCouponResponse[] = [
      { id: 1, couponCode: 'COUPON123', couponStatus:DuolingoCouponStatus.ASSIGNED,expirationDate:'01/12/2024',dateSent:'01/10/2020' },
      { id: 2, couponCode: 'COUPON456', couponStatus:DuolingoCouponStatus.AVAILABLE,expirationDate:'01/12/2024',dateSent:'01/10/2020'},
    ];

    service.getCouponsForCandidate(candidateId).subscribe(coupons => {
      expect(coupons).toEqual(mockCoupons);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/coupon/${candidateId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCoupons);
  });

  it('should retrieve available coupons', () => {
    const mockCoupons: DuolingoCouponResponse[] = [
      { id: 1, couponCode: 'COUPON123', couponStatus:DuolingoCouponStatus.ASSIGNED,expirationDate:'01/12/2024',dateSent:'01/10/2020' },
      { id: 2, couponCode: 'COUPON456', couponStatus:DuolingoCouponStatus.AVAILABLE,expirationDate:'01/12/2024',dateSent:'01/10/2020'},
    ];

    service.getAvailableCoupons().subscribe(coupons => {
      expect(coupons).toEqual(mockCoupons);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/coupon/available`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCoupons);
  });

  it('should retrieve a coupon by code', () => {
    const couponCode = 'COUPON123';
    const mockCoupon = { id: 1, code: 'COUPON123', status: 'available' };

    service.getCouponByCode(couponCode).subscribe(coupon => {
      expect(coupon).toEqual(mockCoupon);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/coupon/find/${couponCode}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCoupon);
  });
});
