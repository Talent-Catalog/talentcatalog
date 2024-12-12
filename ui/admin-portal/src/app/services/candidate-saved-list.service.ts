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
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {
  IHasSetOfSavedLists,
  SavedList,
  SearchSavedListRequest
} from "../model/saved-list";

@Injectable({providedIn: 'root'})
export class CandidateSavedListService {

  private apiUrl = environment.apiUrl + '/candidate-saved-list';

  constructor(private http: HttpClient) {}

  replace(id: number, request: IHasSetOfSavedLists): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/replace`, request);
  }

  search(id: number, request: SearchSavedListRequest): Observable<SavedList[]> {
    return this.http.post<SavedList[]>(`${this.apiUrl}/${id}/search`, request);
  }
}
