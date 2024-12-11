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

import {VisaJobCheckUkComponent} from './visa-job-check-uk.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgbAccordionModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {
  RelocatingDependantsComponent
} from "../../../../../visa/visa-job-assessments/relocating-dependants/relocating-dependants.component";
import {DependantsComponent} from "../../../../../intake/dependants/dependants.component";
import {MockCandidate} from "../../../../../../../MockData/MockCandidate";
import {
  mockCandidateIntakeData
} from "../../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {MockCandidateVisaJobCheck} from "../../../../../../../MockData/MockCandidateVisaCheck";
import {mockCandidateOpportunity} from "../../../../../../../MockData/MockCandidateOpportunity";

describe('VisaJobCheckUkComponent', () => {
  let component: VisaJobCheckUkComponent;
  let fixture: ComponentFixture<VisaJobCheckUkComponent>;
  const mockCandidate = new MockCandidate();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,NgbAccordionModule,ReactiveFormsModule,
        NgSelectModule],
      declarations: [ VisaJobCheckUkComponent, RelocatingDependantsComponent, DependantsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobCheckUkComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.candidateIntakeData = mockCandidateIntakeData;
    component.visaCheckRecord = { country: { id: 1 } } as any; // Mock visa check record
    component.selectedJobCheck = MockCandidateVisaJobCheck; // Mock selected job check
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should find candidate opportunity associated with selected job check', () => {
    const candidateOpportunity = mockCandidateOpportunity;

    fixture.detectChanges(); // ngOnInit() gets called here

    expect(candidateOpportunity.jobOpp.id).toEqual(component.selectedJobCheck.jobOpp.id);
  });

  it('should expand all panels after view init', () => {
    fixture.detectChanges(); // ngOnInit() gets called here
    component.visaJobUk = { expandAll: jasmine.createSpy('expandAll') } as any;

    component.ngAfterViewInit();

    expect(component.visaJobUk.expandAll).toHaveBeenCalled();
  });
});
