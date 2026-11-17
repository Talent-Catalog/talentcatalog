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
import {HeaderComponent} from "./header.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {AuthorizationService} from "../../services/authorization.service";
import {CandidateService} from "../../services/candidate.service";
import {BrandingInfo, BrandingService} from "../../services/branding.service";
import {AuthenticationService} from "../../services/authentication.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgbCollapseModule, NgbTypeaheadModule} from "@ng-bootstrap/ng-bootstrap";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {of, throwError} from "rxjs";
import {Candidate} from "../../model/candidate";
import {MockCandidate} from "../../MockData/MockCandidate";
import {RouterLinkStubDirective} from "../login/login.component.spec";
import {RouterTestingModule} from "@angular/router/testing";
import {CandidateNameNumSearchComponent} from "../util/candidate-name-num-search/candidate-name-num-search.component";
import {User} from "../../model/user";
import {CreatedByComponent} from "../util/user/created-by/created-by.component";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {expect, vi} from 'vitest';

describe('HeaderComponent', () => {
    let component: HeaderComponent;
    let fixture: ComponentFixture<HeaderComponent>;
    let authServiceSpy: MockedObject<AuthorizationService>;
    let candidateServiceSpy: MockedObject<CandidateService>;
    let brandingServiceSpy: MockedObject<BrandingService>;
    let authenticationServiceSpy: MockedObject<AuthenticationService>;
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
        const authServiceSpyObj = {
            canViewCandidateName: vi.fn().mockName("AuthorizationService.canViewCandidateName"),
            isAnAdmin: vi.fn().mockName("AuthorizationService.isAnAdmin"),
            isSystemAdminOnly: vi.fn().mockName("AuthorizationService.isSystemAdminOnly"),
            isEmployerPartner: vi.fn().mockName("AuthorizationService.isEmployerPartner")
        };
        const candidateServiceSpyObj = {
            findByCandidateNumberOrName: vi.fn().mockName("CandidateService.findByCandidateNumberOrName"),
            findByExternalId: vi.fn().mockName("CandidateService.findByExternalId"),
            findByCandidateEmailPhoneOrWhatsapp: vi.fn().mockName("CandidateService.findByCandidateEmailPhoneOrWhatsapp")
        };
        const brandingServiceSpyObj = {
            getBrandingInfo: vi.fn().mockName("BrandingService.getBrandingInfo")
        };
        const authenticationServiceSpyObj = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser"),
            logout: vi.fn().mockName("AuthenticationService.logout")
        };

        await TestBed.configureTestingModule({
            declarations: [HeaderComponent, RouterLinkStubDirective, CandidateNameNumSearchComponent, CreatedByComponent],
            imports: [FormsModule, NgbTypeaheadModule, NgbCollapseModule, ReactiveFormsModule, RouterTestingModule],
            providers: [
                { provide: AuthorizationService, useValue: authServiceSpyObj },
                { provide: CandidateService, useValue: candidateServiceSpyObj },
                { provide: BrandingService, useValue: brandingServiceSpyObj },
                { provide: AuthenticationService, useValue: authenticationServiceSpyObj },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ],
            schemas: [NO_ERRORS_SCHEMA] // To ignore subcomponent and directive errors
        }).compileComponents();

        authServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
        candidateServiceSpy = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
        brandingServiceSpy = TestBed.inject(BrandingService) as MockedObject<BrandingService>;
        authenticationServiceSpy = TestBed.inject(AuthenticationService) as MockedObject<AuthenticationService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(HeaderComponent);
        component = fixture.componentInstance;
        const brandingInfo: BrandingInfo = { logo: 'logo.png', websiteUrl: 'http://example.com' };
        brandingServiceSpy.getBrandingInfo.mockReturnValue(of(brandingInfo));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize logo and websiteUrl on init', fakeAsync(() => {
        const brandingInfo: BrandingInfo = { logo: 'logo.png', websiteUrl: 'http://example.com' };
        brandingServiceSpy.getBrandingInfo.mockReturnValue(of(brandingInfo));

        component.ngOnInit();
        tick(5000);
        expect(component.logo).toBe(brandingInfo.logo);
        expect(component.websiteUrl).toBe(brandingInfo.websiteUrl);
    }));

    it('should handle branding service error', fakeAsync(() => {
        const errorMessage = 'Error loading branding info';
        brandingServiceSpy.getBrandingInfo.mockReturnValue(throwError(errorMessage));

        component.ngOnInit();
        tick();

        expect(component.error).toBe(errorMessage);
    }));

    it('should perform candidate number or name search', fakeAsync(() => {

        candidateServiceSpy.findByCandidateNumberOrName.mockReturnValue(of(mockCandidateSearchResult));

        const searchText$ = of('123');
        component.doNumberOrNameSearch(searchText$).subscribe(result => {
            expect(result).toEqual(candidates);
        });

        tick(300); // debounceTime
    }));

    it('should handle candidate number or name search error', fakeAsync(() => {
        candidateServiceSpy.findByCandidateNumberOrName.mockReturnValue(throwError('Error'));

        const searchText$ = of('123');
        component.doNumberOrNameSearch(searchText$).subscribe(result => {
            expect(result).toEqual([]);
            expect(component.searchFailed).toBe(true);
        });

        tick(300); // debounceTime
    }));

    it('should perform external ID search', fakeAsync(() => {
        candidateServiceSpy.findByExternalId.mockReturnValue(of(mockCandidateSearchResult));

        const searchText$ = of('ext123');
        component.doExternalIdSearch(searchText$).subscribe(result => {
            expect(result).toEqual(candidates);
        });

        tick(300); // debounceTime
    }));

    it('should handle external ID search error', fakeAsync(() => {
        candidateServiceSpy.findByExternalId.mockReturnValue(throwError('Error'));

        const searchText$ = of('ext123');
        component.doExternalIdSearch(searchText$).subscribe(result => {
            expect(result).toEqual([]);
            expect(component.searchFailed).toBe(true);
        });

        tick(300); // debounceTime
    }));

    it('should perform email phone or whatsapp search', fakeAsync(() => {
        candidateServiceSpy.findByCandidateEmailPhoneOrWhatsapp.mockReturnValue(of(mockCandidateSearchResult));

        const searchText$ = of('email@example.com');
        component.doEmailPhoneOrWhatsappSearch(searchText$).subscribe(result => {
            expect(result).toEqual(candidates);
        });

        tick(300); // debounceTime
    }));

    it('should handle email phone or whatsapp search error', fakeAsync(() => {
        candidateServiceSpy.findByCandidateEmailPhoneOrWhatsapp.mockReturnValue(throwError('Error'));

        const searchText$ = of('email@example.com');
        component.doEmailPhoneOrWhatsappSearch(searchText$).subscribe(result => {
            expect(result).toEqual([]);
            expect(component.searchFailed).toBe(true);
        });

        tick(300); // debounceTime
    }));

    it('should return formatted candidate row', () => {
        authServiceSpy.canViewCandidateName.mockReturnValue(true);
        expect(component.renderCandidateRow(candidates[0])).toBe('123456: John Doe');

        authServiceSpy.canViewCandidateName.mockReturnValue(false);
        expect(component.renderCandidateRow(candidates[0])).toBe('123456');
    });

    it('should return logged in user info', () => {
        const user: User = candidates[0].user;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);
        component.ngOnInit();
        expect(component.loggedInUserInfo()).toContain('mockuser (MP Limited)');

        authenticationServiceSpy.getLoggedInUser.mockReturnValue(null);
        component.ngOnInit();
        expect(component.loggedInUserInfo()).toBe('Not logged in');
    });

    it('should return true if user can view candidate name', () => {
        authServiceSpy.canViewCandidateName.mockReturnValue(true);
        expect(component.canViewCandidateName()).toBe(true);
    });

    it('should return false if user cannot view candidate name', () => {
        authServiceSpy.canViewCandidateName.mockReturnValue(false);
        expect(component.canViewCandidateName()).toBe(false);
    });

    it('should return true if user is an admin', () => {
        authServiceSpy.isAnAdmin.mockReturnValue(true);
        expect(component.isAnAdmin()).toBe(true);
    });

    it('should return false if user is not an admin', () => {
        authServiceSpy.isAnAdmin.mockReturnValue(false);
        expect(component.isAnAdmin()).toBe(false);
    });

    it('should return true if user is a system admin only', () => {
        authServiceSpy.isSystemAdminOnly.mockReturnValue(true);
        expect(component.isSystemAdminOnly()).toBe(true);
    });

    it('should return false if user is not a system admin only', () => {
        authServiceSpy.isSystemAdminOnly.mockReturnValue(false);
        expect(component.isSystemAdminOnly()).toBe(false);
    });

});
