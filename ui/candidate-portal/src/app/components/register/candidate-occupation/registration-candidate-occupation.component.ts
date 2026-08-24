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

import {AfterViewChecked, Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Subscription} from 'rxjs';
import {CandidateService} from '../../../services/candidate.service';
import {CandidateOccupationService} from '../../../services/candidate-occupation.service';
import {CandidateOccupation} from '../../../model/candidate-occupation';
import {Occupation} from '../../../model/occupation';
import {OccupationService} from '../../../services/occupation.service';
import {RegistrationService} from '../../../services/registration.service';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {DeleteOccupationComponent} from './delete/delete-occupation.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateJobExperience} from '../../../model/candidate-job-experience';

@Component({
  selector: 'app-registration-candidate-occupation',
  templateUrl: './registration-candidate-occupation.component.html',
  styleUrls: ['./registration-candidate-occupation.component.scss']
})
export class RegistrationCandidateOccupationComponent implements OnInit, OnDestroy, AfterViewChecked {

  /* todo: Look at the code for certifications/education for a potentially better way to structure this component.
        particulary around the editing and saving. */

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  error: any;
  _loading = {
    candidate: true,
    occupations: true
  };
  saving: boolean;
  form: UntypedFormGroup;
  candidateOccupations: CandidateOccupation[];
  occupations: Occupation[];
  showForm: boolean;
  subscription: Subscription;
  invalidOccupation: CandidateOccupation;
  candidateJobExperiences: CandidateJobExperience[];
  // Index within candidateOccupations currently open in the form, or null when adding a new one
  editingIndex: number | null = null;
  // Set when a new occupation is saved while no principal is selected yet, driving the
  // "Occupation added" confirmation. It is not dismissible and deliberately stays put
  // through further adds/edits - only selecting a principal occupation clears it.
  justAddedOccupation = false;
  // Set when the candidate removes the occupation that was their principal one,
  // driving a warning that they need to choose a new principal occupation. Same
  // rule as above: not dismissible, only clears once a principal is selected.
  principalOccupationRemoved = false;

  @ViewChild('selectPrincipalHeading') selectPrincipalHeadingRef?: ElementRef<HTMLElement>;
  // Set only when the candidate saves their first occupation during this session (not on
  // initial load), so we scroll/focus exactly once as a reaction to that action rather
  // than on every render or every time a returning candidate's data happens to load.
  private pendingSelectModeFocus = false;

  constructor(private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private occupationService: OccupationService,
              private candidateOccupationService: CandidateOccupationService,
              public registrationService: RegistrationService,
              public translateService: TranslateService,
              private modalService: NgbModal) {
  }

