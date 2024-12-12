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
import {Country} from "../../../../model/country";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CountryService} from "../../../../services/country.service";

@Component({
  selector: 'app-edit-country',
  templateUrl: './edit-country.component.html',
  styleUrls: ['./edit-country.component.scss']
})
export class EditCountryComponent implements OnInit {

  countryId: number;
  countryForm: UntypedFormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private countryService: CountryService) {
  }

  ngOnInit() {
    this.loading = true;
    this.countryService.get(this.countryId).subscribe(country => {
      this.countryForm = this.fb.group({
        name: [country.name, Validators.required],
        status: [country.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.countryService.update(this.countryId, this.countryForm.value).subscribe(
      (country) => {
        this.closeModal(country);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(country: Country) {
    this.activeModal.close(country);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
