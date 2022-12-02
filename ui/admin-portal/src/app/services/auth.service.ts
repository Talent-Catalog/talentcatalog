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
import {JwtResponse} from "../model/jwt-response";
import {throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {environment} from "../../environments/environment";
import {Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {Role, User} from "../model/user";
import {LoginRequest} from "../model/base";
import {Observable} from "rxjs/index";
import {EncodedQrImage} from "../util/qr";
import {Candidate} from "../model/candidate";
import {PartnerType} from "../model/partner";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  apiUrl = environment.apiUrl + '/auth';

  private loggedInUser: User;

  constructor(private router: Router,
              private http: HttpClient,
              private localStorageService: LocalStorageService) {
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

  //Can be used when we switch to user providing Role enum
  assignableUserRoles(): Role[] {
    const userRole: Role = this.getLoggedInRole();
    let assignableRoles: Role[] = [];
    switch (userRole) {
      case Role.sourcepartneradmin:
        assignableRoles.push(Role.limited, Role.semilimited);
        break;

      case Role.admin:
        assignableRoles.push(Role.limited, Role.semilimited, Role.sourcepartneradmin);
        break;

      case Role.systemadmin:
        //System admin can assign all roles
        assignableRoles = Object.values(Role);
        break;
    }
    return assignableRoles;
  }

  canAssignPartner(): boolean {
    const loggedInUser = this.getLoggedInUser();
    return loggedInUser == null ? false : Role[loggedInUser.role] === Role.systemadmin;
  }

  canViewCandidateCountry(): boolean {
    let result: boolean = false;
    switch (this.getLoggedInRole()) {
       case Role.systemadmin:
       case Role.admin:
       case Role.sourcepartneradmin:
       case Role.semilimited:
        result = true;
     }
     return result;
  }

  canViewCandidateCV(): boolean {
    let result: boolean = false;

    let partnerType = this.getPartnerType();
    if (partnerType != null && partnerType != PartnerType.Partner) {
      switch (this.getLoggedInRole()) {
        case Role.systemadmin:
        case Role.admin:
        case Role.sourcepartneradmin:
          result = true;
      }
    }
     return result;
  }

  canViewCandidateName(): boolean {
    let result: boolean = false;
    switch (this.getLoggedInRole()) {
       case Role.systemadmin:
       case Role.admin:
       case Role.sourcepartneradmin:
        result = true;
     }
     return result;
  }

  isAnAdmin(): boolean {
    let admin: boolean = false;
    switch (this.getLoggedInRole()) {
       case Role.systemadmin:
       case Role.admin:
       case Role.sourcepartneradmin:
        admin = true;
     }
     return admin;
  }

  isAuthenticated(): boolean {
    return this.getLoggedInUser() != null;
  }

  isReadOnly(): boolean {
    const loggedInUser = this.getLoggedInUser();
    return loggedInUser == null ? true : loggedInUser.readOnly;
  }

  isSystemAdminOnly(): boolean {
    return this.getLoggedInRole() === Role.systemadmin;
  }

  isAdminOrGreater(): boolean {
    return [Role.systemadmin, Role.admin].includes(this.getLoggedInRole());
  }

  isSourcePartnerAdminOrGreater(): boolean {
    return [Role.systemadmin, Role.admin, Role.sourcepartneradmin].includes(this.getLoggedInRole());
  }

  getLoggedInRole(): Role {
    const loggedInUser = this.getLoggedInUser();
    return loggedInUser == null ? Role.limited : Role[loggedInUser.role];
  }

  getLoggedInUser(): User {
    if (!this.loggedInUser) {
      this.loggedInUser = this.localStorageService.get('user');
    }

    if (!AuthService.isValidUserInfo(this.loggedInUser)) {
      console.log("invalid user");
      this.logout();
      this.router.navigate(['login']);
      this.loggedInUser = null;
    }

    return this.loggedInUser;
  }

  getPartnerType(): string {
    const loggedInUser = this.getLoggedInUser();
    return loggedInUser == null ? null : loggedInUser.partner?.partnerType;
  }

  setNewLoggedInUser(new_user) {
    this.localStorageService.set('user', new_user);
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

  getToken(): string {
    return this.localStorageService.get('access-token');
  }

  logout() {
    this.http.post(`${this.apiUrl}/logout`, null);
    this.localStorageService.remove('user');
    this.localStorageService.remove('access-token');
  }

  mfaSetup(): Observable<EncodedQrImage> {
    return this.http.post<EncodedQrImage>(`${this.apiUrl}/mfa-setup`, null);
  }

  private storeCredentials(response: JwtResponse) {
    this.localStorageService.remove('access-token');
    this.localStorageService.remove('user');
    this.localStorageService.set('access-token', response.accessToken);
    this.localStorageService.set('user', response.user);
    this.loggedInUser = response.user;
  }

  /**
   * True if the currently logged in user is permitted to edit the given candidate's details
   * @param candidate Candidate to be checked
   */
  isEditableCandidate(candidate: Candidate): boolean {
    let editable = false;
    const loggedInUser = this.getLoggedInUser()
    //Must be logged in
    if (loggedInUser) {
      //Cannot be a read only user
      if (!this.isReadOnly()) {
        const role = this.getLoggedInRole();
        //Must have some kind of admin role
        if (role !== Role.limited && role !== Role.semilimited) {
          if (this.isDefaultSourcePartner()) {
            //Default source partners with admin roles can edit all candidates
            editable = true;
          } else {
            //Can only edit candidate if the candidate is assigned to the user's partner
            const candidateSourcePartner = candidate.user.partner;
            editable = candidateSourcePartner.id === loggedInUser.partner.id;
          }
        }
      }
    }
    return editable;
  }

  /**
   * True if a user is logged in and they are associated with the default source partner.
   */
  isDefaultSourcePartner(): boolean {
    let defaultSourcePartner = false;
    const loggedInUser = this.getLoggedInUser();
    if (loggedInUser) {
      defaultSourcePartner = loggedInUser.partner?.defaultSourcePartner;
    }
    return defaultSourcePartner;
  }
}
