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
import {Candidate} from "../../../../model/candidate";

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

   onSave() {
    this.error = null;

    const val = this.candidateForm.value;
    if (this.showCandidateMessage && (!val.candidateMessage || val.candidateMessage.length < 1)) {
      this.error = 'Please enter a message for the candidate about what they need to complete';
      return;
    }

    this.saving = true;
    this.candidateService.updateStatus(this.candidateId, this.candidateForm.value).subscribe(
      (candidate) => {
        this.saving = false;
        this.closeModal(candidate);
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidate: Candidate) {
    this.activeModal.close(candidate);
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
