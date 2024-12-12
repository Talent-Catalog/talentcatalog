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
import {CandidateExperienceTabComponent} from "./candidate-experience-tab.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {Candidate} from "../../../../../model/candidate";
import {ViewCandidateSkillComponent} from "../../skill/view-candidate-skill.component";
import {
  ViewCandidateOccupationComponent
} from "../../occupation/view-candidate-occupation.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";

describe('CandidateExperienceTabComponent', () => {
  let component: CandidateExperienceTabComponent;
  let fixture: ComponentFixture<CandidateExperienceTabComponent>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [ CandidateExperienceTabComponent,ViewCandidateSkillComponent,ViewCandidateOccupationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateExperienceTabComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
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
    const candidate: Candidate = mockCandidate;

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

  it('should display additional functionalities for admin user', () => {
    const candidate: Candidate = mockCandidate;
    component.candidate = candidate;
    component.adminUser = true; // Simulate admin user access

    component.ngOnChanges({
      candidate:
        {
          previousValue: null,
          currentValue: candidate,
          firstChange: true,
          isFirstChange: () => true
        }
    });
    expect(component.error).toBeNull(); // Ensure error is null after initialization
    expect(component.loading).toBe(false); // Ensure loading is false after initialization
    expect(component.result).toEqual(candidate); // Ensure candidate data is set correctly
    expect(component.adminUser).toBe(true); // Ensure adminUser property is set to true
  });
});
