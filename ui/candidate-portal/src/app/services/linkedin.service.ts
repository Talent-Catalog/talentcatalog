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
   * Checks a candidate's eligibility for the LinkedIn Candidate Assistance Service.
   * @param candidateId - The ID of the candidate to check
   * @returns Observable<boolean> true if the candidate is eligible
   */
  checkEligibility(candidateId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiBaseUrl}/${candidateId}/eligibility`);
  }

}
