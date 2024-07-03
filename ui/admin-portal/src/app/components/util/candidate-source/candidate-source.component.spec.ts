/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {CandidateSource} from "../../../model/base";
import {MockCandidateSource} from "../../../MockData/MockCandidateSource";
import {of, throwError} from "rxjs";
import {RouterLinkStubDirective} from "../../login/login.component.spec";
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {MockSavedList} from "../../../MockData/MockSavedList";

fdescribe('CandidateSourceComponent', () => {
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

  beforeEach(async () => {
    const savedSearchSpy = jasmine.createSpyObj('SavedSearchService', ['get']);
    const savedListSpy = jasmine.createSpyObj('SavedListService', ['createFolder']);
    const authSpy = jasmine.createSpyObj('AuthorizationService', ['canAccessSalesforce']);
    const authServiceSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    const salesforceSpy = jasmine.createSpyObj('SalesforceService', ['joblink']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const locationSpy = jasmine.createSpyObj('Location', ['path']);
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);

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

    component.candidateSource = new MockCandidateSource();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle show more', () => {
    component.seeMore = false;
    savedSearchService.get.and.returnValue(of(new MockSavedSearch()));
    component.toggleShowMore();
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

  it('should fetch saved search when seeMore is true', () => {
    component.seeMore = true;
    savedSearchService.get.and.returnValue(of(new MockSavedSearch()));
    component.ngOnChanges({
      candidateSource: {
        currentValue: { id: 1 } as CandidateSource,
        previousValue: { id: 2 } as CandidateSource,
        firstChange: false,
        isFirstChange: () => false,
      },
    });
    expect(savedSearchService.get).toHaveBeenCalledWith(1);
  });

  it('should handle errors when fetching saved search', () => {
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
    savedListService.createFolder.and.returnValue(of(MockSavedList));
    component.doShowListFolder();
    expect(savedListService.createFolder).toHaveBeenCalled();
  });

  it('should fetch saved search on init', () => {
    component.seeMore = true;
    savedSearchService.get.and.returnValue(of(new MockSavedSearch()));
    component.ngOnChanges({
      candidateSource: {
        currentValue: { id: 2 } as CandidateSource,
        previousValue: { id: 1 } as CandidateSource,
        firstChange: false,
        isFirstChange: () => false,
      },
    });
    expect(savedSearchService.get).toHaveBeenCalledWith(1);
  });
});
