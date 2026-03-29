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

import {Component, EventEmitter, Input} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {By} from '@angular/platform-browser';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {ChangePasswordComponent} from '../../../../account/change-password/change-password.component';
import {CandidateProfileComponent} from './candidate-profile.component';
import {Candidate} from '../../../../../model/candidate';
import {Country} from '../../../../../model/country';
import {EducationMajor} from '../../../../../model/education-major';
import {Language} from '../../../../../model/language';
import {LanguageLevel} from '../../../../../model/language-level';
import {Occupation} from '../../../../../model/occupation';
import {SurveyType} from '../../../../../model/survey-type';
import {CandidateService} from '../../../../../services/candidate.service';
import {CountryService} from '../../../../../services/country.service';
import {EducationMajorService} from '../../../../../services/education-major.service';
import {LanguageLevelService} from '../../../../../services/language-level.service';
import {LanguageService} from '../../../../../services/language.service';
import {OccupationService} from '../../../../../services/occupation.service';
import {SurveyTypeService} from '../../../../../services/survey-type.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-error',
  template: ''
})
class ErrorStubComponent {
  @Input() error: unknown;
}

@Component({
  selector: 'tc-loading',
  template: ''
})
class TcLoadingStubComponent {
  @Input() loading: unknown;
}

@Component({
  selector: 'app-tab-header',
  template: '<ng-content></ng-content>'
})
class TabHeaderStubComponent {}

@Component({
  selector: 'app-download-cv',
  template: ''
})
class DownloadCvStubComponent {}

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() type?: string;
  @Input() color?: string;
  @Input() routerLink?: unknown;
}

@Component({
  selector: 'tc-description-list',
  template: '<ng-content></ng-content>'
})
class TcDescriptionListStubComponent {
  @Input() direction?: string;
  @Input() compact?: boolean;
  @Input() size?: string;
}

@Component({
  selector: 'tc-description-item',
  template: '<ng-content></ng-content>'
})
class TcDescriptionItemStubComponent {
  @Input() label?: string;
}

@Component({
  selector: 'app-candidate-occupation-card',
  template: ''
})
class CandidateOccupationCardStubComponent {
  @Input() candidateOccupation: unknown;
  @Input() occupations: unknown;
  @Input() preview?: boolean;
}

@Component({
  selector: 'app-candidate-job-experience-card',
  template: ''
})
class CandidateJobExperienceCardStubComponent {
  @Input() experience: unknown;
  @Input() countries: unknown;
  @Input() preview?: boolean;
}

@Component({
  selector: 'app-candidate-education-card',
  template: ''
})
class CandidateEducationCardStubComponent {
  @Input() candidateEducation: unknown;
  @Input() countries: unknown;
  @Input() majors: unknown;
  @Input() preview?: boolean;
}

@Component({
  selector: 'app-candidate-exam-card',
  template: ''
})
class CandidateExamCardStubComponent {
  @Input() exam: unknown;
  @Input() preview?: boolean;
}

@Component({
  selector: 'app-candidate-certification-card',
  template: ''
})
class CandidateCertificationCardStubComponent {
  @Input() certificate: unknown;
  @Input() preview?: boolean;
}

@Component({
  selector: 'app-candidate-language-card',
  template: ''
})
class CandidateLanguageCardStubComponent {
  @Input() language: unknown;
  @Input() languages: unknown;
  @Input() languageLevels: unknown;
  @Input() preview?: boolean;
}

@Component({
  selector: 'app-candidate-attachments',
  template: ''
})
class CandidateAttachmentsStubComponent {
  @Input() preview?: boolean;
}

function makeCountry(id: number, name: string): Country {
  return {
    id,
    name,
    status: 'active',
    translatedName: name
  };
}

function makeOccupation(id: number, name: string): Occupation {
  return {id, name};
}

function makeMajor(id: number, name: string): EducationMajor {
  return {id, name};
}

function makeLanguage(id: number, name: string): Language {
  return {id, name};
}

function makeLanguageLevel(id: number, name: string): LanguageLevel {
  return {id, name, level: id};
}

function makeSurveyType(id: number, name: string): SurveyType {
  return {id, name, status: 'active'} as SurveyType;
}

