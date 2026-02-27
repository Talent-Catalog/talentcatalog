import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LinkedinService {

  private apiBaseUrl = environment.apiUrl + '/linkedin';

  constructor(private http: HttpClient) { }

  /**
   * Checks if a candidate is eligible for the LinkedIn Premium membership upgrade offer.
   * @param candidateId - ID of candidate
   * @returns Observable<boolean> true if the candidate is eligible
   */
  isEligible(candidateId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiBaseUrl}/${candidateId}/eligibility`);
  }

  /**
   * Checks if a candidate has redeemed the LinkedIn Premium membership upgrade offer.
   * @param candidateId - ID of candidate
   * @returns Observable<boolean> true if the candidate has redeemed the offer
   */
  hasRedeemed(candidateId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiBaseUrl}/${candidateId}/redemption`);
  }

}
