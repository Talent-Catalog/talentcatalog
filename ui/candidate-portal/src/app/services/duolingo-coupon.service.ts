import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {DuolingoCouponResponse} from "../model/duolingo-coupon";

@Injectable({
  providedIn: 'root'
})
export class DuolingoCouponService {

  private apiBaseUrl = environment.apiUrl+'/coupon';

  constructor(private http: HttpClient) {}

  /**
   * Retrieve all coupons assigned to a candidate
   * @param candidateId - ID of the candidate
   */
  getCouponsForCandidate(candidateId: number): Observable<DuolingoCouponResponse[]> {
    return this.http.get<DuolingoCouponResponse[]>(`${this.apiBaseUrl}/${candidateId}`);
  }
}
