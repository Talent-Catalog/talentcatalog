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

import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
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
export class RegistrationCandidateOccupationComponent implements OnInit, OnDestroy {

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
  showForm;
  subscription;
  invalidOccupation: CandidateOccupation;
  candidateOccupation: CandidateOccupation;
  candidateJobExperiences: CandidateJobExperience[];

  constructor(private fb: UntypedFormBuilder,
              private router: Router,
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

  addOccupation() {
    if (this.form.valid) {
      this.candidateOccupations.push(this.form.value);
      this.setUpForm();
    }
    this.showForm = true;

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
          this.candidateOccupations.splice(index, 1);
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
          this.candidateOccupations.splice(index, 1);
        }
      })
      .catch(() => { /* Isn't possible */ });
  }

  save(dir: string) {
    if (this.form.valid) {
      this.addOccupation();
    }
    this.invalidOccupation = this.candidateOccupations.find(occ => occ.yearsExperience < 0 || occ.yearsExperience == null);
    const request = {
      updates: this.candidateOccupations
    };
    if (!this.invalidOccupation) {
      this.candidateOccupationService.updateCandidateOccupations(request).subscribe(
        (response) => {
          if (dir === 'next') {
            this.onSave.emit();
            this.registrationService.next();
          } else {
            this.registrationService.back();
          }
        },
        (error) => {
          this.error = error;
        });
    } else {
      this.error = "You need to put in a years experience value (from 0 upwards).";
    }
  }

  cancel() {
    this.onSave.emit();
  }

  back() {
    this.save('back');
  }

  next() {
    this.save('next');
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
      const existingIds = this.candidateOccupations.map(candidateOcc => candidateOcc.occupationId
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
