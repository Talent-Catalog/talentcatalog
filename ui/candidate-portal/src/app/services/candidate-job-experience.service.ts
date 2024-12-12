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
import {CandidateJobExperience} from "../model/candidate-job-experience";

@Injectable({
  providedIn: 'root'
})
export class CandidateJobExperienceService {

  private apiUrl: string = environment.apiUrl + '/job-experience';

  constructor(private http: HttpClient) { }

  createJobExperience(request): Observable<CandidateJobExperience> {
    return this.http.post<CandidateJobExperience>(`${this.apiUrl}`, request);
  }

  updateJobExperience(request: any): Observable<CandidateJobExperience> {
    return this.http.post<CandidateJobExperience>(`${this.apiUrl}/update`, request);
  }

  deleteJobExperience(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
