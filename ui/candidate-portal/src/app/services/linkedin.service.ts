import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {ServiceAssignment, UpdateServiceResourceStatusRequest} from "../model/services";

@Injectable({
  providedIn: 'root'
})
export class LinkedinService {

  private apiBaseUrl = environment.apiUrl + '/linkedin';

  constructor(private http: HttpClient) { }

  /**
   * Checks if a candidate is eligible for the LinkedIn Premium membership upgrade offer.
   * @returns Observable<boolean> true if the candidate is eligible
   */
  isEligible(candidateId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiBaseUrl}/${candidateId}/eligibility`);
  }

  /**
   * Checks if a candidate has redeemed the LinkedIn Premium membership upgrade offer.
   * @returns Observable<boolean> true if the candidate has redeemed the offer
   */
  hasRedeemed(candidateId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiBaseUrl}/${candidateId}/redemption`);
  }

  /** Assigns a single coupon for the LinkedIn Premium membership upgrade offer. */
  assign(candidateId: number): Observable<ServiceAssignment> {
    return this.http.post<ServiceAssignment>(`${this.apiBaseUrl}/${candidateId}/assign`, null);
  }

  /** Updates status of a single coupon for the LinkedIn Premium membership upgrade offer. */
  updateCouponStatus(updateServiceResourceStatusRequest: UpdateServiceResourceStatusRequest):
    Observable<void> {
    return this.http.post<void>(
      `${this.apiBaseUrl}/update-coupon-status`,
      updateServiceResourceStatusRequest
    );
  }

}
