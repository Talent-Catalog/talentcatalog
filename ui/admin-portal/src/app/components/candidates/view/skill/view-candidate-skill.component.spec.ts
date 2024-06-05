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
import {ViewCandidateSkillComponent} from "./view-candidate-skill.component";
import {CandidateSkillService} from "../../../../services/candidate-skill.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {Candidate} from "../../../../model/candidate";
import {of, throwError} from "rxjs";

fdescribe('ViewCandidateSkillComponent', () => {
  let component: ViewCandidateSkillComponent;
  let fixture: ComponentFixture<ViewCandidateSkillComponent>;
  let candidateSkillService: jasmine.SpyObj<CandidateSkillService>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const candidateSkillServiceSpy = jasmine.createSpyObj('CandidateSkillService', ['search']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateSkillComponent],
      providers: [
        { provide: CandidateSkillService, useValue: candidateSkillServiceSpy }
      ]
    }).compileComponents();

    candidateSkillService = TestBed.inject(CandidateSkillService) as jasmine.SpyObj<CandidateSkillService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateSkillComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component correctly and load data', () => {
    const candidate: Candidate = mockCandidate;

    const candidateSkillsResponse = {
      content: [
        { skill: 'JavaScript', timePeriod: 5 },
        { skill: 'Angular', timePeriod: 3 }
      ]
    }; 

    // Simulate candidateSkillService.search() returning mock data
    // @ts-expect-error
    candidateSkillService.search.and.returnValue(of(candidateSkillsResponse));

    // Set input properties
    component.candidate = candidate;

    // Trigger ngOnChanges manually
    component.ngOnChanges({  candidate: {
        currentValue: component.candidate,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }} );

    // Expect candidateSkillService.search() to have been called with the correct candidate ID and pagination parameters
    expect(candidateSkillService.search).toHaveBeenCalledWith({
      candidateId: candidate.id,
      pageNumber: 0,
      pageSize: 20
    });

    // Simulate candidateSkillService.search() completing and emitting mock response data
    fixture.detectChanges();

    // Expect loading to be false after data loading completes
    expect(component.loading).toBe(false);

    // Expect component's candidateSkills property to be set with the response data
    expect(component.candidateSkills).toEqual(candidateSkillsResponse.content);
  });

  it('should handle error when loading data', () => {
    const candidate: Candidate = mockCandidate;
    const errorResponse = 'Error loading candidate skills';

    // Simulate candidateSkillService.search() returning an error
    candidateSkillService.search.and.returnValue(throwError(errorResponse));

    // Set input properties
    component.candidate = candidate;

    // Trigger ngOnChanges manually
    component.ngOnChanges({  candidate: {
        currentValue: component.candidate,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }} );

    // Expect loading to be false after data loading completes
    expect(component.loading).toBe(false);

    // Expect component's error property to be set with the error response
    expect(component.error).toBe(errorResponse);
  });
});
