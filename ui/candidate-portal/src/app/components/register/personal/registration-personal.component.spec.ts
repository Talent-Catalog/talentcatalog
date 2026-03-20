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

import {Component, EventEmitter, forwardRef, Input, NO_ERRORS_SCHEMA, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {
  ControlValueAccessor,
  FormsModule,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule
} from '@angular/forms';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationPersonalComponent} from './registration-personal.component';
import {Candidate} from '../../../model/candidate';
import {Country} from '../../../model/country';
import {CandidateService} from '../../../services/candidate.service';
import {CountryService} from '../../../services/country.service';
import {RegistrationService} from '../../../services/registration.service';
import {LanguageService} from '../../../services/language.service';
import {ExternalLinkService} from '../../../services/external-link.service';

@Component({
  selector: 'tc-input',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TcInputStubComponent),
    multi: true
  }]
})
class TcInputStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() type?: string;
  @Input() placeholder?: string;
  @Input() formControlName?: string;
  @Input() min?: number;

  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({
  selector: 'ng-select',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => NgSelectStubComponent),
    multi: true
  }]
})
class NgSelectStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() items?: unknown[];
  @Input() clearable?: boolean;
  @Input() searchable?: boolean;
  @Input() placeholder?: string;
  @Input() formControlName?: string;
  @Input() bindValue?: string;
  @Input() bindLabel?: string;
  @Input() multiple?: boolean | string;
  @Output() ngModelChange = new EventEmitter<unknown>();

  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

function makeCountry(id: number, name: string): Country {
  return {
    id,
    name,
    status: 'active',
    translatedName: name
  };
}

function makeCandidateResponse(overrides: Partial<Candidate> = {}): Candidate {
  return {
    user: {
      firstName: 'Jane',
      lastName: 'Doe'
    } as Candidate['user'],
    gender: 'female',
    dob: '1995-06-15',
    country: makeCountry(6405, 'Jordan'),
    city: 'Amman',
    state: 'Amman Governorate',
    yearOfArrival: 2020,
    nationality: makeCountry(6404, 'Syria'),
    candidateCitizenships: [],
    externalId: null,
    unhcrRegistered: 'No',
    unhcrNumber: null,
    unhcrConsent: null,
    candidateNumber: 'C12345',
    id: 1,
    acceptedPrivacyPolicyId: null,
    acceptedPrivacyPolicyDate: null,
    publicId: 'public-id',
    ...overrides
  } as Candidate;
}

