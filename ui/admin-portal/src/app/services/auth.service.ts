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
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {environment} from "../../environments/environment";
import {Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {Role, User} from "../model/user";
import {LoginRequest} from "../model/base";
import {EncodedQrImage} from "../util/qr";
import {Candidate, ShortCandidate} from "../model/candidate";
import {Job, ShortJob} from "../model/job";
import {CandidateOpportunity} from "../model/candidate-opportunity";
import {RxStompConfig} from "@stomp/rx-stomp";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  apiUrl = environment.apiUrl + '/auth';

  private loggedInUser: User;

  constructor(private router: Router,
              private http: HttpClient,
              private localStorageService: LocalStorageService ) {
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
      case Role.partneradmin:
        assignableRoles.push(Role.limited, Role.semilimited);
        break;

      case Role.admin:
        assignableRoles.push(Role.limited, Role.semilimited, Role.partneradmin);
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

  canAssignTask(): boolean {
    //For now only TBB can do this.
    //Todo Need to make this more broadly available. It gets complicated when assigning tasks
    //to a list - if that list has candidates from multiple partners.
    return this.isDefaultSourcePartner();
  }

  canCreateJob() : boolean {
    return this.isJobCreator();
  }

  canViewCandidateCountry(): boolean {
    let result: boolean = false;
    switch (this.getLoggedInRole()) {
       case Role.systemadmin:
       case Role.admin:
       case Role.partneradmin:
       case Role.semilimited:
        result = true;
     }
     return result;
  }

  /**
   * True if the currently logged in user is permitted to see candidate CVs
   */
  canViewCandidateCV(): boolean {
    let result: boolean = false;

    if (this.isJobCreator() || this.isSourcePartner()) {
      switch (this.getLoggedInRole()) {
        case Role.systemadmin:
        case Role.admin:
        case Role.partneradmin:
          result = true;
      }
    }
     return result;
  }

  /**
   * True if the currently logged in user is permitted to see candidate names
   */
  canViewCandidateName(): boolean {
    let result: boolean = false;
    if (this.isSourcePartner() || this.isJobCreator()) {
      switch (this.getLoggedInRole()) {
        case Role.systemadmin:
        case Role.admin:
        case Role.partneradmin:
          result = true;
      }
    }
    return result;
  }

  isCandidateOurs(candidate: ShortCandidate): boolean {
    let ours = false;
    const loggedInUser = this.getLoggedInUser()
    //Must be logged in
    if (loggedInUser) {
      ours = candidate.user?.partner?.id === loggedInUser.partner.id;
    }
    return ours;
  }

  isJobOurs(job: ShortJob): boolean {

    //For now all jobs belong to just the default partner.
    return this.isDefaultJobCreator();

    //todo Eventually when we have proper recruiter partner support, the code will look like this:
    /*
      let ours = false;
      const loggedInUser = this.getLoggedInUser()
      //Must be logged in
      if (loggedInUser) {
        ours = job.recruiterPartner?.id === loggedInUser.partner.id;
      }
      return ours;
    */
  }

  /**
   * True if the currently logged in user is permitted to see the given candidate's private
   * and potentially sensitive information - such as intake data
   * @param candidate Candidate to be checked
   */
  canViewPrivateCandidateInfo(candidate: Candidate): boolean {
    let visible = false;
    const loggedInUser = this.getLoggedInUser()
    //Must be logged in
    if (loggedInUser) {

      if (this.isJobCreator() || this.isSourcePartner()) {

        //Must have some kind of admin role
        const role = this.getLoggedInRole();
        if (role !== Role.limited && role !== Role.semilimited) {
          if (this.isDefaultPartner()) {
            //Default partners with admin roles can see all candidate info
            visible = true;
          } else {
            //Can only see private candidate info if the candidate is assigned to the user's partner
            const candidateSourcePartner = candidate.user.partner;
            visible = candidateSourcePartner.id === loggedInUser.partner.id;
          }
        }
      }
    }
    return visible;
  }

  /**
   * True if currently logged-in user works for the default source partner or a SourcePartner or
   * RecruiterPartner.
   * @private
   */
  private commonSeniorPartnerAuth(): boolean {
    let ok = false;
    const loggedInUser = this.getLoggedInUser()
    //Must be logged in
    if (loggedInUser) {
      if (this.isDefaultSourcePartner()) {
        //Default source partners can
        ok = true;
      } else {
        if (this.isSourcePartner() || this.isJobCreator()) {
            ok = true;
        }
      }
    }
    return ok;
  }
  /**
   * True if the currently logged in user is permitted to manage candidate tasks.
   */
  canManageCandidateTasks(): boolean {
    return this.commonSeniorPartnerAuth();
  }

  /**
   * True if the currently logged in user is permitted to publish lists.
   */
  canPublishList(): boolean {
    return this.commonSeniorPartnerAuth();
  }

  /**
   * Can they see and click on links to take them to Salesforce
   */
  canAccessSalesforce(): boolean {
    return this.isDefaultSourcePartner();
  }

  /**
   * True if the currently logged in user is permitted to update salesforce.
   */
  canUpdateSalesforce(): boolean {
    return this.commonSeniorPartnerAuth();
  }

  isAnAdmin(): boolean {
    let admin: boolean = false;
    switch (this.getLoggedInRole()) {
       case Role.systemadmin:
       case Role.admin:
       case Role.partneradmin:
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

  isPartnerAdminOrGreater(): boolean {
    return [Role.systemadmin, Role.admin, Role.partneradmin].includes(this.getLoggedInRole());
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

  isJobCreator(): boolean {
    const loggedInUser = this.getLoggedInUser();
    return loggedInUser == null ? false : loggedInUser.partner?.jobCreator;
  }

  isSourcePartner(): boolean {
    const loggedInUser = this.getLoggedInUser();
    return loggedInUser == null ? false : loggedInUser.partner?.sourcePartner;
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

  /**
   * Returns an RxStompConfig, populated with the current Authorization header token in
   * currentHeaders.
   */
  getRxStompConfig(): RxStompConfig {

    const config: RxStompConfig = {
      // Which server?
      //todo 9090 is john's lap top only
      //todo Not sure why need websocket on end?
      brokerURL: 'ws://localhost:9090/jobchat/websocket',

      // Headers
      connectHeaders: {
      },

      // How often to heartbeat?
      // Interval in milliseconds, set to 0 to disable
      heartbeatIncoming: 0, // Typical value 0 - disabled
      heartbeatOutgoing: 20000, // Typical value 20000 - every 20 seconds

      // Wait in milliseconds before attempting auto reconnect
      // Set to 0 to disable
      // Typical value 500 (500 milli seconds)
      reconnectDelay: 5000,

      // Will log diagnostics on console
      // It can be quite verbose, not recommended in production
      // Skip this key to stop logging to console
      debug: (msg: string): void => {
        console.log(new Date(), msg);
      },
    }

    const token = this.getToken();
    if (token) {
      config.connectHeaders.Authorization = `Bearer ${token}`
    }

    return config;
  }

  getToken(): string {
      //Automatically reconfigure RxStomp with the current token
      // like this.rxStomp.configure(this.getRxStompConfig()); - but currently is recursive
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
   * True if a user is logged in and they are associated with the default destination partner.
   */
  isDefaultJobCreator(): boolean {
    let defaultJobCreator = false;
    const loggedInUser = this.getLoggedInUser();
    if (loggedInUser) {
      defaultJobCreator = loggedInUser.partner?.defaultJobCreator;
    }
    return defaultJobCreator;
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

  isDefaultPartner(): boolean {
    return this.isDefaultSourcePartner() || this.isDefaultJobCreator();
  }

  /**
   * True is a user is logged in and they are solely responsible for certain candidate opportunities.
   * <p/>
   * This will be source partners.
   */
  ownsOpps() {
    //Source partners own candidate opportunities for the candidates they manage
    let result: boolean = false;

    const loggedInUser = this.getLoggedInUser();
    if (loggedInUser) {
      result = this.isDefaultSourcePartner() || this.isSourcePartner();
    }

    return result;
  }

  /**
   * Returns true if the currently logged-in user can change the stage of the given job
   * @param job Job
   */
  canChangeJobStage(job: Job): boolean {
    let result: boolean = false;

    //Can only change stage of jobs that have been published
    if (job.publishedDate != null) {
      //Todo Temporary fix
      result = this.isPartnerAdminOrGreater();

      //todo Temporary commented out
      //Current logic is that only a system admin or the contact user, defaulting to the creating user
      //of the job, can change the stage.
      // const loggedInUser = this.getLoggedInUser();
      // if (loggedInUser) {
      //   if (this.isSystemAdminOnly()) {
      //     result = true;
      //   } else {
      //     const contactUser = job.contactUser;
      //     const createUser = job.createdBy;
      //     const owner = contactUser != null ? contactUser : createUser;
      //     if (owner != null) {
      //       result = owner.id === loggedInUser.id;
      //     }
      //   }
      // }
    }
    return result
  }

  /**
   * True if the currently logged-in user can edit the given candidate opp.
   * @param opp Candidate opportunity
   */
  canEditCandidateOpp(opp: CandidateOpportunity) {
    return this.isPartnerAdminOrGreater() &&
      (this.isCandidateOurs(opp.candidate) || this.isJobOurs(opp.jobOpp));
  }
}
