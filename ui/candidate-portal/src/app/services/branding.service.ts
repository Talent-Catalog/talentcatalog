/*
 * Copyright (c) 2024 Talent Catalog.
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
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {AuthenticationService} from "./authentication.service";

export interface BrandingInfo {
  logo: string;
  partnerName: string;
  websiteUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class BrandingService {
  apiUrl: string = environment.apiUrl + '/branding';
  partnerAbbreviation: string;

  constructor(private http: HttpClient, private authenticationService: AuthenticationService) { }

  getBrandingInfo(): Observable<BrandingInfo> {
    if (this.authenticationService.isRegistered()) {
      let url = `${this.apiUrl}`;
      if (this.partnerAbbreviation) {
        url += `?p=${this.partnerAbbreviation}`;
      }
      return this.http.get<BrandingInfo>(url);
    } else {
      let brandingInfo: BrandingInfo = {
        logo: "assets/images/tc-logo-2.png",
        partnerName: "a Talent Catalog partner",
        websiteUrl: ""
      }
      return of(brandingInfo);
    }
  }

  /**
   * Gets branding info from the API regardless of registration status.
   * This is useful for determining partner-specific features (like chatbot visibility)
   * that need to work for both registered and unregistered users.
   * The API endpoint is public and supports both cases.
   */
  getBrandingInfoFromApi(): Observable<BrandingInfo> {
    let url = `${this.apiUrl}`;
    if (this.partnerAbbreviation) {
      url += `?p=${this.partnerAbbreviation}`;
    }
    return this.http.get<BrandingInfo>(url);
  }

  setPartnerAbbreviation(partnerAbbreviation: string) {
    this.partnerAbbreviation = partnerAbbreviation;
  }
}
