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
import {forkJoin} from "rxjs";
import {PartnerService} from "../../../../../services/partner.service";
import {Partner, PartnerType} from "../../../../../model/partner";
import {SearchPartnerRequest, SearchUserRequest} from "../../../../../model/base";
import {UserService} from "../../../../../services/user.service";
import {User} from "../../../../../model/user";
import {AuthService} from "../../../../../services/auth.service";

@Component({
  selector: 'app-edit-job-info',
  templateUrl: './edit-job-info.component.html',
  styleUrls: ['./edit-job-info.component.scss']
})
export class EditJobInfoComponent implements OnInit {

  jobId: number;

  job: Job;

  jobForm: FormGroup;

  recruiters: Partner[];
  users: User[];

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private authService: AuthService,
              private jobService: JobService,
              private partnerService: PartnerService,
              private userService: UserService
  ) { }

  ngOnInit(): void {
    this.error = null;
    this.loading = true;
    const userRequest: SearchUserRequest = {
      sortFields: ["firstName", "lastName"],
      sortDirection: "ASC"
    };
    const partnerRequest: SearchPartnerRequest = {partnerType: PartnerType.RecruiterPartner};
    forkJoin({
      'job': this.jobService.get(this.jobId),
      'partners': this.partnerService.search(partnerRequest),
      'users': this.userService.search(userRequest),
    }).subscribe(results => {
      this.loading = false;
      this.recruiters = results['partners'];
      this.users = results['users'].map(u => {u.name = u.firstName + " " + u.lastName; return u});
      this.job = results['job'];
      this.createForm();
    }, error => {
      this.loading = false;
      this.error = error;
    });
  }

  private createForm() {
    this.jobForm = this.fb.group({
      submissionDueDate: [this.job.submissionDueDate],
      contactEmail: [this.job.contactEmail],
      contactUser: [this.job.contactUser],
      recruiterPartner: [this.job.recruiterPartner]
      //  todo other fields
    });
  }

  get submissionDueDate(): Date {
    return this.jobForm?.value.submissionDueDate;
  }

  onSave() {
    this.error = null;
    this.saving = true;
    //todo need to add contactEmail, recruiter  etc
    const request: UpdateJobRequest = {
      submissionDueDate: this.submissionDueDate
    }

    this.jobService.update(this.jobId, request).subscribe(
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
