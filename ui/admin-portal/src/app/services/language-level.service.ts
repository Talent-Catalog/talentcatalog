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
import {LanguageLevel} from "../model/language-level";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {SystemLanguage} from "../model/language";

@Injectable({
  providedIn: 'root'
})
export class LanguageLevelService {

  private apiUrl: string = environment.apiUrl + '/language-level';

  constructor(private http: HttpClient) { }

  addSystemLanguageTranslations(langCode: string, file: File): Observable<SystemLanguage> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post<SystemLanguage>(`${this.apiUrl}/system/${langCode}`, formData);
  }

  listLanguageLevels(): Observable<LanguageLevel[]> {
    return this.http.get<LanguageLevel[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<LanguageLevel>> {
    return this.http.post<SearchResults<LanguageLevel>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<LanguageLevel> {
    return this.http.get<LanguageLevel>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<LanguageLevel>  {
    return this.http.post<LanguageLevel>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<LanguageLevel>  {
    return this.http.put<LanguageLevel>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
