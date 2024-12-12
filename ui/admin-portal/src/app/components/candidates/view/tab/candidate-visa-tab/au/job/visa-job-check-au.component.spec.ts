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
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {VisaJobCheckAuComponent} from "./visa-job-check-au.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {OccupationService} from "../../../../../../../services/occupation.service";
import {
  CandidateOccupationService
} from "../../../../../../../services/candidate-occupation.service";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {
  mockCandidateIntakeData
} from "../../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {MockCandidate} from "../../../../../../../MockData/MockCandidate";
import {CandidateVisa, CandidateVisaJobCheck} from "../../../../../../../model/candidate";
import {of} from "rxjs";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateService} from "../../../../../../../services/candidate.service";
import {UpdatedByComponent} from "../../../../../../util/user/updated-by/updated-by.component";
import {NgbAccordionModule} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidateVisaJobCheck} from "../../../../../../../MockData/MockCandidateVisaCheck";

describe('VisaJobCheckAuComponent', () => {
  let component: VisaJobCheckAuComponent;
  let fixture: ComponentFixture<VisaJobCheckAuComponent>;
  let candidateEducationService: jasmine.SpyObj<CandidateEducationService>;
  let candidateOccupationService: jasmine.SpyObj<CandidateOccupationService>;
  let occupationService: jasmine.SpyObj<OccupationService>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  const mockCandidate = new MockCandidate();

  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get']);
    const candidateEducationServiceSpy = jasmine.createSpyObj('CandidateEducationService', ['list']);
    const candidateOccupationServiceSpy = jasmine.createSpyObj('CandidateOccupationService', ['get']);
    const occupationServiceSpy = jasmine.createSpyObj('OccupationService', ['listOccupations']);
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule,NgbAccordionModule,NgSelectModule],
      declarations: [ VisaJobCheckAuComponent,UpdatedByComponent],
      providers: [
        { provide: CandidateEducationService, useValue: candidateEducationServiceSpy },
        { provide: CandidateOccupationService, useValue: candidateOccupationServiceSpy },
        { provide: OccupationService, useValue: occupationServiceSpy },
        { provide: CandidateService, useValue: candidateServiceSpy },
       ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    candidateEducationService = TestBed.inject(CandidateEducationService) as jasmine.SpyObj<CandidateEducationService>;
    candidateOccupationService = TestBed.inject(CandidateOccupationService) as jasmine.SpyObj<CandidateOccupationService>;
    occupationService = TestBed.inject(OccupationService) as jasmine.SpyObj<OccupationService>;
    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobCheckAuComponent);
    component = fixture.componentInstance;
    component.selectedJobCheck = MockCandidateVisaJobCheck;
    component.candidate = mockCandidate;
    component.candidateIntakeData = mockCandidateIntakeData;
    component.visaCheckRecord = { country: { id: 1 }, candidateVisaJobChecks: [ { id: 1, occupation: { id: 1 } } as CandidateVisaJobCheck ] } as CandidateVisa;
    // component.visaJobAus =
    candidateEducationService.list.and.returnValue(of([]));
    candidateOccupationService.get.and.returnValue(of(mockCandidate.candidateOccupations));
    occupationService.listOccupations.and.returnValue(of());
    candidateService.get.and.returnValue(of(mockCandidate));
    component.candOccupations = mockCandidate.candidateOccupations;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  })
  //
  // it('should fetch candidate occupations, qualifications, and occupations on init', () => {
  //   expect(candidateEducationService.list).toHaveBeenCalledWith(1);
  //   expect(candidateOccupationService.get).toHaveBeenCalledWith(1);
  //   expect(occupationService.listOccupations).toHaveBeenCalled();
  // });
  //
  //
  // it('should calculate IELTS score type correctly', () => {
  //   expect(component.ieltsScoreType).toBe('Estimated'); // Adjust based on your logic
  // });
});
