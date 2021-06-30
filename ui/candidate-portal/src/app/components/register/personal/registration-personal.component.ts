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

import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {Candidate} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {CountryService} from "../../../services/country.service";
import {Country} from "../../../model/country";
import {RegistrationService} from "../../../services/registration.service";
import {generateYearArray} from "../../../util/year-helper";
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-registration-personal',
  templateUrl: './registration-personal.component.html',
  styleUrls: ['./registration-personal.component.scss']
})
export class RegistrationPersonalComponent implements OnInit, OnDestroy {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  form: FormGroup;
  error: any;
  // Component states
  _loading = {
    candidate: true,
    countries: true
  };
  saving: boolean;

  candidate: Candidate;
  countries: Country[];
  nationalities: Country[];
  years: number[];
  subscription;
  tbbCandValid: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private countryService: CountryService,
              public translateService: TranslateService,
              public registrationService: RegistrationService) { }

  ngOnInit() {
    this.saving = false;
    this.years = generateYearArray();
    this.form = this.fb.group({
      /* PERSONAL */
      firstName: [null, Validators.required],
      lastName: [null, Validators.required],
      gender: [null, Validators.required],
      dob: [null, Validators.required],
      /* LOCATION */
      countryId: ['', Validators.required],
      city: [''],
      yearOfArrival: [''],
      /* NATIONALITY */
      nationality: ['', Validators.required],
      // registeredWithUN: ['', Validators.required],
      // registrationId: ['', Validators.required]
    });
    this.loadDropDownData();
    //listen for change of language and save
    this.subscription = this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
      this.loadDropDownData();
    });

    this.candidateService.getCandidatePersonal().subscribe(
      (response) => {
        this.form.patchValue({
          /* PERSONAL */
          firstName: response.user ? response.user.firstName : null,
          lastName: response.user ? response.user.lastName : null,
          gender: response.gender || null ,
          dob: response.dob || null,
          /* LOCATION */
          countryId: response.country ? response.country.id : null,
          city: response.city,
          yearOfArrival: response.yearOfArrival,
          /* NATIONALITY */
          nationality: response.nationality ? response.nationality.id : null,
          // registeredWithUN: response.registeredWithUN,
          // registrationId: response.registrationId

        });
        this._loading.candidate = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      }
    );
  }

  // private tbbCriteriaValidator(): ValidatorFn {
  //   return (control: AbstractControl): ValidationErrors | null => {
  //     //If there are subtypes associated with the currently selected type,
  //     //as indicated by a non null savedSearchTypeSubInfos, the subtype control
  //     //is required, ie must have a non empty value.
  //     return this.country && (control.value == null) ?
  //       { 'subtypeRequired': true } : null;
  //   };
  // };

  get tbbCriteriaFailed() {
    if (this.country === this.nationality) {
      return true;
    } else {
      return false;
    }
  }

  get nationality() {
    return this.form.value.nationality.toString();
  }

  get country() {
    return this.form.value.countryId.toString();
  }

  loadDropDownData() {
    this._loading.countries = true;

    /* Load the countries */
    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
        this._loading.countries = false;
      },
      (error) => {
        this.error = error;
        this._loading.countries = false;
      }
    );
  }

  save(dir: string) {
    this.saving = true;
    if (this.form.invalid) {
      return;
    }

    // If the candidate hasn't changed anything, skip the update service call
    if (this.form.pristine) {
      if (dir === 'next') {
        this.onSave.emit();
        this.registrationService.next();
      } else {
        this.registrationService.back();
      }
      return;
    }

    // Save changes
    this.candidateService.updateCandidatePersonal(this.form.value).subscribe(
      () => {
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
  }

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
    return l.candidate || l.countries
  }

  cancel() {
    this.onSave.emit();
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

}
