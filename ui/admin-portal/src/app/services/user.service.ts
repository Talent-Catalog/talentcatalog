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
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from '../model/search-results';
import {UpdateUserRequest, User} from '../model/user';
import {SearchUserRequest} from "../model/base";

@Injectable({providedIn: 'root'})
export class UserService {

  private apiUrl = environment.apiUrl + '/user';

  constructor(private http: HttpClient) {}

  // This method is to display users for the 'approver' field in the create-update-user component's form
  listAdminUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}`);
  }

  search(request: SearchUserRequest): Observable<User[]> {
    return this.http.post<User[]>(`${this.apiUrl}/search`, request);
  }

  searchPaged(request): Observable<SearchResults<User>> {
    return this.http.post<SearchResults<User>>(`${this.apiUrl}/search-paged`, request);
  }

  get(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  create(details: UpdateUserRequest): Observable<User>  {
    return this.http.post<User>(`${this.apiUrl}`, details);
  }

  update(id: number, details: UpdateUserRequest): Observable<User>  {
    return this.http.put<User>(`${this.apiUrl}/${id}`, details);
  }

  updatePassword(id: number, request) {
    return this.http.put(`${this.apiUrl}/password/${id}`, request);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  resetMfa(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/mfa-reset/${id}`, null);
  }
}
