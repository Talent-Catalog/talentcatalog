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

import {Component, forwardRef, Input, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {BehaviorSubject, of, throwError} from 'rxjs';

import {Candidate, CandidateStatus} from '../../../model/candidate';
import {TermsInfoDto} from '../../../model/terms-info-dto';
import {AuthenticationService} from '../../../services/authentication.service';
import {CandidateService} from '../../../services/candidate.service';
import {TermsInfoService} from '../../../services/terms-info.service';
import {ChangePasswordComponent} from '../change-password/change-password.component';
import {LoginComponent} from './login.component';

@Pipe({
  name: 'translate'
})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({
  selector: 'app-error',
  template: ''
})
class AppErrorStubComponent {
  @Input() error: any;
}

@Component({
  selector: 'tc-field',
  template: '<ng-content></ng-content>'
})
class TcFieldStubComponent {
}

@Component({
  selector: 'tc-label',
  template: '<ng-content></ng-content>'
})
class TcLabelStubComponent {
  @Input() for: string;
}

@Component({
  selector: 'tc-input',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TcInputStubComponent),
      multi: true
    }
  ]
})
class TcInputStubComponent implements ControlValueAccessor {
  @Input() id: string;
  @Input() type: string;
  @Input() invalid: boolean;

  writeValue(): void {
  }

  registerOnChange(): void {
  }

  registerOnTouched(): void {
  }

