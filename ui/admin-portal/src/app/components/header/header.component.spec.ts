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
import {HeaderComponent} from "./header.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {AuthorizationService} from "../../services/authorization.service";
import {CandidateService} from "../../services/candidate.service";
import {BrandingInfo, BrandingService} from "../../services/branding.service";
import {AuthenticationService} from "../../services/authentication.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgbCollapseModule, NgbTypeaheadModule} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {of, throwError} from "rxjs";
import {Candidate} from "../../model/candidate";
import {MockCandidate} from "../../MockData/MockCandidate";
import {RouterLinkStubDirective} from "../login/login.component.spec";
import {RouterTestingModule} from "@angular/router/testing";
import {
  CandidateNameNumSearchComponent
} from "../util/candidate-name-num-search/candidate-name-num-search.component";
import {User} from "../../model/user";
import {CreatedByComponent} from "../util/user/created-by/created-by.component";
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let brandingServiceSpy: jasmine.SpyObj<BrandingService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;
  const candidates: Candidate[] = [new MockCandidate()];
  const mockCandidateSearchResult = {
    content: candidates,
    number: 0,
    size: candidates.length,
    totalElements: candidates.length,
    totalPages: 1,
    last: true,
    first: true,
    numberOfElements: candidates.length,
  };
  beforeEach(async () => {
    const authServiceSpyObj = jasmine.createSpyObj('AuthorizationService', ['canViewCandidateName', 'isAnAdmin', 'isSystemAdminOnly','isEmployerPartner']);
    const candidateServiceSpyObj = jasmine.createSpyObj('CandidateService', ['findByCandidateNumberOrName', 'findByExternalId', 'findByCandidateEmailOrPhone']);
    const brandingServiceSpyObj = jasmine.createSpyObj('BrandingService', ['getBrandingInfo']);
    const authenticationServiceSpyObj = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser', 'logout']);

    await TestBed.configureTestingModule({
      declarations: [HeaderComponent,RouterLinkStubDirective,CandidateNameNumSearchComponent, CreatedByComponent],
      imports: [FormsModule, NgbTypeaheadModule,NgbCollapseModule, ReactiveFormsModule, HttpClientTestingModule, RouterTestingModule],
      providers: [
        { provide: AuthorizationService, useValue: authServiceSpyObj },
        { provide: CandidateService, useValue: candidateServiceSpyObj },
        { provide: BrandingService, useValue: brandingServiceSpyObj },
        { provide: AuthenticationService, useValue: authenticationServiceSpyObj }
      ],
      schemas: [NO_ERRORS_SCHEMA]  // To ignore subcomponent and directive errors
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    brandingServiceSpy = TestBed.inject(BrandingService) as jasmine.SpyObj<BrandingService>;
    authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    const brandingInfo: BrandingInfo = { logo: 'logo.png', websiteUrl: 'http://example.com' };
    brandingServiceSpy.getBrandingInfo.and.returnValue(of(brandingInfo));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize logo and websiteUrl on init', fakeAsync(() => {
    const brandingInfo: BrandingInfo = { logo: 'logo.png', websiteUrl: 'http://example.com' };
    brandingServiceSpy.getBrandingInfo.and.returnValue(of(brandingInfo));

    component.ngOnInit();
    tick(5000)
    expect(component.logo).toBe(brandingInfo.logo);
    expect(component.websiteUrl).toBe(brandingInfo.websiteUrl);
  }));

  it('should handle branding service error', fakeAsync(() => {
    const errorMessage = 'Error loading branding info';
    brandingServiceSpy.getBrandingInfo.and.returnValue(throwError(errorMessage));

    component.ngOnInit();
    tick();

    expect(component.error).toBe(errorMessage);
  }));

  it('should perform candidate number or name search', fakeAsync(() => {

    candidateServiceSpy.findByCandidateNumberOrName.and.returnValue(of(mockCandidateSearchResult));

    const searchText$ = of('123');
    component.doNumberOrNameSearch(searchText$).subscribe(result => {
      expect(result).toEqual(candidates);
    });

    tick(300); // debounceTime
  }));

  it('should handle candidate number or name search error', fakeAsync(() => {
    candidateServiceSpy.findByCandidateNumberOrName.and.returnValue(throwError('Error'));

    const searchText$ = of('123');
    component.doNumberOrNameSearch(searchText$).subscribe(result => {
      expect(result).toEqual([]);
      expect(component.searchFailed).toBeTrue();
    });

    tick(300); // debounceTime
  }));

  it('should perform external ID search', fakeAsync(() => {
    candidateServiceSpy.findByExternalId.and.returnValue(of(mockCandidateSearchResult));

    const searchText$ = of('ext123');
    component.doExternalIdSearch(searchText$).subscribe(result => {
      expect(result).toEqual(candidates);
    });

    tick(300); // debounceTime
  }));

  it('should handle external ID search error', fakeAsync(() => {
    candidateServiceSpy.findByExternalId.and.returnValue(throwError('Error'));

    const searchText$ = of('ext123');
    component.doExternalIdSearch(searchText$).subscribe(result => {
      expect(result).toEqual([]);
      expect(component.searchFailed).toBeTrue();
    });

    tick(300); // debounceTime
  }));

  it('should perform email or phone search', fakeAsync(() => {
    candidateServiceSpy.findByCandidateEmailOrPhone.and.returnValue(of(mockCandidateSearchResult));

    const searchText$ = of('email@example.com');
    component.doEmailOrPhoneSearch(searchText$).subscribe(result => {
      expect(result).toEqual(candidates);
    });

    tick(300); // debounceTime
  }));

  it('should handle email or phone search error', fakeAsync(() => {
    candidateServiceSpy.findByCandidateEmailOrPhone.and.returnValue(throwError('Error'));

    const searchText$ = of('email@example.com');
    component.doEmailOrPhoneSearch(searchText$).subscribe(result => {
      expect(result).toEqual([]);
      expect(component.searchFailed).toBeTrue();
    });

    tick(300); // debounceTime
  }));

  it('should return formatted candidate row', () => {
    authServiceSpy.canViewCandidateName.and.returnValue(true);
    expect(component.renderCandidateRow(candidates[0])).toBe('123456: John Doe');

    authServiceSpy.canViewCandidateName.and.returnValue(false);
    expect(component.renderCandidateRow(candidates[0])).toBe('123456');
  });

  it('should return logged in user info', () => {
    const user: User = candidates[0].user;
    authenticationServiceSpy.getLoggedInUser.and.returnValue(user);
    component.ngOnInit();
    expect(component.loggedInUserInfo()).toContain('mockuser (MP Limited)');

    authenticationServiceSpy.getLoggedInUser.and.returnValue(null);
    component.ngOnInit();
    expect(component.loggedInUserInfo()).toBe('Not logged in');
  });

  it('should return true if user can view candidate name', () => {
    authServiceSpy.canViewCandidateName.and.returnValue(true);
    expect(component.canViewCandidateName()).toBeTrue();
  });

  it('should return false if user cannot view candidate name', () => {
    authServiceSpy.canViewCandidateName.and.returnValue(false);
    expect(component.canViewCandidateName()).toBeFalse();
  });

  it('should return true if user is an admin', () => {
    authServiceSpy.isAnAdmin.and.returnValue(true);
    expect(component.isAnAdmin()).toBeTrue();
  });

  it('should return false if user is not an admin', () => {
    authServiceSpy.isAnAdmin.and.returnValue(false);
    expect(component.isAnAdmin()).toBeFalse();
  });

  it('should return true if user is a system admin only', () => {
    authServiceSpy.isSystemAdminOnly.and.returnValue(true);
    expect(component.isSystemAdminOnly()).toBeTrue();
  });

  it('should return false if user is not a system admin only', () => {
    authServiceSpy.isSystemAdminOnly.and.returnValue(false);
    expect(component.isSystemAdminOnly()).toBeFalse();
  });

});