  ngOnInit() {
    this.candidateOccupations = [];
    this.saving = false;
    this.showForm = true;
    this.setUpForm();

    this.loadDropDownData();
    // listen for change of language and save
    this.subscription = this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
      this.loadDropDownData();
    });

    this.candidateService.getCandidateCandidateOccupations().subscribe(
      (candidate) => {
        this.candidateOccupations = candidate.candidateOccupations.map(occ => {
          return {
            id: occ.id,
            occupation: occ.occupation,
            occupationId: occ.occupation?.id,
            yearsExperience: occ.yearsExperience,
            migrationOccupation: occ.migrationOccupation,
            principal: occ.id === candidate.principalOccupation?.id,
          };
        });
        this._loading.candidate = false;
        this.showForm = this.candidateOccupations.length === 0;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      }
    );
  }

  ngAfterViewChecked() {
    if (this.pendingSelectModeFocus && this.selectPrincipalHeadingRef) {
      this.pendingSelectModeFocus = false;
      const heading = this.selectPrincipalHeadingRef.nativeElement;
      const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      heading.scrollIntoView({behavior: prefersReducedMotion ? 'auto' : 'smooth', block: 'start'});
      heading.focus();
    }
  }

  loadDropDownData() {
    this._loading.occupations = true;

    this.occupationService.listOccupations().subscribe(
      (response) => {
        this.occupations = response;
        this._loading.occupations = false;
      },
      (error) => {
        this.error = error;
        this._loading.occupations = false;
      }
    );
  }

  setUpForm() {
    this.form = this.fb.group({
      id: [null],
      occupationId: [null, Validators.required],
      yearsExperience: [null, [Validators.required, Validators.min(0)]],
    });
  }

  openAddForm() {
    this.editingIndex = null;
    this.setUpForm();
    this.showForm = true;
  }

  editOccupation(index: number) {
    const occ = this.candidateOccupations[index];
    this.editingIndex = index;
    this.form.setValue({
      id: occ.id ?? null,
      occupationId: occ.occupationId,
      yearsExperience: occ.yearsExperience
    });
    this.showForm = true;
  }

  saveDraft() {
    if (!this.form.valid) {
      return;
    }
    if (this.editingIndex != null) {
      this.candidateOccupations[this.editingIndex] = {
        ...this.candidateOccupations[this.editingIndex],
        ...this.form.value
      };
    } else {
      const hadPrincipalAlready = this.hasPrincipal;
      // Entering "select principal" mode for the first time this session: scroll
      // the new heading into view and move focus to it once the view updates.
      if (this.candidateOccupations.length === 0) {
        this.pendingSelectModeFocus = true;
      }
      this.candidateOccupations.push({...this.form.value, principal: false});
      this.justAddedOccupation = !hadPrincipalAlready;
    }
    this.discardDraft();
  }

  discardDraft() {
    this.editingIndex = null;
    this.setUpForm();
    this.showForm = false;
  }

  /** Commits a valid in-progress occupation draft before saving the step. */
  private commitPendingDraft() {
    if (this.showForm && this.form.valid) {
      this.saveDraft();
    }
  }

  getOccupationName(occupationId: number): string {
    return this.occupations?.find(occ => occ.id === occupationId)?.name;
  }

  /**
   * Without a stable trackBy, Angular treats every row as new on each change
   * detection cycle and destroys/recreates the tc-radio (and its NgModel)
   * inside, which triggers more change detection and can pin the browser in a
   * render loop. Prefer the persisted entity id; newly added rows don't have
   * one yet, so fall back to occupationId (unique per candidate occupation).
   */
  trackByOccupationId(_index: number, occupation: CandidateOccupation): number {
    return occupation.id ?? occupation.occupationId;
  }

  deleteOccupation(index: number, occupationId: number) {
    this.candidateService.getCandidateJobExperiences().subscribe(
      results => {
        // check if the occupation has job experiences associated
        this.candidateJobExperiences = results.candidateJobExperiences.filter(experience =>
          experience.candidateOccupation.occupation.id === occupationId);
        // if associated job experience, display modal to confirm deletion
        if (this.candidateJobExperiences.length > 0) {
          this.deleteModal(index);
        } else {
          this.removeOccupation(index);
        }
    });
  }

  deleteModal(index: number) {
    const deleteOccupationModal = this.modalService.open(DeleteOccupationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteOccupationModal.result
      .then((result) => {
        // remove occupation from occupations if confirmed modal
        if (result === true) {
          this.removeOccupation(index);
        }
      })
      .catch(() => { /* Isn't possible */ });
  }

  private removeOccupation(index: number) {
    const wasPrincipal = this.candidateOccupations[index]?.principal;
    this.candidateOccupations.splice(index, 1);

    if (wasPrincipal) {
      this.principalOccupationRemoved = true;
      this.justAddedOccupation = false;
    }

    if (this.editingIndex === index) {
      this.discardDraft();
    } else if (this.editingIndex != null && this.editingIndex > index) {
      this.editingIndex--;
    }
  }

  selectPrincipal(selected: CandidateOccupation) {
    this.candidateOccupations.forEach(occ => occ.principal = occ === selected);
    this.justAddedOccupation = false;
    this.principalOccupationRemoved = false;
  }

  get hasPrincipal(): boolean {
    return this.candidateOccupations?.some(occ => occ.principal);
  }

  // An open draft (adding or editing) that's currently invalid would otherwise be
  // silently dropped if the candidate proceeds to the next step without noticing.
  get nextDisabled(): boolean {
    return !this.hasPrincipal || (this.showForm && this.form.invalid);
  }

  save() {
    this.commitPendingDraft();
    this.invalidOccupation = this.candidateOccupations.find(occ => occ.yearsExperience < 0 || occ.yearsExperience == null);
    const request = {
      updates: this.candidateOccupations
    };
    if (!this.invalidOccupation) {
      this.saving = true;
      this.candidateOccupationService.updateCandidateOccupations(request).subscribe(
        () => {
          this.saving = false;
          this.onSave.emit();
          this.registrationService.next();
        },
        (error) => {
          this.saving = false;
          this.error = error;
        });
    } else {
      this.error = "You need to put in a years experience value (from 0 upwards).";
    }
  }

  cancel() {
    this.onSave.emit();
  }

  // Going back never requires (or persists) a principal selection - only completing
  // the step forward does, per hasPrincipal/nextDisabled. This intentionally does not
  // save in-progress occupation changes; they are only persisted via next().
  back() {
    this.registrationService.back();
  }

  next() {
    this.save();
  }

  get loading() {
    return this._loading.candidate || this._loading.occupations;
  }

  get filteredOccupations(): Occupation[] {
    if (!this.occupations) {
      return [];
    } else if (!this.candidateOccupations || !this.occupations.length) {
      return this.occupations;
    } else {
      const existingIds = this.candidateOccupations
        .filter((candidateOcc, index) => index !== this.editingIndex)
        .map(candidateOcc => candidateOcc.occupationId
          ? candidateOcc.occupationId.toString()
          : candidateOcc.occupation.id.toString()
        );
      // Remove the Unknown occupation from the occupations (only show if an existing id)
      existingIds.push('0');
      return this.occupations.filter(occ => !existingIds.includes(occ.id.toString()));
    }
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
