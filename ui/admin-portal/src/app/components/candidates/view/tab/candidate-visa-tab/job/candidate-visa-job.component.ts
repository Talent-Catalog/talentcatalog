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
import {ShortJob} from "../../../../../../model/job";
import {HasNameSelectorComponent} from "../../../../../util/has-name-selector/has-name-selector.component";
import {
  CandidateVisaJobService,
  CreateCandidateVisaJobRequest
} from "../../../../../../services/candidate-visa-job.service";
import {ConfirmationComponent} from "../../../../../util/confirm/confirmation.component";
import {Candidate, CandidateIntakeData, CandidateVisa, CandidateVisaJobCheck} from "../../../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {AuthorizationService} from "../../../../../../services/authorization.service";

@Component({
  selector: 'app-candidate-visa-job',
  templateUrl: './candidate-visa-job.component.html',
  styleUrls: ['./candidate-visa-job.component.scss']
})
export class CandidateVisaJobComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  /**
   * Two way data binding to keep logic contained in this reusable component. Handle the selection of the visa job check
   * and also fetching the updated version of the visa job to be passed back to display.
   * This allows us to keep the form data updated when switching between visa jobs.
   */
  @Input() selectedJob: CandidateVisaJobCheck;
  @Output() selectedJobChange = new EventEmitter<CandidateVisaJobCheck>();

  selectedIndex: number;
  loading: boolean;
  error: string;
  form: UntypedFormGroup;

  constructor(private candidateVisaJobService: CandidateVisaJobService,
              private modalService: NgbModal,
              private fb: UntypedFormBuilder,
              private authService: AuthorizationService) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      jobIndex: [0]
    });
    this.selectedJobChange.emit(this.visaCheckRecord.candidateVisaJobChecks[0]);
  }

  private get filteredSfJobs(): ShortJob[] {
    /**
     * IF there are no existing visa job checks, return all the jobs associated with candidate's candidate opportunities
     * but filtered by the same destination country as the specific visa check country.
     * ELSE add additional filter removing completed visa job check jobs
     * SO that we avoid double ups of visa job checks for the same job,
     * and that jobs are only having visa checks for their appropriate destination country.
     */
    if (!this.hasJobChecks) {
      const ops = this.candidate.candidateOpportunities
        .map(co => co.jobOpp)
        .filter(jo => jo.country.id == this.visaCheckRecord.country.id);
      return ops;
    } else {
      /**
       * NOTE: Some job checks don't have a SF Job Opp associated as these were entered in an earlier version of the code.
       */
      const existingJobIds: number [] = this.visaCheckRecord.candidateVisaJobChecks
        .map(jobCheck => jobCheck?.jobOpp?.id);

      return this.candidate.candidateOpportunities
        .map(co => co.jobOpp)
        .filter(jo => !existingJobIds.includes(jo.id) && jo.country.id == this.visaCheckRecord.country.id)
    }
  }

  get hasJobOpps() {
    return this.candidate.candidateOpportunities?.length > 0;
  }

  get hasJobChecks() {
    return this.visaCheckRecord?.candidateVisaJobChecks?.length > 0;
  }

  addJob() {
    const modal = this.modalService.open(HasNameSelectorComponent);
    modal.componentInstance.hasNames = this.filteredSfJobs;
    modal.componentInstance.label = "Candidate's Job Opportunities for Destination: " + this.visaCheckRecord.country.name;

    modal.result
    .then((selection: ShortJob) => {
      if (selection) {
        this.createVisaJobCheck(selection);
      }
    })
    .catch(() => {
      //User cancelled selection
    });
  }

  createVisaJobCheck(jobOpp: ShortJob) {
    this.loading = true;
    let request: CreateCandidateVisaJobRequest = {
      jobOppId: jobOpp.id,
    }
    this.candidateVisaJobService.create(this.visaCheckRecord.id, request).subscribe(
      (jobCheck) => {
        this.visaCheckRecord?.candidateVisaJobChecks?.push(jobCheck);
        this.form.controls['jobIndex'].patchValue(this.visaCheckRecord?.candidateVisaJobChecks?.lastIndexOf(jobCheck));
        this.selectedJobChange.emit(jobCheck);
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });

  }

  deleteJob(i: number) {
    const confirmationModal = this.modalService.open(ConfirmationComponent);
    const visaJobCheck: CandidateVisaJobCheck = this.visaCheckRecord.candidateVisaJobChecks[i];
    const jobName = visaJobCheck.jobOpp.name;
    confirmationModal.componentInstance.message =
      "Are you sure you want to delete the job check for " + jobName + '?';
    confirmationModal.result
    .then((result) => {
      if (result === true) {
        this.doDelete(i);
      }
    })
    .catch(() => {});
  }

  private doDelete(i: number) {
    this.loading = true;
    let jobCheck = this.visaCheckRecord.candidateVisaJobChecks[i];
    this.candidateVisaJobService.delete(jobCheck.id).subscribe(
      (done) => {
        this.loading = false;
        this.visaCheckRecord.candidateVisaJobChecks.splice(i, 1);
        this.form.controls.jobIndex.patchValue(0);
        this.fetchUpdatedSelectedJob(this.visaCheckRecord.candidateVisaJobChecks[0]);
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  /**
   * Takes in the selected visa job and gets the updated object from the database. This updated visa job check
   * object is emitted back to parent and the visa check record is updated.
   * This allows us to retain the changed form data when switching between visa jobs.
   */
  fetchUpdatedSelectedJob(visaJob: CandidateVisaJobCheck) {
    if (visaJob) {
      this.candidateVisaJobService.get(visaJob.id).subscribe(
        (result) => {
          this.visaCheckRecord.candidateVisaJobChecks[this.form.controls.jobIndex.value] = result;
          this.selectedJobChange.emit(result);
        }
      )
    }
  }

  canDeleteVisaJob() : boolean {
    return this.authService.isSystemAdminOnly();
  }

  fetchCandidateOppIdForJob(jobId: number): number {
    if (this.hasJobOpps) {
      return this.candidate.candidateOpportunities.find(
        co => co.jobOpp.id === jobId).id;
    }
  }

  isEditable(): boolean {
    return this.authService.isEditableCandidate(this.candidate);
  }
}
