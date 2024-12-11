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
import {Occupation} from '../../../../../model/occupation';
import {JobOccupationComponent} from './job-occupation.component';
import {By} from '@angular/platform-browser';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidateVisaJobCheck} from "../../../../../MockData/MockCandidateVisaCheck";

describe('JobOccupationComponent', () => {
  let component: JobOccupationComponent;
  let fixture: ComponentFixture<JobOccupationComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [JobOccupationComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [UntypedFormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobOccupationComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(UntypedFormBuilder);
    component.visaJobCheck = MockCandidateVisaJobCheck;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have a form with visaJobOccupationId and visaJobOccupationNotes controls', () => {
    expect(component.form.contains('visaJobOccupationId')).toBeTrue();
    expect(component.form.contains('visaJobOccupationNotes')).toBeTrue();
  });

  it('should initialize form with visaJobCheck values', () => {
    const testOccupation: Occupation = {
      id: 1,
      name: 'Test Occupation',
      isco08Code: 'Test Code',
      status: 'Test Status'
    };
    component.visaJobCheck = {
      id: 1,
      occupation: testOccupation,
      occupationNotes: 'Sample notes'
    };
    component.ngOnInit();
    expect(component.form.get('visaJobOccupationId')?.value).toEqual(testOccupation.id);
    expect(component.form.get('visaJobOccupationNotes')?.value).toEqual('Sample notes');
  });

  it('should update occupation object when visaJobOccupationId value changes', () => {
    const testOccupationId = 1;
    const testOccupation: Occupation = {
      id: testOccupationId,
      name: 'Test Occupation',
      isco08Code: 'Test Code',
      status: 'Test Status'
    };
    const select = fixture.debugElement.query(By.css('ng-select'));
    component.visaJobCheck = { id: 1, occupation: testOccupation, occupationNotes: null };
    fixture.detectChanges();
    select.triggerEventHandler('change', testOccupationId);
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.occupationId).toEqual(testOccupationId);
    expect(component.visaJobCheck.occupation).toEqual(testOccupation);
  });
});
