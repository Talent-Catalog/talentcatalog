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
import {Industry} from "../model/industry";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";

@Injectable({
  providedIn: 'root'
})
export class IndustryService {

  private apiUrl: string = environment.apiUrl + '/industry';

  constructor(private http: HttpClient) { }

  listIndustries(): Observable<Industry[]> {
    return this.http.get<Industry[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<Industry>> {
    return this.http.post<SearchResults<Industry>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<Industry> {
    return this.http.get<Industry>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Industry>  {
    return this.http.post<Industry>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Industry>  {
    return this.http.put<Industry>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
