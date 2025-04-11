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
import {PathwayAssessmentComponent} from './pathway-assessment.component';
import {CandidateVisaCheckService} from '../../../../services/candidate-visa-check.service';
import {By} from '@angular/platform-browser';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('PathwayAssessmentComponent', () => {
  let component: PathwayAssessmentComponent;
  let fixture: ComponentFixture<PathwayAssessmentComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      declarations: [PathwayAssessmentComponent,AutosaveStatusComponent],
      providers: [
        UntypedFormBuilder,
        {provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy}
      ]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PathwayAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('notes textarea should be displayed when visaPathwayAssessment is "Yes"', () => {
    component.form.controls['visaPathwayAssessment'].setValue('Yes');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('#visaPathwayAssessmentNotes'));
    expect(textarea).toBeTruthy();
  });

  it('notes textarea should be displayed when visaPathwayAssessment is "No"', () => {
    component.form.controls['visaPathwayAssessment'].setValue('No');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('#visaPathwayAssessmentNotes'));
    expect(textarea).toBeTruthy();
  });

  it('notes textarea should be displayed when visaPathwayAssessment is "Unsure"', () => {
    component.form.controls['visaPathwayAssessment'].setValue('Unsure');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('#visaPathwayAssessmentNotes'));
    expect(textarea).toBeTruthy();
  });

  it('notes textarea should not be displayed when visaPathwayAssessment is set to a value other than "Yes", "No", or "Unsure"', () => {
    component.form.controls['visaPathwayAssessment'].setValue(null);
    fixture.detectChanges();
    let textarea = fixture.debugElement.query(By.css('#visaPathwayAssessmentNotes'));
    expect(textarea).toBeFalsy();

    component.form.controls['visaPathwayAssessment'].setValue('Maybe');
    fixture.detectChanges();
    textarea = fixture.debugElement.query(By.css('#visaPathwayAssessmentNotes'));
    expect(textarea).toBeFalsy();
  });
});
