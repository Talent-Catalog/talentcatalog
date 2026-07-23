/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateStatus, UpdateCandidateStatusInfo} from "../../../model/candidate";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {EnumOption, enumOptions, toEnumKey} from "../../../util/enum";

@Component({
  selector: 'app-candidate-status-selector',
  templateUrl: './candidate-status-selector.component.html',
  styleUrls: ['./candidate-status-selector.component.scss']
})
export class CandidateStatusSelectorComponent implements OnInit {

  @Input() candidateStatus: CandidateStatus;
  @Output() statusInfoUpdate = new EventEmitter<UpdateCandidateStatusInfo>();

  candidateStatusInfoForm: UntypedFormGroup;

  candidateStatusOptions: EnumOption[];

  constructor(private fb: UntypedFormBuilder) {
  }

  ngOnInit(): void {

    //Filter out the draft option. Users should not be able to set a candidate's status to draft.
    this.candidateStatusOptions =
      enumOptions(CandidateStatus).filter(option => option.stringValue !== CandidateStatus.draft);

    // Sometimes the status may be a key from the DB, other times it may come as a string value
    // (e.g. CandidateStatus.active). So I need to convert to a key for the ng-select to bind in the form.
    this.candidateStatusInfoForm = this.fb.group({
      status: [toEnumKey(CandidateStatus, this.candidateStatus), Validators.required],
      comment: [null, Validators.required],
      candidateMessage: [null],
    });

    this.candidateStatusInfoForm.valueChanges.subscribe(() => this.onUpdate());

    this.onUpdate();
  }

  get candidateMessage(): string {
    return this.candidateStatusInfoForm.value?.candidateMessage;
  }

  get comment(): string {
    return this.candidateStatusInfoForm.value?.comment;
  }

  get status(): CandidateStatus {
    return this.candidateStatusInfoForm.value?.status;
  }

  private onUpdate() {
    this.statusInfoUpdate.emit({
      status: this.status,
      comment: this.comment,
      candidateMessage: this.candidateMessage
    });
  }

}
