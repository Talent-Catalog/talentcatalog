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
import {UntypedFormBuilder} from '@angular/forms';
import {Candidate} from '../../../model/candidate';
import {Observable} from 'rxjs';
import {CandidateSource, UpdateCandidateContextNoteRequest} from '../../../model/base';
import {CandidateSourceService} from '../../../services/candidate-source.service';
import {getCandidateSourceType} from "../../../model/saved-search";
import {AutoSaveComponentBase} from "../autosave/AutoSaveComponentBase";

@Component({
  selector: 'app-candidate-context-note',
  templateUrl: './candidate-context-note.component.html',
  styleUrls: ['./candidate-context-note.component.scss']
})
export class CandidateContextNoteComponent extends AutoSaveComponentBase
  implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() candidateSource: CandidateSource;
  @Input() editable: boolean;

  constructor(private fb: UntypedFormBuilder,
              private candidateSourceService: CandidateSourceService) {
    super(null);
  }

  ngOnInit() {
    this.form = this.fb.group({
      contextNote: [this.candidate.contextNote],
    });
  }

  doSave(formValue: any): Observable<void> {
    const request: UpdateCandidateContextNoteRequest = {
      candidateId: this.candidate.id,
      contextNote: this.contextNote
    }
    return this.candidateSourceService.updateContextNote(this.candidateSource, request);
  }

  onSuccessfulSave() {
    this.candidate.contextNote = this.contextNote;
  }

  get contextNote(): string {
    return this.form.value?.contextNote;
  }

  get title(): string {
    return "Context notes for " + this.candidate.user.firstName + " in " + this.candidateSource.name +
      " " + getCandidateSourceType(this.candidateSource);
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Replace the form value with the new candidates context notes when
    //changing from one candidate to the next or when selection has changed.
    if (this.form) {
      this.form.controls['contextNote'].patchValue(this.candidate.contextNote, {emitEvent: false});
    }
  }


}
