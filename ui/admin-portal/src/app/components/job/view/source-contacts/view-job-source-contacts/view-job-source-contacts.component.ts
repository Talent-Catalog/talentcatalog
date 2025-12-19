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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Job} from "../../../../../model/job";
import {PartnerService} from "../../../../../services/partner.service";
import {
  Partner,
  sourceCountriesAsString,
  UpdatePartnerJobContactRequest
} from "../../../../../model/partner";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {User} from "../../../../../model/user";
import {UserService} from "../../../../../services/user.service";
import {SearchUserRequest} from "../../../../../model/base";
import {
  HasNameSelectorComponent
} from "../../../../util/has-name-selector/has-name-selector.component";
import {AuthenticationService} from "../../../../../services/authentication.service";
import {CreateChatRequest, JobChatType} from "../../../../../model/chat";
import {ChatService} from "../../../../../services/chat.service";
import {AuthorizationService} from "../../../../../services/authorization.service";

/*
MODEL: Modal popups.
 */
@Component({
  selector: 'app-view-job-source-contacts',
  templateUrl: './view-job-source-contacts.component.html',
  styleUrls: ['./view-job-source-contacts.component.scss']
})
export class ViewJobSourceContactsComponent implements OnInit {
  @Input() job: Job;
  @Input() selectable: boolean;
  @Output() sourcePartnerSelection = new EventEmitter();

  currentSourcePartner: Partner;
  error: any;
  loading: boolean;
  sourcePartners: Partner[];
  private loggedInUserPartnerId: number;

  constructor(
    private authenticationService: AuthenticationService,
    private authorizationService: AuthorizationService,
    private chatService: ChatService,
    private modalService: NgbModal,
    private partnerService: PartnerService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.loggedInUserPartnerId = this.authenticationService.getLoggedInUser()?.partner?.id;

    this.error = null;
    this.loading = true;
    this.partnerService.listSourcePartners(this.job).subscribe(
      (sourcePartners) => {this.setSourcePartners(sourcePartners); this.loading = false},
      (error) => {this.error = error; this.loading = false}
    )
  }

  private setSourcePartners(partners: Partner[]) {
    this.sourcePartners = partners;

    //Now populate all their chats
    this.sourcePartners.forEach(partner => this.fetchSourcePartnerChat(partner));
  }

  editPartnerContact(partner: Partner) {
    //Get users for given partner.
    const request: SearchUserRequest = {
      partnerId: partner.id,
      sortFields: ["firstName", "lastName"],
      sortDirection: "ASC"
    }
    this.error = null;
    this.loading = true;
    this.userService.search(request)
    .subscribe(
      (users) => {this.loading = false; this.selectPartnerContactUser(partner, users)},
      (error) => {this.error = error; this.loading = false},
    );
  }

  /**
   * Select partner contact from drop down of given users
   */
  private selectPartnerContactUser(partner: Partner, users: User[]) {
    users.forEach(user => user.name = user.firstName + " " + user.lastName)
    const selectUserModal = this.modalService.open(HasNameSelectorComponent, {
      centered: true,
      backdrop: 'static'
    });
    selectUserModal.componentInstance.label = "Select user to contact";
    selectUserModal.componentInstance.hasNames = users;
    selectUserModal.result.then(
      (user: User) => {this.updateContact(partner, user)},
    )
    .catch(() => {})
  }

  /**
   * Update the given partners contact user for this job
   * @param partner Partner
   * @param user Contact user
   */
  private updateContact(partner: Partner, user: User) {
    const request: UpdatePartnerJobContactRequest = {
      jobId: this.job.id,
      userId: user.id
    }
    this.error = null;
    this.loading = true;
    this.partnerService.updateJobContact(partner.id, request).subscribe(
      (partner) => {this.updateSourcePartners(partner); this.loading = false},
      error => {this.error = error; this.loading = false}
    );
  }

  /**
   * Update given partner in our array of source partners
   * @param partner Updated partner
   */
  private updateSourcePartners(partner: Partner) {
    let modifiedPartnerIndex = this.sourcePartners.findIndex(p => p.id === partner.id );
    if (modifiedPartnerIndex >= 0) {
      this.sourcePartners[modifiedPartnerIndex] = partner;
    } else {
      console.log("Bug - partner " + partner.id + " not found in source partners")
    }
  }

  isEditable(partner: Partner): boolean {
    //Can only edit if not read only, and viewing as source.
    let canEdit: boolean = !this.authorizationService.isReadOnly()
      && this.authorizationService.isViewingAsSource();
    if (canEdit) {
      canEdit = this.loggedInUserPartnerId === partner.id;
    }
    return canEdit;
  }

  isShowReadStatus(partner: Partner) {
    let showStatus: boolean;

    if (this.selectable) {
      showStatus = partner._jobChat != null;
    } else {
      //Only showstatus if the partner is me
      showStatus = this.loggedInUserPartnerId === partner.id;
    }
    return showStatus;
  }

  sourceCountries(partner: Partner) {
    let ret = "";
    const s = sourceCountriesAsString(partner);
    if (s) {
      ret = "(" + s + ")"
    }
    return ret;
  }

  selectCurrent(partner: Partner) {
    if (this.selectable) {
      this.currentSourcePartner = partner;
      this.sourcePartnerSelection.emit(partner);

    }
  }

  private fetchSourcePartnerChat(partner: Partner) {
    const request: CreateChatRequest = {
      type: JobChatType.JobCreatorSourcePartner,
      jobId: this.job?.id,
      sourcePartnerId: partner?.id
    }

    this.error = null;
    this.chatService.getOrCreate(request).subscribe(
      (chat) => {partner._jobChat = chat},
      (error) => {this.error = error}
    )
  }

  jobContact(partner: Partner): User {
    let user = partner?.jobContact;
    if (!user) {
      user = partner?.defaultContact;
    }
    return user;
  }
}
