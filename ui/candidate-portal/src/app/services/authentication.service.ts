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

import {Inject, Injectable, OnDestroy} from '@angular/core';
import {from, Observable, Subject, throwError} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {User} from "../model/user";
import {LocalStorageService} from "./local-storage.service";
import {CandidateStatus} from "../model/candidate";
import {TcInstanceType} from "../model/tc-instance-type";
import {TermsType} from "../model/terms-info-dto";
import {AuthProvider} from "./auth-provider";
import {AUTH_PROVIDER} from "./auth.tokens";
import {AuthStatus} from "./auth-status";
import {catchError, map, switchMap} from "rxjs/operators";
import {AuthenticationResponse} from "../model/authentication-response";
import {OauthRegistrationRequest} from "../model/oauth-registration-request";

export class AuthenticateInContextTranslationRequest {
  password: string;
}

/**
 * Manages authentication - i.e., login/logout and registration
 */
@Injectable({
  providedIn: 'root'
})
export class AuthenticationService implements OnDestroy {
  apiUrl = environment.apiUrl + '/auth';

  private candidateStatus: CandidateStatus;

  /**
   * Stores current logged in state
   * @private
   */
  private loggedInUser: User = null;

  /**
   * Can be used to subscribe to logged-in state changes - i.e., logins and logouts.
   * <p/>
   * Note that this automatically completes when the app is destroyed - which will clean up any
   * subscriptions - so no need to manage clean of subscriptions in calling code.
   */
  loggedInUser$ = new Subject<User>();

  constructor(@Inject(AUTH_PROVIDER) private authProvider: AuthProvider,
              private http: HttpClient,
              private localStorageService: LocalStorageService
  ) {}

  ngOnDestroy(): void {
    //This will close any subscriptions - freeing resources.
    //See https://stackoverflow.com/a/77426261/929968
    this.loggedInUser$.complete();
  }

  init(): Promise<boolean> {
    return this.authProvider.init();
  }

  isAuthenticated(): boolean {
    return this.authProvider.isAuthenticated();
  }

  login(): Promise<void> {
     return this.authProvider.login();
   }

  register(): Promise<void> {
     return this.authProvider.register();
   }

  logout(): Promise<void> {
    this.localStorageService.remove('user');
    localStorage.clear();

    this.setLoggedInUser(null);
    this.setCandidateStatus(null);

    return this.authProvider.logout();
   }

  completeLogin(): Observable<void> {
    //Retrieve current profile from provider and send to server so that it can be stored in the
    //database.
    return from(this.authProvider.getProfile()).pipe(
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

  completeRegister(request: OauthRegistrationRequest): Observable<void> {
    //Retrieve current profile from provider and send to server so that it can be stored in the
    //database.
    return from(this.authProvider.getProfile()).pipe(
      switchMap(profile => {
          request.profile = profile;
          request.contactConsentRegistration = true;
          request.contactConsentPartners = true;
          return this.http.post(`${this.apiUrl}/register`, request).pipe(
            map((response: AuthenticationResponse) => {
              this.storeAuthenticationData(response);
            }),
            catchError(e => {
                console.log('error', e);
                return throwError(e);
              }
            )
          );
        }
      )
    )
  }

  getToken(): string | undefined {
    if (this.authProvider.isAuthenticated()) {
      return this.authProvider.getToken();
    } else {
      console.log("Not authenticated");
    }
     return this.authProvider.getToken();
  }

  refreshToken(minValiditySeconds = 30): Promise<void> {
    return this.authProvider.refreshToken(minValiditySeconds);
  }

  getAuthStatus(): Observable<AuthStatus> {
    return this.authProvider.getStatus();
  }

  getCurrentAuthStatus(): AuthStatus {
    return this.authProvider.getCurrentStatus();
  }

  clearAuthError(): void {
    this.authProvider.clearError();
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

  isRegistered(): boolean {
    //Recover status from storage - may have been lost during browser refresh.
    if (this.candidateStatus == null) {
      this.candidateStatus = this.localStorageService.get("candidateStatus");
    }
    return this.candidateStatus != null && this.candidateStatus != CandidateStatus.draft;
  }

  setCandidateStatus(candidateStatus: CandidateStatus) {
    this.candidateStatus = candidateStatus;
    this.localStorageService.set('candidateStatus', this.candidateStatus);
  }

  private setLoggedInUser(loggedInUser: User) {
    this.loggedInUser = loggedInUser;
    this.localStorageService.set('user', this.loggedInUser);
    this.loggedInUser$.next(this.loggedInUser);
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
}
