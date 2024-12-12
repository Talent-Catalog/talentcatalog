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
import {CandidateEligibilityTabComponent} from "./candidate-eligibility-tab.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {Candidate} from "../../../../../model/candidate";

describe('CandidateEligibilityTabComponent', () => {
  let component: CandidateEligibilityTabComponent;
  let fixture: ComponentFixture<CandidateEligibilityTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateEligibilityTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateEligibilityTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set initial state correctly', () => {
    expect(component.error).toBeNull(); // Ensure error is null initially
    expect(component.loading).toBeUndefined(); // Ensure loading is undefined initially
    expect(component.result).toBeUndefined(); // Ensure result is undefined initially
  });

  it('should fetch candidate data when candidate changes', () => {
    const candidate: Candidate = new MockCandidate();
    component.candidate = candidate;

    component.ngOnChanges({
      candidate:
        {
          previousValue: null,
          currentValue: candidate,
          firstChange: true,
          isFirstChange: () => true
        }
    });
    expect(component.error).toBeNull(); // Ensure error is null after candidate data changes
    expect(component.loading).toBe(false); // Ensure loading is false after candidate data changes
    expect(component.result).toEqual(candidate); // Ensure candidate data is set correctly
  });
});
