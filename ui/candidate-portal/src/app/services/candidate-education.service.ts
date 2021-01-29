/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {CandidateEducation} from "../model/candidate-education";

@Injectable({
  providedIn: 'root'
})
export class CandidateEducationService {

  private apiUrl: string = environment.apiUrl + '/candidate-education';

  constructor(private http: HttpClient) { }

  createCandidateEducation(request): Observable<CandidateEducation> {
    return this.http.post<CandidateEducation>(`${this.apiUrl}`, request);
  }

  updateCandidateEducation(request): Observable<CandidateEducation> {
     return this.http.post<CandidateEducation>(`${this.apiUrl}/update`, request);
  }

  deleteCandidateEducation(id: number) {
    return this.http.delete<CandidateEducation>(`${this.apiUrl}/${id}`);
  }
}
