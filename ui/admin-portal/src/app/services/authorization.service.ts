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
import {Role, User} from "../model/user";
import {Candidate, ShortCandidate} from "../model/candidate";
import {Job, ShortJob} from "../model/job";
import {CandidateOpportunity} from "../model/candidate-opportunity";
import {AuthenticationService} from "./authentication.service";
import {CandidateSource} from "../model/base";

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

    if (this.isJobCreatorPartner() || this.isSourcePartner()) {
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
    if (this.isSourcePartner() || this.isJobCreatorPartner()) {
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
   * True if the currently logged-in user is permitted to see candidate contact details
   */
  canViewCandidateContact(): boolean {
    let result: boolean = false;
    if (this.isSourcePartner() || this.isJobCreatorPartner()) {
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
   * True if the logged-in user work for the source partner that is currently managing the
   * given candidate
   * @param candidate
   */
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

  isCandidateSourceMine(candidateSource: CandidateSource): boolean {
    let mine = false;
    if (candidateSource) {
      const loggedInUser = this.authenticationService.getLoggedInUser()
      //Must be logged in
      if (loggedInUser) {
        mine = candidateSource.createdBy?.id === loggedInUser.id;
      }
    }
    return mine;
  }

  /**
   * True if the logged-in user created the job or who is the contact for the job.
   * @param job
   */
  isJobMine(job: Job): boolean {
      let ours = false;
      const loggedInUser = this.authenticationService.getLoggedInUser()
      //Must be logged in
      if (loggedInUser) {
        ours = job.createdBy?.id === loggedInUser.id || job.contactUser?.id === loggedInUser.id;
      }
      return ours;
  }

  /**
   * True if the logged-in user works for the default job creator, or works for the partner who created
   * the given job.
   * @param job
   */
  isJobOurs(job: ShortJob): boolean {
      let ours = this.isDefaultJobCreator();
      if (!ours) {
        const loggedInUser = this.authenticationService.getLoggedInUser()
        //Must be logged in
        if (loggedInUser) {
          ours = job.jobCreator?.id === loggedInUser.partner.id;
        }
      }
      return ours;
  }

  isStarredByMe(users: User[]) {
    let starredByMe: boolean = false;
    const me: User = this.authenticationService.getLoggedInUser();
    if (users && me) {
      starredByMe = users.find(u => u.id === me.id ) !== undefined;
    }
    return starredByMe;
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

      if (this.isJobCreatorPartner() || this.isSourcePartner()) {

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
   * JobCreator.
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
        if (this.isSourcePartner() || this.isJobCreatorPartner()) {
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
    return !this.isReadOnly() && this.commonSeniorPartnerAuth();
  }

  /**
   * True if the currently logged in user is permitted to import to lists.
   */
  canImportToList(): boolean {
    //Read only and employer partners can't import to lists.
    return !this.isReadOnly() && !this.isEmployerPartner();
  }

  /**
   * True if the currently logged in user is permitted to export data from candidate sources.
   */
  canExportFromSource(): boolean {
    //Employer partners can't export.
    return !this.isEmployerPartner();
  }

  /**
   * True if the currently logged in user is permitted to publish lists.
   */
  canPublishList(): boolean {
    //Read only and employer partners can't publish lists.
    return !this.isReadOnly() && !this.isEmployerPartner() && this.commonSeniorPartnerAuth();
  }

  /**
   * Can they see and click on links to take them to Salesforce
   */
  canAccessSalesforce(): boolean {
    return this.isDefaultSourcePartner();
  }

  /**
   * Can they see and click on links to take them to candidate Google Drive folders
   */
  canAccessGoogleDrive(): boolean {
    return this.isDefaultSourcePartner();
  }

  /**
   * True if the currently logged in user is permitted to change a candidate's status.
   */
  canUpdateCandidateStatus(): boolean {
    //Employer partners cannot change a candidate's status
    return !this.isReadOnly() && !this.isEmployerPartner() && this.commonSeniorPartnerAuth();
  }

  /**
   * True if the currently logged in user is permitted to update salesforce.
   */
  canUpdateSalesforce(): boolean {
    return !this.isReadOnly() && this.commonSeniorPartnerAuth();
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

  /**
   * ReadOnly users can create their own lists and searches. But they can't change any shared
   * information.
   */
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

  isJobCreatorUser(): boolean {
    const loggedInUser = this.authenticationService.getLoggedInUser();
    return loggedInUser == null ? false : loggedInUser.jobCreator;
  }

  isJobCreatorPartner(): boolean {
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
   * Return true if currently logged-in user is viewing the TC as a source person.
   * <p/>
   * <p>
   * This is more complicated that just asking whether the user works for a source partner because
   * of TBB special status where it is both a source partner and a destination partner.
   * </p>
   * <p>
   * With the third condition check we also allow for a user who is not themselves designated a job
   * creator, but who works for an employer partner. Otherwise, they would be designated source by
   * this check.
   */
  isViewingAsSource(): boolean {
    //View as source partner as long as user is not a job creator or belonging to an employer org.
    return this.isSourcePartner() && !this.isJobCreatorUser() && !this.isEmployerPartner();
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
      this.isDefaultJobCreator() || this.isJobCreatorPartner();
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
    if (!this.isReadOnly() && job.publishedDate != null) {
      result = this.isPartnerAdminOrGreater();
    }
    return result
  }

  /**
   * True if the currently logged-in user can edit the given candidate opp.
   * @param opp Candidate opportunity
   */
  canEditCandidateOpp(opp: CandidateOpportunity) {
    return !this.isReadOnly() && this.isPartnerAdminOrGreater() &&
      (this.isCandidateOurs(opp.candidate) || this.isJobOurs(opp.jobOpp));
  }

  /**
   * True if the currently logged-in user can edit the given candidate source.
   * @param candidateSource Candidate source - ie SavedList or SavedSearch
   * @return true if can be edited, false if source is null
   */
    canEditCandidateSource(candidateSource: CandidateSource) {
    let editable = false;
    if (candidateSource) {
      if (this.isCandidateSourceMine(candidateSource)) {
        //If it is mine, I can edit it
        editable = true;
      } else {
        //If it is not mine I can still edit it if is not fixed and I am not a read only user
        editable = !candidateSource.fixed && !this.isReadOnly();
      }
    }
    return editable
  }

  canSeeGlobalLists() {
      return !this.isEmployerPartner();
  }

  canSeeJobDetails() {
    let result: boolean = false;
    if (this.isSourcePartner() || this.isJobCreatorPartner()) {
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
   * Only a System Admin or the user who created a Job can change its name.
   * @param job
   */
  canChangeJobName(job: Job) {
      let result: boolean = false;
      const loggedInUser = this.authenticationService.getLoggedInUser();
      if ((loggedInUser.id === job.createdBy.id) || this.isSystemAdminOnly()) {
        result = true;
      }
      return result;
  }

}
