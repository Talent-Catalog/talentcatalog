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
import {JobInterestComponent} from './job-interest.component';
import {CandidateVisaJobCheck, YesNo} from '../../../../../model/candidate';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockCandidateVisaJobCheck} from "../../../../../MockData/MockCandidateVisaCheck";
import {NgSelectModule} from "@ng-select/ng-select";

describe('JobInterestComponent', () => {
  let component: JobInterestComponent;
  let fixture: ComponentFixture<JobInterestComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [JobInterestComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [UntypedFormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobInterestComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(UntypedFormBuilder);
    component.visaJobCheck = MockCandidateVisaJobCheck;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have a form with visaJobInterest and visaJobInterestNotes controls', () => {
    expect(component.form.contains('visaJobInterest')).toBeTrue();
    expect(component.form.contains('visaJobInterestNotes')).toBeTrue();
  });

  it('should have initial values for form controls', () => {
    const testVisaJobCheck: CandidateVisaJobCheck = {
      id: 1,
      interest: YesNo.Yes,
      interestNotes: 'Sample notes'
    };
    component.visaJobCheck = testVisaJobCheck;
    component.ngOnInit();
    expect(component.form.get('visaJobInterest')?.value).toEqual(testVisaJobCheck.interest);
    expect(component.form.get('visaJobInterestNotes')?.value).toEqual(testVisaJobCheck.interestNotes);
  });

  it('should return true for hasNotes when visaJobInterest is "Yes"', () => {
    component.form.get('visaJobInterest')?.setValue(YesNo.Yes);
    expect(component.hasNotes).toBeTrue();
  });

  it('should return true for hasNotes when visaJobInterest is "No"', () => {
    component.form.get('visaJobInterest')?.setValue(YesNo.No);
    expect(component.hasNotes).toBeTrue();
  });

  it('should return false for hasNotes when visaJobInterest is null', () => {
    component.form.get('visaJobInterest')?.setValue(null);
    expect(component.hasNotes).toBeFalse();
  });
});
