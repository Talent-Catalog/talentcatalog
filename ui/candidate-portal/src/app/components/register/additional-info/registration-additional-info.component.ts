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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {RegistrationService} from "../../../services/registration.service";
import {SurveyTypeService} from "../../../services/survey-type.service";
import {SurveyType, US_AFGHAN_SURVEY_TYPE} from "../../../model/survey-type";

@Component({
  selector: 'app-registration-additional-info',
  templateUrl: './registration-additional-info.component.html',
  styleUrls: ['./registration-additional-info.component.scss']
})
export class RegistrationAdditionalInfoComponent implements OnInit {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  form: UntypedFormGroup;
  error: any;
  _loading = {
    surveyTypes: true,
    additionalInfo: true,
    candidateSurvey: true,
    linkedInLink: true,
    allNotifications: true
  };
  // Component states
  saving: boolean;

  usAfghan: boolean;
  surveyTypes: SurveyType[];

  constructor(private fb: UntypedFormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              public registrationService: RegistrationService,
              private surveyTypeService: SurveyTypeService) {
  }

  ngOnInit() {
    const linkedInRegex = /^http(s)?:\/\/([\w]+\.)?linkedin\.com\/in\/[A-z0-9_-]+\/?/
    this.saving = false;
    this.form = this.fb.group({
      additionalInfo: [''],
      surveyTypeId: [null, Validators.required],
      surveyComment: [''],
      linkedInLink: ['', Validators.pattern(linkedInRegex)],
      allNotifications: [false],
    });

    this.candidateService.getCandidateSurvey().subscribe(
      (response) => {
        // if afghan
        if (response?.surveyType?.id === US_AFGHAN_SURVEY_TYPE) {
          this.usAfghan = true;
          this.form.removeControl('surveyTypeId');
          this.form.removeControl('surveyComment');
          this._loading.surveyTypes = false;
        } else {
          this.loadDropDownData();
          this.form.patchValue({
            surveyTypeId: response.surveyType ? response.surveyType.id : null,
            surveyComment: response.surveyComment
          });
        }
        this._loading.candidateSurvey = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidateSurvey = false;
      }
    );

    this.candidateService.getCandidateAdditionalInfo().subscribe(
      (candidate) => {
        this.form.patchValue({
          additionalInfo: candidate.additionalInfo,
          linkedInLink: candidate.linkedInLink,
          allNotifications: candidate.allNotifications
        });
        this._loading.additionalInfo = false;
        this._loading.linkedInLink = false;
        this._loading.allNotifications = false;
      },
      (error) => {
        this.error = error;
        this._loading.additionalInfo = false;
        this._loading.linkedInLink = false;
        this._loading.allNotifications = false;
      }
    );

  }

  get allNotifications(): boolean {
    return this.form.controls.allNotifications.value;
  }

  loadDropDownData() {
    this._loading.surveyTypes = true;

    /* Load the survey types  */
    this.surveyTypeService.listActiveSurveyTypes().subscribe(
      (response) => {
        /* Sort order with 'Other' showing last */
        const sortOrder = [1, 2, 3, 4, 5, 6, 7, 9, 8];
        this.surveyTypes = response
          .sort((a, b) => {
            return sortOrder.indexOf(a.id) - sortOrder.indexOf(b.id);
          })
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

    if (this.usAfghan) {
      this.candidateService.updateCandidateOtherInfo(this.form.value).subscribe(
        (candidate) => {

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
    } else {
      this.candidateService.updateCandidateSurvey(this.form.value).subscribe(
        (response) => {

          this.candidateService.updateCandidateOtherInfo(this.form.value).subscribe(
            (candidate) => {

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

  }

  get loading() {
    const l = this._loading;
    return l.surveyTypes || l.additionalInfo || l.candidateSurvey;
  }

  cancel() {
    this.onSave.emit();
  }
}
