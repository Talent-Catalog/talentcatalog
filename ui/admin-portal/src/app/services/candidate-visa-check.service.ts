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
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateVisa} from "../model/candidate";

export interface CreateCandidateVisaCheckRequest {
  countryId: number;
}

@Injectable({providedIn: 'root'})
export class CandidateVisaCheckService {

  private apiUrl = environment.apiUrl + '/candidate-visa-check';

  constructor(private http: HttpClient) {}

  get(id: number): Observable<CandidateVisa> {
    return this.http.get<CandidateVisa>(`${this.apiUrl}/${id}`);
  }

  list(candidateId: number): Observable<CandidateVisa[]> {
    return this.http.get<CandidateVisa[]>(`${this.apiUrl}/${candidateId}/list`);
  }

  create(candidateId: number, candidateVisaCheckRequest: CreateCandidateVisaCheckRequest):
    Observable<CandidateVisa>  {
    return this.http.post<CandidateVisa>(
      `${this.apiUrl}/${candidateId}`, candidateVisaCheckRequest);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  updateIntakeData(id: number, formData: Object): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/intake`, formData);
  }
}
