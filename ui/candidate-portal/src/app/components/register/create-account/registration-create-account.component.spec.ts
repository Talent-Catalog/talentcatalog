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

import {Component, forwardRef, NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, convertToParamMap} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationCreateAccountComponent} from './registration-create-account.component';
import {BrandingService} from '../../../services/branding.service';
import {CandidateService} from '../../../services/candidate.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {RegistrationService} from '../../../services/registration.service';
import {LanguageService} from '../../../services/language.service';
import {Candidate} from '../../../model/candidate';
import {US_AFGHAN_SURVEY_TYPE} from '../../../model/survey-type';

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
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

// ── helpers ──────────────────────────────────────────────────────────────────

function makeActivatedRoute(queryParams: Record<string, string> = {}) {
  return {
    snapshot: {
      queryParams,
      queryParamMap: convertToParamMap(queryParams)
    }
  };
}

// ── suite ────────────────────────────────────────────────────────────────────

describe('RegistrationCreateAccountComponent', () => {
  let component: RegistrationCreateAccountComponent;
  let fixture: ComponentFixture<RegistrationCreateAccountComponent>;

  let brandingServiceSpy: jasmine.SpyObj<BrandingService>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;
  let languageServiceSpy: jasmine.SpyObj<LanguageService>;

  // Configures TestBed and creates the fixture.
  // queryParams lets individual tests inject route params without re-configuring the whole module.
  async function configureAndCreate(queryParams: Record<string, string> = {}, authenticated = false) {
    brandingServiceSpy = jasmine.createSpyObj('BrandingService', ['getBrandingInfo'], {partnerAbbreviation: 'tc'});
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateCandidateSurvey']);
    authenticationServiceSpy = jasmine.createSpyObj('AuthenticationService', ['isAuthenticated', 'register']);
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next']);
    languageServiceSpy = jasmine.createSpyObj('LanguageService', ['setUsAfghan']);

    brandingServiceSpy.getBrandingInfo.and.returnValue(of({
      logo: 'assets/images/tc-logo-2.png',
      partnerName: 'Talent Catalog',
      websiteUrl: ''
    }));
    authenticationServiceSpy.isAuthenticated.and.returnValue(authenticated);

    await TestBed.configureTestingModule({
      declarations: [RegistrationCreateAccountComponent, TcInputStubComponent],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: BrandingService, useValue: brandingServiceSpy},
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: AuthenticationService, useValue: authenticationServiceSpy},
        {provide: RegistrationService, useValue: registrationServiceSpy},
        {provide: LanguageService, useValue: languageServiceSpy},
        {provide: ActivatedRoute, useValue: makeActivatedRoute(queryParams)}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationCreateAccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  // ── basic creation ──────────────────────────────────────────────────────

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  // ── ngOnInit – unauthenticated ──────────────────────────────────────────

  describe('ngOnInit (unauthenticated)', () => {
    beforeEach(async () => configureAndCreate());

    it('should set authenticated to false', () => {
      expect(component.authenticated).toBeFalse();
    });

    it('should set loading to false', () => {
      expect(component.loading).toBeFalse();
    });

    it('should build form with username, password and passwordConfirmation controls', () => {
      expect(component.registrationForm.contains('username')).toBeTrue();
      expect(component.registrationForm.contains('password')).toBeTrue();
      expect(component.registrationForm.contains('passwordConfirmation')).toBeTrue();
    });

    it('should add contactConsentRegistration control', () => {
      expect(component.registrationForm.contains('contactConsentRegistration')).toBeTrue();
    });

    it('should add contactConsentPartners control', () => {
      expect(component.registrationForm.contains('contactConsentPartners')).toBeTrue();
    });

    it('should call getBrandingInfo to retrieve partner name', () => {
      expect(brandingServiceSpy.getBrandingInfo).toHaveBeenCalled();
      expect(component.partnerName).toBe('Talent Catalog');
    });

    it('should NOT call registrationService.next()', () => {
      expect(registrationServiceSpy.next).not.toHaveBeenCalled();
    });

    it('should set usAfghan to false when source param is absent', () => {
      expect(component.usAfghan).toBeFalse();
    });
  });

  // ── ngOnInit – us-afghan query param ───────────────────────────────────

  describe('ngOnInit with source=us-afghan', () => {
    beforeEach(async () => configureAndCreate({source: 'us-afghan'}));

    it('should set usAfghan to true', () => {
      expect(component.usAfghan).toBeTrue();
    });

    it('should call languageService.setUsAfghan(true)', () => {
      expect(languageServiceSpy.setUsAfghan).toHaveBeenCalledWith(true);
    });
  });

  // ── ngOnInit – authenticated ────────────────────────────────────────────

  describe('ngOnInit (authenticated)', () => {
    beforeEach(async () => configureAndCreate({}, true));

    it('should set authenticated to true', () => {
      expect(component.authenticated).toBeTrue();
    });

    it('should call registrationService.next() to skip this step', () => {
      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should NOT call getBrandingInfo', () => {
      expect(brandingServiceSpy.getBrandingInfo).not.toHaveBeenCalled();
    });
  });

  // ── form value accessors ────────────────────────────────────────────────

  describe('form value accessors', () => {
    beforeEach(async () => configureAndCreate());

    it('username getter should reflect the current form value', () => {
      component.registrationForm.patchValue({username: 'user@example.com'});
      expect(component.username).toBe('user@example.com');
    });

    it('password getter should reflect the current form value', () => {
      component.registrationForm.patchValue({password: 'secret123'});
      expect(component.password).toBe('secret123');
    });

    it('passwordConfirmation getter should reflect the current form value', () => {
      component.registrationForm.patchValue({passwordConfirmation: 'secret123'});
      expect(component.passwordConfirmation).toBe('secret123');
    });
  });

  // ── form validation ─────────────────────────────────────────────────────

  describe('form validation', () => {
    beforeEach(async () => configureAndCreate());

    it('should be invalid when all fields are empty', () => {
      expect(component.registrationForm.invalid).toBeTrue();
    });

    it('should be invalid when username is missing', () => {
      component.registrationForm.patchValue({
        username: '',
        password: 'password1',
        passwordConfirmation: 'password1',
        contactConsentRegistration: true
      });
      expect(component.registrationForm.invalid).toBeTrue();
    });

    it('should be invalid when password is missing', () => {
      component.registrationForm.patchValue({
        username: 'user@example.com',
        password: '',
        passwordConfirmation: 'password1',
        contactConsentRegistration: true
      });
      expect(component.registrationForm.invalid).toBeTrue();
    });

    it('should be invalid when contactConsentRegistration is false', () => {
      component.registrationForm.patchValue({
        username: 'user@example.com',
        password: 'password1',
        passwordConfirmation: 'password1',
        contactConsentRegistration: false
      });
      expect(component.registrationForm.invalid).toBeTrue();
    });

    it('should be valid when all required fields are filled and consent given', () => {
      component.registrationForm.patchValue({
        username: 'user@example.com',
        password: 'password1',
        passwordConfirmation: 'password1',
        contactConsentRegistration: true
      });
      expect(component.registrationForm.valid).toBeTrue();
    });
  });

  // ── register() – success, non-US-Afghan ────────────────────────────────

  describe('register() success (non-US-Afghan)', () => {
    beforeEach(async () => configureAndCreate());

    it('should call authenticationService.register with correct fields', () => {
      authenticationServiceSpy.register.and.returnValue(of(void 0));
      component.registrationForm.patchValue({
        username: 'user@example.com',
        password: 'password1',
        passwordConfirmation: 'password1',
        contactConsentRegistration: true,
        contactConsentPartners: false
      });
      component.register();
      const call = authenticationServiceSpy.register.calls.mostRecent().args[0];
      expect(call.username).toBe('user@example.com');
      expect(call.email).toBe('user@example.com');
      expect(call.password).toBe('password1');
      expect(call.passwordConfirmation).toBe('password1');
      expect(call.partnerAbbreviation).toBe('tc');
    });

    it('should call registrationService.next() on success', () => {
      authenticationServiceSpy.register.and.returnValue(of(void 0));
      component.register();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should NOT call updateCandidateSurvey when not US-Afghan', () => {
      authenticationServiceSpy.register.and.returnValue(of(void 0));
      component.register();
      expect(candidateServiceSpy.updateCandidateSurvey).not.toHaveBeenCalled();
    });
  });

  // ── register() – success, US-Afghan ────────────────────────────────────

  describe('register() success (US-Afghan)', () => {
    beforeEach(async () => configureAndCreate({source: 'us-afghan'}));

    it('should call updateCandidateSurvey with the US-Afghan survey type', () => {
      authenticationServiceSpy.register.and.returnValue(of(void 0));
      candidateServiceSpy.updateCandidateSurvey.and.returnValue(of({} as Candidate));
      component.register();
      expect(candidateServiceSpy.updateCandidateSurvey).toHaveBeenCalledWith({
        surveyTypeId: US_AFGHAN_SURVEY_TYPE
      });
    });

    it('should clear saving flag after survey update succeeds', () => {
      authenticationServiceSpy.register.and.returnValue(of(void 0));
      candidateServiceSpy.updateCandidateSurvey.and.returnValue(of({} as Candidate));
      component.register();
      expect(component.saving).toBeFalse();
    });

    it('should set error and clear saving when survey update fails', () => {
      const err = {message: 'Survey failed'};
      authenticationServiceSpy.register.and.returnValue(of(void 0));
      candidateServiceSpy.updateCandidateSurvey.and.returnValue(throwError(err));
      component.register();
      expect(component.error).toEqual(err);
      expect(component.saving).toBeFalse();
    });
  });

  // ── register() – failure ────────────────────────────────────────────────

  describe('register() failure', () => {
    beforeEach(async () => configureAndCreate());

    it('should set error on registration failure', () => {
      const err = {message: 'Registration failed'};
      authenticationServiceSpy.register.and.returnValue(throwError(err));
      component.register();
      expect(component.error).toEqual(err);
    });

    it('should clear saving flag on registration failure', () => {
      authenticationServiceSpy.register.and.returnValue(throwError({message: 'error'}));
      component.register();
      expect(component.saving).toBeFalse();
    });

    it('should NOT call registrationService.next() on failure', () => {
      authenticationServiceSpy.register.and.returnValue(throwError({message: 'error'}));
      component.register();
      expect(registrationServiceSpy.next).not.toHaveBeenCalled();
    });
  });

  // ── UTM / referrer query params ─────────────────────────────────────────

  describe('register() with UTM query params', () => {
    beforeEach(async () => configureAndCreate({
      r: 'ref123',
      utm_source: 'google',
      utm_campaign: 'spring',
      utm_medium: 'email',
      utm_content: 'banner',
    }));

    it('should map query params onto the registration request', () => {
      authenticationServiceSpy.register.and.returnValue(of(void 0));
      component.register();
      const req = authenticationServiceSpy.register.calls.mostRecent().args[0];
      expect(req.referrerParam).toBe('ref123');
      expect(req.utmSource).toBe('google');
      expect(req.utmCampaign).toBe('spring');
      expect(req.utmMedium).toBe('email');
      expect(req.utmContent).toBe('banner');
    });
  });
});
