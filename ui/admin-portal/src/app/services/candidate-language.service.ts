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
import {CandidateLanguage} from "../model/candidate-language";

export interface CreateCandidateLanguageRequest {
  candidateId: number;
  languageId: number;
  spokenLevelId: number;
  writtenLevelId: number;
  migrationLanguage?: string;
}

export interface UpdateCandidateLanguageRequest {
  id: number;
  languageId: number;
  spokenLevelId: number;
  writtenLevelId: number;
  migrationLanguage?: string;
}

@Injectable({providedIn: 'root'})
export class CandidateLanguageService {

  private apiUrl = environment.apiUrl + '/candidate-language';

  constructor(private http: HttpClient) {}

  list(id: number): Observable<CandidateLanguage[]> {
    return this.http.get<CandidateLanguage[]>(`${this.apiUrl}/${id}/list`);
  }

  create(request: CreateCandidateLanguageRequest): Observable<CandidateLanguage>  {
    return this.http.post<CandidateLanguage>(`${this.apiUrl}`, request);
  }

  update(request: UpdateCandidateLanguageRequest): Observable<CandidateLanguage>  {
    return this.http.put<CandidateLanguage>(`${this.apiUrl}`, request);
  }

  delete(id: number): Observable<CandidateLanguage>  {
    return this.http.delete<CandidateLanguage>(`${this.apiUrl}/${id}`);
  }

}
