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
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateNoteService} from '../../../../../services/candidate-note.service';
import {CandidateNote} from '../../../../../model/candidate-note';
import {CandidateService} from "../../../../../services/candidate.service";

@Component({
  selector: 'app-create-candidate-note',
  templateUrl: './create-candidate-note.component.html',
  styleUrls: ['./create-candidate-note.component.scss']
})
export class CreateCandidateNoteComponent implements OnInit {

  candidateNote: CandidateNote;

  candidateForm: UntypedFormGroup;

  candidateId: number;
  countries = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateNoteService: CandidateNoteService,
              private candidateService: CandidateService ) {
  }

  ngOnInit() {
    this.loading = true;

    this.candidateForm = this.fb.group({
      candidateId: [this.candidateId],
      title: ['', [Validators.required]],
      comment: ['']
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    this.candidateNoteService.create(this.candidateForm.value).subscribe(
      (candidateNote) => {
        this.candidateService.updateCandidate()
        this.closeModal(candidateNote);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateNote: CandidateNote) {
    this.activeModal.close(candidateNote);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
