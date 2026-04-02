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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationSubmitComponent} from './registration-submit.component';
import {Candidate, CandidateStatus} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {TermsInfoDto, TermsType} from '../../../model/terms-info-dto';
import {TermsInfoService} from '../../../services/terms-info.service';

@Component({
  selector: 'tc-loading',
  template: ''
})
class TcLoadingStubComponent {
  @Input() loading?: boolean;
}

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() color?: string;
  @Input() disabled?: boolean;
  @Output() onClick = new EventEmitter<void>();
}

@Component({
  selector: 'app-show-terms',
  template: '<ng-content></ng-content>'
})
class ShowTermsStubComponent {
  @Input() requestTermsRead?: boolean;
  @Output() termsRead = new EventEmitter<void>();
}

@Component({
  selector: 'app-error',
  template: ''
})
class ErrorStubComponent {
  @Input() error?: unknown;
}

function makeTerms(content: string, id = 'policy-1'): TermsInfoDto {
  return {
    id,
    content
  };
}

function makeCandidate(overrides: Partial<Candidate> = {}): Candidate {
  return {
    id: 1,
    status: 'pending',
    user: {
      partner: {
        name: 'Talent Beyond Boundaries',
        websiteUrl: 'https://www.talentbeyondboundaries.org',
        defaultSourcePartner: false
      }
    }
  } as Candidate;
}

