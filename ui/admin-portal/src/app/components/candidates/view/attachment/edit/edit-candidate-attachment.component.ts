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
import {CandidateAttachment} from "../../../../../model/candidate-attachment";
import {UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateAttachmentService,
  UpdateCandidateAttachmentRequest
} from "../../../../../services/candidate-attachment.service";

@Component({
  selector: 'app-edit-candidate-attachment',
  templateUrl: './edit-candidate-attachment.component.html',
  styleUrls: ['./edit-candidate-attachment.component.scss']
})
export class EditCandidateAttachmentComponent implements OnInit {

  loading: boolean;
  error: any;

  // Set in the parent component, by referencing the comoponent instance
  attachment: CandidateAttachment;
  form: UntypedFormGroup;

  constructor(private fb: UntypedFormBuilder,
              private modal: NgbActiveModal,
              private candidateAttachmentService: CandidateAttachmentService) { }

  ngOnInit() {
    this.form = this.fb.group({
      id: [this.attachment.id],
      name: [this.attachment.name, Validators.required],
    });
    if (this.attachment.type === 'link') {
      this.form.addControl('location', new UntypedFormControl(this.attachment.location, [Validators.required]));
    }
  }

  save() {
    this.loading = true;
    const request: UpdateCandidateAttachmentRequest = {
      name: this.form.value.name,
      location: this.form.value?.location
    };
    this.candidateAttachmentService.updateAttachment(this.form.value.id, request).subscribe(
      (response) => {
        this.loading = true;
        this.modal.close(response);
      },
      (error) => {
        this.error = error;
        this.loading = true;
      });
  }

  close() {
    this.modal.close();
  }
}
