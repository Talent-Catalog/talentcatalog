/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {CandidateService} from '../../../../services/candidate.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  Candidate,
  CandidateStatus,
  MaritalStatus,
  UpdateCandidateStatusRequest
} from "../../../../model/candidate";
import {EnumOption, enumOptions} from "../../../../util/enum";

@Component({
  selector: 'app-edit-candidate',
  templateUrl: './edit-candidate-status.component.html',
  styleUrls: ['./edit-candidate-status.component.scss']
})
export class EditCandidateStatusComponent implements OnInit {

  candidateId: number;
  candidateForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  candidateStatusOptions: EnumOption[] = enumOptions(CandidateStatus);

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateService: CandidateService) {
  }

  ngOnInit() {
      this.loading = true;
      this.candidateService.get(this.candidateId).subscribe(candidate => {
        this.candidateForm = this.fb.group({
          status: [candidate.status, Validators.required],
          comment: [null, Validators.required],
          candidateMessage: [candidate.candidateMessage || ''],
        });
        this.loading = false;
      });
  }

  get candidateMessage(): string {
    return this.candidateForm.value?.candidateMessage;
  }

  get comment(): string {
    return this.candidateForm.value?.comment;
  }

  get status(): CandidateStatus {
    return this.candidateForm.value?.status;
  }

  onSave() {
    this.error = null;

    const val = this.candidateForm.value;
    if (this.showCandidateMessage && (!val.candidateMessage || val.candidateMessage.length < 1)) {
      this.error = 'Please enter a message for the candidate about what they need to complete';
      return;
    }

    this.saving = true;
    const request: UpdateCandidateStatusRequest = {
      candidateIds: [this.candidateId],
      candidateMessage: this.candidateMessage,
      comment: this.comment,
      status: this.status
    };
    this.candidateService.updateStatus(request).subscribe(
      (candidate) => {
        this.saving = false;
        this.closeModal();
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal() {
    //todo Need to change this to pass back an UpdateStatusInfo - and save done externally
    this.activeModal.close();
  }

  cancel() {
    this.activeModal.dismiss();
  }

  get showCandidateMessage() {
    if (this.loading || !this.candidateForm) {
      return false;
    }
    return this.candidateForm.controls.status.value === 'incomplete';
  }
}
