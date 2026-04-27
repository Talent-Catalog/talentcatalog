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

import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {JobService} from "../../../../../services/job.service";
import {Job, UpdateJobRequest} from "../../../../../model/job";
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

  jobForm: UntypedFormGroup;

  users: User[];

  error;
  loading: boolean;
  saving: boolean;

  evergreenTip = "An evergreen job is always looking for candidates";
  skipCandidateSearchTip = "If checked, partners will not search for candidates";
  nameTip = "Only the user who created a job can change its name. Contact a TC admin if " +
    "this presents an issue.";
  submissionDueDateTip = "All prospective candidates should be submitted so that " +
    "recruitment can proceed by given date."

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private authService: AuthorizationService,
              private jobService: JobService,
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
      name: [this.job.name],
      submissionDueDate: [this.job.submissionDueDate],
      contactUser: [this.job.contactUser?.id],
      evergreen: [this.job.evergreen],
      skipCandidateSearch: [this.job.skipCandidateSearch]
    });
  }

  get contactUser(): number {
    return this.jobForm?.value.contactUser;
  }

  get evergreen(): boolean {
    return this.jobForm?.value.evergreen;
  }

  get skipCandidateSearch(): boolean {
    return this.jobForm?.value.skipCandidateSearch;
  }

  get submissionDueDate(): Date {
    return this.jobForm?.value.submissionDueDate;
  }

  /**
   * Returns null unless changed in form.
   */
  get jobName(): string {
    return this.jobForm?.value.name === this.job.name ? null : this.jobForm.value.name;
  }

  onSave() {
    this.error = null;
    this.saving = true;
    const request: UpdateJobRequest = {
      sfId: this.job.sfId,
      contactUserId: this.contactUser,
      evergreen: this.evergreen,
      skipCandidateSearch: this.skipCandidateSearch,
      submissionDueDate: this.submissionDueDate,
      jobName: this.jobName,
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

  canChangeJobName() {
    return this.authService.canChangeJobName(this.job);
  }
}
