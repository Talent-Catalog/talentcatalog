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
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateJobExperience} from "../model/candidate-job-experience";
import {SearchResults} from '../model/search-results';

export interface CandidateJobExperienceSearchRequest {
    candidateId?: number,
    candidateOccupationId?: number,
    occupationId?: number,
    pageSize?: number,
    pageNumber?: number,
    sortDirection?: string,
    sortFields?: string[]
}

@Injectable({providedIn: 'root'})
export class CandidateJobExperienceService {

  private apiUrl = environment.apiUrl + '/candidate-job-experience';

  constructor(private http: HttpClient) {}

  list(request: CandidateJobExperienceSearchRequest): Observable<CandidateJobExperience[]> {
    return this.http.post<CandidateJobExperience[]>(`${this.apiUrl}/list`, request);
  }

  create(id: number, details): Observable<CandidateJobExperience>  {
    return this.http.post<CandidateJobExperience>(`${this.apiUrl}/${id}`, details);
  }

  update(id: number, details): Observable<CandidateJobExperience>  {
    return this.http.put<CandidateJobExperience>(`${this.apiUrl}/${id}`, details);
  }

  search(request): Observable<SearchResults<CandidateJobExperience>> {
    return this.http.post<SearchResults<CandidateJobExperience>>(`${this.apiUrl}/search`, request);
  }

  delete(id: number): Observable<CandidateJobExperience>  {
    return this.http.delete<CandidateJobExperience>(`${this.apiUrl}/${id}`);
  }

}
