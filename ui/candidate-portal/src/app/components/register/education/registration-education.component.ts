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
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {EducationLevelService} from "../../../services/education-level.service";
import {EducationLevel} from "../../../model/education-level";
import {RegistrationService} from "../../../services/registration.service";
import {CandidateEducation} from "../../../model/candidate-education";
import {CandidateEducationService} from "../../../services/candidate-education.service";
import {EducationMajorService} from "../../../services/education-major.service";
import {EducationMajor} from "../../../model/education-major";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-registration-education',
  templateUrl: './registration-education.component.html',
  styleUrls: ['./registration-education.component.scss']
})
export class RegistrationEducationComponent implements OnInit, OnDestroy {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  error: any;
  saving: boolean;
  _loading = {
    levels: true,
    candidate: true,
    educationMajors: true,
    countries: true
  };

  form: UntypedFormGroup;
  majors: EducationMajor[];
  countries: Country[];
  educationLevels: EducationLevel[];
  candidateEducationItems: CandidateEducation[];
  addingEducation: boolean;
  editingEducation: boolean;
  educationType: string;

  editTarget: CandidateEducation;
  subscription;

  constructor(private fb: UntypedFormBuilder,
              private router: Router,
              private candidateEducationService: CandidateEducationService,
              private candidateService: CandidateService,
              private countryService: CountryService,
              private educationLevelService: EducationLevelService,
              private educationMajorService: EducationMajorService,
              private translateService: TranslateService,
              public registrationService: RegistrationService) {
  }

  ngOnInit() {
    this.saving = false;
    this.editTarget = null;
    this.candidateEducationItems = [];
    this.form = this.fb.group({
      maxEducationLevelId: [null]
    });

    this.form.get('maxEducationLevelId').valueChanges.subscribe(value => {
      if (value) {
        if (this.loading) {
          return;
        }
        const educationLevel: EducationLevel = this.educationLevels.find(e => e.id === value);
        if (educationLevel){
           const education = this.candidateEducationItems.find(e => e.educationType === educationLevel.educationType);
           if (education){
             return;
           }
        }
        this.addingEducation = educationLevel && educationLevel.educationType != null;
        this.educationType = educationLevel ? educationLevel.educationType : null;
      }
    });

    this.loadDropDownData();
    //listen for change of language and save
    this.subscription = this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
      this.loadDropDownData();
    });



    this.candidateService.getCandidateEducation().subscribe(
      (candidate) => {
        this.form.patchValue({
          maxEducationLevelId: candidate.maxEducationLevel?.id > 0 ? candidate.maxEducationLevel.id : null,
        });
        if (candidate.candidateEducations) {
          this.candidateEducationItems = candidate.candidateEducations
        }
        this._loading.candidate = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      }
    );
  }

  loadDropDownData(){
    this._loading.educationMajors = true;
    this._loading.countries = true;
    this._loading.levels = true;

    /* Load data */
    this.educationMajorService.listMajors().subscribe(
      (response) => {
        this.majors = response;
        this._loading.educationMajors = false;
      },
      (error) => {
        this.error = error;
        this._loading.educationMajors = false;
      });

    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
        this._loading.countries = false;
      },
      (error) => {
        this.error = error;
        this._loading.countries = false;
      });

    this.educationLevelService.listEducationLevels().subscribe(
      (response) => {
        this.educationLevels = response;
        this._loading.levels = false;
      },
      (error) => {
        this.error = error;
        this._loading.levels = false;
      }
    );
  }

  save(dir: string) {
    this.saving = true;

    // If nothing input, don't want to send null to database need to send 0.
    if (this.form.value.maxEducationLevelId === null) {
      this.form.get('maxEducationLevelId').patchValue(0);
    }

    this.candidateService.updateCandidateEducationLevel(this.form.value).subscribe(
      (response) => {
        this.saving = false;
        if (dir === 'next') {
          this.onSave.emit();
          this.registrationService.next();
        } else {
          this.registrationService.back();
        }
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  };

  back() {
    if (this.form.invalid || this.form.pristine) {
      // Candidate data shouldn't be updated
      this.registrationService.back();
    } else {
      this.save('back');
    }
  }

  next() {
    this.save('next');
  }

  get loading() {
    const l = this._loading;
    return l.levels || l.candidate || l.educationMajors || l.countries;
  }

  addEducation() {
    this.addingEducation = true;
  }

  handleClose() {
    this.addingEducation = false;
  }

  handleCloseSaved() {
    this.editTarget = null;
    this.editingEducation = false;
  }

  handleCandidateEducationCreated(education: CandidateEducation) {
    let index = -1;
    if (this.candidateEducationItems.length) {
      index = this.candidateEducationItems.findIndex(edu => edu.id === education.id);
    }
    /* Replace the old education item with the updated item */
    if (index >= 0) {
      this.candidateEducationItems.splice(index, 1, education);
    } else {
      this.candidateEducationItems.push(education);
    }
    this.addingEducation = false;
  }

  deleteCandidateEducation(index: number) {
    this.saving = true;
    const education = this.candidateEducationItems[index];
    this.candidateEducationService.deleteCandidateEducation(education.id).subscribe(
      () => {
        this.candidateEducationItems.splice(index, 1);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  cancel() {
    this.onSave.emit();
  }

  editCandidateEducation(education: CandidateEducation) {
    this.editTarget = education;
    this.editingEducation = true;
  }

  handleEducationSaved(education: CandidateEducation, i) {
    this.candidateEducationItems[i] = education;
    this.editTarget = null;
    this.editingEducation = false;
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
