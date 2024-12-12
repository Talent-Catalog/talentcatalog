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
import {CandidateCertification} from "../model/candidate-certification";

export interface CreateCandidateCertificationRequest {
  candidateId: number;
  name: string;
  institution: string;
  dateCompleted: string;
}

export interface UpdateCandidateCertificationRequest {
  id: number;
  name: string;
  institution: string;
  dateCompleted: string;
}

@Injectable({providedIn: 'root'})
export class CandidateCertificationService {

  private apiUrl = environment.apiUrl + '/candidate-certification';

  constructor(private http: HttpClient) {}

  list(id: number): Observable<CandidateCertification[]> {
    return this.http.get<CandidateCertification[]>(`${this.apiUrl}/${id}/list`);
  }

  create(request: CreateCandidateCertificationRequest): Observable<CandidateCertification>  {
    return this.http.post<CandidateCertification>(`${this.apiUrl}`, request);
  }

  update(request: UpdateCandidateCertificationRequest): Observable<CandidateCertification>  {
    return this.http.put<CandidateCertification>(`${this.apiUrl}`, request);
  }

  delete(id: number): Observable<CandidateCertification>  {
    return this.http.delete<CandidateCertification>(`${this.apiUrl}/${id}`);
  }

}
