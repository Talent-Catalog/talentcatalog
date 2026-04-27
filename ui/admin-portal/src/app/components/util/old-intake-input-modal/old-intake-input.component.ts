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
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateNoteService,
  CreateCandidateNoteRequest
} from "../../../services/candidate-note.service";
import {CandidateService, IntakeAuditRequest} from "../../../services/candidate.service";
import {Candidate} from "../../../model/candidate";

@Component({
  selector: 'app-old-intake-input',
  templateUrl: './old-intake-input.component.html',
  styleUrls: ['./old-intake-input.component.scss']
})
export class OldIntakeInputComponent implements OnInit {

  fullIntake: boolean;
  candidate: Candidate;

  form: UntypedFormGroup;

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateNoteService: CandidateNoteService,
              private candidateService: CandidateService) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      oldIntakeCompletedDate: [null],
      oldIntakeCompletedBy: [null],
    });
  }

  onSave() {
    this.saving = true;
    let request: IntakeAuditRequest = {
      completedDate: this.form.value.oldIntakeCompletedDate,
      fullIntake: this.fullIntake
    }
    this.candidateService.completeIntake(this.candidate.id, request).subscribe(
      (candidate)=> {
        this.candidate = candidate;
        this.createNote();
      }, (error) => {
        this.error = error;
        this.saving = false;
      }
    )

  }

  createNote() {
    let intakeType: string = this.fullIntake ? 'Full Intake' : 'Mini Intake'
    const noteRequest: CreateCandidateNoteRequest = {
      candidateId: this.candidate.id,
      title: 'Original intake data entered: ' + intakeType + ' conducted on ' + this.form.value.oldIntakeCompletedDate + ' by ' + this.form.value.oldIntakeCompletedBy + '.',
      comment: 'See details below on who/when this data was entered into the TC. Can find original document in candidates Google drive.'
    };
    // If intake audit save successful, create the corresponding candidate note.
    this.candidateNoteService.create(noteRequest).subscribe(
      (candidateNote) => {
        this.saving = false;
        this.activeModal.close(this.candidate);

      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
