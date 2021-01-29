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
import {CandidateCertificationService} from "../../../../../services/candidate-certification.service";
import {CandidateCertification} from "../../../../../model/candidate-certification";
import {CountryService} from "../../../../../services/country.service";

@Component({
  selector: 'app-edit-candidate-certification',
  templateUrl: './edit-candidate-certification.component.html',
  styleUrls: ['./edit-candidate-certification.component.scss']
})
export class EditCandidateCertificationComponent implements OnInit {

  candidateCertification: CandidateCertification;

  candidateForm: FormGroup;

  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateCertificationService: CandidateCertificationService,
              private countryService: CountryService ) {
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

    this.candidateForm = this.fb.group({
      name: [this.candidateCertification.name, Validators.required],
      institution: [this.candidateCertification.institution, Validators.required],
      dateCompleted: [this.candidateCertification.dateCompleted, Validators.required],
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    this.candidateCertificationService.update(this.candidateCertification.id, this.candidateForm.value).subscribe(
      (candidateCertification) => {
        this.closeModal(candidateCertification);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateCertification: CandidateCertification) {
    this.activeModal.close(candidateCertification);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
