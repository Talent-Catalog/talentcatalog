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
import {TranslatedObject} from "../model/translated-object";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {Translation} from "../model/translation";

@Injectable({
  providedIn: 'root'
})
export class TranslationService {

  private apiUrl: string = environment.apiUrl + '/translation';

  constructor(private http: HttpClient) { }

  listTranslations(): Observable<Translation[]> {
    return this.http.get<Translation[]>(`${this.apiUrl}`);
  }

  search(type: string, request): Observable<SearchResults<TranslatedObject>> {
    return this.http.post<SearchResults<TranslatedObject>>(`${this.apiUrl}/${type}`, request);
  }

  create(details): Observable<Translation>  {
    return this.http.post<Translation>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Translation>  {
    return this.http.put<Translation>(`${this.apiUrl}/${id}`, details);
  }

  loadTranslationsFile(language: string) {
    return this.http.get(`${this.apiUrl}/file/${language}`);
  }

  updateTranslationFile(language: string, translations) {
    return this.http.put(`${this.apiUrl}/file/${language}`, translations);
  }

}
