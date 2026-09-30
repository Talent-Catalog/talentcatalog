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
 * a specific Talent Catalog job and/or an opportunity description. Both jobId and
 * opportunityDescription are optional and independently affect behaviour:
 * <p/>
 * - **jobId + opportunityDescription**: retrieves any already-persisted explanation for that
 *   (candidate, job) pair; if none exists yet (404), generates one using the supplied
 *   description - the backend persists it because jobId is present. Regenerate is available.
 * <p/>
 * - **opportunityDescription only** (no jobId): nothing could have been persisted without a
 *   job, so retrieval is skipped entirely and an explanation is generated directly from the
 *   description (transient - not persisted). Regenerate is available.
 * <p/>
 * - **jobId only** (no opportunityDescription): retrieves any already-persisted explanation for
 *   that (candidate, job) pair; if none exists (404), does nothing further - there is no
 *   description to generate from. Regenerate is NOT available. This matters for candidates
 *   viewed from a job-associated saved list: merely viewing the candidate must never cause an
 *   LLM call, but a previously-generated explanation (generated elsewhere) can still be shown.
 * <p/>
 * - **neither**: nothing to retrieve and nothing to generate from - no explanation UI at all.
 * <p/>
 * The user can also explicitly regenerate an already-displayed explanation, but only when a
 * usable (non-blank) opportunityDescription is currently available.
 * <p/>
 * Changing only opportunityDescription never automatically triggers retrieval or generation -
 * the new value is simply used the next time generation is (explicitly or, per the modes above,
 * automatically) triggered by a candidateId/jobId change. This is intentional: a changed
 * description may make a persisted/displayed explanation stale, but regeneration remains under
 * explicit user control.
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
   * Opportunity description to send to the explanation service when (re)generating. This is the
   * actual text used - it is never derived from the job by this component. Optional: without a
   * usable (non-blank) description, no generation (automatic or explicit) can occur - see the
   * class doc for the resulting behaviour modes.
   */
  @Input() opportunityDescription?: string;

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

  /**
   * Whether a usable (present, non-blank) opportunityDescription is currently available. Drives
   * whether Regenerate is offered, matching the backend's own @NotBlank requirement - a blank
   * description is treated the same as an absent one throughout this component.
   */
  hasUsableDescription(): boolean {
    return this.currentDescription() !== undefined;
  }

  regenerate(): void {
    const opportunityDescription = this.currentDescription();
    if (this.generating || this.regenerating || opportunityDescription === undefined) {
      // Either a generation request is already in flight, or there is nothing usable to send -
      // regenerate() must never be able to make a request with a blank/missing description.
      return;
    }
    this.regenerating = true;
    this.error = null;
    this.candidateService.generateMatchExplanation(this.candidateId, {
      jobId: this.jobId,
      opportunityDescription
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
   * Retrieves/generates according to which of the four jobId/opportunityDescription modes
   * (see class doc) the given key and the currently available description put us in:
   * - jobId present: always attempt retrieval first; on 404, generate only if a description is
   *   available (otherwise merely viewing this candidate must never cause an LLM call);
   * - jobId absent: nothing could have been persisted, so skip retrieval - generate directly if
   *   a description is available, otherwise there is nothing to show at all.
   * The retrieval and any resulting generation are part of this single inner observable, so a
   * subsequent candidateId/jobId change (which resubscribes via switchMap) cancels whichever of
   * the two is still in flight.
   */
  private retrieveOrGenerate(key: CandidateJobKey): Observable<ExplanationOutcome> {
    if (key.jobId == null) {
      if (!this.hasUsableDescription()) {
        // Neither jobId nor a usable description - nothing to retrieve or generate from.
        this.checking = false;
        this.generating = false;
        return of({key});
      }
      this.checking = false;
      this.generating = true;
      return this.generateOutcome(key);
    }

    return this.candidateService.getMatchExplanation(key.candidateId, key.jobId).pipe(
      map((explanation): ExplanationOutcome => ({key, explanation})),
      catchError((err: HttpErrorResponse) => {
        if (err.status === 404) {
          if (!this.hasUsableDescription()) {
            // Nothing persisted, and no description to generate from - stop here. Viewing this
            // candidate (e.g. from a saved list) must never itself cause an LLM call.
            this.checking = false;
            this.generating = false;
            return of({key});
          }
          this.checking = false;
          this.generating = true;
          return this.generateOutcome(key);
        }
        return of({key, error: this.extractErrorMessage(err)} as ExplanationOutcome);
      })
    );
  }

  private generateOutcome(key: CandidateJobKey): Observable<ExplanationOutcome> {
    const opportunityDescription = this.currentDescription();
    if (opportunityDescription === undefined) {
      // Defensive: callers already check hasUsableDescription(), but never send a blank
      // description regardless.
      return of({key});
    }
    return this.candidateService.generateMatchExplanation(key.candidateId, {
      jobId: key.jobId,
      opportunityDescription
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
      this.explanation = outcome.explanation ?? null;
    }
  }

  private isCurrent(candidateId: number, jobId: number | undefined): boolean {
    return candidateId === this.candidateId && jobId === this.jobId;
  }

  /** Returns the current opportunityDescription if non-blank, otherwise undefined. */
  private currentDescription(): string | undefined {
    const value = this.opportunityDescription;
    return value != null && value.trim().length > 0 ? value : undefined;
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
