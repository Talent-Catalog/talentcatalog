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
import {CandidateEducation} from "../model/candidate-education";

export interface CreateCandidateEducationRequest {
  candidateId: number;
  educationType: string;
  countryId: number;
  educationMajorId: number;
  lengthOfCourseYears: number;
  institution: string;
  courseName: string;
  yearCompleted: string;
  incomplete: boolean;
}

export interface UpdateCandidateEducationRequest {
  id: number;
  educationType: string;
  countryId: number;
  majorId: number;
  lengthOfCourseYears: number;
  institution: string;
  courseName: string;
  yearCompleted: string;
  incomplete: boolean;
}

@Injectable({providedIn: 'root'})
export class CandidateEducationService {

  private apiUrl = environment.apiUrl + '/candidate-education';

  constructor(private http: HttpClient) {}

  list(id: number): Observable<CandidateEducation[]> {
    return this.http.get<CandidateEducation[]>(`${this.apiUrl}/${id}/list`);
  }

  create(request: CreateCandidateEducationRequest): Observable<CandidateEducation>  {
    return this.http.post<CandidateEducation>(`${this.apiUrl}`, request);
  }

  update(request: UpdateCandidateEducationRequest): Observable<CandidateEducation>  {
    return this.http.put<CandidateEducation>(`${this.apiUrl}`, request);
  }

  delete(id: number): Observable<CandidateEducation>  {
    return this.http.delete<CandidateEducation>(`${this.apiUrl}/${id}`);
  }

}
