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
  selector: 'app-edit-education-major',
  templateUrl: './edit-education-major.component.html',
  styleUrls: ['./edit-education-major.component.scss']
})
export class EditEducationMajorComponent implements OnInit {

  educationMajorId: number;
  educationMajorForm: UntypedFormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private educationMajorService: EducationMajorService) {
  }

  ngOnInit() {
    this.loading = true;
    this.educationMajorService.get(this.educationMajorId).subscribe(educationMajor => {
      this.educationMajorForm = this.fb.group({
        name: [educationMajor.name, Validators.required],
        status: [educationMajor.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.educationMajorService.update(this.educationMajorId, this.educationMajorForm.value).subscribe(
      (educationMajor) => {
        this.closeModal(educationMajor);
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
