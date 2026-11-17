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
import {ListHomeComponent} from "./list-home.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {SavedSearchService, SavedSearchTypeInfo} from "../../../services/saved-search.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {NgbActiveModal, NgbNavModule, NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {SalesforceService} from "../../../services/salesforce.service";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {MockPartner} from "../../../MockData/MockPartner";
import {MockUser} from "../../../MockData/MockUser";
import {SavedSearchSubtype, SavedSearchType} from "../../../model/saved-search";
import {BrowseCandidateSourcesComponent} from "../../candidates/show/browse/browse-candidate-sources.component";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {RouterLinkStubDirective} from "../../login/login.component.spec";
import {RouterTestingModule} from "@angular/router/testing";
import {DatePipe, TitleCasePipe} from "@angular/common";
import {LocalStorageService} from "../../../services/local-storage.service";
import {expect, vi} from 'vitest';

describe('ListHomeComponent', () => {
    let component: ListHomeComponent;
    let fixture: ComponentFixture<ListHomeComponent>;
    let savedSearchServiceSpy: MockedObject<SavedSearchService>;
    let localStorageServiceSpy: MockedObject<LocalStorageService>;
    let authorizationServiceSpy: MockedObject<AuthorizationService>;
    let authenticationServiceSpy: MockedObject<AuthenticationService>;
    let activeModalSpy: MockedObject<NgbActiveModal>;
    let salesforceServiceSpy: MockedObject<SalesforceService>;

    beforeEach(async () => {
        const savedSearchServiceSpyObj = {
            getSavedSearchTypeInfos: vi.fn().mockName("SavedSearchService.getSavedSearchTypeInfos")
        };
        const localStorageServiceSpyObj = {
            get: vi.fn().mockName("LocalStorageService.get"),
            set: vi.fn().mockName("LocalStorageService.set")
        };
        const authorizationServiceSpyObj = {
            isEmployerPartner: vi.fn().mockName("AuthorizationService.isEmployerPartner"),
            canSeeJobDetails: vi.fn().mockName("AuthorizationService.canSeeJobDetails")
        };
        const authenticationServiceSpyObj = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        };
        const activeModalSpyObj = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };
        const salesforceServiceSpyObj = {
            fetchJob: vi.fn().mockName("SalesforceService.fetchJob")
        };

        authorizationServiceSpyObj.canSeeJobDetails.mockReturnValue(true);

        await TestBed.configureTestingModule({
            declarations: [ListHomeComponent, BrowseCandidateSourcesComponent, RouterLinkStubDirective],
            imports: [NgbNavModule, RouterTestingModule, NgbPaginationModule, ReactiveFormsModule],
            providers: [
                UntypedFormBuilder,
                DatePipe,
                TitleCasePipe,
                { provide: SavedSearchService, useValue: savedSearchServiceSpyObj },
                { provide: LocalStorageService, useValue: localStorageServiceSpyObj },
                { provide: AuthorizationService, useValue: authorizationServiceSpyObj },
                { provide: AuthenticationService, useValue: authenticationServiceSpyObj },
                { provide: NgbActiveModal, useValue: activeModalSpyObj },
                { provide: SalesforceService, useValue: salesforceServiceSpyObj },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        savedSearchServiceSpy = TestBed.inject(SavedSearchService) as MockedObject<SavedSearchService>;
        localStorageServiceSpy = TestBed.inject(LocalStorageService) as MockedObject<LocalStorageService>;
        authorizationServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
        authenticationServiceSpy = TestBed.inject(AuthenticationService) as MockedObject<AuthenticationService>;
        activeModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        salesforceServiceSpy = TestBed.inject(SalesforceService) as MockedObject<SalesforceService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ListHomeComponent);
        component = fixture.componentInstance;
        component.activeTabId = "type:job";
        component.loggedInPartner = new MockPartner();
        const mockSavedSearchTypeInfo: SavedSearchTypeInfo = {
            savedSearchType: SavedSearchType.job,
            title: "SavedSearchTypeInfo",
            categories: [{
                    savedSearchSubtype: SavedSearchSubtype.it,
                    title: "SavedSearchType"
                }]
        };
        component.savedSearchTypeInfos = [mockSavedSearchTypeInfo];

        // savedSearchServiceSpy.getSavedSearchTypeInfos.and.returnValue([mockSavedSearchTypeInfo]);
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(new MockUser());
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should set default tab ID to "MyLists"', () => {
        expect(component['defaultTabId']).toBe('MyLists');
    });

    it('should see public lists for non-employer users', () => {
        // Mocking isEmployerPartner to return false
        authorizationServiceSpy.isEmployerPartner.mockReturnValue(false);
        expect(component.seesPublicLists()).toBe(true);
    });

    it('should not see public lists for employer users', () => {
        // Mocking isEmployerPartner to return true
        authorizationServiceSpy.isEmployerPartner.mockReturnValue(true);
        expect(component.seesPublicLists()).toBe(false);
    });

    it('should not show public lists to partners who are not source, destination or employer', () => {
        authorizationServiceSpy.canSeeJobDetails.mockReturnValue(false);
        expect(component.canSeeJobDetails()).toBe(false);
    });

});
