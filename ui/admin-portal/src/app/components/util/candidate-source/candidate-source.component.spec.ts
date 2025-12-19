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

import {CandidateSourceComponent} from "./candidate-source.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {SavedSearchService} from "../../../services/saved-search.service";
import {SavedListService} from "../../../services/saved-list.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {Router} from "@angular/router";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateSource, DtoType} from "../../../model/base";
import {MockCandidateSource} from "../../../MockData/MockCandidateSource";
import {of, throwError} from "rxjs";
import {RouterLinkStubDirective} from "../../login/login.component.spec";
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {CandidateSourceCacheService} from "../../../services/candidate-source-cache.service";
import {SavedSearch} from "../../../model/saved-search";
import {LocalStorageService} from "../../../services/local-storage.service";

describe('CandidateSourceComponent', () => {
  let component: CandidateSourceComponent;
  let fixture: ComponentFixture<CandidateSourceComponent>;
  let savedSearchService: jasmine.SpyObj<SavedSearchService>;
  let savedListService: jasmine.SpyObj<SavedListService>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let salesforceService: jasmine.SpyObj<SalesforceService>;
  let router: jasmine.SpyObj<Router>;
  let location: jasmine.SpyObj<Location>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let cacheService: CandidateSourceCacheService;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;

  beforeEach(async () => {
    const savedSearchSpy = jasmine.createSpyObj('SavedSearchService', ['get']);
    const savedListSpy = jasmine.createSpyObj('SavedListService', ['createFolder']);
    const authSpy = jasmine.createSpyObj('AuthorizationService',
      ['canAccessSalesforce','isCandidateSourceMine','isStarredByMe']);
    const authServiceSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    const salesforceSpy = jasmine.createSpyObj('SalesforceService', ['joblink']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const locationSpy = jasmine.createSpyObj('Location', ['path']);
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const localStorageServiceSpy = jasmine.createSpyObj('LocalStorageService', ['set', 'get', 'remove']);

    await TestBed.configureTestingModule({
      declarations: [CandidateSourceComponent,RouterLinkStubDirective],
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
    savedSearchService = TestBed.inject(SavedSearchService) as jasmine.SpyObj<SavedSearchService>;
    savedListService = TestBed.inject(SavedListService) as jasmine.SpyObj<SavedListService>;
    authorizationService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    authenticationService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    salesforceService = TestBed.inject(SalesforceService) as jasmine.SpyObj<SalesforceService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    location = TestBed.inject(Location) as jasmine.SpyObj<Location>;
    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    localStorageService = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
    cacheService = TestBed.inject(CandidateSourceCacheService);

    component.candidateSource = new MockCandidateSource();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle show more', () => {
    component.seeMore = false;
    savedSearchService.get.and.returnValue(of(new MockSavedSearch()));
    component.toggleSeeMore();
    expect(component.seeMore).toBeTrue();
  });

  it('should emit open source event', () => {
    spyOn(component.openSource, 'emit');
    component.doOpenSource();
    expect(component.openSource.emit).toHaveBeenCalledWith(component.candidateSource);
  });

  it('should emit select source event', () => {
    spyOn(component.selectSource, 'emit');
    component.doSelectSource();
    expect(component.selectSource.emit).toHaveBeenCalledWith(component.candidateSource);
  });

  it('should emit edit source event', () => {
    spyOn(component.editSource, 'emit');
    component.doEditSource();
    expect(component.editSource.emit).toHaveBeenCalledWith(component.candidateSource);
  });

  it('should emit copy source event', () => {
    spyOn(component.copySource, 'emit');
    component.doCopySource();
    expect(component.copySource.emit).toHaveBeenCalledWith(component.candidateSource);
  });

  it('should emit delete source event', () => {
    spyOn(component.deleteSource, 'emit');
    component.doDeleteSource();
    expect(component.deleteSource.emit).toHaveBeenCalledWith(component.candidateSource);
  });

  it('should emit select columns event', () => {
    spyOn(component.selectColumns, 'emit');
    component.doSelectColumns();
    expect(component.selectColumns.emit).toHaveBeenCalledWith(component.candidateSource);
  });

  it('should emit toggle starred event', () => {
    spyOn(component.toggleStarred, 'emit');
    component.doToggleStarred();
    expect(component.toggleStarred.emit).toHaveBeenCalledWith(component.candidateSource);
  });

  it('should emit toggle watch event', () => {
    spyOn(component.toggleWatch, 'emit');
    component.doToggleWatch();
    expect(component.toggleWatch.emit).toHaveBeenCalledWith(component.candidateSource);
  });

  it('should fetch extended saved search when seeMore is true', () => {
    // Arrange: Set seeMore to true
    component.seeMore = true;

    // Mock the saved search service to return a MockSavedSearch
    savedSearchService.get.and.returnValue(of(new MockSavedSearch()));

    // Create a MockCandidateSource that already includes savedSearchType
    const mockCandidateSource = new MockCandidateSource();
    mockCandidateSource['savedSearchType'] = 'MockSavedSearch'; // Already included in the mock definition

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
    mockCandidateSource['savedSearchType'] = 'MockSavedSearch'; // Already included in the mock definition

    // Set the candidateSource on the component
    component.candidateSource = mockCandidateSource;
    component.seeMore = true;
    savedSearchService.get.and.returnValue(throwError('Error'));
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

  it('should run stats', () => {
    router.navigate.and.callThrough();
    component.doRunStats();
    expect(router.navigate).toHaveBeenCalled();
  });

  it('should show list folder', () => {
    spyOn(window, 'open'); // Spy on window.open
    savedListService.createFolder.and.returnValue(of(MockSavedList));
    component.doShowListFolder();
    expect(savedListService.createFolder).toHaveBeenCalled();
    expect(window.open).toHaveBeenCalled(); // Check if window.open was called
  });

  it('should fetch saved search on init', () => {

    const mockCandidateSource = new MockCandidateSource();
    mockCandidateSource['savedSearchType'] = 'MockSavedSearch'; // Already included in the mock definition

    // Set the candidateSource on the component
    component.candidateSource = mockCandidateSource;
    component.seeMore = true;
    savedSearchService.get.and.returnValue(of(new MockSavedSearch()));
    component.ngOnChanges({
      candidateSource: {
        currentValue: { id: 1 } as CandidateSource,
        previousValue: { } as CandidateSource,
        firstChange: false,
        isFirstChange: () => true,
      },
    });
    expect(savedSearchService.get).toHaveBeenCalledWith(1, DtoType.EXTENDED);
  });

  it('should not call the API if data is already loaded', () => {
    component.candidateSource.dtoType = DtoType.FULL;
    // MODEL: spying on private method
    spyOn(component as any, 'isAlreadyLoaded').and.returnValue(true);

    component.getSavedSearch(1, DtoType.FULL);

    // MODEL: verifying private method was called
    expect((component)['isAlreadyLoaded']).toHaveBeenCalledWith(DtoType.FULL);
    expect(savedSearchService.get).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  });

  it('should call the API and handle successful fetch', () => {
    spyOn(component as any, 'isAlreadyLoaded').and.returnValue(false);
    savedSearchService.get.and.returnValue(of(new MockSavedSearch()));
    spyOn(component as any, 'handleSuccessfulFetch');

    component.getSavedSearch(1, DtoType.FULL);

    expect(savedSearchService.get).toHaveBeenCalledWith(1, DtoType.FULL);
    expect((component)['handleSuccessfulFetch']).toHaveBeenCalledWith(jasmine.anything(), DtoType.FULL);
  });

  it('should handle error if API call fails', () => {
    spyOn(component as any, 'isAlreadyLoaded').and.returnValue(false);
    savedSearchService.get.and.returnValue(throwError('Error'));
    spyOn(component as any, 'handleError');

    component.getSavedSearch(1, DtoType.FULL);

    expect(savedSearchService.get).toHaveBeenCalledWith(1, DtoType.FULL);
    expect((component)['handleError']).toHaveBeenCalledWith('Error');
  });

  it('should correctly identify if the data is already loaded', () => {
    component.candidateSource.dtoType = DtoType.EXTENDED;
    expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBeTrue();
    expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBeTrue();

    component.candidateSource.dtoType = DtoType.FULL;
    expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBeTrue();
    expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBeFalse();

    component.candidateSource.dtoType = DtoType.MINIMAL;
    expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBeFalse();
    expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBeFalse();

    component.candidateSource.dtoType = DtoType.PREVIEW;
    expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBeFalse();
    expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBeFalse();

    component.candidateSource.dtoType = undefined;
    expect((component)['isAlreadyLoaded'](DtoType.FULL)).toBeFalse();
    expect((component)['isAlreadyLoaded'](DtoType.EXTENDED)).toBeFalse();
  });

  it('should handle successful fetch and cache the result', () => {
    const result = new MockSavedSearch();
    spyOn(component as any, 'cacheCandidateSource');

    (component)['handleSuccessfulFetch'](result, DtoType.FULL);

    expect(component.candidateSource.dtoType).toBe(DtoType.FULL);
    expect(component.candidateSource).toEqual(jasmine.objectContaining(result));
    expect((component)['cacheCandidateSource']).toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  });

  it('should handle error correctly', () => {
    (component)['handleError']('Error');

    expect(component.loading).toBeFalse();
    expect(component.error).toBe('Error');
  });

  it('should retrieve candidate source from cache if available', () => {
    component.loading = true;
    component.candidateSource = { id: 1 } as CandidateSource;
    const cachedResult = new MockSavedSearch()
    spyOn(cacheService, 'getFromCache').and.returnValue(cachedResult);

    (component)['getFromCache'](new MockCandidateSource());

    expect(cacheService.getFromCache).toHaveBeenCalled();
    expect(component.candidateSource).toEqual(jasmine.objectContaining(cachedResult));
    expect(component.loading).toBeFalse();
  });

  it('should not modify candidate source if cache is empty', () => {
    component.loading = true;
    component.candidateSource = { id: 1 } as CandidateSource;
    spyOn(cacheService, 'getFromCache').and.returnValue(null);

    (component)['getFromCache'](component.candidateSource);

    expect(cacheService.getFromCache).toHaveBeenCalled();
    expect(component.candidateSource).toEqual({ id: 1 } as CandidateSource);
    expect(component.loading).toBeTrue(); // Because getSavedSearch continues to run if not loaded from cache
  });

});
