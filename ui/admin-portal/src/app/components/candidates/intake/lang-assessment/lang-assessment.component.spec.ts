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

import {LangAssessmentComponent} from "./lang-assessment.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateIntakeData, IntRecruitReason} from "../../../../model/candidate";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {
  NclcScoreValidationComponent
} from "../../../util/nclc-score-validation/nclc-score-validation.component";
import {
  IeltsScoreValidationComponent
} from "../../../util/ielts-score-validation/ielts-score-validation.component";

describe('LangAssessmentComponent', () => {
  let component: LangAssessmentComponent;
  let fixture: ComponentFixture<LangAssessmentComponent>;
  const mockCandidateIntakeData: CandidateIntakeData = {
    // Fill in mock data for the CandidateIntakeData interface properties
    englishAssessment: 'English assessment text',
    englishAssessmentScoreIelts: '7.5',
    englishAssessmentScoreDet: 100,
    frenchAssessment: 'French assessment text',
    frenchAssessmentScoreNclc: 9,
    intRecruitReasons: [IntRecruitReason.Other],
    intRecruitOther: 'Other',
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LangAssessmentComponent, AutosaveStatusComponent, NclcScoreValidationComponent, IeltsScoreValidationComponent],
      imports: [HttpClientTestingModule,NgbTooltipModule,NgSelectModule,FormsModule,ReactiveFormsModule],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LangAssessmentComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = mockCandidateIntakeData;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with provided data', () => {
    const mockCandidateIntakeData = {
      englishAssessment: 'Some English assessment',
      englishAssessmentScoreIelts: null,
      englishAssessmentScoreDet: null,
      frenchAssessment: 'Some French assessment',
      frenchAssessmentScoreNclc: null
    };
    component.candidateIntakeData = mockCandidateIntakeData;
    component.ngOnInit();
    const expectedFormData = {
      englishAssessment: 'Some English assessment',
      englishAssessmentScoreIelts: null,
      englishAssessmentScoreDet: null,
      frenchAssessment: 'Some French assessment',
      frenchAssessmentScoreNclc: null
    };
    expect(component.form.value).toEqual(expectedFormData);
  });

  it('should validate the IELTS score with provided regex pattern', () => {
    // Test valid IELTS scores
    const validScores = [7, 7.5, 8, 8.5, 9];
    validScores.forEach(score => {
      component.form.get('englishAssessmentScoreIelts').setValue(score);
      expect(component.form.valid).toBeTruthy();
    });
  });

  it('should validate the DET score with provided regex pattern', () => {
    // Test valid DET scores
    const validScores = [10, 50, 100, 160];
    validScores.forEach(score => {
      component.form.get('englishAssessmentScoreDet').setValue(score);
      expect(component.form.valid).toBeTruthy();
    });
  });

});
