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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {HealthAssessmentComponent} from './health-assessment.component';
import {CandidateVisaCheckService} from '../../../../services/candidate-visa-check.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('HealthAssessmentComponent', () => {
  let component: HealthAssessmentComponent;
  let fixture: ComponentFixture<HealthAssessmentComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      declarations: [HealthAssessmentComponent,AutosaveStatusComponent],
      providers: [
        UntypedFormBuilder,
        {provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy}
      ]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HealthAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('hasNotes should return false when visaHealthAssessment is not "Yes" or "No"', () => {
    component.form.controls['visaHealthAssessment'].setValue('Maybe');
    expect(component.hasNotes).toBeFalse();

    component.form.controls['visaHealthAssessment'].setValue(null);
    expect(component.hasNotes).toBeFalse();

    component.form.controls['visaHealthAssessment'].setValue('Not sure');
    expect(component.hasNotes).toBeFalse();
  });

  // Optional: Add tests for 'Yes' and 'No' values to ensure completeness
  it('hasNotes should return true when visaHealthAssessment is "Yes"', () => {
    component.form.controls['visaHealthAssessment'].setValue('Yes');
    expect(component.hasNotes).toBeTrue();
  });

  it('hasNotes should return true when visaHealthAssessment is "No"', () => {
    component.form.controls['visaHealthAssessment'].setValue('No');
    expect(component.hasNotes).toBeTrue();
  });
});
