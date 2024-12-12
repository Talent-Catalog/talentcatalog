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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Candidate, UpdateCandidateShareableDocsRequest} from "../../../../model/candidate";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {CandidateAttachment} from "../../../../model/candidate-attachment";
import {CandidateService} from "../../../../services/candidate.service";
import {isSavedList} from "../../../../model/saved-list";
import {CandidateSource} from "../../../../model/base";

@Component({
  selector: 'app-shareable-docs',
  templateUrl: './shareable-docs.component.html',
  styleUrls: ['./shareable-docs.component.scss']
})
export class ShareableDocsComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Output() candidateChange = new EventEmitter<Candidate>();

  @Input() candidateSource: CandidateSource;

  @Output() updatedShareableCV = new EventEmitter<CandidateAttachment>();

  cvs: CandidateAttachment[];
  other: CandidateAttachment[];

  error: boolean;
  loading: boolean;
  saving: boolean;

  savedList: boolean;

  form: UntypedFormGroup;

  constructor(private fb: UntypedFormBuilder,
              private candidateService: CandidateService) {}

  ngOnInit() {

    // Initialise the form
    if (this.isList) {
      this.form = this.fb.group({
        shareableCvAttachmentId: [this.candidate?.listShareableCv?.id],
        shareableDocAttachmentId: [this.candidate?.listShareableDoc?.id],
      });
    } else {
      this.form = this.fb.group({
        shareableCvAttachmentId: [this.candidate?.shareableCv?.id],
        shareableDocAttachmentId: [this.candidate?.shareableDoc?.id],
      });
    }

    this.form.valueChanges.subscribe((formValue) => {
      this.doSave(formValue);
    })
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.loadDropdowns();

    //Replace form value with the new candidates shareable docs when changing from one candidate to the next in a list.
    if (this.form) {
      if (this.isList) {
        this.form.controls['shareableCvAttachmentId'].patchValue(this.candidate?.listShareableCv?.id, {emitEvent: false});
        this.form.controls['shareableDocAttachmentId'].patchValue(this.candidate?.listShareableDoc?.id, {emitEvent: false});
      } else {
        this.form.controls['shareableCvAttachmentId'].patchValue(this.candidate?.shareableCv?.id, {emitEvent: false});
        this.form.controls['shareableDocAttachmentId'].patchValue(this.candidate?.shareableDoc?.id, {emitEvent: false});
      }
    }
  }

  loadDropdowns() {
    //Need to separate cvs & other for the ng select form dropdowns.
    this.cvs = this.filterByCv(true);
    this.other = this.filterByCv(false);
  }

  doSave(formValue: any) {
    this.saving = true;
    const request: UpdateCandidateShareableDocsRequest = {
      shareableCvAttachmentId: formValue.shareableCvAttachmentId,
      shareableDocAttachmentId: formValue.shareableDocAttachmentId
    }
    if (this.isList) {
      request.savedListId = this.candidateSource.id;
    }
    this.candidateService.updateShareableDocs(this.candidate.id, request).subscribe(
      (candidate) => {
        // As null values aren't returned in the DTO we need to capture these and add them to the candidate object so
        // the null value can replace the old values in the existing candidate object.
        if (formValue.shareableCvAttachmentId == null) {
          this.isList ? candidate.listShareableCv = null : candidate.shareableCv = null;
        }
        if (formValue.shareableDocAttachmentId == null) {
          this.isList ? candidate.listShareableDoc = null : candidate.shareableDoc = null;
        }
        this.candidateService.updateCandidate(candidate);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    )
  }

  get shareableCvId() {
    return this.form.value?.shareableCvAttachmentId;
  }

  get shareableDocId() {
    return this.form.value?.shareableDocAttachmentId;
  }

  get isList() {
    return isSavedList(this.candidateSource);
  }

  filterByCv(isCV: boolean) {
    return this.candidate.candidateAttachments?.filter(a => a.cv === isCV);
  }
}
