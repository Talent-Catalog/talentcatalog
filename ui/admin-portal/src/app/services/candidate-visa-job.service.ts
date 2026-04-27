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
import {CandidateVisaJobCheck} from '../model/candidate';
// todo this was originally built for the Visa AU request, don't want to completely redo as don't want to mess up the current AU visa job checks.
// Should eventually just need the job id for the request, and be able to remove name and sf job link.

export interface CreateCandidateVisaJobRequest {
  jobOppId?: number;
}

@Injectable({providedIn: 'root'})
export class CandidateVisaJobService {

  private apiUrl = environment.apiUrl + '/candidate-visa-job';

  constructor(private http: HttpClient) {}

  get(id: number): Observable<CandidateVisaJobCheck> {
    return this.http.get<CandidateVisaJobCheck>(`${this.apiUrl}/${id}`);
  }

  create(visaId: number, candidateVisaJobRequest: CreateCandidateVisaJobRequest):
    Observable<CandidateVisaJobCheck>  {
    return this.http.post<CandidateVisaJobCheck>(
      `${this.apiUrl}/${visaId}`, candidateVisaJobRequest);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }
}
