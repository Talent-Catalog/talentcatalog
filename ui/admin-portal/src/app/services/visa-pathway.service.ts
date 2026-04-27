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
import {Observable} from "rxjs";
import {Country} from "../model/country";

export interface VisaPathway {
  name: string;
  description?: string;
  country?: Country;
  age?: string;
  language?: string;
  empCommitment?: string;
  inclusions?: string;
  other?: string;
  workExperience?: string;
  education?: string;
  educationCredential?: string;
}

@Injectable({
  providedIn: 'root'
})
export class VisaPathwayService {

  private apiUrl: string = environment.apiUrl + '/visa-pathway';

  constructor(private http: HttpClient) { }

  getVisaPathwaysCountry(countryId: number): Observable<VisaPathway[]> {
    return this.http.get<VisaPathway[]>(`${this.apiUrl}/country/${countryId}`);
  }

}
