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

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {Candidate} from "../../../../../model/candidate";
import {SurveyType} from "../../../../../model/survey-type";
import {SurveyTypeService} from "../../../../../services/survey-type.service";

@Component({
  selector: 'app-edit-candidate-survey',
  templateUrl: './edit-candidate-survey.component.html',
  styleUrls: ['./edit-candidate-survey.component.scss']
})
export class EditCandidateSurveyComponent implements OnInit {

  candidateId: number;

  candidateForm: UntypedFormGroup;

  error;
  loading: boolean;
  saving: boolean;

  surveyTypes: SurveyType[];

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private surveyTypeService: SurveyTypeService) {}

  ngOnInit() {
    this.loading = true;
    this.loadDropDownData();

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        surveyTypeId: [candidate.surveyType?.id],
        surveyComment: [candidate.surveyComment]
      });
      this.loading = false;
    });
  }

  loadDropDownData() {
    /* Load the survey types  */
    this.surveyTypeService.listSurveyTypes().subscribe(
      (response) => {
        /* Sort order with 'Other' showing last */
        const sortOrder = [1, 2, 3, 4, 5, 6, 7, 9, 8, 10];
        this.surveyTypes = response
          .sort((a, b) => {
            return sortOrder.indexOf(a.id) - sortOrder.indexOf(b.id);
          })
      },
      (error) => {
        this.error = error;
      }
    );
  }

  onSave() {
    this.saving = true;
    this.candidateService.updateSurvey(this.candidateId, this.candidateForm.value).subscribe(
      (candidate) => {
        this.closeModal(candidate);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidate: Candidate) {
    this.activeModal.close(candidate);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
