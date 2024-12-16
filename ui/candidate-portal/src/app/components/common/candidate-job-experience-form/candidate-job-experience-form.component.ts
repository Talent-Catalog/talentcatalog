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

import {AfterViewInit, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {CandidateJobExperience} from "../../../model/candidate-job-experience";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {CandidateOccupation} from "../../../model/candidate-occupation";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";
import {CandidateJobExperienceService} from "../../../services/candidate-job-experience.service";

@Component({
  selector: 'app-candidate-job-experience-form',
  templateUrl: './candidate-job-experience-form.component.html',
  styleUrls: ['./candidate-job-experience-form.component.scss']
})
export class CandidateJobExperienceFormComponent implements OnInit, AfterViewInit {

  @Input() candidateJobExperience: CandidateJobExperience;
  @Input() candidateOccupation: CandidateOccupation;
  @Input() candidateOccupations: CandidateOccupation[];
  @Input() countries: Country[];

  @Output() formSaved = new EventEmitter<CandidateJobExperience>();
  @Output() formClosed = new EventEmitter<CandidateJobExperience>();

  @ViewChild('top', {static: true}) top: ElementRef;

  loading: boolean;
  saving: boolean;
  error: any;

  form: UntypedFormGroup;

  constructor(private fb: UntypedFormBuilder,
              private countryService: CountryService,
              private candidateOccupationService: CandidateOccupationService,
              private jobExperienceService: CandidateJobExperienceService) { }

  ngOnInit() {
    this.loading = false;
    this.saving = false;
    this.error = null;

    /* Load missing countries */
    if (!this.countries) {
      this.loading = true;
      this.countryService.listCountries().subscribe(
        (response) => {
          this.loading = false;
          this.countries = response
        },
        (error) => {
          this.error = error;
          this.loading = false;
        });
    }

    /* Load missing candidate occupations */
    this.candidateOccupationService.listMyOccupations().subscribe(
      (response) => {
        this.candidateOccupations = response;
      },
      (error) => {
        this.error = error;
      });

    this.form = this.fb.group({
      id: [this.candidateJobExperience ? this.candidateJobExperience.id : null],
      companyName: [this.candidateJobExperience ? this.candidateJobExperience.companyName : '', Validators.required],
      country: [this.candidateJobExperience ? this.candidateJobExperience.countryId : null, Validators.required],
      candidateOccupationId: [this.candidateJobExperience ? this.candidateJobExperience.candidateOccupationId : '', Validators.required],
      role: [this.candidateJobExperience ? this.candidateJobExperience.role : '', Validators.required],
      startDate: [this.candidateJobExperience ? this.candidateJobExperience.startDate : null, Validators.required],
      endDate: [this.candidateJobExperience ? this.candidateJobExperience.endDate : null],
      fullTime: [this.candidateJobExperience ? this.candidateJobExperience.fullTime : null, Validators.required],
      paid: [this.candidateJobExperience ? this.candidateJobExperience.paid : null, Validators.required],
      description: [this.candidateJobExperience ? this.candidateJobExperience.description : '', Validators.required]
    }, {validator: this.startDateBeforeEndDate('startDate', 'endDate')});

    this.form.controls['paid'].valueChanges.subscribe(
      (val) => {
        /* DEBUG */
        // console.log('this.form', this.form);
      },
      (error) => {
        // console.log('error', error);
      });

    /* Patch form with candidates occupation */
    if (this.candidateJobExperience) {
      const exp = this.candidateJobExperience;
      this.form.controls['candidateOccupationId'].patchValue(exp.candidateOccupation.id || exp.candidateOccupationId);
    } else if (this.candidateOccupation && !this.candidateJobExperience) {
      this.form.controls['candidateOccupationId'].patchValue(this.candidateOccupation.id);
    }
  }

  startDateBeforeEndDate(from: string, to: string) {
    return (group: UntypedFormGroup): { [key: string]: any } => {
      let f = group.controls[from];
      let t = group.controls[to];
      if (f.value && t.value && f.value > t.value) {
        return {
          invalidDate: "Date from should be less than Date to"
        };
      }
      return {};
    }
  }

  ngAfterViewInit(): void {
    this.top.nativeElement.scrollIntoView()
  }

  save() {
    this.saving = true;
    if (this.form.value.id) {
      this.jobExperienceService.updateJobExperience(this.form.value).subscribe(
        (response) => this.emitSaveEvent(response),
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    } else {
      this.jobExperienceService.createJobExperience(this.form.value).subscribe(
        (response) => {
          response.candidateOccupation = this.candidateOccupation;
          this.emitSaveEvent(response)
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    }
  }

  emitSaveEvent(exp: CandidateJobExperience) {
    this.formSaved.emit(exp);
    this.saving = false;
  }

  cancel() {
    this.formClosed.emit();
  }

}
