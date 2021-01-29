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
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateEducation} from "../model/candidate-education";

@Injectable({providedIn: 'root'})
export class CandidateEducationService {

  private apiUrl = environment.apiUrl + '/candidate-education';

  constructor(private http:HttpClient) {}

  list(id: number): Observable<CandidateEducation[]> {
    return this.http.get<CandidateEducation[]>(`${this.apiUrl}/${id}/list`);
  }

  create(id: number, details): Observable<CandidateEducation>  {
    return this.http.post<CandidateEducation>(`${this.apiUrl}/${id}`, details);
  }

  update(id: number, details): Observable<CandidateEducation>  {
    return this.http.put<CandidateEducation>(`${this.apiUrl}/${id}`, details);
  }

}
