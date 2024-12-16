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
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditJobInfoComponent} from "../edit-job-info/edit-job-info.component";
import {User} from "../../../../../model/user";
import {SavedList} from "../../../../../model/saved-list";
import {
  JobPrepDueDate,
  JobPrepItem,
  JobPrepSuggestedCandidates
} from "../../../../../model/job-prep-item";
import {AuthorizationService} from "../../../../../services/authorization.service";

@Component({
  selector: 'app-view-job-info',
  templateUrl: './view-job-info.component.html',
  styleUrls: ['./view-job-info.component.scss']
})
export class ViewJobInfoComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;
  @Input() highlightItem: JobPrepItem;
  @Output() jobUpdated = new EventEmitter<Job>();

  constructor(
    private modalService: NgbModal,
    private authorizationService: AuthorizationService
  ) { }

  ngOnInit(): void {
  }

  editJobInfo() {
    const editModal = this.modalService.open(EditJobInfoComponent, {
      centered: true,
      backdrop: 'static'
    });

    editModal.componentInstance.job = this.job;

    editModal.result
    .then((job) => this.jobUpdated.emit(job))
    .catch(() => {});

  }

  showUser(contactUser: User): string {
    return contactUser ?
      contactUser.firstName + " " + contactUser.lastName  : "";
  }

  displaySavedList(list: SavedList): string {
    return list ? list.name + "(" + list.id + ")" : "";
  }

  showEmailUrl(email: string) {
    return email ? "mailto:" + email : "";
  }

  highlightCandidates() {
    return this.highlightItem instanceof JobPrepSuggestedCandidates;
  }

  highlightSubmissionDate() {
    return this.highlightItem instanceof JobPrepDueDate;
  }

  getContactUser(): User {
    return this.job.contactUser ? this.job.contactUser : this.job.createdBy;
  }

  public canSeeJobDetails() {
    return this.authorizationService.canSeeJobDetails()
  }

}
