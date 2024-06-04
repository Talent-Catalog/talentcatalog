/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {CandidateAdditionalInfoTabComponent} from "./candidate-additional-info-tab.component";
import {CandidateService} from "../../../../../services/candidate.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {Candidate} from "../../../../../model/candidate";
import {of} from "rxjs";
import {ViewCandidateSurveyComponent} from "../../survey/view-candidate-survey.component";
import {
  ViewCandidateMediaWillingnessComponent
} from "../../media/view-candidate-media-willingness.component";
fdescribe('CandidateAdditionalInfoTabComponent', () => {
  let component: CandidateAdditionalInfoTabComponent;
  let fixture: ComponentFixture<CandidateAdditionalInfoTabComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['']);
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get']);

    await TestBed.configureTestingModule({
      declarations: [ CandidateAdditionalInfoTabComponent,ViewCandidateSurveyComponent,ViewCandidateMediaWillingnessComponent ],
      providers: [
        { provide: AuthorizationService, useValue: authServiceSpy },
        { provide: CandidateService, useValue: candidateServiceSpy }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateAdditionalInfoTabComponent);
    component = fixture.componentInstance;
    component.loading = false;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch candidate information on initialization', () => {
    const candidate: Candidate = mockCandidate // Provide a sample candidate object for testing
    candidateServiceSpy.get.and.returnValue(of(candidate));

    component.ngOnInit();

    expect(component.loading).toBe(false); // Ensure loading is set to false after initialization
    expect(component.candidate).toEqual(candidate); // Ensure candidate data is set correctly
  });

  it('should fetch candidate information when candidate data changes', () => {
    const initialCandidate: Candidate = mockCandidate;
    const updatedCandidate: Candidate = mockCandidate;
    updatedCandidate.id = 2;
    candidateServiceSpy.get.withArgs(initialCandidate.id).and.returnValue(of(initialCandidate));

    component.candidate = initialCandidate;
    component.ngOnChanges({
      candidate: {
        currentValue: initialCandidate,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    });
    expect(component.loading).toBe(false);
    expect(candidateServiceSpy.get).toHaveBeenCalledWith(initialCandidate.id); // Ensure get method of candidate service is called with the initial candidate ID

    candidateServiceSpy.get.withArgs(initialCandidate.id).and.returnValue(of(updatedCandidate));

    component.ngOnChanges({
      candidate: {
        currentValue: updatedCandidate,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    });
    expect(component.loading).toBe(false); // Ensure loading is set to false after candidate data is fetched
    expect(component.candidate).toEqual(updatedCandidate); // Ensure candidate data is updated correctly
  });
});
