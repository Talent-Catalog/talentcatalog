import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {DuolingoCouponResponse, UpdateCouponStatusRequest} from "../model/duolingo-coupon";
import {environment} from "../../environments/environment";


@Injectable({
  providedIn: 'root',
})
export class DuolingoCouponService {
  private apiBaseUrl = environment.apiUrl+'/coupon';

  constructor(private http: HttpClient) {}

  /**
   * Import coupons from a CSV file
   * @param file - CSV file containing coupons
   */
  importCoupons(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<any>(`${this.apiBaseUrl}/import`, formData, {
      headers: new HttpHeaders({ 'enctype': 'multipart/form-data' }),
    });
  }

  /**
   * Assign an available coupon to a candidate
   * @param candidateId - ID of the candidate
   */
  assignCouponToCandidate(candidateId: number): Observable<DuolingoCouponResponse> {
    return this.http.post<DuolingoCouponResponse>(`${this.apiBaseUrl}/${candidateId}/assign`, {});
  }


  /**
   * Retrieve all coupons assigned to a candidate
   * @param candidateId - ID of the candidate
   */
  getCouponsForCandidate(candidateId: number): Observable<DuolingoCouponResponse[]> {
    return this.http.get<DuolingoCouponResponse[]>(`${this.apiBaseUrl}/${candidateId}`);
  }

  /**
   * Update the status of a specific coupon
   * @param request - Object containing coupon code and new status
   */
  updateCouponStatus(request: UpdateCouponStatusRequest): Observable<void> {
    return this.http.put<void>(`${this.apiBaseUrl}/status`, request);
  }

  /**
   * List all available coupons
   */
  getAvailableCoupons(): Observable<DuolingoCouponResponse[]> {
    return this.http.get<DuolingoCouponResponse[]>(`${this.apiBaseUrl}/available`);
  }

  /**
   * Get a single coupon by its code.
   * @param couponCode - Coupon code to search for.
   */
  getCouponByCode(couponCode: string): Observable<DuolingoCouponResponse> {
    return this.http.get<DuolingoCouponResponse>(`${this.apiBaseUrl}/find/${couponCode}`);
  }

}
