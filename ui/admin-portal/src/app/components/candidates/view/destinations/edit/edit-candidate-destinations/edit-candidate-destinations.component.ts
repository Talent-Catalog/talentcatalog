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
import {CandidateDestination} from "../../../../../../model/candidate-destination";
import {
  CandidateDestinationService,
  UpdateCandidateDestinationRequest
} from "../../../../../../services/candidate-destination.service";
import {EnumOption, enumOptions} from "../../../../../../util/enum";
import {YesNoUnsureLearn} from "../../../../../../model/candidate";

@Component({
  selector: 'app-edit-candidate-destinations',
  templateUrl: './edit-candidate-destinations.component.html',
  styleUrls: ['./edit-candidate-destinations.component.scss']
})
export class EditCandidateDestinationsComponent implements OnInit {
  candidateDestination: CandidateDestination;
  form: UntypedFormGroup;

  error;
  loading: boolean;
  saving: boolean;

  public destInterestOptions: EnumOption[] = enumOptions(YesNoUnsureLearn);

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateDestinationService: CandidateDestinationService) {
  }

  ngOnInit() {
    this.loading = true;
    this.form = this.fb.group({
      interest: [this.candidateDestination.interest],
      notes: [this.candidateDestination.notes]
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    let request: UpdateCandidateDestinationRequest = {
      interest: this.form.value.interest,
      notes: this.form.value.notes
    }
    this.candidateDestinationService.update(this.candidateDestination.id, request).subscribe(
      (candidateDestination) => {
        this.closeModal(candidateDestination);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateDestination: CandidateDestination) {
    this.activeModal.close(candidateDestination);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
