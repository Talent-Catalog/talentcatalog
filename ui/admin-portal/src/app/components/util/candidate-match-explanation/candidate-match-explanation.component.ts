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
 * opportunityDescription are optional and independently affect behaviour.
 * <p/>
 * **This component never performs a GET or POST merely because it was instantiated or its
 * inputs changed.** All network activity is deferred until the user explicitly clicks
 * "Show match explanation" via {@link #show}. This matters because a search can render many
 * candidate cards at once - we must not retrieve/generate an explanation for every one of them
 * just because they were rendered.
 * <p/>
 * Once opened, behaviour depends on which of jobId/opportunityDescription are present:
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
 *   description to generate from, and a "no explanation generated" message is shown instead.
 *   Regenerate is NOT available. This matters for candidates viewed from a job-associated saved
 *   list: opening the explanation can show a previously-generated one, but can never itself
 *   cause an LLM call.
 * <p/>
 * - **neither**: nothing to retrieve and nothing to generate from - no explanation UI at all
 *   (not even the "Show" action).
 * <p/>
 * Closing the explanation (see {@link #close}) never discards what was loaded - reopening it
 * (for the same candidate/job context) simply redisplays it without a new GET/POST. If
 * candidateId/jobId change, any previously loaded explanation belonged to the old context and is
 * discarded, returning to the closed state; the user must explicitly open it again for the new
 * context. Changing only opportunityDescription never automatically triggers retrieval or
 * generation - the new value is simply used the next time generation is (explicitly) triggered
 * via {@link #regenerate}, which itself requires a usable (non-blank) description.
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

  /** Whether the user has opened (expanded) the explanation UI. */
  opened = false;

  /**
   * Whether a retrieval/generation attempt has already completed for the current
   * candidateId/jobId context - whether it found/generated an explanation, found nothing to
   * generate from, or failed. Used so that closing and reopening (without the context changing)
   * simply redisplays the result rather than making a new GET/POST.
   */
  loaded = false;

  explanation: CandidateMatchExplanation | null = null;
  error: string | null = null;

  /** Checking for a persisted explanation. */
  checking = false;
  /** Automatically generating because none was persisted yet. */
  generating = false;
  /** Explicitly regenerating an already-displayed explanation. */
  regenerating = false;

  /** Triggers a retrieval/generation for a key, or (when null) cancels one already in flight. */
  private trigger$ = new Subject<CandidateJobKey | null>();
  /** Emits whenever candidateId/jobId change, to cancel any still in-flight explicit regenerate(). */
  private cancelRegenerate$ = new Subject<void>();
  private destroy$ = new Subject<void>();

  constructor(private candidateService: CandidateService) {
    this.trigger$.pipe(
      switchMap(key => key ? this.retrieveOrGenerate(key) : of(null)),
      takeUntil(this.destroy$)
    ).subscribe(outcome => {
      if (outcome) {
        this.applyOutcome(outcome);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['candidateId'] || changes['jobId']) {
      // A new candidate/job context - any previously loaded explanation belongs to the old
      // context and must not be shown here. Return to the closed state; the user must
      // explicitly open the explanation again for this new context - no automatic GET/POST.
      this.opened = false;
      this.loaded = false;
      this.explanation = null;
      this.error = null;
      this.checking = false;
      this.generating = false;
      this.regenerating = false;
      this.cancelRegenerate$.next();
      this.trigger$.next(null); // cancel any retrieval still in flight for the old context
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.cancelRegenerate$.complete();
  }

  /** Whether there is any useful context at all - if not, no UI (not even "Show") is offered. */
  hasContext(): boolean {
    return this.jobId != null || this.hasUsableDescription();
  }

  /**
   * Whether a usable (present, non-blank) opportunityDescription is currently available. Drives
   * whether Regenerate is offered, matching the backend's own @NotBlank requirement - a blank
   * description is treated the same as an absent one throughout this component.
   */
  hasUsableDescription(): boolean {
    return this.currentDescription() !== undefined;
  }

  /**
   * Opens the explanation UI. Only retrieves/generates if there isn't already a result (or a
   * request already in flight) for the current candidateId/jobId context.
   */
  show(): void {
    this.opened = true;
    if (this.loaded || this.checking || this.generating) {
      return;
    }
    this.checking = true;
    this.error = null;
    this.trigger$.next({candidateId: this.candidateId, jobId: this.jobId});
  }

  /** Collapses the explanation UI. Nothing already loaded is discarded. */
  close(): void {
    this.opened = false;
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
        this.loaded = true;
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
   *   available (otherwise merely opening this candidate's explanation must never cause an LLM
   *   call);
   * - jobId absent: nothing could have been persisted, so skip retrieval - generate directly if
   *   a description is available, otherwise there is nothing to show at all.
   * The retrieval and any resulting generation are part of this single inner observable, so a
   * subsequent candidateId/jobId change (which pushes `null` through the same trigger, cancelling
   * via switchMap) cancels whichever of the two is still in flight.
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
            // Nothing persisted, and no description to generate from - stop here. Opening this
            // candidate's explanation (e.g. from a saved list) must never itself cause an LLM call.
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
    this.loaded = true;
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
