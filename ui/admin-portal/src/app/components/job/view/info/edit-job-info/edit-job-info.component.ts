/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {JobService} from "../../../../../services/job.service";
import {Job, UpdateJobRequest} from "../../../../../model/job";
import {PartnerService} from "../../../../../services/partner.service";
import {SearchUserRequest} from "../../../../../model/base";
import {UserService} from "../../../../../services/user.service";
import {User} from "../../../../../model/user";
import {AuthorizationService} from "../../../../../services/authorization.service";

@Component({
  selector: 'app-edit-job-info',
  templateUrl: './edit-job-info.component.html',
  styleUrls: ['./edit-job-info.component.scss']
})
export class EditJobInfoComponent implements OnInit {

  job: Job;

  jobForm: FormGroup;

  users: User[];

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private authService: AuthorizationService,
              private jobService: JobService,
              private partnerService: PartnerService,
              private userService: UserService
  ) { }

  ngOnInit(): void {
    this.error = null;
    this.loading = true;
    const userRequest: SearchUserRequest = {
      partnerId: this.job.jobCreator?.id,
      sortFields: ["firstName", "lastName"],
      sortDirection: "ASC"
    };

    this.userService.search(userRequest).subscribe(
      users => {
        this.users = users.map(u => {u.name = u.firstName + " " + u.lastName; return u});
        this.loading = false;
        this.createForm()
      },
      error => {this.error = error; this.loading = false}
    )
  }

  private createForm() {
    this.jobForm = this.fb.group({
      submissionDueDate: [this.job.submissionDueDate],
      contactUser: [this.job.contactUser?.id],
    });
  }

  get contactUser(): number {
    return this.jobForm?.value.contactUser;
  }

  get submissionDueDate(): Date {
    return this.jobForm?.value.submissionDueDate;
  }

  onSave() {
    this.error = null;
    this.saving = true;
    const request: UpdateJobRequest = {
      sfId: this.job.sfId,
      contactUserId: this.contactUser,
      submissionDueDate: this.submissionDueDate
    }

    this.jobService.update(this.job.id, request).subscribe(
      (job) => {
        this.closeModal(job);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(job: Job) {
    this.activeModal.close(job);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  canChangeJobStage() {
    return this.authService.canChangeJobStage(this.job);
  }
}
