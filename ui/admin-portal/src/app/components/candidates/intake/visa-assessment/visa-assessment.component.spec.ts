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
import {VisaAssessmentComponent} from "./visa-assessment.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateService} from "../../../../services/candidate.service";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {VisaEligibility} from "../../../../model/candidate";

describe('VisaAssessmentComponent', () => {
  let component: VisaAssessmentComponent;
  let fixture: ComponentFixture<VisaAssessmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaAssessmentComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaAssessmentComponent);
    component = fixture.componentInstance;
    component.selectedJobCheck = {
      id: 1,
      putForward: VisaEligibility.Yes,
      notes: 'Candidate meets all visa requirements.'
    };
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('visaJobId').value).toBe(1);
    expect(component.form.get('visaJobPutForward').value).toBe(VisaEligibility.Yes);
    expect(component.form.get('visaJobNotes').value).toBe('Candidate meets all visa requirements.');
  });
});
