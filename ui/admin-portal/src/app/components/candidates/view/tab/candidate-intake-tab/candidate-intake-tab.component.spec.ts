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

import {CandidateIntakeTabComponent} from "./candidate-intake-tab.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../../../services/candidate.service";
import {EducationLevelService} from "../../../../../services/education-level.service";
import {OccupationService} from "../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {AuthenticationService} from "../../../../../services/authentication.service";
import {CandidateCitizenshipService} from "../../../../../services/candidate-citizenship.service";
import {CandidateExamService} from "../../../../../services/candidate-exam.service";
import {CandidateDependantService} from "../../../../../services/candidate-dependant.service";
import {CountryService} from "../../../../../services/country.service";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {of} from "rxjs";
import {
  NgbAccordion,
  NgbAccordionModule,
  NgbDatepickerModule,
  NgbTooltipModule
} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {ExamsComponent} from "../../../intake/exams/exams.component";
import {CitizenshipsComponent} from "../../../intake/citizenships/citizenships.component";
import {IntRecruitmentComponent} from "../../../intake/int-recruitment/int-recruitment.component";
import {RuralComponent} from "../../../intake/rural/rural.component";
import {ConfirmContactComponent} from "../../../intake/confirm-contact/confirm-contact.component";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {FixedInputComponent} from "../../../../util/intake/fixed-input/fixed-input.component";
import {ExportPdfComponent} from "../../../../util/export-pdf/export-pdf.component";
import {
  AvailImmediateReason,
  CandidateCitizenship,
  CandidateDestination,
  CandidateExam,
  CandidateIntakeData,
  Exam,
  HasPassport,
  YesNo,
  YesNoUnsure
} from "../../../../../model/candidate";
import {MockJob} from "../../../../../MockData/MockJob";
import {
  CandidateCitizenshipCardComponent
} from "../../../intake/citizenships/card/candidate-citizenship-card.component";
import {
  MonitoringEvaluationConsentComponent
} from "../../../intake/monitoring-evaluation-consent/monitoring-evaluation-consent.component";
import {CrimeComponent} from "../../../intake/crime/crime.component";
import {ConflictComponent} from "../../../intake/conflict/conflict.component";
import {FinalAgreementComponent} from "../../../intake/final-agreement/final-agreement.component";
import {CovidVaccinationComponent} from "../../../intake/vaccination/covid-vaccination.component";
import {DrivingLicenseComponent} from "../../../intake/driving-license/driving-license.component";
import {VisaIssuesComponent} from "../../../intake/visa-issues/visa-issues.component";
import {VisaRejectComponent} from "../../../intake/visa-reject/visa-reject.component";
import {CandidateExamCardComponent} from "../../../intake/exams/card/candidate-exam-card.component";
import {LangAssessmentComponent} from "../../../intake/lang-assessment/lang-assessment.component";
import {ViewCandidateLanguageComponent} from "../../language/view-candidate-language.component";
import {DependantsComponent} from "../../../intake/dependants/dependants.component";
import {
  ResidenceStatusComponent
} from "../../../intake/residence-status/residence-status.component";
import {WorkPermitComponent} from "../../../intake/work-permit/work-permit.component";
import {WorkStatusComponent} from "../../../intake/work-status/work-status.component";
import {
  MilitaryServiceComponent
} from "../../../intake/military-service/military-service.component";
import {FamilyComponent} from "../../../intake/family/family.component";
import {MaritalStatusComponent} from "../../../intake/marital-status/marital-status.component";
import {
  RegistrationUnhcrComponent
} from "../../../intake/registration-unhcr/registration-unhcr.component";
import {HostChallengesComponent} from "../../../intake/host-challenges/host-challenges.component";
import {HomeLocationComponent} from "../../../intake/home-location/home-location.component";
import {
  ResettlementThirdComponent
} from "../../../intake/resettlement-third/resettlement-third.component";
import {HostEntryComponent} from "../../../intake/host-entry/host-entry.component";
import {DatePickerComponent} from "../../../../util/date-picker/date-picker.component";
import {WorkAbroadComponent} from "../../../intake/work-abroad/work-abroad.component";
import {
  NclcScoreValidationComponent
} from "../../../../util/nclc-score-validation/nclc-score-validation.component";
import {
  IeltsScoreValidationComponent
} from "../../../../util/ielts-score-validation/ielts-score-validation.component";
import {
  DetScoreValidationComponent
} from "../../../../util/det-score-validation/det-score-validation.component";

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
const mockCandidateDestination: CandidateDestination = {
  id: 1,
  country:MockJob.country,
  interest: YesNoUnsure.Yes,
  notes:'SimpleNote'
}
export const mockCandidateIntakeData: CandidateIntakeData = {
  asylumYear: '2023',
  availImmediate: YesNo.Yes,
  availImmediateJobOps: 'Some job opportunities',
  availImmediateReason: AvailImmediateReason.Other,
  availImmediateNotes: 'Some notes',
  candidateCitizenships: [mockCitizenship],
  candidateExams:[mockCandidateExam],
  candidateDependants: [],
  candidateDestinations:[mockCandidateDestination]
  // Add more properties with predefined values here
};
describe('CandidateIntakeTabComponent', () => {
  let component: CandidateIntakeTabComponent;
  let fixture: ComponentFixture<CandidateIntakeTabComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let educationLevelServiceSpy: jasmine.SpyObj<EducationLevelService>;
  let occupationServiceSpy: jasmine.SpyObj<OccupationService>;
  let languageLevelServiceSpy: jasmine.SpyObj<LanguageLevelService>;
  let noteServiceSpy: jasmine.SpyObj<CandidateNoteService>;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let candidateCitizenshipServiceSpy: jasmine.SpyObj<CandidateCitizenshipService>;
  let candidateExamServiceSpy: jasmine.SpyObj<CandidateExamService>;
  let candidateDependantServiceSpy: jasmine.SpyObj<CandidateDependantService>;
  const mockCandidate = new MockCandidate();

  beforeEach(async () => {
    const candidateSpy = jasmine.createSpyObj('CandidateService', ['get','getIntakeData']);
    const countrySpy = jasmine.createSpyObj('CountryService', ['isPalestine','listCountries','listTCDestinations']);
    const educationSpy = jasmine.createSpyObj('EducationLevelService', ['listEducationLevels']);
    const occupationSpy = jasmine.createSpyObj('OccupationService', ['listOccupations']);
    const languageSpy = jasmine.createSpyObj('LanguageLevelService', ['listLanguageLevels']);
    const noteSpy = jasmine.createSpyObj('CandidateNoteService', ['getIntakeData']);
    const authSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);

    authSpy.getLoggedInUser.and.returnValue(of({ id: 1, name: 'Test User' }));

    const citizenshipSpy = jasmine.createSpyObj('CandidateCitizenshipService', ['create']);
    citizenshipSpy.create.and.returnValue(of(null));

    const examSpy = jasmine.createSpyObj('CandidateExamService', ['create']);
    examSpy.create.and.returnValue(of(null));

    const dependantSpy = jasmine.createSpyObj('CandidateDependantService', ['create']);
    dependantSpy.create.and.returnValue(of(null));

    await TestBed.configureTestingModule({
      declarations: [CandidateIntakeTabComponent,ExamsComponent,
        CandidateCitizenshipCardComponent,CitizenshipsComponent,IntRecruitmentComponent,
        RuralComponent,MonitoringEvaluationConsentComponent,ConfirmContactComponent,
        CrimeComponent,ConflictComponent,FinalAgreementComponent,CovidVaccinationComponent,
        CandidateExamCardComponent,LangAssessmentComponent,ViewCandidateLanguageComponent,
        DependantsComponent,ResidenceStatusComponent,WorkPermitComponent,WorkStatusComponent,
        DrivingLicenseComponent,VisaIssuesComponent,VisaRejectComponent,
        MilitaryServiceComponent,FamilyComponent,MaritalStatusComponent,RegistrationUnhcrComponent,HostChallengesComponent,
        HomeLocationComponent,ResettlementThirdComponent,HostEntryComponent,
        DatePickerComponent,WorkAbroadComponent,NclcScoreValidationComponent,IeltsScoreValidationComponent,DetScoreValidationComponent,
        AutosaveStatusComponent,FixedInputComponent,ExportPdfComponent],
      imports: [HttpClientTestingModule,NgbDatepickerModule,NgbTooltipModule,FormsModule,ReactiveFormsModule, NgSelectModule,NgbAccordionModule],
      providers: [
        { provide: CandidateService, useValue: candidateSpy },
        { provide: CountryService, useValue: countrySpy },
        { provide: EducationLevelService, useValue: educationSpy },
        { provide: OccupationService, useValue: occupationSpy },
        { provide: LanguageLevelService, useValue: languageSpy },
        { provide: CandidateNoteService, useValue: noteSpy },
        { provide: AuthenticationService, useValue: authSpy },
        { provide: CandidateCitizenshipService, useValue: citizenshipSpy },
        { provide: CandidateExamService, useValue: examSpy },
        { provide: CandidateDependantService, useValue: dependantSpy },
        { provide: AuthenticationService, useValue: authSpy },
        NgbAccordion
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateIntakeTabComponent);
    component = fixture.componentInstance;

    // Initialize spies
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    educationLevelServiceSpy = TestBed.inject(EducationLevelService) as jasmine.SpyObj<EducationLevelService>;
    occupationServiceSpy = TestBed.inject(OccupationService) as jasmine.SpyObj<OccupationService>;
    languageLevelServiceSpy = TestBed.inject(LanguageLevelService) as jasmine.SpyObj<LanguageLevelService>;
    noteServiceSpy = TestBed.inject(CandidateNoteService) as jasmine.SpyObj<CandidateNoteService>;
    authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    candidateCitizenshipServiceSpy = TestBed.inject(CandidateCitizenshipService) as jasmine.SpyObj<CandidateCitizenshipService>;
    candidateExamServiceSpy = TestBed.inject(CandidateExamService) as jasmine.SpyObj<CandidateExamService>;
    candidateDependantServiceSpy = TestBed.inject(CandidateDependantService) as jasmine.SpyObj<CandidateDependantService>;

    // Mock candidate data
    component.candidate = mockCandidate;
    component.candidateIntakeData = mockCandidateIntakeData;
    candidateCitizenshipServiceSpy.create.and.returnValue(of(mockCitizenship));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return true when fullIntakeCompletedDate is not null', () => {
    component.candidate = mockCandidate;
    expect(component.candidate.id).toBe(1);
    expect(component.fullIntakeComplete).toBeTrue();
  });
});
