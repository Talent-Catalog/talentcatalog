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

import {
  AfterViewInit,
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges
} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Candidate} from '../../../model/candidate';
import {
  catchError,
  debounceTime,
  switchMap,
  takeUntil,
  tap
} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {
  CandidateSource,
  UpdateCandidateContextNoteRequest
} from '../../../model/base';
import {CandidateSourceService} from '../../../services/candidate-source.service';
import {isSavedSearch} from "../../../model/saved-search";

@Component({
  selector: 'app-candidate-context-note',
  templateUrl: './candidate-context-note.component.html',
  styleUrls: ['./candidate-context-note.component.scss']
})
export class CandidateContextNoteComponent implements OnInit, AfterViewInit, OnChanges, OnDestroy {

  @Input() candidate: Candidate;
  @Input() candidateSource: CandidateSource;
  @Input() sourceType: String;
  @Input() savedSearchSelectionChange: boolean;

  form: FormGroup;

  private unsubscribe = new Subject<void>()
  error: string;
  saving: boolean;
  typing: boolean;

  constructor(private fb: FormBuilder,
              private candidateSourceService: CandidateSourceService) { }

  ngOnInit() {
    this.form = this.fb.group({
      contextNote: [this.candidate.contextNote],
    });
  }

  get contextNote(): string {
    return this.form.value?.contextNote;
  }

  get isCandidateSelected(): boolean {
    return this.candidate.selected;
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Replace the form value with the new candidates context notes when
    //changing from one candidate to the next or when selection has changed.
    if (this.form) {
      this.form.controls['contextNote'].patchValue(this.candidate.contextNote);
    }
  }

  ngAfterViewInit() {
    //Set timeout (milliseconds)
    this.autoSaveNote(1000);
  }

  private autoSaveNote(timeout: number) {
    this.form.valueChanges.pipe(

      tap(() => this.typing = true),

      //Only pass values on if there has been inactivity for the given timeout
      debounceTime(timeout),

      //Do a save of the received form values.
      switchMap(formValue => {
          this.typing = false;
          const request: UpdateCandidateContextNoteRequest = {
            candidateId: this.candidate.id,
            contextNote: formValue.contextNote
          }
          this.error = null;
          this.saving = true;
          return this.candidateSourceService.updateContextNote(this.candidateSource, request);
        }
      ),

      //We catch errors, copying them to this.error, but then just continuing
      catchError((error, caught) => {
        this.saving = false;
        this.error = error;
        return caught;
      }),

      //Subscription will continue until the given Observable emits.
      //See ngOnDestroy
      takeUntil(this.unsubscribe)
    ).subscribe(

      //Save has completed successfully
      () => {
        this.saving = false
        this.candidate.contextNote = this.contextNote;
        },
          //Theoretically never get here because we catch errors in the pipe
          (error) => {
        this.saving = false;
        this.error = error;
      }
    )
  }

  ngOnDestroy(): void {
    //Stop subscribing by emitting a value from the Unsubscribe Observable
    //See takeUntil in the above pipe.
    this.unsubscribe.next();
  }

  isSearch() {
    return isSavedSearch(this.candidateSource);
  }
}
