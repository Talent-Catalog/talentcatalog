/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import type {MockedObject} from "vitest";
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
import {NgbAccordionModule, NgbDatepickerModule, NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {ExamsComponent} from "../../../intake/exams/exams.component";
import {CitizenshipsComponent} from "../../../intake/citizenships/citizenships.component";
import {IntRecruitmentComponent} from "../../../intake/int-recruitment/int-recruitment.component";
import {RuralComponent} from "../../../intake/rural/rural.component";
import {ConfirmContactComponent} from "../../../intake/confirm-contact/confirm-contact.component";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {FixedInputComponent} from "../../../../util/intake/fixed-input/fixed-input.component";
import {PrintPageComponent} from "../../../../util/export-pdf/print-page.component";
import {AvailImmediateReason, CandidateCitizenship, CandidateDestination, CandidateExam, CandidateIntakeData, Exam, HasPassport, YesNo, YesNoUnsure} from "../../../../../model/candidate";
import {MockJob} from "../../../../../MockData/MockJob";
import {CandidateCitizenshipCardComponent} from "../../../intake/citizenships/card/candidate-citizenship-card.component";
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
import {ResidenceStatusComponent} from "../../../intake/residence-status/residence-status.component";
import {WorkPermitComponent} from "../../../intake/work-permit/work-permit.component";
import {WorkStatusComponent} from "../../../intake/work-status/work-status.component";
import {MilitaryServiceComponent} from "../../../intake/military-service/military-service.component";
import {FamilyComponent} from "../../../intake/family/family.component";
import {MaritalStatusComponent} from "../../../intake/marital-status/marital-status.component";
import {RegistrationUnhcrComponent} from "../../../intake/registration-unhcr/registration-unhcr.component";
import {HostChallengesComponent} from "../../../intake/host-challenges/host-challenges.component";
import {HomeLocationComponent} from "../../../intake/home-location/home-location.component";
import {ResettlementThirdComponent} from "../../../intake/resettlement-third/resettlement-third.component";
import {HostEntryComponent} from "../../../intake/host-entry/host-entry.component";
import {DatePickerComponent} from "../../../../util/date-picker/date-picker.component";
import {WorkAbroadComponent} from "../../../intake/work-abroad/work-abroad.component";
import {NclcScoreValidationComponent} from "../../../../util/nclc-score-validation/nclc-score-validation.component";
import {IeltsScoreValidationComponent} from "../../../../util/ielts-score-validation/ielts-score-validation.component";
import {DetScoreValidationComponent} from "../../../../util/det-score-validation/det-score-validation.component";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {DirectiveModule} from "../../../../../directives/directive.module";
import {MockUser} from "../../../../../MockData/MockUser";
import {expect, vi} from 'vitest';

const mockCitizenship: CandidateCitizenship = {
    id: 1,
    nationality: MockJob.country,
    hasPassport: HasPassport.ValidPassport,
    passportExp: '02/03/2028',
    notes: 'Note'
};
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
    country: MockJob.country,
    interest: YesNoUnsure.Yes,
    notes: 'SimpleNote'
};
export const mockCandidateIntakeData: CandidateIntakeData = {
    asylumYear: '2023',
    availImmediate: YesNo.Yes,
    availImmediateJobOps: 'Some job opportunities',
    availImmediateReason: AvailImmediateReason.Other,
    availImmediateNotes: 'Some notes',
    candidateCitizenships: [mockCitizenship],
    candidateExams: [mockCandidateExam],
    candidateDependants: [],
    candidateDestinations: [mockCandidateDestination]
    // Add more properties with predefined values here
};
describe('CandidateIntakeTabComponent', () => {
    let component: CandidateIntakeTabComponent;
    let fixture: ComponentFixture<CandidateIntakeTabComponent>;
    let candidateServiceSpy: MockedObject<CandidateService>;
    let countryServiceSpy: MockedObject<CountryService>;
    let educationLevelServiceSpy: MockedObject<EducationLevelService>;
    let occupationServiceSpy: MockedObject<OccupationService>;
    let languageLevelServiceSpy: MockedObject<LanguageLevelService>;
    let noteServiceSpy: MockedObject<CandidateNoteService>;
    let authServiceSpy: MockedObject<AuthenticationService>;
    let candidateCitizenshipServiceSpy: MockedObject<CandidateCitizenshipService>;
    let candidateExamServiceSpy: MockedObject<CandidateExamService>;
    let candidateDependantServiceSpy: MockedObject<CandidateDependantService>;
    let authorizationServiceSpy: MockedObject<AuthorizationService>;

    const mockCandidate = new MockCandidate();

    beforeEach(async () => {
        const candidateSpy = {
            get: vi.fn().mockName("CandidateService.get"),
            getIntakeData: vi.fn().mockName("CandidateService.getIntakeData"),
            candidateUpdated: vi.fn().mockName("CandidateService.candidateUpdated"),
            updateCandidate: vi.fn().mockName("CandidateService.updateCandidate")
        };
        const countrySpy = {
            isPalestine: vi.fn().mockName("CountryService.isPalestine"),
            listCountries: vi.fn().mockName("CountryService.listCountries"),
            listTCDestinations: vi.fn().mockName("CountryService.listTCDestinations")
        };
        const educationSpy = {
            listEducationLevels: vi.fn().mockName("EducationLevelService.listEducationLevels")
        };
        const occupationSpy = {
            listOccupations: vi.fn().mockName("OccupationService.listOccupations")
        };
        const languageSpy = {
            listLanguageLevels: vi.fn().mockName("LanguageLevelService.listLanguageLevels")
        };
        const noteSpy = {
            getIntakeData: vi.fn().mockName("CandidateNoteService.getIntakeData")
        };
        const authSpy = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        };
        const authorizationSpy = {
            isEditableCandidate: vi.fn().mockName("AuthorizationService.isEditableCandidate"),
            canAccessSalesforce: vi.fn().mockName("AuthorizationService.canAccessSalesforce")
        };
        candidateSpy.candidateUpdated.mockReturnValue(of(null));

        const citizenshipSpy = {
            create: vi.fn().mockName("CandidateCitizenshipService.create")
        };
        citizenshipSpy.create.mockReturnValue(of(null));

        const examSpy = {
            create: vi.fn().mockName("CandidateExamService.create")
        };
        examSpy.create.mockReturnValue(of(null));

        const dependantSpy = {
            create: vi.fn().mockName("CandidateDependantService.create")
        };
        dependantSpy.create.mockReturnValue(of(null));

        await TestBed.configureTestingModule({
            declarations: [CandidateIntakeTabComponent, ExamsComponent,
                CandidateCitizenshipCardComponent, CitizenshipsComponent, IntRecruitmentComponent,
                RuralComponent, ConfirmContactComponent,
                CrimeComponent, ConflictComponent, FinalAgreementComponent, CovidVaccinationComponent,
                CandidateExamCardComponent, LangAssessmentComponent, ViewCandidateLanguageComponent,
                DependantsComponent, ResidenceStatusComponent, WorkPermitComponent, WorkStatusComponent,
                DrivingLicenseComponent, VisaIssuesComponent, VisaRejectComponent,
                MilitaryServiceComponent, FamilyComponent, MaritalStatusComponent, RegistrationUnhcrComponent, HostChallengesComponent,
                HomeLocationComponent, ResettlementThirdComponent, HostEntryComponent,
                DatePickerComponent, WorkAbroadComponent, NclcScoreValidationComponent, IeltsScoreValidationComponent, DetScoreValidationComponent,
                AutosaveStatusComponent, FixedInputComponent, PrintPageComponent],
            imports: [NgbDatepickerModule, NgbTooltipModule, FormsModule, ReactiveFormsModule, NgSelectModule, NgbAccordionModule, DirectiveModule],
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
                { provide: AuthorizationService, useValue: authorizationSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CandidateIntakeTabComponent);
        component = fixture.componentInstance;

        // Initialize spies
        candidateServiceSpy = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
        countryServiceSpy = TestBed.inject(CountryService) as MockedObject<CountryService>;
        educationLevelServiceSpy = TestBed.inject(EducationLevelService) as MockedObject<EducationLevelService>;
        occupationServiceSpy = TestBed.inject(OccupationService) as MockedObject<OccupationService>;
        languageLevelServiceSpy = TestBed.inject(LanguageLevelService) as MockedObject<LanguageLevelService>;
        noteServiceSpy = TestBed.inject(CandidateNoteService) as MockedObject<CandidateNoteService>;
        authServiceSpy = TestBed.inject(AuthenticationService) as MockedObject<AuthenticationService>;
        candidateCitizenshipServiceSpy = TestBed.inject(CandidateCitizenshipService) as MockedObject<CandidateCitizenshipService>;
        candidateExamServiceSpy = TestBed.inject(CandidateExamService) as MockedObject<CandidateExamService>;
        candidateDependantServiceSpy = TestBed.inject(CandidateDependantService) as MockedObject<CandidateDependantService>;
        authorizationServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;

        // Mock candidate data
        component.candidate = mockCandidate;
        component.candidateIntakeData = mockCandidateIntakeData;
        candidateCitizenshipServiceSpy.create.mockReturnValue(of(mockCitizenship));
        authServiceSpy.getLoggedInUser.mockReturnValue(new MockUser());
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should return true when fullIntakeCompletedDate is not null', () => {
        component.candidate = mockCandidate;
        expect(component.candidate.id).toBe(1);
        expect(component.fullIntakeComplete).toBe(true);
    });
});
