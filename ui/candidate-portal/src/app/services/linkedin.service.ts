import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {
  IssueReportRequest,
  ServiceAssignment,
  UpdateServiceResourceStatusRequest
} from "../model/services";

@Injectable({
  providedIn: 'root'
})
export class LinkedinService {

  private apiBaseUrl = environment.apiUrl + '/linkedin';

  constructor(private http: HttpClient) { }

  /**
   * Checks if a candidate is eligible for the LinkedIn Premium membership upgrade offer.
   *
   * @returns Observable<boolean> true if the candidate is eligible
   */
  isEligible(candidateId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiBaseUrl}/${candidateId}/eligibility`);
  }

  /**
   * Returns the candidate's assignment to an RESERVED resource, or a REDEEMED resource
   * if no RESERVED one exists, or null if neither is found.
   *
   * @returns Observable<boolean> true if the candidate has redeemed the offer
   */
  findAssignmentWithReservedOrRedeemedResource(candidateId: number): Observable<ServiceAssignment> {
    return this.http.get<ServiceAssignment>(`${this.apiBaseUrl}/${candidateId}/find-assignment`);
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

  /**
   * Adds candidate to #LinkedInIssueReport List with assignment details and issue comment as
   * context note.
   */
  addCandidateToIssueReportList(request: IssueReportRequest): Observable<void> {
    return this.http.post<void>(`${this.apiBaseUrl}/issue-report`, request);
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
