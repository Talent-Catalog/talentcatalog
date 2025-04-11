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
import {VisaCheckCaComponent} from "./visa-check-ca.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {
  mockCandidateIntakeData
} from "../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {CandidateVisa, CandidateVisaJobCheck} from "../../../../../../model/candidate";
import {NgbAccordionModule} from "@ng-bootstrap/ng-bootstrap";

describe('VisaCheckCaComponent', () => {
  let component: VisaCheckCaComponent;
  let fixture: ComponentFixture<VisaCheckCaComponent>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgbAccordionModule],
      declarations: [VisaCheckCaComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckCaComponent);
    component = fixture.componentInstance;

    component.candidate = mockCandidate;
    component.candidateIntakeData = mockCandidateIntakeData;
    component.visaCheckRecord = {
      candidateVisaJobChecks: [
        { id: 1, occupation: { id: 1, name: 'Job 1' } } as CandidateVisaJobCheck,
        { id: 2, occupation: { id: 2, name: 'Job 2' } } as CandidateVisaJobCheck
      ]
    } as CandidateVisa;

    fixture.detectChanges(); // trigger data binding
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select the first job by default', () => {
    expect(component.selectedJob).toEqual(component.visaCheckRecord.candidateVisaJobChecks[0]);
  });
});
