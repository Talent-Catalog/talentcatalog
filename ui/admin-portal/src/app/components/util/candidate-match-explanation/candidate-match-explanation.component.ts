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
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Observable, of, Subject} from 'rxjs';
import {NgbPopover} from '@ng-bootstrap/ng-bootstrap';
import {catchError, map, switchMap} from 'rxjs/operators';
import {CandidateService} from '../../../services/candidate.service';
import {Candidate} from '../../../model/candidate';
import {CandidateJobExperience} from '../../../model/candidate-job-experience';
import {
  CandidateMatchExplanation,
  ExperienceMatchExplanation
} from '../../../model/candidate-match-explanation';

interface ExplanationRequest {
  candidateId: number;
  matchingId: number;
  regenerate: boolean;
}

interface ExplanationOutcome {
  explanation?: CandidateMatchExplanation;
  error?: string;
}

/**
 * Displays the LLM-generated explanation of how a candidate's job experience relates to the
 * description of a Matching - the matching context of the candidate source being viewed.
 * <p/>
 * **This component never makes a request merely because it was instantiated or its inputs
 * changed.** Nothing is requested until the user explicitly clicks "Show match explanation" (see
 * {@link #show}). This matters because a search can render many candidate cards - we must not
 * request an explanation for each of them just because they were rendered.
 * <p/>
 * When shown, the server returns its cached explanation for the candidate and Matching if that is
 * still current, otherwise it generates a new one from the Matching's current description. The
 * server always obtains the description itself - this component never supplies it.
 * <p/>
 * Regenerate always generates a new explanation, replacing the cached one. Explanations are only
 * automatically refreshed when the Matching's description changes, not when the candidate's job
 * experience changes, so this lets users refresh an explanation themselves.
 * <p/>
 * Closing the explanation (see {@link #close}) never discards what was loaded - reopening it
 * simply redisplays it. But if the candidate, the Matching or the Matching's updatedDate changes,
 * any loaded explanation (or request in flight) belongs to the old context, so it is discarded and
 * the component returns to the closed state.
 * <p/>
 * If requirementsPendingMessage is set - the search's requirements have been edited but not yet
 * saved to the Matching - that message is shown instead, since an explanation would be of the
 * saved requirements, not the edited ones.
 */
@Component({
  selector: 'app-candidate-match-explanation',
  templateUrl: './candidate-match-explanation.component.html',
  styleUrls: ['./candidate-match-explanation.component.scss']
})
export class CandidateMatchExplanationComponent implements OnChanges, OnDestroy {

  /** ID of the candidate being explained. */
  @Input({required: true}) candidateId!: number;

  /**
   * ID of the Matching whose description the candidate is explained against. No explanation can
   * be requested without one.
   */
  @Input() matchingId?: number;

  /**
   * The Matching's updatedDate. Only used to detect that the Matching's description has changed,
   * which makes any displayed explanation out of date.
   */
  @Input() matchingUpdatedDate?: string;

  /**
   * Set when the requirements have been edited but not yet saved to the Matching, explaining what
   * the user needs to do before explanations reflect their changes. Shown instead of the
   * explanation UI.
   */
  @Input() requirementsPendingMessage?: string;

  /**
   * The candidate's already-loaded data, if available. Used only to look up (by ID) the current
   * job experiences referenced by an explanation so they can be shown on hover - this component
   * never fetches the candidate itself. Changing it does not affect the explanation lifecycle.
   */
  @Input() candidate?: Candidate;

  /** Whether the user has opened (expanded) the explanation UI. */
  opened = false;

  explanation: CandidateMatchExplanation | null = null;
  error: string | null = null;

  /** Getting an explanation (cached or newly generated) because none is displayed yet. */
  loading = false;
  /** Explicitly regenerating the displayed explanation. */
  regenerating = false;

  /** Triggers a request, or (when null) cancels one already in flight. */
  private request$ = new Subject<ExplanationRequest | null>();

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
   * @param candidateService Used to get explanations.
   */
  constructor(private candidateService: CandidateService) {
    //switchMap means that a new request, or a cancellation (null), discards the response to any
    //request still in flight - so a response for an old context can never be displayed.
    this.request$.pipe(
      switchMap(request => request ? this.fetch(request) : of(null)),
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
    if (changes['candidateId'] || changes['matchingId'] || changes['matchingUpdatedDate']) {
      // A new candidate, Matching or Matching description - any previously loaded explanation
      // belongs to the old context and must not be shown. Return to the closed state; the user
      // must explicitly open the explanation again - no automatic request.
      this.opened = false;
      this.explanation = null;
      this.error = null;
      this.loading = false;
      this.regenerating = false;
      this.request$.next(null);
    }
  }

  ngOnDestroy(): void {
    this.cancelExperiencePopoverCloseCheck();
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
   * Opens the explanation UI, getting the explanation unless it is already displayed or being
   * requested.
   */
  show(): void {
    this.opened = true;
    if (this.explanation || this.loading || this.regenerating) {
      return;
    }
    this.request(false);
  }

  /** Collapses the explanation UI. Nothing already loaded is discarded. */
  close(): void {
    this.opened = false;
  }

  /** Tries again to get the explanation, after a failure. */
  retry(): void {
    if (!this.loading && !this.regenerating) {
      this.request(false);
    }
  }

  /**
   * Generates a new explanation, replacing the displayed (and cached) one, which remains
   * displayed until the new one arrives.
   */
  regenerate(): void {
    if (!this.loading && !this.regenerating) {
      this.request(true);
    }
  }

  private request(regenerate: boolean): void {
    if (this.matchingId == null || this.requirementsPendingMessage) {
      return;
    }
    this.loading = !regenerate;
    this.regenerating = regenerate;
    this.error = null;
    this.request$.next({candidateId: this.candidateId, matchingId: this.matchingId, regenerate});
  }

  private fetch(request: ExplanationRequest): Observable<ExplanationOutcome> {
    return this.candidateService.getOrGenerateMatchExplanation(request.candidateId, {
      matchingId: request.matchingId,
      regenerate: request.regenerate
    }).pipe(
      map((explanation): ExplanationOutcome => ({explanation})),
      catchError(error => of({error} as ExplanationOutcome))
    );
  }

  private applyOutcome(outcome: ExplanationOutcome): void {
    this.loading = false;
    this.regenerating = false;
    if (outcome.error) {
      // Any previously displayed explanation is kept - just surface the error.
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
}
