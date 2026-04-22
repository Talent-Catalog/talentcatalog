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
import {JwtAuthenticationResponse} from "../model/jwt-authentication-response";
import {Observable, Subject} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {User} from "../model/user";
import {LocalStorageService} from "./local-storage.service";
import {CandidateStatus} from "../model/candidate";
import {TcInstanceType} from "../model/tc-instance-type";
import {TermsType} from "../model/terms-info-dto";
import {AuthProvider} from "./auth-provider";
import {AUTH_PROVIDER} from "./auth.tokens";

export class AuthenticateInContextTranslationRequest {
  password: string;
}

/**
 * Manages authentication - ie login/logout and registration
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
   * Can be used to subscribe to logged in state changes - ie logins and logouts.
   * <p/>
   * Note that this automatically completes when app is destroyed - which will clean up any
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
    this.localStorageService.remove('access-token');
    localStorage.clear();

    this.setLoggedInUser(null);
    this.setCandidateStatus(null);

    return this.authProvider.logout();     
   }

  getToken(): string | undefined {
     return this.authProvider.getToken();
  }

  refreshToken(minValiditySeconds = 30): Promise<void> {
    return this.authProvider.refreshToken(minValiditySeconds);
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

  private storeCredentials(response: JwtAuthenticationResponse) {
    //Remove any old credentials from storage
    this.localStorageService.remove('access-token');
    this.localStorageService.remove('user');
    this.localStorageService.remove('can_view_chats');
    this.localStorageService.remove('tc_instance_type');

    //Update new credentials in storage
    this.localStorageService.set('access-token', response.accessToken);
    this.localStorageService.set('can_view_chats', response.canViewChats);
    this.localStorageService.set('tc_instance_type', response.tcInstanceType);

    this.setLoggedInUser(response.user);
  }
}
