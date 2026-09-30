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

import {Component, Input, OnChanges, OnDestroy, SimpleChanges} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {Observable, of, Subject} from 'rxjs';
import {catchError, map, switchMap, takeUntil} from 'rxjs/operators';
import {CandidateService} from '../../../services/candidate.service';
import {CandidateMatchExplanation} from '../../../model/candidate-match-explanation';

interface CandidateJobKey {
  candidateId: number;
  jobId?: number;
}

interface ExplanationOutcome {
  key: CandidateJobKey;
  explanation?: CandidateMatchExplanation;
  error?: string;
}

/**
 * Displays the on-demand LLM-generated match explanation for a candidate, optionally scoped to
 * a specific Talent Catalog job.
 * <p/>
 * On first use (and whenever candidateId/jobId change):
 * - if jobId is supplied, retrieves any already-persisted explanation for that (candidate, job)
 *   pair; if none has been generated yet (the retrieval returns 404), one is generated
 *   automatically and persisted;
 * - if jobId is absent, there is nothing that could have been persisted, so an explanation is
 *   generated directly (and not persisted).
 * <p/>
 * The user can also explicitly regenerate an already-displayed explanation.
 * <p/>
 * Changing only opportunityDescription does NOT trigger any automatic retrieval/generation -
 * the new value is simply used the next time generation is (explicitly or automatically)
 * triggered. This is intentional: a changed description may make a persisted explanation stale,
 * but regeneration remains under explicit user control.
 */
@Component({
  selector: 'app-candidate-match-explanation',
  templateUrl: './candidate-match-explanation.component.html',
  styleUrls: ['./candidate-match-explanation.component.scss']
})
export class CandidateMatchExplanationComponent implements OnChanges, OnDestroy {

  /** ID of the candidate being explained. */
  @Input({required: true}) candidateId!: number;

  /** Optional ID of the Talent Catalog job the explanation is persisted/retrieved against. */
  @Input() jobId?: number;

  /**
   * Opportunity description to send to the explanation service when (re)generating.
   * This is the actual text used - it is never derived from the job by this component.
   */
  @Input({required: true}) opportunityDescription!: string;

  explanation: CandidateMatchExplanation | null = null;
  error: string | null = null;

  /** Checking for a persisted explanation. */
  checking = false;
  /** Automatically generating because none was persisted yet. */
  generating = false;
  /** Explicitly regenerating an already-displayed explanation. */
  regenerating = false;

  private candidateJob$ = new Subject<CandidateJobKey>();
  /** Emits whenever candidateId/jobId change, to cancel any still in-flight explicit regenerate(). */
  private cancelRegenerate$ = new Subject<void>();
  private destroy$ = new Subject<void>();

  constructor(private candidateService: CandidateService) {
    this.candidateJob$.pipe(
      switchMap(key => this.retrieveOrGenerate(key)),
      takeUntil(this.destroy$)
    ).subscribe(outcome => this.applyOutcome(outcome));
  }

  ngOnChanges(changes: SimpleChanges): void {
    const candidateOrJobChanged = !!changes['candidateId'] || !!changes['jobId'];
    if (candidateOrJobChanged && this.candidateId != null) {
      this.explanation = null;
      this.error = null;
      this.checking = true;
      this.generating = false;
      // A regenerate() in flight for the *previous* pair must not be able to affect this (new)
      // pair's state when it eventually completes - cancel it outright rather than merely
      // guarding its callbacks, so a stale success or error can have no effect at all.
      this.regenerating = false;
      this.cancelRegenerate$.next();
      this.candidateJob$.next({candidateId: this.candidateId, jobId: this.jobId});
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.cancelRegenerate$.complete();
  }

  regenerate(): void {
    if (this.generating || this.regenerating) {
      // A generation request is already in flight - don't start another.
      return;
    }
    this.regenerating = true;
    this.error = null;
    this.candidateService.generateMatchExplanation(this.candidateId, {
      jobId: this.jobId,
      opportunityDescription: this.opportunityDescription
    }).pipe(
      // Cancelled by ngOnChanges if candidateId/jobId change before this completes, so a stale
      // response (success or error) can never reach these handlers for a pair that is no
      // longer current.
      takeUntil(this.cancelRegenerate$),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (explanation) => {
        this.regenerating = false;
        this.explanation = explanation;
      },
      error: (error) => {
        this.regenerating = false;
        // Keep the previously displayed explanation - just surface the error.
        this.error = error;
      }
    });
  }

  /**
   * Retrieves the persisted explanation for the given key, automatically generating one if
   * none exists (404). If the key has no jobId, there is nothing that could have been
   * persisted, so an explanation is generated directly without attempting a retrieval first.
   * The retrieval and any resulting generation are part of this single inner observable, so a
   * subsequent candidateId/jobId change (which resubscribes via switchMap) cancels whichever of
   * the two is still in flight.
   */
  private retrieveOrGenerate(key: CandidateJobKey): Observable<ExplanationOutcome> {
    if (key.jobId == null) {
      this.checking = false;
      this.generating = true;
      return this.generateOutcome(key);
    }

    return this.candidateService.getMatchExplanation(key.candidateId, key.jobId).pipe(
      map((explanation): ExplanationOutcome => ({key, explanation})),
      catchError((err: HttpErrorResponse) => {
        if (err.status === 404) {
          this.checking = false;
          this.generating = true;
          return this.generateOutcome(key);
        }
        return of({key, error: this.extractErrorMessage(err)} as ExplanationOutcome);
      })
    );
  }

  private generateOutcome(key: CandidateJobKey): Observable<ExplanationOutcome> {
    return this.candidateService.generateMatchExplanation(key.candidateId, {
      jobId: key.jobId,
      opportunityDescription: this.opportunityDescription
    }).pipe(
      map((explanation): ExplanationOutcome => ({key, explanation})),
      catchError((genErr) => of({key, error: genErr} as ExplanationOutcome))
    );
  }

  private applyOutcome(outcome: ExplanationOutcome): void {
    this.checking = false;
    this.generating = false;
    if (!this.isCurrent(outcome.key.candidateId, outcome.key.jobId)) {
      // Superseded by a newer candidateId/jobId pair. switchMap already cancels the stale
      // inner observable, but this guards against applying an already-in-flight result too.
      return;
    }
    if (outcome.error) {
      this.error = outcome.error;
    } else {
      this.explanation = outcome.explanation;
    }
  }

  private isCurrent(candidateId: number, jobId: number | undefined): boolean {
    return candidateId === this.candidateId && jobId === this.jobId;
  }

  private extractErrorMessage(err: HttpErrorResponse): string {
    if (err.error != null && err.error.message) {
      return err.error.message;
    }
    if (err.message != null) {
      return err.message;
    }
    return `${err.status} ${err.statusText}`;
  }
}
