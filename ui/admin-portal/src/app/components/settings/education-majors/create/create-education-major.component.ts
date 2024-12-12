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
import {EducationMajor} from "../../../../model/education-major";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EducationMajorService} from "../../../../services/education-major.service";

@Component({
  selector: 'app-create-education-major',
  templateUrl: './create-education-major.component.html',
  styleUrls: ['./create-education-major.component.scss']
})

export class CreateEducationMajorComponent implements OnInit {

  educationMajorForm: UntypedFormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private educationMajorService: EducationMajorService) {
  }

  ngOnInit() {
    this.educationMajorForm = this.fb.group({
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.educationMajorService.create(this.educationMajorForm.value).subscribe(
      (educationMajor) => {
        this.closeModal(educationMajor)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(educationMajor: EducationMajor) {
    this.activeModal.close(educationMajor);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
