/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {ReactiveFormsModule, FormBuilder} from '@angular/forms';
import {VisaEligibilityAssessmentComponent} from './visa-eligibility-assessment.component';
import {CandidateService} from '../../../../services/candidate.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {MockCandidateVisa} from "../../../../MockData/MockCandidateVisa";
describe('VisaEligibilityAssessmentComponent', () => {
  let component: VisaEligibilityAssessmentComponent;
  let fixture: ComponentFixture<VisaEligibilityAssessmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      declarations: [VisaEligibilityAssessmentComponent,AutosaveStatusComponent],
      providers: [
        FormBuilder,
        {
          provide: CandidateService,
          useValue: jasmine.createSpyObj('CandidateService', ['someMethod'])
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaEligibilityAssessmentComponent);
    component = fixture.componentInstance;
    component.visaCheckRecord = MockCandidateVisa;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // it('form should initialize correctly with provided visaCheckRecord data', () => {
  //
  //   component.ngOnInit();
  //
  //   expect(component.form.value.visaId).toEqual(MockCandidateVisa.id);
  //   expect(component.form.value.visaCountryId).toEqual(MockCandidateVisa.country.id);
  //   expect(component.form.value.visaEligibilityAssessment).toEqual(MockCandidateVisa.visaEligibilityAssessment);
  // });
});