describe('RegistrationSubmitComponent', () => {
  let component: RegistrationSubmitComponent;
  let fixture: ComponentFixture<RegistrationSubmitComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let termsInfoServiceSpy: jasmine.SpyObj<TermsInfoService>;

  async function configureAndCreate(options?: {
    candidate?: Candidate;
    terms?: TermsInfoDto;
    candidateError?: unknown;
    termsError?: unknown;
    submitError?: unknown;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', [
      'getCandidatePersonal',
      'submitRegistration'
    ]);
    authenticationServiceSpy = jasmine.createSpyObj('AuthenticationService', ['setCandidateStatus']);
    termsInfoServiceSpy = jasmine.createSpyObj('TermsInfoService', ['getCurrentByType']);

    const candidate = options?.candidate ?? makeCandidate();
    const terms = options?.terms ?? makeTerms('<p>Policy content</p>');

    if (options?.candidateError) {
      candidateServiceSpy.getCandidatePersonal.and.returnValue(throwError(options.candidateError));
    } else {
      candidateServiceSpy.getCandidatePersonal.and.returnValue(of(candidate));
    }

    if (options?.termsError) {
      termsInfoServiceSpy.getCurrentByType.and.returnValue(throwError(options.termsError));
    } else {
      termsInfoServiceSpy.getCurrentByType.and.returnValue(of(terms));
    }

    if (options?.submitError) {
      candidateServiceSpy.submitRegistration.and.returnValue(throwError(options.submitError));
    } else {
      candidateServiceSpy.submitRegistration.and.returnValue(of({
        status: 'pending'
      } as Candidate));
    }

    await TestBed.configureTestingModule({
      declarations: [
        RegistrationSubmitComponent,
        TcLoadingStubComponent,
        TcButtonStubComponent,
        ShowTermsStubComponent,
        ErrorStubComponent
      ],
      imports: [TranslateModule.forRoot()],
      providers: [
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: AuthenticationService, useValue: authenticationServiceSpy},
        {provide: TermsInfoService, useValue: termsInfoServiceSpy}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationSubmitComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();

    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should load the current privacy policy and candidate', async () => {
      await configureAndCreate();

      expect(termsInfoServiceSpy.getCurrentByType).toHaveBeenCalledWith(TermsType.CANDIDATE_PRIVACY_POLICY);
      expect(candidateServiceSpy.getCandidatePersonal).toHaveBeenCalled();
      expect(component.currentPrivacyPolicy.id).toBe('policy-1');
      expect(component.candidate.user.partner.name).toBe('Talent Beyond Boundaries');
    });
  });

  describe('template tc components', () => {
    it('should render tc-loading with the component loading state', async () => {
      await configureAndCreate();
      const loadingEl = fixture.debugElement.query(By.directive(TcLoadingStubComponent));

      expect(loadingEl).toBeTruthy();
      expect(loadingEl.componentInstance.loading).toBe(component.loading);
    });

    it('should render the consent tc-button when consent is required', async () => {
      await configureAndCreate({
        terms: makeTerms('<p>Policy content</p>')
      });
      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));

      expect(component.consentRequired).toBeTrue();
      expect(buttons.length).toBe(1);
      expect(buttons[0].componentInstance.color).toBe('success');
    });

    it('should render the no-consent tc-button when policy is empty and candidate is not managed by TBB', async () => {
      TestBed.resetTestingModule();
      await configureAndCreate({
        terms: makeTerms(''),
        candidate: makeCandidate({
          user: {
            partner: {
              name: 'Partner',
              websiteUrl: 'https://example.org',
              defaultSourcePartner: false
            }
          } as any
        })
      });

      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));
      const showTerms = fixture.debugElement.query(By.directive(ShowTermsStubComponent));

      expect(component.consentRequired).toBeFalse();
      expect(buttons.length).toBe(1);
      expect(showTerms).toBeNull();
    });

    it('should render the show-terms child when policy content exists', async () => {
      await configureAndCreate({
        terms: makeTerms('<p>Policy content</p>')
      });

      expect(fixture.debugElement.query(By.directive(ShowTermsStubComponent))).toBeTruthy();
    });
  });

  describe('consentRequired', () => {
    it('should return true when policy content exists', async () => {
      await configureAndCreate({
        terms: makeTerms('<p>Policy content</p>')
      });

      expect(component.consentRequired).toBeTrue();
    });

    it('should return true when candidate is managed by TBB even without policy content', async () => {
      await configureAndCreate({
        terms: makeTerms(''),
        candidate: {
          ...makeCandidate(),
          user: {
            partner: {
              name: 'TBB',
              websiteUrl: 'https://example.org',
              defaultSourcePartner: true
            }
          } as any
        } as Candidate
      });

      expect(component.consentRequired).toBeTrue();
    });

    it('should return false when policy is empty and candidate is not managed by TBB', async () => {
      await configureAndCreate({
        terms: makeTerms(''),
        candidate: makeCandidate({
          user: {
            partner: {
              name: 'Partner',
              websiteUrl: 'https://example.org',
              defaultSourcePartner: false
            }
          } as any
        })
      });

      expect(component.consentRequired).toBeFalse();
    });
  });

  describe('submit', () => {
    beforeEach(async () => configureAndCreate());

    it('should submit registration and update the candidate status on success', () => {
      component.submit();

      expect(candidateServiceSpy.submitRegistration).toHaveBeenCalledWith({
        acceptedPrivacyPolicyId: 'policy-1'
      });
      expect(authenticationServiceSpy.setCandidateStatus).toHaveBeenCalledWith(CandidateStatus.pending);
      expect(component.loading).toBeFalse();
      expect(component.error).toBeNull();
    });

    it('should set error and clear loading when submit fails', async () => {
      TestBed.resetTestingModule();
      const serverError = {status: 500};
      await configureAndCreate({submitError: serverError});

      component.submit();

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
      expect(authenticationServiceSpy.setCandidateStatus).not.toHaveBeenCalled();
    });
  });

  describe('helpers', () => {
    beforeEach(async () => configureAndCreate());

    it('should return the partner description without website when full is false', () => {
      expect(component.getPartnerDescription(false)).toBe('Talent Beyond Boundaries');
    });

    it('should return the partner description with website when full is true', () => {
      expect(component.getPartnerDescription(true)).toBe(
        'Talent Beyond Boundaries (https://www.talentbeyondboundaries.org)'
      );
    });

    it('should return null when partner information is missing', async () => {
      TestBed.resetTestingModule();
      await configureAndCreate({
        candidate: {
          id: 1,
          status: 'pending',
          user: null
        } as any
      });

      expect(component.getPartnerDescription(true)).toBeNull();
    });

    it('should set readTerms when setReadTerms is called', () => {
      component.setReadTerms();

      expect(component.readTerms).toBeTrue();
    });
  });

  describe('error paths', () => {
    it('should set error when terms fail to load', async () => {
      const serverError = {status: 503};
      await configureAndCreate({termsError: serverError});

      expect(component.error).toEqual(serverError);
    });

    it('should set error when candidate fails to load', async () => {
      const serverError = {status: 404};
      await configureAndCreate({candidateError: serverError});

      expect(component.error).toEqual(serverError);
    });
  });
});
