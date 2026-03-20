/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";


/**
 * Service for managing CASI inventory and assignments in the admin portal.
 * Provides methods to import inventory, count available items, and assign items to candidates or lists.
 *
 * @author sadatmalik
 */
@Injectable({
  providedIn: 'root'
})
export class CasiAdminService {
  private apiBaseUrl = environment.apiUrl + '/services';

  constructor(private http: HttpClient) { }

  /**
   * Import inventory from a CSV file
   * @param provider - The provider of the service (e.g., "LINKEDIN", "DUOLINGO", "REFERENCE")
   * @param serviceCode - The specific service code (e.g., "PREMIUM_COUPON", "PROCTORED_COUPON", "VOUCHER")
   * @param file - CSV file containing coupons
   */
  importInventory(provider: string, serviceCode: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<any>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/import`,
      formData,
      {headers: new HttpHeaders({ 'enctype': 'multipart/form-data' })}
    );
  }

  /**
   * Get the count of available inventory (e.g., coupons) for a specific provider and service code
   * @param provider - The provider of the service (e.g., "LINKEDIN", "DUOLINGO", "REFERENCE")
   * @param serviceCode - The specific service code (e.g., "PREMIUM_COUPON", "PROCTORED_COUPON", "VOUCHER")
   * @returns An Observable containing the count of available items
   */
  countAvailable(provider: string, serviceCode: string): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/available/count`
    );
  }

  /**
   * Assign an inventory item (e.g., coupon) to a specific candidate
   * @param provider
   * @param serviceCode
   * @param candidateId
   */
  assignToCandidate(provider: string, serviceCode: string, candidateId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/assign/candidate/${candidateId}`,
      null
    );
  }

  /**
   * Assign an inventory item (e.g., coupon) to a specific list for all candidates on that list
   * @param provider
   * @param serviceCode
   * @param listId
   */
  assignToList(provider: string, serviceCode: string, listId: number): Observable<any[]> {
    return this.http.post<any[]>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/assign/list/${listId}`,
      null
    );
  }
}
