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
import {Language, SystemLanguage} from "../model/language";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";

@Injectable({
  providedIn: 'root'
})
export class LanguageService {

  private apiUrl: string = environment.apiUrl + '/language';

  constructor(private http: HttpClient) { }

  listLanguages(): Observable<Language[]> {
    return this.http.get<Language[]>(`${this.apiUrl}`);
  }

  listSystemLanguages(): Observable<SystemLanguage[]> {
    return this.http.get<SystemLanguage[]>(`${this.apiUrl}/system`);
  }

  search(request): Observable<SearchResults<Language>> {
    return this.http.post<SearchResults<Language>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<Language> {
    return this.http.get<Language>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Language>  {
    return this.http.post<Language>(`${this.apiUrl}`, details);
  }

  addSystemLanguage(langCode: string): Observable<SystemLanguage>  {
    return this.http.post<SystemLanguage>(`${this.apiUrl}/system/${langCode}`, null);
  }

  update(id: number, details): Observable<Language>  {
    return this.http.put<Language>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
