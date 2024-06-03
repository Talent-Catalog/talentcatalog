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
import {VisaCheckAuComponent} from "./visa-check-au.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageModule, LocalStorageService} from "angular-2-local-storage";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {
  AvailImmediateReason,
  CandidateCitizenship, CandidateExam,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck, Exam, HasPassport, YesNoUnsure
} from "../../../../../../model/candidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockJob} from "../../../../../../MockData/MockJob";

fdescribe('VisaCheckAuComponent', () => {
  let component: VisaCheckAuComponent;
  let fixture: ComponentFixture<VisaCheckAuComponent>;
  const mockCandidate = new MockCandidate();
  const mockCitizenship: CandidateCitizenship = {
    id: 1,
    nationality: MockJob.country,
    hasPassport: HasPassport.ValidPassport,
    passportExp: '02/03/2028',
    notes: 'Note'
  }
  const mockCandidateExam: CandidateExam = {
    id: 1,
    exam: Exam.TOEFL,
    otherExam: 'TOEFL',
    score: '95',
    year: 2022,
    notes: 'Passed with high marks'
  };
  const mockCandidateIntakeData: CandidateIntakeData = {
    asylumYear: '2023',
    availImmediate: YesNoUnsure.Yes,
    availImmediateJobOps: 'Some job opportunities',
    availImmediateReason: AvailImmediateReason.Other,
    availImmediateNotes: 'Some notes',
    candidateCitizenships: [mockCitizenship],
    candidateExams:[mockCandidateExam],
    candidateDependants: [],
    // Add more properties with predefined values here
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaCheckAuComponent ],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,LocalStorageModule.forRoot({})],
      providers: [
        { provide: NgbModal, useValue: {} },
        { provide: LocalStorageService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckAuComponent);
    component = fixture.componentInstance;

    // Initialize input properties
    component.candidate = mockCandidate;
    component.candidateIntakeData = mockCandidateIntakeData;
    component.visaCheckRecord = {
      candidateVisaJobChecks: [
        { id: 1 } as CandidateVisaJobCheck,
        { id: 2 } as CandidateVisaJobCheck
      ]
    } as CandidateVisa;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize currentYear and birthYear correctly', () => {
    const currentYear = new Date().getFullYear().toString();
    expect(component.currentYear).toBe(currentYear);
    expect(component.birthYear).toBe('Mon ');
  });

  it('should select the first job by default', () => {
    expect(component.selectedJob).toBe(component.visaCheckRecord.candidateVisaJobChecks[0]);
  });
});
