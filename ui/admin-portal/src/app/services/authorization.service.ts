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
import {Role} from "../model/user";
import {Candidate, ShortCandidate} from "../model/candidate";
import {Job, ShortJob} from "../model/job";
import {CandidateOpportunity} from "../model/candidate-opportunity";
import {AuthenticationService} from "./authentication.service";

@Injectable({
  providedIn: 'root'
})
export class AuthorizationService {

  constructor(private authenticationService: AuthenticationService) {
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
    const loggedInUser = this.authenticationService.getLoggedInUser();
    return loggedInUser == null ? false : Role[loggedInUser.role] === Role.systemadmin;
  }

  canAssignTask(): boolean {
    //For now only TBB can do this.
    //Todo Need to make this more broadly available. It gets complicated when assigning tasks
    //to a list - if that list has candidates from multiple partners.
    return this.isDefaultSourcePartner();
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
    if (candidate) {
      const loggedInUser = this.authenticationService.getLoggedInUser()
      //Must be logged in
      if (loggedInUser) {
        ours = candidate.user?.partner?.id === loggedInUser.partner.id;
      }
    }
    return ours;
  }

  isJobOurs(job: ShortJob): boolean {

    //For now all jobs belong to just the default partner.
    return this.isDefaultJobCreator();

    //todo Eventually when we have proper recruiter partner support, the code will look like this:
    /*
      let ours = false;
      const loggedInUser = this.authenticationService.getLoggedInUser()
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
    const loggedInUser = this.authenticationService.getLoggedInUser()
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
    const loggedInUser = this.authenticationService.getLoggedInUser()
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
    return this.authenticationService.getLoggedInUser() != null;
  }

  isReadOnly(): boolean {
    const loggedInUser = this.authenticationService.getLoggedInUser();
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
    const loggedInUser = this.authenticationService.getLoggedInUser();
    return loggedInUser == null ? Role.limited : Role[loggedInUser.role];
  }

  isJobCreator(): boolean {
    const loggedInUser = this.authenticationService.getLoggedInUser();
    return loggedInUser == null ? false : loggedInUser.partner?.jobCreator;
  }

  isEmployerPartner(): boolean {
    const loggedInUser = this.authenticationService.getLoggedInUser();
    const partner = loggedInUser == null ? null : loggedInUser.partner;
    return partner == null ? false : partner.employer != null && partner.jobCreator;
  }

  isSourcePartner(): boolean {
    const loggedInUser = this.authenticationService.getLoggedInUser();
    return loggedInUser == null ? false : loggedInUser.partner?.sourcePartner;
  }

  /**
   * True if the currently logged-in user is permitted to edit the given candidate's details
   * @param candidate Candidate to be checked
   */
  isEditableCandidate(candidate: Candidate): boolean {
    let editable = false;
    const loggedInUser = this.authenticationService.getLoggedInUser()
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
    const loggedInUser = this.authenticationService.getLoggedInUser();
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
    const loggedInUser = this.authenticationService.getLoggedInUser();
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
   * This will be source partners and job creators
   */
  ownsOpps() {
    //Source partners own candidate opportunities for the candidates they manage
    let result: boolean = false;

    const loggedInUser = this.authenticationService.getLoggedInUser();
    if (loggedInUser) {
      result = this.isDefaultSourcePartner() || this.isSourcePartner() ||
      this.isDefaultJobCreator() || this.isJobCreator();
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
      // const loggedInUser = this.authenticationService.getLoggedInUser();
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
