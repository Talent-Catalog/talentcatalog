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
import {Job} from "../../../../../model/job";
import {forkJoin} from "rxjs";
import {PartnerService} from "../../../../../services/partner.service";
import {Partner, PartnerType} from "../../../../../model/partner";
import {SearchPartnerRequest} from "../../../../../model/base";

@Component({
  selector: 'app-edit-job-info',
  templateUrl: './edit-job-info.component.html',
  styleUrls: ['./edit-job-info.component.scss']
})
export class EditJobInfoComponent implements OnInit {

  jobId: number;

  jobForm: FormGroup;

  recruiters: Partner[];

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private jobService: JobService,
              private partnerService: PartnerService
  ) { }

  ngOnInit(): void {
    this.error = null;
    this.loading = true;
    const partnerRequest: SearchPartnerRequest = {partnerType: PartnerType.RecruiterPartner};
    forkJoin({
      'job': this.jobService.get(this.jobId),
      'partners': this.partnerService.search(partnerRequest),
    }).subscribe(results => {
      this.loading = false;
      this.recruiters = results['partners'];
      let job: Job = results['job'];
      this.createForm(job);
    }, error => {
      this.loading = false;
      this.error = error;
    });
  }

  private createForm(job: Job) {
    this.jobForm = this.fb.group({
      submissionDueDate: [job.submissionDueDate],
      contactEmail: [job.contactEmail],
      recruiterPartner: [job.recruiterPartner]
      //  todo other fields
    });
  }

  onSave() {
    this.error = null;
    this.saving = true;
    //todo need to add contactEmail, recruiter

    this.jobService.update(this.jobId, this.jobForm.value).subscribe(
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
}
