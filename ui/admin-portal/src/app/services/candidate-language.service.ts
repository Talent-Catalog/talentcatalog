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
import {CandidateLanguage} from "../model/candidate-language";

@Injectable({providedIn: 'root'})
export class CandidateLanguageService {

  private apiUrl = environment.apiUrl + '/candidate-language';

  constructor(private http: HttpClient) {}

  list(id: number): Observable<CandidateLanguage[]> {
    return this.http.get<CandidateLanguage[]>(`${this.apiUrl}/${id}/list`);
  }

  create(id: number, details): Observable<CandidateLanguage>  {
    return this.http.post<CandidateLanguage>(`${this.apiUrl}/${id}`, details);
  }

  update(id: number, details): Observable<CandidateLanguage>  {
    return this.http.put<CandidateLanguage>(`${this.apiUrl}/${id}`, details);
  }

}
