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
import {JobEligibilityAssessmentComponent} from './job-eligibility-assessment.component';
import {TBBEligibilityAssessment} from '../../../../../model/candidate';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidateVisaJobCheck} from "../../../../../MockData/MockCandidateVisaCheck";

describe('JobEligibilityAssessmentComponent', () => {
  let component: JobEligibilityAssessmentComponent;
  let fixture: ComponentFixture<JobEligibilityAssessmentComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [JobEligibilityAssessmentComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [UntypedFormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobEligibilityAssessmentComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(UntypedFormBuilder);
    component.visaJobCheck = MockCandidateVisaJobCheck;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have a form with visaJobTbbEligibility control', () => {
    expect(component.form.contains('visaJobTbbEligibility')).toBeTrue();
  });

  it('should update form value when input changes', () => {
    const testValue: TBBEligibilityAssessment = TBBEligibilityAssessment.Discuss;
    const ngSelect = fixture.nativeElement.querySelector('ng-select');
    ngSelect.value = testValue;
    ngSelect.dispatchEvent(new Event('change'));
    component.form.get('visaJobTbbEligibility').setValue(testValue);
    expect(component.form.get('visaJobTbbEligibility')?.value).toEqual(testValue);
  });
});
