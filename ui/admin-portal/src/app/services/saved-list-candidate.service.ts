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
import {IHasSetOfCandidates} from "../model/saved-list";

@Injectable({
  providedIn: 'root'
})
export class SavedListCandidateService {

  private apiUrl: string = environment.apiUrl + '/saved-list-candidate';

  constructor(private http: HttpClient) {
  }

  merge(id: number, request: IHasSetOfCandidates): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/merge`, request);
  }

  remove(id: number, request: IHasSetOfCandidates): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/remove`, request);
  }

  replace(id: number, request: IHasSetOfCandidates): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/replace`, request);
  }
}
