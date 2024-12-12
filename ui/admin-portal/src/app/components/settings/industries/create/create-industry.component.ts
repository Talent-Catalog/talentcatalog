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
import {Industry} from "../../../../model/industry";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {IndustryService} from "../../../../services/industry.service";

@Component({
  selector: 'app-create-industry',
  templateUrl: './create-industry.component.html',
  styleUrls: ['./create-industry.component.scss']
})

export class CreateIndustryComponent implements OnInit {

  industryForm: UntypedFormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private industryService: IndustryService) {
  }

  ngOnInit() {
    this.industryForm = this.fb.group({
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.industryService.create(this.industryForm.value).subscribe(
      (industry) => {
        this.closeModal(industry)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(industry: Industry) {
    this.activeModal.close(industry);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
