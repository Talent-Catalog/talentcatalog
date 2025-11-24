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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {AutoSaveComponentBase} from "../autosave/AutoSaveComponentBase";
import {Candidate, UpdateCandidateShareableNotesRequest} from "../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {Observable} from "rxjs";
import {CandidateService} from "../../../services/candidate.service";
import {AuthorizationService} from "../../../services/authorization.service";

@Component({
  selector: 'app-candidate-shareable-notes',
  templateUrl: './candidate-shareable-notes.component.html',
  styleUrls: ['./candidate-shareable-notes.component.scss']
})
export class CandidateShareableNotesComponent extends AutoSaveComponentBase
  implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor(private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private authService: AuthorizationService) {
    super(candidateService);
  }

  ngOnInit() {
    this.form = this.fb.group({
      shareableNotes: [this.candidate.shareableNotes],
    });
  }

  doSave(formValue: any): Observable<Candidate> {
    const request: UpdateCandidateShareableNotesRequest = {
      shareableNotes: this.shareableNotes
    }
    return this.candidateService.updateShareableNotes(this.candidate.id, request);
  }

  onSuccessfulSave() {
    this.candidate.shareableNotes = this.shareableNotes;
  }

  get shareableNotes(): string {
    return this.form.value?.shareableNotes;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.form) {
      // Update value
      this.form.controls['shareableNotes'].patchValue(
        this.candidate.shareableNotes,
        { emitEvent: false }
      );
    }
  }

}
