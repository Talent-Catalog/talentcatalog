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
import {EducationLevel} from "../model/education-level";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {User} from "../model/user";
import {Candidate} from "../model/candidate";
import {SystemLanguage} from "../model/language";

@Injectable({
  providedIn: 'root'
})
export class EducationLevelService {

  private apiUrl: string = environment.apiUrl + '/education-level';

  constructor(private http: HttpClient) { }

  listEducationLevels(): Observable<EducationLevel[]> {
    return this.http.get<EducationLevel[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<EducationLevel>> {
    return this.http.post<SearchResults<EducationLevel>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<EducationLevel> {
    return this.http.get<EducationLevel>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<EducationLevel>  {
    return this.http.post<EducationLevel>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<EducationLevel>  {
    return this.http.put<EducationLevel>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  addSystemLanguageTranslations(langCode: string, file: File): Observable<SystemLanguage> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post<SystemLanguage>(`${this.apiUrl}/system/${langCode}`, formData);
  }

}
