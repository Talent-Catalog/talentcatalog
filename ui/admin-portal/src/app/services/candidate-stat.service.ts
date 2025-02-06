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
import {StatReport} from "../model/stat-report";

export interface CandidateStatsRequest {
  runOldStats?: boolean;
  listId?: number;
  searchId?: number;
  dateFrom?: string;
  dateTo?: string;
  statNames?: string[];
}

@Injectable({providedIn: 'root'})
export class CandidateStatService {

  private apiUrl = environment.apiUrl + '/candidate/stat';

  constructor(private http: HttpClient) {}

  getAllStats(details: CandidateStatsRequest): Observable<StatReport[]> {
    return this.http.post<StatReport[]>(`${this.apiUrl}/all`, details);
  }

  getAllStatNames(): Observable<StatReport[]> {
    return this.http.get<StatReport[]>(`${this.apiUrl}/names`);
  }

}
