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

import {Component, Input, OnInit} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../../util/intake/IntakeComponentTabBase';
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck,
  getIeltsScoreTypeString
} from '../../../../../../model/candidate';
import {FormBuilder, FormGroup} from "@angular/forms";
import {CandidateService} from "../../../../../../services/candidate.service";
import {CountryService} from "../../../../../../services/country.service";
import {EducationLevelService} from "../../../../../../services/education-level.service";
import {OccupationService} from "../../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../../services/candidate-note.service";
import {AuthService} from "../../../../../../services/auth.service";
import {
  CandidateVisaJobService,
  CreateCandidateVisaJobRequest
} from "../../../../../../services/candidate-visa-job.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateVisaJobAssessementComponent} from "../../../../visa/visa-job-assessments/modal/create-visa-job-assessement.component";
import {ConfirmationComponent} from "../../../../../util/confirm/confirmation.component";
import {Country} from "../../../../../../model/country";

@Component({
  selector: 'app-visa-check-au',
  templateUrl: './visa-check-au.component.html',
  styleUrls: ['./visa-check-au.component.scss']
})
export class VisaCheckAuComponent extends IntakeComponentTabBase implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaRecord: CandidateVisa;
  loading: boolean;
  form: FormGroup;
  @Input() nationalities: Country[];
  saving: boolean;
  jobIndex: number;
  selectedJobCheck: CandidateVisaJobCheck;
  currentYear: string;
  birthYear: string;

  constructor(candidateService: CandidateService,
              countryService: CountryService,
              educationLevelService: EducationLevelService,
              occupationService: OccupationService,
              languageLevelService: LanguageLevelService,
              noteService: CandidateNoteService,
              authService: AuthService,
              private candidateVisaJobService: CandidateVisaJobService,
              private modalService: NgbModal,
              private fb: FormBuilder) {
    super(candidateService, countryService, educationLevelService, occupationService, languageLevelService, noteService, authService)
  }

  onDataLoaded(init: boolean) {
    if (init) {
      if (this.visaRecord) {
        this.currentYear = new Date().getFullYear().toString();
        this.birthYear = this.candidate?.dob?.toString().slice(0, 4);

        //If we have some visa checks, select the first one
        if (this.visaRecord?.candidateVisaJobChecks?.length > 0) {
          this.jobIndex = 0;
        }
      }

      this.form = this.fb.group({
        jobIndex: [this.jobIndex]
      });

      this.changeJobOpp(null);
    }
  }

  addRecord() {
    const modal = this.modalService.open(CreateVisaJobAssessementComponent);

    modal.result
      .then((request: CreateCandidateVisaJobRequest) => {
        if (request) {
          this.createRecord(request)
        }
      })
      .catch(() => {
        //User cancelled selection
      });
  }

  createRecord(request: CreateCandidateVisaJobRequest) {
    this.loading = true;
    this.candidateVisaJobService.create(this.visaRecord.id, request)
      .subscribe(
        (jobCheck) => {
          this.visaRecord?.candidateVisaJobChecks?.push(jobCheck);
          this.form.controls['jobIndex'].patchValue(this.visaRecord?.candidateVisaJobChecks?.lastIndexOf(jobCheck));
          this.changeJobOpp(null);
          this.selectedJobCheck = jobCheck;
          this.loading = false;
        },
        (error) => {
          this.error = error;
          this.loading = false;
        });

  }

  deleteRecord(i: number) {
    const confirmationModal = this.modalService.open(ConfirmationComponent);
    const visaJobCheck: CandidateVisaJobCheck = this.visaRecord.candidateVisaJobChecks[i];

    confirmationModal.componentInstance.message =
      "Are you sure you want to delete the job check for " + visaJobCheck.name;
    confirmationModal.result
      .then((result) => {
        if (result === true) {
          this.doDelete(i, visaJobCheck);
        }
      })
      .catch(() => {});
  }

  private doDelete(i: number, visaJobCheck: CandidateVisaJobCheck) {
    this.loading = true;
    this.candidateVisaJobService.delete(visaJobCheck.id).subscribe(
      (done) => {
        this.loading = false;
        this.visaRecord.candidateVisaJobChecks.splice(i, 1);
        this.changeJobOpp(null);
        this.form.controls.jobIndex.patchValue(0);
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  changeJobOpp(event: Event) {
    this.jobIndex = this.form.controls.jobIndex.value;
    if (this.visaRecord.candidateVisaJobChecks) {
      this.selectedJobCheck = this.visaRecord.candidateVisaJobChecks[this.jobIndex];
    }
    //this.jobCheckAu.changeCheck(this.selectedJobCheck);
  }

  get selectedCountry(): string {
    return this.visaRecord?.country?.name;
  }

  get ieltsScoreType(): string {
    return getIeltsScoreTypeString(this.candidate);
  }
}