describe('RegistrationPersonalComponent', () => {
  let component: RegistrationPersonalComponent;
  let fixture: ComponentFixture<RegistrationPersonalComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;
  let languageServiceSpy: jasmine.SpyObj<LanguageService>;
  let externalLinkServiceSpy: jasmine.SpyObj<ExternalLinkService>;

  async function configureAndCreate(options?: {
    usAfghan?: boolean;
    candidateResponse?: Partial<Candidate>;
    candidatePersonalError?: unknown;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', [
      'getCandidatePersonal',
      'setCandNumberStorage',
      'updateCandidatePersonal'
    ]);
    countryServiceSpy = jasmine.createSpyObj('CountryService', [
      'listCountries',
      'listStates'
    ]);
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next', 'back']);
    languageServiceSpy = jasmine.createSpyObj('LanguageService', [
      'getSelectedLanguage',
      'isUsAfghan'
    ]);
    externalLinkServiceSpy = jasmine.createSpyObj('ExternalLinkService', ['getLink']);

    if (options?.candidatePersonalError) {
      candidateServiceSpy.getCandidatePersonal.and.returnValue(
        throwError(options.candidatePersonalError)
      );
    } else {
      candidateServiceSpy.getCandidatePersonal.and.returnValue(of(
        makeCandidateResponse(options?.candidateResponse)
      ));
    }
    countryServiceSpy.listCountries.and.returnValue(of([
      makeCountry(6404, 'Syria'),
      makeCountry(6405, 'Jordan'),
      makeCountry(6178, 'United States'),
      makeCountry(6180, 'Afghanistan')
    ]));
    countryServiceSpy.listStates.and.returnValue(of(['State A', 'State B']));
    languageServiceSpy.getSelectedLanguage.and.returnValue('en');
    languageServiceSpy.isUsAfghan.and.returnValue(options?.usAfghan ?? false);
    externalLinkServiceSpy.getLink.and.returnValue('https://example.com/eligibility');

    await TestBed.configureTestingModule({
      declarations: [
        RegistrationPersonalComponent,
        TcInputStubComponent,
        NgSelectStubComponent
      ],
      imports: [
        FormsModule,
        ReactiveFormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: CountryService, useValue: countryServiceSpy},
        {provide: RegistrationService, useValue: registrationServiceSpy},
        {provide: LanguageService, useValue: languageServiceSpy},
        {provide: ExternalLinkService, useValue: externalLinkServiceSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationPersonalComponent);
    component = fixture.componentInstance;

    const translateService = TestBed.inject(TranslateService);
    translateService.use('en');

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    beforeEach(async () => configureAndCreate());

    it('should build the expected form controls', () => {
      expect(component.form.contains('firstName')).toBeTrue();
      expect(component.form.contains('lastName')).toBeTrue();
      expect(component.form.contains('gender')).toBeTrue();
      expect(component.form.contains('dob')).toBeTrue();
      expect(component.form.contains('countryId')).toBeTrue();
      expect(component.form.contains('state')).toBeTrue();
      expect(component.form.contains('city')).toBeTrue();
      expect(component.form.contains('yearOfArrival')).toBeTrue();
      expect(component.form.contains('nationalityId')).toBeTrue();
      expect(component.form.contains('otherNationality')).toBeTrue();
      expect(component.form.contains('otherNationalityIds')).toBeTrue();
      expect(component.form.contains('externalId')).toBeTrue();
      expect(component.form.contains('unhcrRegistered')).toBeTrue();
      expect(component.form.contains('unhcrNumber')).toBeTrue();
      expect(component.form.contains('unhcrConsent')).toBeTrue();
    });

    it('should load candidate personal details into the form', () => {
      expect(candidateServiceSpy.getCandidatePersonal).toHaveBeenCalled();
      expect(countryServiceSpy.listCountries).toHaveBeenCalled();
      expect(candidateServiceSpy.setCandNumberStorage).toHaveBeenCalledWith('C12345');

      expect(component.form.value.firstName).toBe('Jane');
      expect(component.form.value.lastName).toBe('Doe');
      expect(component.form.value.gender).toBe('female');
      expect(component.form.value.countryId).toBe(6405);
      expect(component.form.value.nationalityId).toBe(6404);
    });

    it('should set loading to false after initial data loads', () => {
      expect(component.loading).toBeFalse();
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-input components for the text fields', () => {
      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).toContain('firstName');
      expect(inputIds).toContain('lastName');
      expect(inputIds).toContain('city');
    });

    it('should render ng-select controls with the tc-select class', () => {
      const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));
      const selectIds = selectEls.map(debugEl => debugEl.componentInstance.id);

      expect(selectIds).toContain('gender');
      expect(selectIds).toContain('countryId');
      expect(selectIds).toContain('yearOfArrival');
      expect(selectIds).toContain('nationalityId');
      expect(selectIds).toContain('otherNationality');
      expect(selectIds).toContain('unhcrRegistered');

      selectEls.forEach(debugEl => {
        expect(debugEl.nativeElement.classList).toContain('tc-select');
      });
    });

    it('should render tc-label and tc-date-picker for the migrated fields', () => {
      const nativeElement = fixture.nativeElement as HTMLElement;

      expect(nativeElement.querySelector('tc-date-picker')).toBeTruthy();
      expect(nativeElement.querySelector('tc-label[for="firstName"]')).toBeTruthy();
      expect(nativeElement.querySelector('tc-label[for="lastName"]')).toBeTruthy();
      expect(nativeElement.querySelector('tc-label[for="countryId"]')).toBeTruthy();
    });
  });

  describe('conditional fields', () => {
    it('should show state as tc-input fallback when states are unavailable', async () => {
      await configureAndCreate();

      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);
      const selectIds = fixture.debugElement
        .queryAll(By.directive(NgSelectStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).toContain('stateAsText');
      expect(selectIds).not.toContain('state');
    });

    it('should show state as tc-select when states are available', async () => {
      await configureAndCreate();

      // The default fixture state has `states === null`, so the component initially
      // renders the text-input fallback rather than the state select.
      let selectIds = fixture.debugElement
        .queryAll(By.directive(NgSelectStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(selectIds).not.toContain('state');

      component.states = ['State A', 'State B'];
      fixture.detectChanges();

      selectIds = fixture.debugElement
        .queryAll(By.directive(NgSelectStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(selectIds).toContain('state');
    });

    it('should show the US-Afghan externalId tc-input when in US-Afghan mode', async () => {
      await configureAndCreate({
        usAfghan: true,
        candidateResponse: {
          user: {
            firstName: null,
            lastName: 'Doe'
          } as Candidate['user'],
          country: makeCountry(0, ''),
          nationality: makeCountry(0, ''),
          unhcrRegistered: null
        }
      });

      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).toContain('externalId');
      expect(component.form.value.countryId).toBe(RegistrationPersonalComponent.usaId);
      expect(component.form.value.nationalityId).toBe(RegistrationPersonalComponent.afghanistanId);
    });

    it('should show unhcr number input and consent select when registeredWithUnhcr is yes', async () => {
      await configureAndCreate();

      component.form.patchValue({unhcrRegistered: 'Yes'});
      fixture.detectChanges();

      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);
      const selectIds = fixture.debugElement
        .queryAll(By.directive(NgSelectStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).toContain('unhcrNumber');
      expect(selectIds).toContain('unhcrConsent');
    });

    it('should hide unhcr number and consent fields when unhcrRegistered is not Yes', async () => {
      await configureAndCreate();

      component.form.patchValue({unhcrRegistered: 'Yes'});
      fixture.detectChanges();

      component.form.patchValue({unhcrRegistered: 'No'});
      fixture.detectChanges();

      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);
      const selectIds = fixture.debugElement
        .queryAll(By.directive(NgSelectStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).not.toContain('unhcrNumber');
      expect(selectIds).not.toContain('unhcrConsent');
    });
  });

  describe('save (via next())', () => {
    beforeEach(async () => configureAndCreate());

    it('should not call updateCandidatePersonal if form is invalid', () => {
      component.form.controls['firstName'].setValue(null); // required field
      component.form.controls['firstName'].markAsDirty();

      component.next();

      expect(candidateServiceSpy.updateCandidatePersonal).not.toHaveBeenCalled();
    });

    it('should call registrationService.next() without an API call when form is pristine', () => {
      expect(component.form.pristine).toBeTrue();

      component.next();

      expect(candidateServiceSpy.updateCandidatePersonal).not.toHaveBeenCalled();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should call updateCandidatePersonal with form value when form is dirty and valid', () => {
      candidateServiceSpy.updateCandidatePersonal.and.returnValue(of({} as Candidate));

      component.form.controls['city'].setValue('Beirut');
      component.form.markAsDirty();

      component.next();

      expect(candidateServiceSpy.updateCandidatePersonal).toHaveBeenCalledWith(component.form.value);
    });

    it('should emit onSave and onPartnerAssignment, then call registrationService.next() on success', () => {
      candidateServiceSpy.updateCandidatePersonal.and.returnValue(of({} as Candidate));

      const onSaveSpy = spyOn(component.onSave, 'emit');
      const onPartnerSpy = spyOn(component.onPartnerAssignment, 'emit');

      component.form.controls['city'].setValue('Beirut');
      component.form.markAsDirty();

      component.next();

      expect(onSaveSpy).toHaveBeenCalled();
      expect(onPartnerSpy).toHaveBeenCalled();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should set saving to false and populate error on API failure', () => {
      const serverError = {status: 500, message: 'Internal Server Error'};
      candidateServiceSpy.updateCandidatePersonal.and.returnValue(
        throwError(serverError)
      );

      component.form.controls['city'].setValue('Beirut');
      component.form.markAsDirty();

      component.next();

      expect(component.saving).toBeFalse();
      expect(component.error).toEqual(serverError);
      expect(registrationServiceSpy.next).not.toHaveBeenCalled();
    });

    it('should not emit onSave or navigate on API failure', () => {
      candidateServiceSpy.updateCandidatePersonal.and.returnValue(
        throwError({status: 500})
      );

      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.form.controls['city'].setValue('Beirut');
      component.form.markAsDirty();

      component.next();

      expect(onSaveSpy).not.toHaveBeenCalled();
      expect(registrationServiceSpy.next).not.toHaveBeenCalled();
    });
  });

  describe('back()', () => {
    beforeEach(async () => configureAndCreate());

    it('should call registrationService.back() directly when form is pristine', () => {
      expect(component.form.pristine).toBeTrue();

      component.back();

      expect(registrationServiceSpy.back).toHaveBeenCalled();
      expect(candidateServiceSpy.updateCandidatePersonal).not.toHaveBeenCalled();
    });

    it('should call registrationService.back() directly when form is invalid', () => {
      component.form.controls['firstName'].setValue(null);
      component.form.controls['firstName'].markAsDirty();

      component.back();

      expect(registrationServiceSpy.back).toHaveBeenCalled();
      expect(candidateServiceSpy.updateCandidatePersonal).not.toHaveBeenCalled();
    });

    it('should save then call registrationService.back() when form is dirty and valid', () => {
      candidateServiceSpy.updateCandidatePersonal.and.returnValue(of({} as Candidate));

      component.form.controls['city'].setValue('Beirut');
      component.form.markAsDirty();

      component.back();

      expect(candidateServiceSpy.updateCandidatePersonal).toHaveBeenCalled();
      expect(registrationServiceSpy.back).toHaveBeenCalled();
    });

    it('should set error and not navigate if save-on-back API call fails', () => {
      const serverError = {status: 503, message: 'Service Unavailable'};
      candidateServiceSpy.updateCandidatePersonal.and.returnValue(
        throwError(serverError)
      );

      component.form.controls['city'].setValue('Beirut');
      component.form.markAsDirty();

      component.back();

      expect(component.error).toEqual(serverError);
      expect(registrationServiceSpy.back).not.toHaveBeenCalled();
    });
  });

  describe('getCandidatePersonal error', () => {
    it('should set error and stop loading if getCandidatePersonal fails', async () => {
      const serverError = {status: 404, message: 'Not Found'};
      await configureAndCreate({candidatePersonalError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });
  });

  describe('getEligibilityLink', () => {
    it('should delegate to externalLinkService with eligibility and current language', async () => {
      await configureAndCreate();

      const result = component.getEligibilityLink();

      expect(externalLinkServiceSpy.getLink).toHaveBeenCalledWith('eligibility', 'en');
      expect(result).toBe('https://example.com/eligibility');
    });
  });
});
