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
import {Occupation} from "../../../../model/occupation";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {OccupationService} from "../../../../services/occupation.service";

@Component({
  selector: 'app-edit-occupation',
  templateUrl: './edit-occupation.component.html',
  styleUrls: ['./edit-occupation.component.scss']
})
export class EditOccupationComponent implements OnInit {

  occupationId: number;
  occupationForm: UntypedFormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private occupationService: OccupationService) {
  }

  ngOnInit() {
    this.loading = true;
    this.occupationService.get(this.occupationId).subscribe(occupation => {
      this.occupationForm = this.fb.group({
        name: [occupation.name, Validators.required],
        status: [occupation.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.occupationService.update(this.occupationId, this.occupationForm.value).subscribe(
      (occupation) => {
        this.closeModal(occupation);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(occupation: Occupation) {
    this.activeModal.close(occupation);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