function makeCandidate(overrides: Partial<Candidate> = {}): Candidate {
  const jordan = makeCountry(1, 'Jordan');
  const lebanon = makeCountry(2, 'Lebanon');
  const surveyType = makeSurveyType(7, 'Community referral');

  return {
    id: 1,
    candidateNumber: 'TC-123',
    acceptedPrivacyPolicyId: '',
    acceptedPrivacyPolicyDate: '',
    publicId: 'public-1',
    status: 'active',
    allNotifications: true,
    gender: 'Female',
    dob: new Date('1994-01-01'),
    address1: '',
    city: 'Amman',
    state: 'Amman',
    country: jordan,
    yearOfArrival: 2019,
    nationality: lebanon,
    candidateCitizenships: [],
    phone: '+962700000000',
    whatsapp: '+962711111111',
    externalId: 'EXT-1',
    externalIdSource: '',
    partnerRef: '',
    unhcrRegistered: 'Yes' as any,
    unhcrNumber: 'UN-123',
    unhcrConsent: 'Yes' as any,
    unrwaRegistered: 'Unsure' as any,
    unrwaNumber: '',
    user: {
      id: 10,
      email: 'candidate@example.com',
      firstName: 'Amina',
      lastName: 'Saleh'
    } as any,
    candidateReviewStatusItems: [],
    migrationEducationMajor: null as any,
    additionalInfo: 'Additional context',
    linkedInLink: 'https://linkedin.com/in/example',
    candidateMessage: '',
    maxEducationLevel: null as any,
    folderlink: '',
    sflink: '',
    videolink: '',
    regoPartnerParam: '',
    regoReferrerParam: '',
    regoUtmCampaign: '',
    regoUtmContent: '',
    regoUtmMedium: '',
    regoUtmSource: '',
    regoUtmTerm: '',
    shareableCv: null as any,
    shareableDoc: null as any,
    listShareableCv: null as any,
    listShareableDoc: null as any,
    muted: false,
    changePassword: false,
    shareableNotes: '',
    surveyType,
    surveyComment: 'Survey comment',
    selected: false,
    createdDate: 0,
    updatedDate: 0,
    contextNote: '',
    maritalStatus: null as any,
    drivingLicense: null as any,
    unhcrStatus: null as any,
    ieltsScore: '',
    numberDependants: 0,
    englishAssessmentScoreIelts: '',
    frenchAssessmentScoreNclc: 0,
    candidateExams: [{id: 1}] as any,
    candidateAttachments: [],
    taskAssignments: [],
    candidateOpportunities: [],
    candidateProperties: [],
    mediaWillingness: '',
    relocatedAddress: '',
    relocatedCity: '',
    relocatedState: '',
    relocatedCountry: null as any,
    candidateCertifications: [{id: 1}] as any,
    candidateEducations: [{id: 1}] as any,
    candidateJobExperiences: [{id: 1}] as any,
    candidateLanguages: [{id: 1}] as any,
    candidateOccupations: [{id: 1}] as any,
    candidateDestinations: [{
      id: 1,
      country: lebanon,
      interest: 'High',
      notes: 'Can relocate'
    }] as any,
    ...overrides
  };
}