  setDisabledState(): void {
  }
}

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() disabled: boolean;
}

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  let authenticationServiceSpy:
    jasmine.SpyObj<AuthenticationService>;
  let candidateServiceSpy:
    jasmine.SpyObj<CandidateService>;
  let termsInfoServiceSpy:
    jasmine.SpyObj<TermsInfoService>;
  let modalServiceSpy:
    jasmine.SpyObj<NgbModal>;
  let routerNavigateSpy: jasmine.Spy;

  let queryParams$: BehaviorSubject<Record<string, string>>;

  const defaultCandidate = createCandidate();
  const defaultPolicy = createPolicy();

  beforeEach(async () => {
    queryParams$ = new BehaviorSubject<Record<string, string>>({});

    authenticationServiceSpy =
      jasmine.createSpyObj<AuthenticationService>(
        'AuthenticationService',
        [
          'login',
          'setCandidateStatus'
        ]
      );

    candidateServiceSpy =
      jasmine.createSpyObj<CandidateService>(
        'CandidateService',
        [
          'getCandidatePersonal',
          'setCandNumberStorage'
        ]
      );

    termsInfoServiceSpy =
      jasmine.createSpyObj<TermsInfoService>(
        'TermsInfoService',
        ['getCurrentCandidatePolicy']
      );

    modalServiceSpy =
      jasmine.createSpyObj<NgbModal>(
        'NgbModal',
        ['open']
      );

    authenticationServiceSpy.login.and.returnValue(
      of(null)
    );

    candidateServiceSpy.getCandidatePersonal.and.returnValue(
      of(defaultCandidate)
    );

    termsInfoServiceSpy.getCurrentCandidatePolicy.and.returnValue(
      of(defaultPolicy)
    );

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        RouterTestingModule
      ],
      declarations: [
        LoginComponent,
        TranslatePipeStub,
        AppErrorStubComponent,
        TcFieldStubComponent,
        TcLabelStubComponent,
        TcInputStubComponent,
        TcButtonStubComponent
      ],
      providers: [
        {
          provide: AuthenticationService,
          useValue: authenticationServiceSpy
        },
        {
          provide: CandidateService,
          useValue: candidateServiceSpy
        },
        {
          provide: TermsInfoService,
          useValue: termsInfoServiceSpy
        },
        {
          provide: NgbModal,
          useValue: modalServiceSpy
        },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: queryParams$.asObservable(),
            snapshot: {
              queryParams: {}
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;

    const router = TestBed.inject(Router);

    routerNavigateSpy = spyOn(
      router,
      'navigateByUrl'
    ).and.returnValue(
      Promise.resolve(true)
    );

    fixture.detectChanges();
  });

  // Existing test retained.
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Existing test retained.
  it('should render the tc form fields and disable submit while the form is invalid', () => {
    const fields = fixture.debugElement.queryAll(
      By.directive(TcFieldStubComponent)
    );

    const button = fixture.debugElement.query(
      By.directive(TcButtonStubComponent)
    ).componentInstance as TcButtonStubComponent;

    expect(fields.length).toBe(2);
    expect(button.disabled).toBeTrue();
  });

  // Existing test retained.
  it('should enable submit once both login fields are populated', () => {
    component.loginForm.patchValue({
      username: 'candidate@example.com',
      password: 'password123'
    });

    fixture.detectChanges();

    const button = fixture.debugElement.query(
      By.directive(TcButtonStubComponent)
    ).componentInstance as TcButtonStubComponent;

    const inputs = fixture.debugElement.queryAll(
      By.directive(TcInputStubComponent)
    ).map(
      debugElement =>
        debugElement.componentInstance as TcInputStubComponent
    );

    expect(button.disabled).toBeFalse();
    expect(inputs[0].invalid).toBeFalse();
    expect(inputs[1].invalid).toBeFalse();
  });

  it('should initialize the login form', () => {
    expect(component.loginForm).toBeTruthy();

    expect(component.loginForm.controls.username.value)
    .toBe('');

    expect(component.loginForm.controls.password.value)
    .toBe('');

    expect(component.loginForm.invalid).toBeTrue();
  });

  it('should use home as the default return URL', () => {
    expect(component.returnUrl).toBe('/home');
  });

  it('should use the returnUrl query parameter', () => {
    queryParams$.next({
      returnUrl: '/profile'
    });

    expect(component.returnUrl).toBe('/profile');
  });

  it('should return username and password through the getters', () => {
    component.loginForm.patchValue({
      username: 'candidate@example.com',
      password: 'password123'
    });

    expect(component.username)
    .toBe('candidate@example.com');

    expect(component.password)
    .toBe('password123');
  });

  it('should clear the existing error before attempting login', () => {
    component.error = 'Previous error';

    component.login();

    expect(component.error).toBeNull();
  });

  it('should not authenticate when the form is invalid', () => {
    component.login();

    expect(authenticationServiceSpy.login)
    .not.toHaveBeenCalled();

    expect(component.loading).toBeFalsy();
  });

  it('should not submit another login while already loading', () => {
    populateValidLoginForm();

    component.loading = true;
    component.login();

    expect(authenticationServiceSpy.login)
    .not.toHaveBeenCalled();

    expect(component.loading).toBeTrue();
  });

  it('should send the username, password and null reCAPTCHA token', () => {
    populateValidLoginForm();

    component.login();

    expect(authenticationServiceSpy.login)
    .toHaveBeenCalledTimes(1);

    const request =
      authenticationServiceSpy.login.calls
      .mostRecent().args[0];

    expect(request.username)
    .toBe('candidate@example.com');

    expect(request.password)
    .toBe('password123');

    expect(request.reCaptchaV3Token)
    .toBeNull();
  });

  it('should fetch candidate and policy after authentication', () => {
    populateValidLoginForm();

    component.login();

    expect(
      termsInfoServiceSpy.getCurrentCandidatePolicy
    ).toHaveBeenCalledTimes(1);

    expect(
      candidateServiceSpy.getCandidatePersonal
    ).toHaveBeenCalledTimes(1);

    expect(component.loading).toBeFalse();
  });

  it('should configure candidate storage and status after login', () => {
    populateValidLoginForm();

    component.login();

    expect(
      candidateServiceSpy.setCandNumberStorage
    ).toHaveBeenCalledOnceWith('123');

    expect(
      authenticationServiceSpy.setCandidateStatus
    ).toHaveBeenCalledOnceWith(
      CandidateStatus.active
    );
  });

  it('should navigate to the requested return URL when policy is accepted', () => {
    const candidate = createCandidate({
      acceptedPrivacyPolicyId: 'policy-1'
    });

    const policy = createPolicy({
      id: 'policy-1',
      content: 'Current privacy policy'
    });

    candidateServiceSpy.getCandidatePersonal.and.returnValue(
      of(candidate)
    );

    termsInfoServiceSpy.getCurrentCandidatePolicy.and.returnValue(
      of(policy)
    );

    component.returnUrl = '/profile';
    populateValidLoginForm();

    component.login();

    expect(routerNavigateSpy)
    .toHaveBeenCalledOnceWith('/profile');

    expect(modalServiceSpy.open)
    .not.toHaveBeenCalled();
  });

  it('should navigate to home when the current policy has not been accepted', () => {
    const candidate = createCandidate({
      acceptedPrivacyPolicyId: 'old-policy',
      changePassword: true
    });

    const policy = createPolicy({
      id: 'new-policy',
      content: 'New privacy policy'
    });

    candidateServiceSpy.getCandidatePersonal.and.returnValue(
      of(candidate)
    );

    termsInfoServiceSpy.getCurrentCandidatePolicy.and.returnValue(
      of(policy)
    );

    component.returnUrl = '/profile';
    populateValidLoginForm();

    component.login();

    expect(routerNavigateSpy)
    .toHaveBeenCalledOnceWith('/home');

    expect(modalServiceSpy.open)
    .not.toHaveBeenCalled();
  });

  it('should use the return URL when no policy content exists', () => {
    const policy = createPolicy({
      id: 'different-policy',
      content: ''
    });

    termsInfoServiceSpy.getCurrentCandidatePolicy.and.returnValue(
      of(policy)
    );

    component.returnUrl = '/profile';
    populateValidLoginForm();

    component.login();

    expect(routerNavigateSpy)
    .toHaveBeenCalledOnceWith('/profile');
  });

  it('should open change-password modal when required', () => {
    const candidate = createCandidate({
      changePassword: true,
      acceptedPrivacyPolicyId: 'policy-1'
    });

    const policy = createPolicy({
      id: 'policy-1',
      content: 'Current privacy policy'
    });

    candidateServiceSpy.getCandidatePersonal.and.returnValue(
      of(candidate)
    );

    termsInfoServiceSpy.getCurrentCandidatePolicy.and.returnValue(
      of(policy)
    );

    populateValidLoginForm();

    component.login();

    expect(modalServiceSpy.open)
    .toHaveBeenCalledOnceWith(
      ChangePasswordComponent,
      {
        centered: true
      }
    );

    expect(routerNavigateSpy)
    .toHaveBeenCalledOnceWith('/home');
  });

  it('should handle authentication errors', () => {
    const error = new Error('Invalid username or password');
    const consoleSpy = spyOn(console, 'log');

    authenticationServiceSpy.login.and.returnValue(
      throwError(error)
    );

    populateValidLoginForm();

    component.login();

    expect(consoleSpy)
    .toHaveBeenCalledOnceWith(error);

    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();

    expect(
      termsInfoServiceSpy.getCurrentCandidatePolicy
    ).not.toHaveBeenCalled();

    expect(
      candidateServiceSpy.getCandidatePersonal
    ).not.toHaveBeenCalled();

    expect(routerNavigateSpy)
    .not.toHaveBeenCalled();
  });

  it('should handle errors while fetching candidate and policy', () => {
    const error = new Error(
      'Unable to retrieve candidate details'
    );

    candidateServiceSpy.getCandidatePersonal.and.returnValue(
      throwError(error)
    );

    populateValidLoginForm();

    component.login();

    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();

    expect(
      candidateServiceSpy.setCandNumberStorage
    ).not.toHaveBeenCalled();

    expect(
      authenticationServiceSpy.setCandidateStatus
    ).not.toHaveBeenCalled();

    expect(routerNavigateSpy)
    .not.toHaveBeenCalled();
  });

  function populateValidLoginForm(): void {
    component.loginForm.patchValue({
      username: 'candidate@example.com',
      password: 'password123'
    });
  }
});

function createCandidate(
  overrides: Partial<Candidate> = {}
): Candidate {
  return {
    candidateNumber: '123',
    status: CandidateStatus.active,
    acceptedPrivacyPolicyId: 'policy-1',
    changePassword: false,
    ...overrides
  } as Candidate;
}

function createPolicy(
  overrides: Partial<TermsInfoDto> = {}
): TermsInfoDto {
  return {
    id: 'policy-1',
    content: '',
    ...overrides
  };
}
