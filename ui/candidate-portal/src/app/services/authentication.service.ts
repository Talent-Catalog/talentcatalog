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

import {Injectable, OnDestroy} from '@angular/core';
import {catchError, map} from "rxjs/operators";
import {JwtResponse} from "../model/jwt-response";
import {Observable, Subject, throwError} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {User} from "../model/user";
import {AuthenticateInContextTranslationRequest} from "./auth.service";
import {LoginRequest} from "../model/base";
import {LocalStorageService} from "./local-storage.service";

/**
 * Manages authentication - ie login/logout.
 * <p/>
 * See also Auth service which is more about authorization.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthenticationService implements OnDestroy {
  apiUrl = environment.apiUrl + '/auth';

  /**
   * Stores current logged in state
   * @private
   */
  private loggedInUser: User = null;

  /**
   * Can be used to subscribe to logged in state changes - ie logins and logouts.
   * <p/>
   * Note that this automatically completes when app is destroyed - which will clean up any
   * subscriptions - so no need to manage clean of subscriptions in calling code.
   */
  loggedInUser$ = new Subject<User>();

  constructor(
    private http: HttpClient,
    private localStorageService: LocalStorageService
  ) {}

  ngOnDestroy(): void {
    //This will close any subscriptions - freeing resources.
    //See https://stackoverflow.com/a/77426261/929968
    this.loggedInUser$.complete();
  }

  authenticateInContextTranslation(password: string): Observable<void> {
    const request: AuthenticateInContextTranslationRequest = {
      password: password
    }
    return this.http.post<void>(`${this.apiUrl}/xlate`, request);
  }

  getLoggedInUser(): User {
    if (!this.loggedInUser) {
      //We don't have a loggedInUser stored - can we pick it up from storage?
      const user: User = this.localStorageService.get('user');
      //Update logged-in user retrieved from storage
      if (user != null) {
        // don't want to set this to null as it effectively indicates a logout for any observers
        this.setLoggedInUser(user);
      }
    }

    return this.loggedInUser;
  }

  getToken(): string {
    return this.localStorageService.get('access-token');
  }

  isAuthenticated(): boolean {
    return this.getLoggedInUser() != null;
  }

  login(credentials: LoginRequest) {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      map((response: JwtResponse) => {
        this.storeCredentials(response);
      }),
      catchError(e => {
          console.log('error', e);
          return throwError(e);
        }
      )
    );
  }

  logout() {
    this.http.post(`${this.apiUrl}/logout`, null).subscribe({
      next: () => {}
    })
    this.localStorageService.remove('user');
    this.localStorageService.remove('access-token');
    localStorage.clear();

    this.setLoggedInUser(null)
  }

  setLoggedInUser(loggedInUser: User) {
    this.loggedInUser = loggedInUser;
    this.localStorageService.set('user', this.loggedInUser);
    this.loggedInUser$.next(this.loggedInUser);
  }

  private storeCredentials(response: JwtResponse) {
    //Remove any old credentials from storage
    this.localStorageService.remove('access-token');
    this.localStorageService.remove('user');

    //Update new credentials in storage
    this.localStorageService.set('access-token', response.accessToken);

    this.setLoggedInUser(response.user);
  }

}