describe('CandidateProfileComponent', () => {
  let component: CandidateProfileComponent;
  let fixture: ComponentFixture<CandidateProfileComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let occupationServiceSpy: jasmine.SpyObj<OccupationService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let educationMajorServiceSpy: jasmine.SpyObj<EducationMajorService>;
  let languageServiceSpy: jasmine.SpyObj<LanguageService>;
  let languageLevelServiceSpy: jasmine.SpyObj<LanguageLevelService>;
  let surveyTypeServiceSpy: jasmine.SpyObj<SurveyTypeService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let onLangChange$: EventEmitter<any>;

  async function configureAndCreate(options?: {
    candidate?: Candidate;
    usAfghan?: boolean;
    queryParams?: Record<string, unknown>;
    occupationsError?: unknown;
    countriesError?: unknown;
    majorsError?: unknown;
    languagesError?: unknown;
    languageLevelsError?: unknown;
    surveyTypesError?: unknown;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['getProfile']);
    occupationServiceSpy = jasmine.createSpyObj('OccupationService', ['listOccupations']);
    countryServiceSpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    educationMajorServiceSpy = jasmine.createSpyObj('EducationMajorService', ['listMajors']);
    languageServiceSpy = jasmine.createSpyObj('LanguageService', ['listLanguages', 'changeLanguage']);
    languageLevelServiceSpy = jasmine.createSpyObj('LanguageLevelService', ['listLanguageLevels']);
    surveyTypeServiceSpy = jasmine.createSpyObj('SurveyTypeService', ['listActiveSurveyTypes']);
    modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);
    onLangChange$ = new EventEmitter<any>();

    occupationServiceSpy.listOccupations.and.returnValue(
      options?.occupationsError ? throwError(options.occupationsError) : of([makeOccupation(1, 'Engineer')])
    );
    countryServiceSpy.listCountries.and.returnValue(
      options?.countriesError ? throwError(options.countriesError) : of([makeCountry(1, 'Jordan'), makeCountry(2, 'Lebanon')])
    );
    educationMajorServiceSpy.listMajors.and.returnValue(
      options?.majorsError ? throwError(options.majorsError) : of([makeMajor(10, 'Engineering')])
    );
    languageServiceSpy.listLanguages.and.returnValue(
      options?.languagesError ? throwError(options.languagesError) : of([makeLanguage(1, 'Arabic')])
    );
    languageLevelServiceSpy.listLanguageLevels.and.returnValue(
      options?.languageLevelsError ? throwError(options.languageLevelsError) : of([makeLanguageLevel(1, 'Advanced')])
    );
    surveyTypeServiceSpy.listActiveSurveyTypes.and.returnValue(
      options?.surveyTypesError ? throwError(options.surveyTypesError) : of([makeSurveyType(7, 'Community referral')])
    );

    await TestBed.configureTestingModule({
      declarations: [
        CandidateProfileComponent,
        ErrorStubComponent,
        TcLoadingStubComponent,
        TabHeaderStubComponent,
        DownloadCvStubComponent,
        TcButtonStubComponent,
        TcDescriptionListStubComponent,
        TcDescriptionItemStubComponent,
        CandidateOccupationCardStubComponent,
        CandidateJobExperienceCardStubComponent,
        CandidateEducationCardStubComponent,
        CandidateExamCardStubComponent,
        CandidateCertificationCardStubComponent,
        CandidateLanguageCardStubComponent,
        CandidateAttachmentsStubComponent
      ],
      imports: [TranslateModule.forRoot()],
      providers: [
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: OccupationService, useValue: occupationServiceSpy},
        {provide: CountryService, useValue: countryServiceSpy},
        {provide: EducationMajorService, useValue: educationMajorServiceSpy},
        {provide: LanguageService, useValue: languageServiceSpy},
        {provide: LanguageLevelService, useValue: languageLevelServiceSpy},
        {provide: SurveyTypeService, useValue: surveyTypeServiceSpy},
        {
          provide: ActivatedRoute,
          useValue: {snapshot: {queryParams: options?.queryParams ?? {}}}
        },
        {provide: NgbModal, useValue: modalServiceSpy}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateProfileComponent);
    component = fixture.componentInstance;
    component.candidate = options?.candidate ?? makeCandidate();
    component.usAfghan = options?.usAfghan ?? false;

    const translateService = TestBed.inject(TranslateService);
    Object.defineProperty(translateService, 'onLangChange', {
      value: onLangChange$
    });

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();

    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should load dropdown data on init', async () => {
      await configureAndCreate();

      expect(occupationServiceSpy.listOccupations).toHaveBeenCalled();
      expect(countryServiceSpy.listCountries).toHaveBeenCalled();
      expect(educationMajorServiceSpy.listMajors).toHaveBeenCalled();
      expect(languageServiceSpy.listLanguages).toHaveBeenCalled();
      expect(languageLevelServiceSpy.listLanguageLevels).toHaveBeenCalled();
      expect(surveyTypeServiceSpy.listActiveSurveyTypes).toHaveBeenCalled();
    });

    it('should reload dropdown data when language changes', async () => {
      await configureAndCreate();
      occupationServiceSpy.listOccupations.calls.reset();

      onLangChange$.next({lang: 'ar'});

      expect(occupationServiceSpy.listOccupations).toHaveBeenCalled();
    });
  });

  describe('template tc components', () => {
    it('should render tc-loading and the profile action buttons', async () => {
      await configureAndCreate();

      expect(fixture.debugElement.query(By.directive(TcLoadingStubComponent))).toBeTruthy();

      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));
      expect(buttons.length).toBeGreaterThan(0);
      expect(buttons[0].componentInstance.type).toBe('outline');
      expect(buttons[0].componentInstance.color).toBe('primary');
    });

    it('should render the edit buttons with the expected profile routes', async () => {
      await configureAndCreate();

      const routedButtons = fixture.debugElement
        .queryAll(By.directive(TcButtonStubComponent))
        .map(debugEl => debugEl.componentInstance.routerLink)
        .filter(routerLink => !!routerLink)
        .map(routerLink => JSON.stringify(routerLink));

      expect(routedButtons).toContain(JSON.stringify(['edit', 'contact']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'personal']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'occupations']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'experience']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'education']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'exams']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'certifications']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'languages']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'destinations']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'additional']));
      expect(routedButtons).toContain(JSON.stringify(['edit', 'upload']));
    });

    it('should render description lists for the migrated read-only sections', async () => {
      await configureAndCreate();

      const descriptionLists = fixture.debugElement.queryAll(By.directive(TcDescriptionListStubComponent));
      const descriptionItems = fixture.debugElement.queryAll(By.directive(TcDescriptionItemStubComponent));

      expect(descriptionLists.length).toBeGreaterThan(0);
      expect(descriptionItems.length).toBeGreaterThan(0);
      expect(descriptionLists[0].componentInstance.direction).toBe('column');
      expect(descriptionLists[0].componentInstance.size).toBe('lg');
    });

    it('should render the profile child cards in preview mode', async () => {
      await configureAndCreate();

      expect(fixture.debugElement.query(By.directive(CandidateOccupationCardStubComponent)).componentInstance.preview).toBeTrue();
      expect(fixture.debugElement.query(By.directive(CandidateJobExperienceCardStubComponent)).componentInstance.preview).toBeTrue();
      expect(fixture.debugElement.query(By.directive(CandidateEducationCardStubComponent)).componentInstance.preview).toBeTrue();
      expect(fixture.debugElement.query(By.directive(CandidateExamCardStubComponent)).componentInstance.preview).toBeTrue();
      expect(fixture.debugElement.query(By.directive(CandidateCertificationCardStubComponent)).componentInstance.preview).toBeTrue();
      expect(fixture.debugElement.query(By.directive(CandidateLanguageCardStubComponent)).componentInstance.preview).toBeTrue();
      expect(fixture.debugElement.query(By.directive(CandidateAttachmentsStubComponent)).componentInstance.preview).toBeTrue();
    });

    it('should show relocated address details when available', async () => {
      await configureAndCreate({
        candidate: makeCandidate({
          relocatedAddress: '123 Street',
          relocatedCity: 'Amman',
          relocatedState: 'Amman',
          relocatedCountry: makeCountry(3, 'Canada')
        })
      });

      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('123 Street');
      expect(text).toContain('Canada');
    });
  });

  describe('helpers', () => {
    beforeEach(async () => configureAndCreate());

    it('should return the matching country name', () => {
      expect(component.getCountryName(makeCountry(1, 'Ignored'))).toBe('Jordan');
    });

    it('should return the matching survey type name', () => {
      // The default candidate fixture uses surveyType.id = 7, which matches the default survey-type spy data.
      expect(component.getSurveyTypeName()).toBe('Community referral');
    });

    it('should return false from showRelocatedAddress when no relocated data exists', () => {
      component.candidate = makeCandidate({
        relocatedAddress: '',
        relocatedCity: '',
        relocatedState: '',
        relocatedCountry: null as any,
        candidateOpportunities: []
      });

      expect(component.showRelocatedAddress()).toBeFalse();
    });

    it('should return true from showRelocatedAddress when an opportunity is at acceptance or later', () => {
      component.candidate = makeCandidate({
        candidateOpportunities: [{
          lastActiveStage: 'acceptance'
        }] as any
      });

      expect(component.showRelocatedAddress()).toBeTrue();
    });
  });

  describe('change password', () => {
    beforeEach(async () => configureAndCreate());

    it('should open the change password modal', () => {
      component.openChangePasswordModal();

      expect(modalServiceSpy.open).toHaveBeenCalledWith(ChangePasswordComponent, {centered: true});
    });
  });

  describe('error paths', () => {
    it('should set error when occupations fail to load', async () => {
      const serverError = {status: 500};
      await configureAndCreate({occupationsError: serverError});

      expect(component.error).toEqual(serverError);
    });

    it('should set error when countries fail to load', async () => {
      const serverError = {status: 503};
      await configureAndCreate({countriesError: serverError});

      expect(component.error).toEqual(serverError);
    });

    it('should set error when majors fail to load', async () => {
      const serverError = {status: 502};
      await configureAndCreate({majorsError: serverError});

      expect(component.error).toEqual(serverError);
    });

    it('should set error when languages fail to load', async () => {
      const serverError = {status: 504};
      await configureAndCreate({languagesError: serverError});

      expect(component.error).toEqual(serverError);
    });

    it('should set error when language levels fail to load', async () => {
      const serverError = {status: 501};
      await configureAndCreate({languageLevelsError: serverError});

      expect(component.error).toEqual(serverError);
    });

    it('should set error when survey types fail to load', async () => {
      const serverError = {status: 400};
      await configureAndCreate({surveyTypesError: serverError});

      expect(component.error).toEqual(serverError);
    });
  });
});
