/*
 * Copyright (c) 2025 Talent Catalog.
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
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {EducationLevel} from "../../../../../model/education-level";
import {EducationLevelService} from "../../../../../services/education-level.service";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-edit-max-education-level',
  templateUrl: './edit-max-education-level.component.html',
  styleUrls: ['./edit-max-education-level.component.scss']
})
export class EditMaxEducationLevelComponent implements OnInit {
  educationLevel: EducationLevel;
  educationLevels: EducationLevel[];

  form: UntypedFormGroup;

  error: string;
  loading: boolean;
  saving: boolean;

  constructor(
    private educationLevelService: EducationLevelService,
    private activeModal: NgbActiveModal,
    private fb: UntypedFormBuilder
  ) {
  }

  ngOnInit() {
    this.loading = true;
    this.form = this.fb.group({
      educationLevelId: [this.educationLevel?.id, Validators.required]
    });

    // Load education levels
    this.educationLevelService.listEducationLevels().subscribe(
      (response) => {
        this.educationLevels = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  save() {
    const selectedId = this.form.value.educationLevelId;
    this.activeModal.close(selectedId);
  }
}
