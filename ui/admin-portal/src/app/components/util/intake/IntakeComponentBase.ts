/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {AfterViewInit, Input, OnDestroy} from "@angular/core";
import {catchError, debounceTime, switchMap, takeUntil} from "rxjs/operators";
import {Subject} from "rxjs";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Candidate, CandidateIntakeData} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";

export class IntakeComponentBase implements AfterViewInit, OnDestroy {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;

  error: string;
  form: FormGroup;
  saving: boolean;
  show: boolean;

  private unsubscribe = new Subject<void>()

  constructor(
    protected fb: FormBuilder,
    private candidateService: CandidateService,
  ) {
  }

  ngAfterViewInit(): void {
    this.setupAutosave();
  }

  private setupAutosave() {
    this.form.valueChanges.pipe(
      debounceTime(1000),
      switchMap(formValue => {
          this.error = null;
          this.saving = true;
          this.show = !this.show;
          return this.candidateService.updateIntakeData(this.candidate.id, formValue);
        }
      ),
      catchError((error, caught) => {
        this.saving = false;
        this.error = error;
        return caught;
      }),
      takeUntil(this.unsubscribe)
    ).subscribe(
      () => this.saving = false,
      (error) => {
        //Theoretically never get here because we catch errors in the pipe
        this.saving = false;
        this.error = error;
      }
    )
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
  }

}
