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
import {CandidateReviewStatusItem} from "../model/candidate-review-status-item";

@Injectable({providedIn: 'root'})
export class CandidateReviewStatusService {

  private apiUrl = environment.apiUrl + '/candidate-reviewstatus';

  constructor(private http: HttpClient) {}

  get(id): Observable<CandidateReviewStatusItem>  {
    return this.http.get<CandidateReviewStatusItem>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<CandidateReviewStatusItem>  {
    return this.http.post<CandidateReviewStatusItem>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<CandidateReviewStatusItem>  {
    return this.http.put<CandidateReviewStatusItem>(`${this.apiUrl}/${id}`, details);
  }

}
