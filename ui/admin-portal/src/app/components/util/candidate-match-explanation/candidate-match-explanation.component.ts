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

import {Component, DestroyRef, Input, OnChanges, OnDestroy, SimpleChanges} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {HttpErrorResponse} from '@angular/common/http';
import {Observable, of, Subject} from 'rxjs';
import {NgbPopover} from '@ng-bootstrap/ng-bootstrap';
import {catchError, map, switchMap, takeUntil} from 'rxjs/operators';
import {CandidateService} from '../../../services/candidate.service';
import {hasTextContent} from '../../../util/string';
import {Candidate} from '../../../model/candidate';
import {CandidateJobExperience} from '../../../model/candidate-job-experience';
import {
  CandidateMatchExplanation,
  ExperienceMatchExplanation
} from '../../../model/candidate-match-explanation';

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

  /**
   * The candidate's already-loaded data, if available. Used only to look up (by ID) the current
   * job experiences referenced by an explanation so they can be shown on hover - this component
   * never fetches the candidate itself. Changing it does not affect the explanation lifecycle.
   */
  @Input() candidate?: Candidate;

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
  private candidateJobKey$ = new Subject<CandidateJobKey | null>();
  /** Emits whenever candidateId/jobId change, to cancel any still in-flight explicit regenerate(). */
  private cancelRegenerate$ = new Subject<void>();

  /** The candidate's current job experiences, indexed by ID - rebuilt whenever candidate changes. */
  private experiencesById = new Map<number, CandidateJobExperience>();

  /**
   * How long to wait, after the pointer leaves the heading or popover (or the heading loses
   * focus), before deciding whether to close - just long enough for the pointer to cross the
   * small gap between the heading and the popover. Not a general close delay: the popover closes
   * as soon as this elapses unless the pointer has entered the heading or popover again.
   */
  static readonly EXPERIENCE_POPOVER_BRIDGE_MS = 100;

  /** The experience popover currently open (at most one at a time), and what is keeping it open. */
  private activeExperiencePopover: NgbPopover | null = null;
  private pointerOverExperienceTrigger = false;
  private pointerOverExperiencePopover = false;
  private experienceTriggerFocused = false;
  private experiencePopoverCloseTimer: ReturnType<typeof setTimeout> | null = null;

  /**
   * @param candidateService Used to retrieve/generate explanations.
   * @param destroyRef Needed for takeUntilDestroyed() outside the injection context (eg in
   * regenerate()) - inside it (eg this constructor), takeUntilDestroyed() finds it itself.
   */
  constructor(private candidateService: CandidateService,
              private destroyRef: DestroyRef) {
    this.candidateJobKey$.pipe(
      switchMap(candidateJobKey => candidateJobKey ? this.retrieveOrGenerate(candidateJobKey) : of(null)),
      takeUntilDestroyed()
    ).subscribe(outcome => {
      if (outcome) {
        this.applyOutcome(outcome);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['candidate']) {
      this.experiencesById = this.indexExperiences(this.candidate);
    }
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
      this.candidateJobKey$.next(null); // cancel any retrieval still in flight for the old context
    }
  }

  ngOnDestroy(): void {
    this.cancelExperiencePopoverCloseCheck();
  }

  /** Whether there is any useful context at all - if not, no UI (not even "Show") is offered. */
  hasContext(): boolean {
    return this.jobId != null || this.hasUsableDescription();
  }

  /**
   * Whether a usable opportunityDescription - one with some text content - is currently
   * available. Drives whether Regenerate is offered. A blank description, or one that is only
   * empty HTML (eg from a cleared rich text editor), is treated the same as an absent one
   * throughout this component (see hasTextContent).
   */
  hasUsableDescription(): boolean {
    return this.currentDescription() !== undefined;
  }

  /**
   * The candidate's current job experience with the given ID, or undefined if it isn't present
   * in the supplied candidate data - e.g. the candidate wasn't supplied, or a persisted
   * explanation refers to an experience that has since been deleted. Matched by ID, never by
   * position.
   */
  findExperience(experienceId: number): CandidateJobExperience | undefined {
    return this.experiencesById.get(experienceId);
  }

  /**
   * Heading for an experience explanation: "<Job Title> (<id>)" when the explanation carries a
   * job title, otherwise (e.g. an explanation persisted before job titles were recorded)
   * "Experience #<id>".
   */
  experienceHeading(item: ExperienceMatchExplanation): string {
    const jobTitle = item.jobTitle?.trim();
    return jobTitle
      ? `${jobTitle} (${item.experienceId})`
      : `Experience #${item.experienceId}`;
  }

  // ---------------------------------------------------------------------------------------------
  // Experience popover: open while the pointer is over the heading or the popover itself, or the
  // heading has keyboard focus; close once none of those is true. Escape and outside clicks also
  // close it (ngbPopover autoClose="outside").
  // ---------------------------------------------------------------------------------------------

  onExperienceTriggerEnter(popover: NgbPopover): void {
    this.activateExperiencePopover(popover);
    this.pointerOverExperienceTrigger = true;
  }

  onExperienceTriggerLeave(): void {
    this.pointerOverExperienceTrigger = false;
    this.scheduleExperiencePopoverCloseCheck();
  }

  /**
   * Keyboard focus opens the popover and keeps it open until blur. Focus caused by a mouse click
   * is ignored (it doesn't match :focus-visible) - otherwise clicking the heading would hold the
   * popover open after the pointer leaves.
   */
  onExperienceTriggerFocus(popover: NgbPopover, event: FocusEvent): void {
    if (!(event.target as Element).matches(':focus-visible')) {
      return;
    }
    this.activateExperiencePopover(popover);
    this.experienceTriggerFocused = true;
  }

  onExperienceTriggerBlur(): void {
    this.experienceTriggerFocused = false;
    this.scheduleExperiencePopoverCloseCheck();
  }

  onExperiencePopoverEnter(): void {
    this.pointerOverExperiencePopover = true;
    this.cancelExperiencePopoverCloseCheck();
  }

  onExperiencePopoverLeave(): void {
    this.pointerOverExperiencePopover = false;
    this.scheduleExperiencePopoverCloseCheck();
  }

  /** Keeps state consistent when ngbPopover itself closes the popover (Escape, outside click). */
  onExperiencePopoverHidden(popover: NgbPopover): void {
    if (popover === this.activeExperiencePopover) {
      this.resetExperiencePopoverState();
    }
  }

  /** Opens the given popover, first closing any other experience popover that is open. */
  private activateExperiencePopover(popover: NgbPopover): void {
    this.cancelExperiencePopoverCloseCheck();
    if (this.activeExperiencePopover !== popover) {
      const previous = this.activeExperiencePopover;
      this.resetExperiencePopoverState();
      previous?.close();
      this.activeExperiencePopover = popover;
    }
    if (!popover.isOpen()) {
      popover.open();
    }
  }

  private scheduleExperiencePopoverCloseCheck(): void {
    this.cancelExperiencePopoverCloseCheck();
    this.experiencePopoverCloseTimer = setTimeout(() => {
      this.experiencePopoverCloseTimer = null;
      if (!this.pointerOverExperienceTrigger && !this.pointerOverExperiencePopover
        && !this.experienceTriggerFocused) {
        const popover = this.activeExperiencePopover;
        this.resetExperiencePopoverState();
        popover?.close();
      }
    }, CandidateMatchExplanationComponent.EXPERIENCE_POPOVER_BRIDGE_MS);
  }

  private cancelExperiencePopoverCloseCheck(): void {
    if (this.experiencePopoverCloseTimer !== null) {
      clearTimeout(this.experiencePopoverCloseTimer);
      this.experiencePopoverCloseTimer = null;
    }
  }

  private resetExperiencePopoverState(): void {
    this.cancelExperiencePopoverCloseCheck();
    this.activeExperiencePopover = null;
    this.pointerOverExperienceTrigger = false;
    this.pointerOverExperiencePopover = false;
    this.experienceTriggerFocused = false;
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
    this.candidateJobKey$.next({candidateId: this.candidateId, jobId: this.jobId});
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
      takeUntilDestroyed(this.destroyRef)
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

  /**
   * Indexes the candidate's job experiences by ID. Experiences may be supplied at the top level
   * of the candidate and/or nested within its occupations (depending on how the candidate was
   * loaded), so both are included.
   */
  private indexExperiences(candidate?: Candidate): Map<number, CandidateJobExperience> {
    const experiences: CandidateJobExperience[] = [...(candidate?.candidateJobExperiences ?? [])];
    for (const occupation of candidate?.candidateOccupations ?? []) {
      experiences.push(...(occupation?.candidateJobExperiences ?? []));
    }

    const byId = new Map<number, CandidateJobExperience>();
    for (const experience of experiences) {
      if (experience?.id != null && !byId.has(experience.id)) {
        byId.set(experience.id, experience);
      }
    }
    return byId;
  }

  private isCurrent(candidateId: number, jobId: number | undefined): boolean {
    return candidateId === this.candidateId && jobId === this.jobId;
  }

  /** Returns the current opportunityDescription if it has text content, otherwise undefined. */
  private currentDescription(): string | undefined {
    const value = this.opportunityDescription;
    return hasTextContent(value) ? value : undefined;
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
