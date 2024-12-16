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
import {EducationLevel} from "../../../../model/education-level";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EducationLevelService} from "../../../../services/education-level.service";

@Component({
  selector: 'app-create-education-level',
  templateUrl: './create-education-level.component.html',
  styleUrls: ['./create-education-level.component.scss']
})

export class CreateEducationLevelComponent implements OnInit {

  educationLevelForm: UntypedFormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private educationLevelService: EducationLevelService) {
  }

  ngOnInit() {
    this.educationLevelForm = this.fb.group({
      level: [null, Validators.required],
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.educationLevelService.create(this.educationLevelForm.value).subscribe(
      (educationLevel) => {
        this.closeModal(educationLevel)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(educationLevel: EducationLevel) {
    this.activeModal.close(educationLevel);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
