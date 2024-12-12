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
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateEducationService,
  UpdateCandidateEducationRequest
} from "../../../../../services/candidate-education.service";
import {CandidateEducation} from "../../../../../model/candidate-education";
import {CountryService} from "../../../../../services/country.service";
import {EducationMajorService} from "../../../../../services/education-major.service";
import {generateYearArray} from "../../../../../util/year-helper";

@Component({
  selector: 'app-edit-candidate-education',
  templateUrl: './edit-candidate-education.component.html',
  styleUrls: ['./edit-candidate-education.component.scss']
})
export class EditCandidateEducationComponent implements OnInit {

  candidateEducation: CandidateEducation;

  candidateForm: UntypedFormGroup;

  countries = [];
  majors = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateEducationService: CandidateEducationService,
              private countryService: CountryService,
              private educationMajorService: EducationMajorService) {
  }

  ngOnInit() {
    this.loading = true;

    /*load the countries */
    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    /*load the countries */
    this.educationMajorService.listMajors().subscribe(
      (response) => {
        this.majors = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.years = generateYearArray(1950, true, null, 4);

    this.candidateForm = this.fb.group({
      courseName: [this.candidateEducation.courseName],
      institution: [this.candidateEducation.institution],
      countryId: [this.candidateEducation.country ? this.candidateEducation.country.id : null, Validators.required],
      majorId: [this.candidateEducation.educationMajor ? this.candidateEducation.educationMajor.id : null, Validators.required],
      yearCompleted: [this.candidateEducation.yearCompleted],
      educationType: [this.candidateEducation.educationType],
      lengthOfCourseYears: [this.candidateEducation.lengthOfCourseYears],
      incomplete: [this.candidateEducation.incomplete ]
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    const request: UpdateCandidateEducationRequest = {
      id: this.candidateEducation.id,
      courseName: this.candidateForm.value.courseName,
      institution: this.candidateForm.value.institution,
      countryId: this.candidateForm.value.countryId,
      majorId: this.candidateForm.value.majorId,
      yearCompleted: this.candidateForm.value.yearCompleted,
      lengthOfCourseYears: this.candidateForm.value.lengthOfCourseYears,
      educationType: this.candidateForm.value.educationType,
      incomplete: this.candidateForm.value.incomplete
    }
    this.candidateEducationService.update(request).subscribe(
      (candidateEducation) => {
        this.closeModal(candidateEducation);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateEducation: CandidateEducation) {
    this.activeModal.close(candidateEducation);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
