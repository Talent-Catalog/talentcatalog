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
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {CandidateAttachmentService} from "../../../../../services/candidate-attachment.service";
import {
  CandidateAttachment,
  CandidateAttachmentRequest
} from "../../../../../model/candidate-attachment";
import {forkJoin, Observable} from "rxjs";
import {UploadType} from "../../../../../model/task";

@Component({
  selector: 'app-create-candidate-attachment',
  templateUrl: './create-candidate-attachment.component.html',
  styleUrls: ['./create-candidate-attachment.component.scss']
})
export class CreateCandidateAttachmentComponent implements OnInit {

  error: any;
  uploading: boolean;

  // Set in the parent component, by referencing the component instance
  candidateId: number;
  type: string;
  attachments: CandidateAttachment[];

  form: UntypedFormGroup;

  constructor(private modal: NgbActiveModal,
              private candidateAttachmentService: CandidateAttachmentService,
              private fb: UntypedFormBuilder) { }

  ngOnInit() {

    this.attachments = [];

    this.form = this.fb.group({
      candidateId: [this.candidateId],
      type: [this.type],
      location: [''],
      name: [''],
      cv: [false]
    });
  }

  cancel() {
    this.modal.close();
  }

  close() {
    this.modal.close();
  }

  // For link attachment
  save() {
    const request: CandidateAttachmentRequest = new CandidateAttachmentRequest();
    request.candidateId = this.candidateId;
    request.type = this.form.value.type;
    request.name = this.form.value.name;
    request.location = this.form.value.location;
    request.cv = false;
    request.uploadType = UploadType.other;
    this.candidateAttachmentService.createAttachment(request).subscribe(
      (response) => this.modal.close(),
      (error) => this.error = error
    );
  }

  startServerUpload(files: File[]) {
    this.error = null;
    this.uploading = true;
    this.attachments = [];

    const cv: boolean = this.form.value.cv;

    const uploads: Observable<CandidateAttachment>[] = [];
    for (const file of files) {
      const formData: FormData = new FormData();
      formData.append('file', file);

      uploads.push(this.candidateAttachmentService
        .uploadAttachment(this.candidateId, cv, formData));
    }

    forkJoin(...uploads).subscribe(
      (results: CandidateAttachment[]) => {
        this.attachments.push(...results);
        this.uploading = false;
      },
      error => {
        this.error = error;
        this.uploading = false;
      }
    );

  }

  onError(error: string) {
    this.error = error;
  }
}
