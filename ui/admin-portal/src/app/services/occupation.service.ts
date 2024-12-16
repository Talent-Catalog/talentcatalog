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
import {Occupation} from "../model/occupation";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {SystemLanguage} from "../model/language";

@Injectable({
  providedIn: 'root'
})
export class OccupationService {

  private apiUrl: string = environment.apiUrl + '/occupation';

  constructor(private http: HttpClient) { }

  listOccupations(): Observable<Occupation[]> {
    return this.http.get<Occupation[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<Occupation>> {
    return this.http.post<SearchResults<Occupation>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<Occupation> {
    return this.http.get<Occupation>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Occupation>  {
    return this.http.post<Occupation>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Occupation>  {
    return this.http.put<Occupation>(`${this.apiUrl}/${id}`, details);
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
