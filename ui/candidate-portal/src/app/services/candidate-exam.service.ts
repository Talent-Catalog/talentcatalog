/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {CandidateExam} from '../model/candidate';

@Injectable({providedIn: 'root'})
export class CandidateExamService {

  private apiUrl = environment.apiUrl + '/candidate-exam';

  constructor(private http: HttpClient) {}

  updateCandidateExams(id:number,request): Observable<CandidateExam[]> {
    return this.http.put<CandidateExam[]>(`${this.apiUrl}/${id}`, request);
  }
  
}