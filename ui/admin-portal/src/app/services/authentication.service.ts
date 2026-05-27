/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Inject, Injectable, OnDestroy} from '@angular/core';
import {from, Observable, Subject, throwError} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {User} from "../model/user";
import {EncodedQrImage} from "../util/qr";
import {LocalStorageService} from "./local-storage.service";
import {TcInstanceType} from "../model/tc-instance-type";
import {TermsType} from "../model/terms-info-dto";
import {IDP_PROVIDER} from "./idp.tokens";
import {IdpProvider} from "./idp-provider";
import {IdpStatus} from "./idp-status";
import {catchError, map, switchMap} from "rxjs/operators";
import {AuthenticationResponse} from "../model/authentication-response";

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

  constructor(@Inject(IDP_PROVIDER) private idpProvider: IdpProvider,
    private http: HttpClient,
    private localStorageService: LocalStorageService
  ) {}

  ngOnDestroy(): void {
    //This will close any subscriptions - freeing resources.
    //See https://stackoverflow.com/a/77426261/929968
    this.loggedInUser$.complete();
  }

  getLoggedInUser(): User {
    if (!this.loggedInUser) {
      //We don't have a loggedInUser stored - can we pick it up from storage?
      const user: User = this.localStorageService.get('user');
      if (!AuthenticationService.isValidUserInfo(user)) {
        console.log("invalid user");
        this.logout();
      } else {
        //Update logged-in user retrieved from storage
        if (user != null) {
          // don't want to set this to null as it effectively indicates a logout for any observers
          this.setLoggedInUser(user);
        }
      }
    }

    return this.loggedInUser;
  }

  canViewChats(): boolean {
    return this.localStorageService.get('can_view_chats');
  }

  getCandidatePolicyType(): TermsType {
    let tcInstanceType = this.getTcInstanceType();
    return tcInstanceType == TcInstanceType.GRN ?
      TermsType.GRN_CANDIDATE_PRIVACY_POLICY : TermsType.TBB_CANDIDATE_PRIVACY_POLICY;
  }

  getTcInstanceType(): TcInstanceType {
    return this.localStorageService.get('tc_instance_type');
  }

  isGrnInstance(): boolean {
    return this.getTcInstanceType() == TcInstanceType.GRN;
  }

  /**
   * Check that user - possibly retrieved from cache - is not junk
   * @param user User object to check
   */
  private static isValidUserInfo(user: User){

    //Null user is OK
    if (user == null) {
      return true;
    }

    //If user exists it should have a role
    if (user.role) {
      //It should also have a non null readOnly indicator (as an example of another field that
      //should be there and not null
      return user.readOnly != null;
    } else {
      return false;
    }
  }

  clearAuthError(): void {
    this.idpProvider.clearError();
  }

  init(): Promise<boolean> {
    return this.idpProvider.init();
  }

  getAuthStatus(): Observable<IdpStatus> {
    return this.idpProvider.getStatus();
  }

  getToken(): string | undefined {
    if (this.idpProvider.isAuthenticated()) {
      return this.idpProvider.getToken();
    } else {
      console.log("Not authenticated");
    }
    return this.idpProvider.getToken();
  }

  refreshToken(minValiditySeconds = 30): Promise<void> {
    return this.idpProvider.refreshToken(minValiditySeconds);
  }

  isAuthenticated(): boolean {
    return this.idpProvider.isAuthenticated();
  }

  login(redirectUri: string, lang: string = 'en'): Promise<void> {
    return this.idpProvider.login(redirectUri, lang);
  }

  completeLogin(): Observable<void> {
    //Retrieve current profile from provider and send to server so that it can be stored in the
    //database.
    return from(this.idpProvider.getProfile()).pipe(
      switchMap(profile =>
        this.http.post(`${this.apiUrl}/login`, profile).pipe(
          map((response: AuthenticationResponse) => {
            this.storeAuthenticationData(response);
          }),
          catchError(e => {
              console.log('error', e);
              return throwError(e);
            }
          )
        )
      )
    )
  }

  logout(): Promise<void> {
    this.localStorageService.remove('user');
    localStorage.clear();

    this.setLoggedInUser(null);

    return this.idpProvider.logout();
  }

  private storeAuthenticationData(response: AuthenticationResponse) {
    //Remove any old data from storage
    this.localStorageService.remove('user');
    this.localStorageService.remove('can_view_chats');
    this.localStorageService.remove('tc_instance_type');

    //Update new data in storage
    this.localStorageService.set('can_view_chats', response.canViewChats);
    this.localStorageService.set('tc_instance_type', response.tcInstanceType);

    this.setLoggedInUser(response.user);
  }

  mfaSetup(): Observable<EncodedQrImage> {
    return this.http.post<EncodedQrImage>(`${this.apiUrl}/mfa-setup`, null);
  }

  setLoggedInUser(loggedInUser: User) {
    this.loggedInUser = loggedInUser;
    this.localStorageService.set('user', this.loggedInUser);
    this.loggedInUser$.next(this.loggedInUser);
  }

}
