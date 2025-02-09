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
import {SearchResults} from '../model/search-results';
import {UpdateUserRequest, User} from '../model/user';
import {SearchUserRequest} from "../model/base";
import {SendResetPasswordEmailRequest} from "../model/candidate";
import {SendVerifyEmailRequest} from "../model/user";

@Injectable({providedIn: 'root'})
export class UserService {

  private apiUrl = environment.apiUrl + '/user';

  constructor(private http: HttpClient) {}

  /**
   * Returns a string containing info about the given user
   * @param user User
   * @param useUsername True if username should be used instead of first and last name
   * @param showRole True if the user's role should be returned
   */
  static userToString(user: User, useUsername: boolean, showRole: boolean): string {
    let s: string;
    if (user == null) {
      s = "";
    } else if (this.isTalentCatalogSystemAdmin(user)) {
      s = "Talent Catalog";
    } else {
      const name = useUsername ? user.username : user.firstName + ' ' + user.lastName;
      let extras: string;
      if (this.isCandidate(user)) {
        extras = user.partner?.abbreviation + " candidate"
      } else {

        extras = user.partner?.abbreviation;
        if (showRole) {
          extras += " " + user.role;
          if (user.readOnly) {
            extras +="-read only";
          }
        }
      }
      s = name + " (" + extras + ")";
    }
    return s;
  }

  static isCandidate(user: User): boolean {
    return user.role === "user";
  }

  static isTalentCatalogSystemAdmin(user: User): boolean {
    return user.firstName === "System" && user.lastName === "Admin" && user.role === "systemadmin";
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

  sendResetPassword(request: SendResetPasswordEmailRequest) {
    return this.http.post(`${this.apiUrl}/reset-password-email`, request);
  }

  checkPasswordResetToken(request) {
    return this.http.post(`${this.apiUrl}/check-token`, request);
  }

  resetPassword(request) {
    return this.http.post(`${this.apiUrl}/reset-password`, request);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  resetMfa(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/mfa-reset/${id}`, null);
  }

  sendVerifyEmail(request:SendVerifyEmailRequest) {
    return this.http.post(`${this.apiUrl}/verify-email`, request);
  }

  checkEmailVerificationToken(request: { token: string }) {
    return this.http.post(`${this.apiUrl}/check-email-verification-token`, request);
  }

  verifyEmail(request: { token: string }) {
    return this.http.post(`${this.apiUrl}/verify-email-token`, request);
  }
}
