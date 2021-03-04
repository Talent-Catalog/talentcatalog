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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {RegistrationService} from "../../../services/registration.service";
import {SurveyTypeService} from "../../../services/survey-type.service";
import {SurveyType} from "../../../model/survey-type";
import {linkedInUrl} from "../../../model/candidate";

@Component({
  selector: 'app-registration-additional-info',
  templateUrl: './registration-additional-info.component.html',
  styleUrls: ['./registration-additional-info.component.scss']
})
export class RegistrationAdditionalInfoComponent implements OnInit {

  @Input() submitApplication: boolean = false;
  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  form: FormGroup;
  error: any;
  _loading = {
    surveyTypes: true,
    additionalInfo: true,
    candidateSurvey: true,
    linkedInLink: true
  };
  // Component states
  saving: boolean;

  surveyTypes: SurveyType[];

  linkedInUrl = linkedInUrl;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              public registrationService: RegistrationService,
              private surveyTypeService: SurveyTypeService) {
  }

  ngOnInit() {
    const linkedInRegex = /http(s)?:\/\/([\w]+\.)?linkedin\.com\/in\/[A-z0-9_-]+\/?/
    this.saving = false;
    this.form = this.fb.group({
      additionalInfo: [''],
      surveyTypeId: [null, Validators.required],
      surveyComment: [''],
      linkedInLink: ['', Validators.pattern(linkedInRegex)],
      submit: this.submitApplication
    });


    this.loadDropDownData();

    this.candidateService.getCandidateAdditionalInfo().subscribe(
      (response) => {
        this.form.patchValue({
          additionalInfo: response.additionalInfo,
          linkedInLink: response.linkedInLink
        });
        this._loading.additionalInfo = false;
        this._loading.linkedInLink = false;
      },
      (error) => {
        this.error = error;
        this._loading.additionalInfo = false;
        this._loading.linkedInLink = false;
      }
    );

    this.candidateService.getCandidateSurvey().subscribe(
      (response) => {
        this.form.patchValue({
          surveyTypeId: response.surveyType ? response.surveyType.id : null,
          surveyComment: response.surveyComment
        });
        this._loading.candidateSurvey = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidateSurvey = false;
      }
    );

  }

  loadDropDownData() {
    this._loading.surveyTypes = true;

    /* Load the survey types  */
    this.surveyTypeService.listSurveyTypes().subscribe(
      (response) => {
        this.surveyTypes = response
          .sort((a, b) => a.id > b.id ? 1 : -1) // Order by surveyType id
        this._loading.surveyTypes = false;
      },
      (error) => {
        this.error = error;
        this._loading.surveyTypes = false;
      }
    );
  }

  save(dir: string) {
    this.saving = true;

    /* Don't submit the registration application if the user is going back */
    if (dir === 'back') {
      this.form.controls.submit.patchValue(false);
    }
    /* Update survey before updating additional info as that triggers confirmation email and registration complete*/
    this.candidateService.updateCandidateSurvey(this.form.value).subscribe(
      (response) => {

        this.candidateService.updateCandidateAdditionalInfo(this.form.value).subscribe(
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
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );

  }

  get loading() {
    const l = this._loading;
    return l.surveyTypes || l.additionalInfo || l.candidateSurvey;
  }

  cancel() {
    this.onSave.emit();
  }
}
