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
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateService} from '../../../../../services/candidate.service';
import {Candidate, Gender} from '../../../../../model/candidate';
import {CountryService} from '../../../../../services/country.service';
import {generateYearArray} from '../../../../../util/year-helper';
import {EnumOption, enumOptions} from "../../../../../util/enum";
import {EMAIL_REGEX} from "../../../../../model/base";

@Component({
  selector: 'app-edit-candidate-contact',
  templateUrl: './edit-candidate-contact.component.html',
  styleUrls: ['./edit-candidate-contact.component.scss']
})
export class EditCandidateContactComponent implements OnInit {

  candidateId: number;


  candidateForm: FormGroup;

  genderOptions: EnumOption[] = enumOptions(Gender);
  nationalities = [];
  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  protected readonly emailRegex: string = EMAIL_REGEX;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateService: CandidateService,
              private countryService: CountryService ) {
  }

  ngOnInit() {
    this.loading = true;

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

    /*load the nationalities */
    this.countryService.listCountries().subscribe(
      (response) => {
        this.nationalities = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        firstName: [candidate.user.firstName],
        lastName: [candidate.user.lastName],
        gender: [candidate.gender],
        address1: [candidate.address1],
        city: [candidate.city],
        state: [candidate.state],
        countryId: [candidate.country ? candidate.country.id : null, Validators.required],
        yearOfArrival: [candidate.yearOfArrival],
        phone: [candidate.phone],
        whatsapp: [candidate.whatsapp],
        email: [candidate.user.email],
        dob: [candidate.dob],
        nationalityId: [candidate.nationality ? candidate.nationality.id : null, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.update(this.candidateId, this.candidateForm.value).subscribe(
      (candidate) => {
        this.closeModal(candidate);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidate: Candidate) {
    this.activeModal.close(candidate);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
