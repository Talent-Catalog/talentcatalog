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
import {EducationMajor} from "../model/education-major";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {SystemLanguage} from "../model/language";

@Injectable({
  providedIn: 'root'
})
export class EducationMajorService {

  private apiUrl: string = environment.apiUrl + '/education-major';

  constructor(private http: HttpClient) { }

  listMajors(): Observable<EducationMajor[]> {
    return this.http.get<EducationMajor[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<EducationMajor>> {
    return this.http.post<SearchResults<EducationMajor>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<EducationMajor> {
    return this.http.get<EducationMajor>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<EducationMajor>  {
    return this.http.post<EducationMajor>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<EducationMajor>  {
    return this.http.put<EducationMajor>(`${this.apiUrl}/${id}`, details);
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
