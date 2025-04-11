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
import {CandidateEducationTabComponent} from './candidate-education-tab.component';
import {Candidate} from "../../../../../model/candidate";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {
  ViewCandidateCertificationComponent
} from "../../certification/view-candidate-certification.component";
import {ViewCandidateEducationComponent} from "../../education/view-candidate-education.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

describe('CandidateEducationTabComponent', () => {
  let component: CandidateEducationTabComponent;
  let fixture: ComponentFixture<CandidateEducationTabComponent>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [CandidateEducationTabComponent,ViewCandidateCertificationComponent,ViewCandidateEducationComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateEducationTabComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch candidate education information on initialization', () => {
    const candidate: Candidate = mockCandidate; // Provide a sample candidate object for testing
    component.candidate = candidate;

    component.ngOnInit();
    component.ngOnChanges({
      candidate:
        {
          previousValue: null,
          currentValue: mockCandidate,
          firstChange: true,
          isFirstChange: () => true
        }
    });
    expect(component.error).toBe(null); // Ensure error is set to null after initialization
    expect(component.loading).toBe(false); // Ensure loading is set to false after initialization
    expect(component.result).toEqual(candidate); // Ensure candidate education information is set correctly
  });

  it('should fetch candidate education information when candidate data changes', () => {
    const initialCandidate: Candidate = mockCandidate;
    const updatedCandidate: Candidate = mockCandidate;
    updatedCandidate.id = 2;
    component.candidate = initialCandidate;

    component.ngOnChanges({
      candidate:
        {
          previousValue: null,
          currentValue: initialCandidate,
          firstChange: true,
          isFirstChange: () => true
        }
    });

    expect(component.loading).toBe(false); // Ensure loading is set to false after candidate education information is fetched
    expect(component.result).toEqual(initialCandidate); // Ensure candidate education information is set correctly

    component.ngOnChanges({
      candidate:
        {
          previousValue: initialCandidate,
          currentValue: updatedCandidate,
          firstChange: true,
          isFirstChange: () => true
        }
    });
    expect(component.loading).toBe(false); // Ensure loading is set to false after candidate education information is fetched
    expect(component.result).toEqual(updatedCandidate); // Ensure candidate education information is updated correctly
  });
});
