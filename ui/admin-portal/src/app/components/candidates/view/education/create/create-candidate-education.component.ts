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

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateEducationService} from "../../../../../services/candidate-education.service";
import {CandidateEducation} from "../../../../../model/candidate-education";
import {CountryService} from "../../../../../services/country.service";
import {EducationMajorService} from "../../../../../services/education-major.service";
import {generateYearArray} from "../../../../../util/year-helper";

@Component({
  selector: 'app-create-candidate-education',
  templateUrl: './create-candidate-education.component.html',
  styleUrls: ['./create-candidate-education.component.scss']
})
export class CreateCandidateEducationComponent implements OnInit {

  candidateEducation: CandidateEducation;

  candidateForm: FormGroup;

  candidateId: number;
  majors = [];
  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateEducationService: CandidateEducationService,
              private countryService: CountryService,
              private educationMajorService: EducationMajorService) {
  }

  ngOnInit() {
    this.loading = true;

    /* load the years */
    this.years = generateYearArray(1950, true);

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

    this.candidateForm = this.fb.group({
      courseName: ['', [Validators.required]],
      institution: ['', [Validators.required]],
      countryId: ['', [Validators.required]],
      educationMajorId: ['', [Validators.required]],
      yearCompleted: [''],
      lengthOfCourseYears: [''],
      educationType: ['', [Validators.required]],
      incomplete: ['', ]
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    this.candidateEducationService.create(this.candidateId, this.candidateForm.value).subscribe(
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
