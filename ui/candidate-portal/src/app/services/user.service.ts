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
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from "../../environments/environment";
import {User} from '../model/user';
import {SendResetPasswordEmailRequest} from "../model/candidate";
import {SendVerifyEmailRequest} from "../model/user";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  apiUrl = environment.apiUrl + '/user';

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
    } else {
      const name = useUsername ? user.username : user.firstName + ' ' + user.lastName;
      let extras: string;
      if (this.isCandidate(user)) {
        extras = user.partner?.abbreviation + " candidate"
      } else {
        extras = user.partner?.abbreviation + (showRole ? " " + user.role : "")
      }
      s = name + " (" + extras + ")";
    }
    return s;
  }

  static isCandidate(user: User): boolean {
    return user.role === "user";
  }

  getMyUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}`);
  }

  updatePassword(request) {
    return this.http.post(`${this.apiUrl}/password`, request);
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

  sendVerifyEmail(request: SendVerifyEmailRequest) {
    return this.http.post(`${this.apiUrl}/verify-email`, request);
  }

}
