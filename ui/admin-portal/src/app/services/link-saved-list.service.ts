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
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {LinkSavedList} from "../model/link-saved-list";

@Injectable({
  providedIn: 'root'
})
export class LinkSavedListService {

  private apiUrl: string = environment.apiUrl + '/link-saved-list';

  constructor(private http: HttpClient) { }

  listLinks(): Observable<LinkSavedList[]> {
    return this.http.get<LinkSavedList[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<LinkSavedList>> {
    return this.http.post<SearchResults<LinkSavedList>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<LinkSavedList> {
    return this.http.get<LinkSavedList>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<LinkSavedList>  {
    return this.http.post<LinkSavedList>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<LinkSavedList>  {
    return this.http.put<LinkSavedList>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
