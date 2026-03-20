import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {ServiceAssignment, UpdateServiceResourceStatusRequest} from "../model/services";

/**
 * Service for managing CASI eligibility and assignments in the candidate portal.
 * Provides methods to check eligibility, get current assignments, assign services, and update
 * resource status for candidates.
 *
 * @author sadatmalik
 */
@Injectable({
  providedIn: 'root'
})
export class CasiPortalService {
  private apiBaseUrl = environment.apiUrl + '/services';

  constructor(private http: HttpClient) { }

  checkEligibility(provider: string, serviceCode: string): Observable<boolean> {
    return this.http.get<boolean>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/eligibility`
    );
  }

  getAssignment(provider: string, serviceCode: string): Observable<ServiceAssignment> {
    return this.http.get<ServiceAssignment>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/assignment`
    );
  }

  assign(provider: string, serviceCode: string): Observable<ServiceAssignment> {
    return this.http.post<ServiceAssignment>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/assign`,
      null
    );
  }

  updateResourceStatus(provider: string, serviceCode: string,
      request: UpdateServiceResourceStatusRequest): Observable<void> {
    return this.http.put<void>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/resources/status`,
      request
    );
  }
}
