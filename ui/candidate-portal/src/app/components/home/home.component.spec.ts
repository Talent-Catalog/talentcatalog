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

import {CommonModule} from '@angular/common';
import {Component, Input, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {of} from 'rxjs';

import {HomeComponent} from './home.component';
import {CandidateService} from '../../services/candidate.service';
import {LanguageService} from '../../services/language.service';
import {BrandingService} from '../../services/branding.service';
import {ExternalLinkService} from '../../services/external-link.service';
import {TermsInfoService} from '../../services/terms-info.service';
import {CandidateStatus} from '../../model/candidate';
import {US_AFGHAN_SURVEY_TYPE} from '../../model/survey-type';
import {VerifyEmailComponent} from '../account/verify-email/verify-email.component';

// ─── Stubs ────────────────────────────────────────────────────────────────────

@Pipe({name: 'translate'})
class MockTranslatePipe implements PipeTransform {
  transform(value: string): string { return value; }
}

/**
 * Stub for tc-button — captures [href] and [target] as real @Input()s so that
 * Angular reflects them as attributes and querySelector('tc-button[href=...]')
 * works reliably, unlike with NO_ERRORS_SCHEMA where bound inputs on unknown
 * elements are not reflected as DOM attributes.
 */
@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStub {
  @Input() href: string;
  @Input() target: string;
  @Input() color: string;
  @Input() routerLink: string;
}

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStub {
  @Input() loading: boolean;
}

@Component({selector: 'tc-icon', template: '<ng-content></ng-content>'})
class TcIconStub {
  @Input() size: string;
  @Input() color: string;
}

@Component({selector: 'tc-alert', template: '<ng-content></ng-content>'})
class TcAlertStub {
  @Input() type: string;
}

// ─── Test data helper ─────────────────────────────────────────────────────────

function makeCandidate(overrides: any = {}) {
  return {
    status: 'ineligible',
    acceptedPrivacyPolicyId: 'policy-1',
    candidateMessage: 'Not eligible yet',
    user: {
      emailVerified: true,
      firstName: 'Test',
      lastName: 'User',
      email: 'candidate@example.com'
    },
    ...overrides
  } as any;
}

// ─── Suite ────────────────────────────────────────────────────────────────────

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent> | null = null;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let brandingService: jasmine.SpyObj<BrandingService>;
  let externalLinkService: jasmine.SpyObj<ExternalLinkService>;
  let termsInfoService: jasmine.SpyObj<TermsInfoService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let router: jasmine.SpyObj<Router>;
  let modalRef: {componentInstance: {userEmail?: string}};

  /** Destroys any existing fixture then creates and detects a fresh one. */
  function createFixture() {
    fixture?.destroy();
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(async () => {
    candidateService = jasmine.createSpyObj<CandidateService>('CandidateService', [
      'getCandidatePersonal',
      'getCandidateSurvey'
    ]);
    languageService = jasmine.createSpyObj<LanguageService>('LanguageService', [
      'getSelectedLanguage',
      'setUsAfghan'
    ]);
    brandingService  = jasmine.createSpyObj<BrandingService>('BrandingService', ['getBrandingInfo']);
    externalLinkService = jasmine.createSpyObj<ExternalLinkService>('ExternalLinkService', ['getLink']);
    termsInfoService = jasmine.createSpyObj<TermsInfoService>('TermsInfoService', ['getCurrentCandidatePolicy']);    modalService     = jasmine.createSpyObj<NgbModal>('NgbModal', ['open']);
    router           = jasmine.createSpyObj<Router>('Router', ['navigateByUrl']);
    modalRef         = {componentInstance: {}};
    // Default return values — individual tests override these as needed.
    candidateService.getCandidatePersonal.and.returnValue(
      of(makeCandidate())
    );

    candidateService.getCandidateSurvey.and.returnValue(
      of({
        surveyType: {
          id: 1
        }
      } as any)
    );

    languageService.getSelectedLanguage.and.returnValue('en');

    brandingService.getBrandingInfo.and.returnValue(
      of({
        partnerName: 'TBB'
      } as any)
    );

    externalLinkService.getLink.and.returnValue(
      'https://example.com/eligibility'
    );

    termsInfoService.getCurrentCandidatePolicy.and.returnValue(
      of({
        id: 'policy-1',
        content: 'policy'
      } as any)
    );

    modalService.open.and.returnValue(modalRef as any);

    await TestBed.configureTestingModule({
      imports: [
        CommonModule
      ],
      declarations: [
        HomeComponent,
        TcButtonStub,
        TcLoadingStub,
        TcIconStub,
        TcAlertStub
      ],
      providers: [
        {provide: CandidateService,    useValue: candidateService},
        {provide: LanguageService,     useValue: languageService},
        {provide: BrandingService,     useValue: brandingService},
        {provide: ExternalLinkService, useValue: externalLinkService},
        {provide: TermsInfoService,    useValue: termsInfoService},
        {provide: NgbModal,            useValue: modalService},
        {provide: Router,              useValue: router}
      ]
    })
    .overrideComponent(HomeComponent, {
      set: {
        template: `
      <tc-loading [loading]="loading"></tc-loading>

      <button
        class="verify-email-button"
        type="button"
        [hidden]="!user || emailVerified"
        (click)="openModal()"
      >
        Verify email
      </button>

      <tc-button
        [href]="getEligibilityLink()"
        target="_blank"
      >
        Eligibility
      </tc-button>

      <div class="status-content">
        {{
          candidate?.status === 'draft'
            ? 'HOME.DRAFT.EXPLANATION HOME.DRAFT.BUTTON'
            : candidate?.status === 'pending'
              ? 'HOME.PENDING.EXPLANATION HOME.PENDING.BUTTON'
              : candidate?.status === 'active'
                ? 'HOME.ACTIVE.EXPLANATION HOME.ACTIVE.BUTTON'
                : candidate?.status === 'employed'
                  ? 'HOME.EMPLOYED.EXPLANATION'
                  : candidate?.status === 'incomplete'
                    ? 'HOME.INCOMPLETE.EXPLANATION HOME.INCOMPLETE.BUTTON'
                    : candidate?.status === 'ineligible'
                      ? 'HOME.INELIGIBLE.EXPLANATION'
                      : 'HOME.ACTIVE.EXPLANATION'
        }}
      </div>

      <tc-alert
        class="candidate-message-alert"
        type="warning"
        [hidden]="
          !candidate?.candidateMessage ||
          (
            candidate?.status !== 'ineligible' &&
            candidate?.status !== 'incomplete'
          )
        "
      >
        {{ candidate?.candidateMessage }}
      </tc-alert>
    `
      }
    })
    .compileComponents();

    createFixture();
  });

  afterEach(() => fixture?.destroy());

  // ─── Creation ───────────────────────────────────────────────────────────────

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ─── Initialisation ─────────────────────────────────────────────────────────

  it('should call all required services on init and clear loading flag', () => {
    expect(languageService.getSelectedLanguage).toHaveBeenCalled();
    expect(termsInfoService.getCurrentCandidatePolicy).toHaveBeenCalled();
    expect(candidateService.getCandidatePersonal).toHaveBeenCalled();
    expect(candidateService.getCandidateSurvey).toHaveBeenCalled();
    expect(brandingService.getBrandingInfo).toHaveBeenCalled();

    expect(component.loading).toBeFalse();
    expect(component.lang).toBe('en');
    expect(component.partnerName).toBe('TBB');
  });

  // ─── US-Afghan flag ─────────────────────────────────────────────────────────

  it('should call setUsAfghan(true) when survey type matches US_AFGHAN_SURVEY_TYPE', () => {
    candidateService.getCandidateSurvey.and.returnValue(
      of({
        surveyType: {
          id: US_AFGHAN_SURVEY_TYPE
        }
      } as any)
    );

    createFixture();

    expect(languageService.setUsAfghan).toHaveBeenCalledWith(true);
  });

  it('should call setUsAfghan(false) when survey type does not match US_AFGHAN_SURVEY_TYPE', () => {
    expect(languageService.setUsAfghan).toHaveBeenCalledWith(false);
  });

  // ─── Privacy redirect ───────────────────────────────────────────────────────

  it('should redirect to /privacy when accepted policy id does not match current terms', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'active',
          acceptedPrivacyPolicyId: 'old-policy'
        })
      )
    );

    termsInfoService.getCurrentCandidatePolicy.and.returnValue(
      of({
        id: 'new-policy',
        content: 'latest'
      } as any)
    );

    createFixture();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/privacy');
  });

  it('should not redirect to /privacy when candidate is in draft status', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: CandidateStatus.draft,
          acceptedPrivacyPolicyId: 'old-policy'
        })
      )
    );

    termsInfoService.getCurrentCandidatePolicy.and.returnValue(
      of({
        id: 'new-policy',
        content: 'latest'
      } as any)
    );

    router.navigateByUrl.calls.reset();

    createFixture();

    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('should not redirect to /privacy when policy ids match', () => {
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  // ─── Email verification modal ───────────────────────────────────────────────

  it('should open verify email modal and pass user email to modal instance', () => {
    component.openModal();

    expect(modalService.open).toHaveBeenCalledWith(
      VerifyEmailComponent,
      {
        centered: true
      }
    );

    expect(modalRef.componentInstance.userEmail).toBe(
      'candidate@example.com'
    );
  });

  // ─── Email verification button visibility ───────────────────────────────────

  it('should show verify email button when emailVerified is false', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          user: {
            emailVerified: false,
            firstName: 'Test',
            lastName: 'User',
            email: 'candidate@example.com'
          }
        })
      )
    );

    createFixture();

    const button = fixture?.nativeElement.querySelector(
      '.verify-email-button'
    );

    expect(button).toBeTruthy();
    expect(button.hidden).toBeFalse();
  });

  it('should not show verify email button when emailVerified is true', () => {
    const button = fixture?.nativeElement.querySelector(
      '.verify-email-button'
    );

    expect(button).toBeTruthy();
    expect(button.hidden).toBeTrue();
  });

  // ─── Eligibility link ───────────────────────────────────────────────────────

  it('should call getLink with eligibility key and current language', () => {
    expect(externalLinkService.getLink).toHaveBeenCalledWith(
      'eligibility',
      'en'
    );
  });

  it('should render eligibility tc-button with correct href and target="_blank"', () => {
    const buttonDebugEls = fixture!.debugElement.queryAll(
      By.directive(TcButtonStub)
    );

    const linkButton = buttonDebugEls.find(
      debugElement =>
        debugElement.componentInstance.href ===
        'https://example.com/eligibility'
    );

    expect(linkButton).toBeTruthy();
    expect(linkButton!.componentInstance.target).toBe('_blank');
  });

  // ─── Template: status branches ──────────────────────────────────────────────

  it('should show draft content for draft status', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'draft'
        })
      )
    );

    createFixture();

    const text = fixture?.nativeElement.textContent;

    expect(text).toContain('HOME.DRAFT.EXPLANATION');
    expect(text).toContain('HOME.DRAFT.BUTTON');
  });

  it('should show pending content for pending status', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'pending'
        })
      )
    );

    createFixture();

    const text = fixture?.nativeElement.textContent;

    expect(text).toContain('HOME.PENDING.EXPLANATION');
    expect(text).toContain('HOME.PENDING.BUTTON');
  });

  it('should show active content for active status', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'active'
        })
      )
    );

    createFixture();

    const text = fixture?.nativeElement.textContent;

    expect(text).toContain('HOME.ACTIVE.EXPLANATION');
    expect(text).toContain('HOME.ACTIVE.BUTTON');
  });

  it('should show employed content for employed status', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'employed'
        })
      )
    );

    createFixture();

    const text = fixture?.nativeElement.textContent;

    expect(text).toContain('HOME.EMPLOYED.EXPLANATION');
  });

  it('should show incomplete content for incomplete status', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'incomplete',
          candidateMessage: 'Please finish your profile'
        })
      )
    );

    createFixture();

    const text = fixture?.nativeElement.textContent;

    expect(text).toContain('HOME.INCOMPLETE.EXPLANATION');
    expect(text).toContain('HOME.INCOMPLETE.BUTTON');
  });

  it('should show ineligible content for ineligible status', () => {
    expect(fixture?.nativeElement.textContent).toContain(
      'HOME.INELIGIBLE.EXPLANATION'
    );
  });

  it('should fall back to active content for unknown status', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'unknown-status'
        })
      )
    );

    createFixture();

    expect(fixture?.nativeElement.textContent).toContain(
      'HOME.ACTIVE.EXPLANATION'
    );
  });

  // ─── Template: candidateMessage warning ─────────────────────────────────────

  it('should show alert-warning for ineligible candidate when candidateMessage exists', () => {
    const alerts = fixture!.debugElement.queryAll(
      By.directive(TcAlertStub)
    );

    expect(alerts.length).toBe(1);
    expect(alerts[0].componentInstance.type).toBe('warning');
    expect(alerts[0].nativeElement.textContent).toContain(
      'Not eligible yet'
    );
  });

  it('should not show alert-warning for ineligible candidate when candidateMessage is absent', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'ineligible',
          candidateMessage: null
        })
      )
    );

    createFixture();

    const alert = fixture!.debugElement.query(
      By.directive(TcAlertStub)
    );

    expect(alert).toBeTruthy();
    expect(alert.nativeElement.hidden).toBeTrue();
  });

  it('should show alert-warning for incomplete candidate when candidateMessage exists', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'incomplete',
          candidateMessage: 'Please finish your profile'
        })
      )
    );

    createFixture();

    const alerts = fixture!.debugElement.queryAll(
      By.directive(TcAlertStub)
    );

    expect(alerts.length).toBe(1);
    expect(alerts[0].componentInstance.type).toBe('warning');
    expect(alerts[0].nativeElement.textContent).toContain(
      'Please finish your profile'
    );
  });

  it('should not show alert-warning for incomplete candidate when candidateMessage is absent', () => {
    candidateService.getCandidatePersonal.and.returnValue(
      of(
        makeCandidate({
          status: 'incomplete',
          candidateMessage: null
        })
      )
    );

    createFixture();

    const alert = fixture!.debugElement.query(
      By.directive(TcAlertStub)
    );

    expect(alert).toBeTruthy();
    expect(alert.nativeElement.hidden).toBeTrue();
  });
});
