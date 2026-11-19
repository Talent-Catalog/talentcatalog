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
import {CandidateSourceComponent} from "./candidate-source.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {SavedSearchService} from "../../../services/saved-search.service";
import {SavedListService} from "../../../services/saved-list.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {Router} from "@angular/router";
import {Location} from "@angular/common";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateSource, DtoType} from "../../../model/base";
import {MockCandidateSource} from "../../../MockData/MockCandidateSource";
import {of, throwError} from "rxjs";
import {RouterLinkStubDirective} from "../../../../test-helpers/router-link-stub.directive";
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {CandidateSourceCacheService} from "../../../services/candidate-source-cache.service";
import {LocalStorageService} from "../../../services/local-storage.service";
import {expect, vi} from 'vitest';

describe('CandidateSourceComponent', () => {
    let component: CandidateSourceComponent;
    let fixture: ComponentFixture<CandidateSourceComponent>;
    let savedSearchService: MockedObject<SavedSearchService>;
    let savedListService: MockedObject<SavedListService>;
    let authorizationService: MockedObject<AuthorizationService>;
    let authenticationService: MockedObject<AuthenticationService>;
    let salesforceService: MockedObject<SalesforceService>;
    let router: MockedObject<Router>;
    let location: MockedObject<Location>;
    let modalService: MockedObject<NgbModal>;
    let cacheService: CandidateSourceCacheService;
    let localStorageService: MockedObject<LocalStorageService>;

    beforeEach(async () => {
        const savedSearchSpy = {
            get: vi.fn().mockName("SavedSearchService.get")
        };
        const savedListSpy = {
            createFolder: vi.fn().mockName("SavedListService.createFolder")
        };
        const authSpy = {
            canAccessSalesforce: vi.fn().mockName("AuthorizationService.canAccessSalesforce"),
            isCandidateSourceMine: vi.fn().mockName("AuthorizationService.isCandidateSourceMine"),
            isStarredByMe: vi.fn().mockName("AuthorizationService.isStarredByMe")
        };
        const authServiceSpy = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        };
        const salesforceSpy = {
            joblink: vi.fn().mockName("SalesforceService.joblink")
        };
        const routerSpy = {
            navigate: vi.fn().mockName("Router.navigate"),
            createUrlTree: vi.fn().mockName("Router.createUrlTree"),
            serializeUrl: vi.fn().mockName("Router.serializeUrl")
        };
        const locationSpy = {
            path: vi.fn().mockName("Location.path"),
            prepareExternalUrl: vi.fn().mockName("Location.prepareExternalUrl")
        };
        const modalSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };
        const localStorageServiceSpy = {
            set: vi.fn().mockName("LocalStorageService.set"),
            get: vi.fn().mockName("LocalStorageService.get"),
            remove: vi.fn().mockName("LocalStorageService.remove")
        };

        await TestBed.configureTestingModule({
            declarations: [CandidateSourceComponent, RouterLinkStubDirective],
            providers: [
                { provide: SavedSearchService, useValue: savedSearchSpy },
                { provide: SavedListService, useValue: savedListSpy },
                { provide: AuthorizationService, useValue: authSpy },
                { provide: AuthenticationService, useValue: authServiceSpy },
                { provide: SalesforceService, useValue: salesforceSpy },
                { provide: Router, useValue: routerSpy },
                { provide: Location, useValue: locationSpy },
                { provide: NgbModal, useValue: modalSpy },
                { provide: LocalStorageService, useValue: localStorageServiceSpy }
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(CandidateSourceComponent);
        component = fixture.componentInstance;
        savedSearchService = TestBed.inject(SavedSearchService) as MockedObject<SavedSearchService>;
        savedListService = TestBed.inject(SavedListService) as MockedObject<SavedListService>;
        authorizationService = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
        authenticationService = TestBed.inject(AuthenticationService) as MockedObject<AuthenticationService>;
        salesforceService = TestBed.inject(SalesforceService) as MockedObject<SalesforceService>;
        router = TestBed.inject(Router) as MockedObject<Router>;
        location = TestBed.inject(Location) as MockedObject<Location>;
        modalService = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
        localStorageService = TestBed.inject(LocalStorageService) as MockedObject<LocalStorageService>;
        cacheService = TestBed.inject(CandidateSourceCacheService);

        component.candidateSource = new MockCandidateSource();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should toggle show more', () => {
        component.seeMore = false;
        savedSearchService.get.mockReturnValue(of(new MockSavedSearch()));
        component.toggleSeeMore();
        expect(component.seeMore).toBe(true);
    });

    it('should emit open source event', () => {
        vi.spyOn(component.openSource, 'emit').mockReturnValue(undefined);
        component.doOpenSource();
        expect(component.openSource.emit).toHaveBeenCalledWith(component.candidateSource);
    });

    it('should emit select source event', () => {
        vi.spyOn(component.selectSource, 'emit').mockReturnValue(undefined);
        component.doSelectSource();
        expect(component.selectSource.emit).toHaveBeenCalledWith(component.candidateSource);
    });

    it('should emit edit source event', () => {
        vi.spyOn(component.editSource, 'emit').mockReturnValue(undefined);
        component.doEditSource();
        expect(component.editSource.emit).toHaveBeenCalledWith(component.candidateSource);
    });

    it('should emit copy source event', () => {
        vi.spyOn(component.copySource, 'emit').mockReturnValue(undefined);
        component.doCopySource();
        expect(component.copySource.emit).toHaveBeenCalledWith(component.candidateSource);
    });

    it('should emit delete source event', () => {
        vi.spyOn(component.deleteSource, 'emit').mockReturnValue(undefined);
        component.doDeleteSource();
        expect(component.deleteSource.emit).toHaveBeenCalledWith(component.candidateSource);
    });

    it('should emit select columns event', () => {
        vi.spyOn(component.selectColumns, 'emit').mockReturnValue(undefined);
        component.doSelectColumns();
        expect(component.selectColumns.emit).toHaveBeenCalledWith(component.candidateSource);
    });

    it('should emit toggle starred event', () => {
        vi.spyOn(component.toggleStarred, 'emit').mockReturnValue(undefined);
        component.doToggleStarred();
        expect(component.toggleStarred.emit).toHaveBeenCalledWith(component.candidateSource);
    });

    it('should emit toggle watch event', () => {
        vi.spyOn(component.toggleWatch, 'emit').mockReturnValue(undefined);
        component.doToggleWatch();
        expect(component.toggleWatch.emit).toHaveBeenCalledWith(component.candidateSource);
    });

    it('should fetch extended saved search when seeMore is true', () => {
        // Arrange: Set seeMore to true
        component.seeMore = true;

        // Mock the saved search service to return a MockSavedSearch
        savedSearchService.get.mockReturnValue(of(new MockSavedSearch()));

        // Create a MockCandidateSource that already includes savedSearchType
        const mockCandidateSource = new MockCandidateSource();
        mockCandidateSource['defaultSearch'] = false; //This makes it look like a saved search

        // Set the candidateSource on the component
        component.candidateSource = mockCandidateSource;

        // Act: Simulate the ngOnChanges lifecycle hook
        component.ngOnChanges({
            candidateSource: {
                currentValue: mockCandidateSource, // use mockCandidateSource directly
                previousValue: { id: 2 } as CandidateSource, // previous candidate source (optional detail)
                firstChange: false, // it's not the first change
                isFirstChange: () => false, // function that returns false
            },
        });

        // Assert: Check that the savedSearchService was called with correct arguments
        expect(savedSearchService.get).toHaveBeenCalledWith(1, DtoType.EXTENDED);
    });

    it('should handle errors when fetching saved search', () => {
        // Create a MockCandidateSource that already includes savedSearchType
        const mockCandidateSource = new MockCandidateSource();
        mockCandidateSource['defaultSearch'] = false; //This makes it look like a saved search

        // Set the candidateSource on the component
        component.candidateSource = mockCandidateSource;
        component.seeMore = true;
        savedSearchService.get.mockReturnValue(throwError('Error'));
        component.ngOnChanges({
            candidateSource: {
                currentValue: { id: 2 } as CandidateSource,
                previousValue: { id: 1 } as CandidateSource,
                firstChange: false,
                isFirstChange: () => false,
            },
        });
        expect(component.error).toBe('Error');
    });

    it('should copy the external link and show a confirmation modal', () => {
        router.createUrlTree.mockReturnValue({} as any);
        router.serializeUrl.mockReturnValue('/list/1');
        location.prepareExternalUrl.mockReturnValue('/list/1');
        const modalRef: any = { componentInstance: {} };
        modalService.open.mockReturnValue(modalRef);
        vi.spyOn(document, 'execCommand').mockReturnValue(undefined);

        component.doCopyLink();

        const expectedText = document.location.origin + '/list/1';
        expect(document.execCommand).toHaveBeenCalledWith('copy');
        expect(modalService.open).toHaveBeenCalled();
        expect(modalRef.componentInstance.title).toBe('Copied link to clipboard');
        expect(modalRef.componentInstance.showCancel).toBe(false);
        expect(modalRef.componentInstance.message).toContain(expectedText);
    });

    it('should navigate to the new search page with the list id', () => {
        component.candidateSource = new MockCandidateSource();
        router.navigate.mockClear();

        component.doSearchList();

        expect(router.navigate).toHaveBeenCalledWith(['/searches'], { queryParams: { tab: 'NewSearch', list: component.candidateSource.id } });
    });

    it('should not navigate when there is no list id', () => {
        component.candidateSource = { ...new MockCandidateSource(), id: null };
        router.navigate.mockClear();

        component.doSearchList();

        expect(router.navigate).not.toHaveBeenCalled();
    });

      it('should run stats', () => {
          router.navigate.mockClear();

          component.doRunStats();

          expect(router.navigate).toHaveBeenCalledOnce();
      });

    it('should show list folder', () => {
        vi.spyOn(window, 'open').mockReturnValue(undefined); // Spy on window.open
        savedListService.createFolder.mockReturnValue(of(MockSavedList));
        component.doShowListFolder();
        expect(savedListService.createFolder).toHaveBeenCalled();
        expect(window.open).toHaveBeenCalled(); // Check if window.open was called
    });

    it('should fetch saved search on init', () => {

        const mockCandidateSource = new MockCandidateSource();
        mockCandidateSource['defaultSearch'] = false; //This makes it look like a saved search

        // Set the candidateSource on the component
        component.candidateSource = mockCandidateSource;
        component.seeMore = true;
        savedSearchService.get.mockReturnValue(of(new MockSavedSearch()));
        component.ngOnChanges({
            candidateSource: {
                currentValue: { id: 1 } as CandidateSource,
                previousValue: {} as CandidateSource,
                firstChange: false,
                isFirstChange: () => true,
            },
        });
        expect(savedSearchService.get).toHaveBeenCalledWith(1, DtoType.EXTENDED);
    });

    it('should not call the API if data is already loaded', () => {
        component.candidateSource.dtoType = DtoType.FULL;
        // MODEL: spying on private method
        vi.spyOn(component as any, 'isAlreadyLoaded').mockReturnValue(true);

        component.getSavedSearch(1, DtoType.FULL);

        // MODEL: verifying private method was called
        expect((component)['isAlreadyLoaded']).toHaveBeenCalledWith(DtoType.FULL);
        expect(savedSearchService.get).not.toHaveBeenCalled();
        expect(component.loading).toBe(false);
    });

    it('should call the API and handle successful fetch', () => {
        vi.spyOn(component as any, 'isAlreadyLoaded').mockReturnValue(false);
        savedSearchService.get.mockReturnValue(of(new MockSavedSearch()));
        vi.spyOn(component as any, 'handleSuccessfulFetch').mockReturnValue(undefined);

        component.getSavedSearch(1, DtoType.FULL);

        expect(savedSearchService.get).toHaveBeenCalledWith(1, DtoType.FULL);
        expect((component)['handleSuccessfulFetch']).toHaveBeenCalledWith(expect.anything(), DtoType.FULL);
    });

    it('should handle error if API call fails', () => {
        vi.spyOn(component as any, 'isAlreadyLoaded').mockReturnValue(false);
        savedSearchService.get.mockReturnValue(throwError('Error'));
        vi.spyOn(component as any, 'handleError').mockReturnValue(undefined);

        component.getSavedSearch(1, DtoType.FULL);

        expect(savedSearchService.get).toHaveBeenCalledWith(1, DtoType.FULL);
        expect((component)['handleError']).toHaveBeenCalledWith('Error');
    });

    it('should correctly identify if the data is already loaded', () => {
        component.candidateSource.dtoType = DtoType.EXTENDED;
        expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBe(true);
        expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBe(true);

        component.candidateSource.dtoType = DtoType.FULL;
        expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBe(true);
        expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBe(false);

        component.candidateSource.dtoType = DtoType.MINIMAL;
        expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBe(false);
        expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBe(false);

        component.candidateSource.dtoType = DtoType.PREVIEW;
        expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBe(false);
        expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBe(false);

        component.candidateSource.dtoType = undefined;
        expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBe(false);
        expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBe(false);
    });

    it('should handle successful fetch and cache the result', () => {
        const result = new MockSavedSearch();
        vi.spyOn(component as any, 'cacheCandidateSource').mockReturnValue(undefined);

        (component)['handleSuccessfulFetch'](result, DtoType.FULL);

        expect(component.candidateSource.dtoType).toBe(DtoType.FULL);
        expect(component.candidateSource).toEqual(expect.objectContaining(result));
        expect((component)['cacheCandidateSource']).toHaveBeenCalled();
        expect(component.loading).toBe(false);
    });

    it('should handle error correctly', () => {
        (component)['handleError']('Error');

        expect(component.loading).toBe(false);
        expect(component.error).toBe('Error');
    });

    it('should retrieve candidate source from cache if available', () => {
        component.loading = true;
        component.candidateSource = { id: 1 } as CandidateSource;
        const cachedResult = new MockSavedSearch();
        vi.spyOn(cacheService, 'getFromCache').mockReturnValue(cachedResult);

        (component)['getFromCache'](new MockCandidateSource());

        expect(cacheService.getFromCache).toHaveBeenCalled();
        expect(component.candidateSource).toEqual(expect.objectContaining(cachedResult));
        expect(component.loading).toBe(false);
    });

    it('should not modify candidate source if cache is empty', () => {
        component.loading = true;
        component.candidateSource = { id: 1 } as CandidateSource;
        vi.spyOn(cacheService, 'getFromCache').mockReturnValue(null);

        (component)['getFromCache'](component.candidateSource);

        expect(cacheService.getFromCache).toHaveBeenCalled();
        expect(component.candidateSource).toEqual({ id: 1 } as CandidateSource);
        expect(component.loading).toBe(true); // Because getSavedSearch continues to run if not loaded from cache
    });

});
