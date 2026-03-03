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
   * Checks if a candidate already has a redeemed or assigned LinkedIn Premium membership coupon.
   * @returns Observable<boolean> true if the candidate has redeemed the offer
   */
  findRedeemedOrAssignedCoupon(candidateId: number): Observable<ServiceAssignment> {
    return this.http.get<ServiceAssignment>(`${this.apiBaseUrl}/${candidateId}/assignment-check`);
  }

  /** Assigns a single coupon for the LinkedIn Premium membership upgrade offer. */
  assign(candidateId: number): Observable<ServiceAssignment> {
    return this.http.post<ServiceAssignment>(`${this.apiBaseUrl}/${candidateId}/assign`, null);
  }

  /** Updates status of a single coupon for the LinkedIn Premium membership upgrade offer. */
  updateCouponStatus(updateServiceResourceStatusRequest: UpdateServiceResourceStatusRequest):
    Observable<void> {
    return this.http.put<void>(
      `${this.apiBaseUrl}/update-coupon-status`,
      updateServiceResourceStatusRequest
    );
  }

  /** Adds candidate to #LinkedInIssueReport List with assignment details as context note. */
  addCandidateToIssueReportList(assignment: ServiceAssignment): Observable<void> {
    return this.http.post<void>(`${this.apiBaseUrl}/issue-report`, assignment);
  }

  /** Checks if candidate is on #LinkedInIssueReport List. */
  isOnIssueReportList(candidateId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiBaseUrl}/${candidateId}/issue-report`);
  }

  /** Checks if candidate is on #LinkedInAssignmentFailure List. */
  isOnAssignmentFailureList(candidateId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiBaseUrl}/${candidateId}/assignment-failure`);
  }

}
