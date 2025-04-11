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

import {ShowCandidatesComponent} from "./show-candidates.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {NgbModal, NgbOffcanvas, NgbPaginationModule, NgbTypeaheadModule} from "@ng-bootstrap/ng-bootstrap";
import {DatePipe, TitleCasePipe} from "@angular/common";
import {CandidateService} from "../../../services/candidate.service";
import {
  CandidateSourceDescriptionComponent
} from "../../util/candidate-source-description/candidate-source-description.component";
import {AutosaveStatusComponent} from "../../util/autosave-status/autosave-status.component";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {SavedSearchService} from "../../../services/saved-search.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {MockCandidateSource} from "../../../MockData/MockCandidateSource";
import {MockUser} from "../../../MockData/MockUser";
import {SavedSearchType} from "../../../model/saved-search";
import {of, throwError} from "rxjs";
import {LocalStorageService} from "../../../services/local-storage.service";

describe('ShowCandidatesComponent', () => {
  let component: ShowCandidatesComponent;
  let fixture: ComponentFixture<ShowCandidatesComponent>;
  let formBuilder: UntypedFormBuilder;


  // Create mock service instances using jasmine.createSpyObj
  const mockCandidateService = jasmine.createSpyObj('CandidateService', ['someMethod']);
  const mockCandidateSourceService = jasmine.createSpyObj('CandidateSourceService', ['someMethod']);
  const mockSavedSearchService = jasmine.createSpyObj('SavedSearchService', ['clearSelection', 'doSearch', 'isSavedSearch','getSavedSearchTypeInfos']);
  const mockLocalStorageService = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
  const mockNgbModal = jasmine.createSpyObj('NgbModal', ['open']);
  const mockNgbOffcanvas = jasmine.createSpyObj('NgbOffcanvas', ['open', 'dismiss']);
  const mockAuthorizationService = jasmine.createSpyObj('AuthorizationService',
    ['canAssignTask', 'canAccessSalesforce', 'canEditCandidateSource', 'canPublishList',
      'canImportToList', 'canResolveTasks', 'canUpdateCandidateStatus', 'canUpdateSalesforce',
      'canManageCandidateTasks', 'isCandidateSourceMine', 'isEmployerPartner', 'canExportFromSource', 'isReadOnly',
      'isStarredByMe']);
  const mockAuthenticationService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
  const mockSalesforceService = jasmine.createSpyObj('SalesforceService', ['joblink']);
  const mockLocation = jasmine.createSpyObj('Location', ['back']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ShowCandidatesComponent, SortedByComponent, AutosaveStatusComponent, CandidateSourceDescriptionComponent],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        NgbTypeaheadModule,
        NgbPaginationModule,
        ReactiveFormsModule
      ],
      providers: [
        UntypedFormBuilder,
        DatePipe,
        TitleCasePipe,
        {provide: CandidateService, useValue: mockCandidateService},
        {provide: CandidateSourceService, useValue: mockCandidateSourceService},
        {provide: SavedSearchService, useValue: mockSavedSearchService},
        {provide: LocalStorageService, useValue: mockLocalStorageService},
        {provide: NgbModal, useValue: mockNgbModal},
        {provide: NgbOffcanvas, useValue: mockNgbOffcanvas},
        {provide: AuthorizationService, useValue: mockAuthorizationService},
        {provide: AuthenticationService, useValue: mockAuthenticationService},
        {provide: SalesforceService, useValue: mockSalesforceService},
        {provide: Location, useValue: mockLocation}
      ]
    })
    .compileComponents();
  });


  beforeEach(() => {
    fixture = TestBed.createComponent(ShowCandidatesComponent);
    component = fixture.componentInstance;

    // Inject the FormBuilder and create the form group
    formBuilder = TestBed.inject(UntypedFormBuilder);
    component.candidateSource = new MockCandidateSource();
    component.searchForm = formBuilder.group({
      keyword: [''],
      showClosedOpps: [false], // Ensure this matches the formControlName used in the template
      statusesDisplay: [[]]
    });
    setTimeout(() => {
      fixture.detectChanges(); // Detect changes to apply the formGroup to the template
    }, 500)
  });


  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should clear selection for saved search and perform search', () => {
    component.loggedInUser = new MockUser();

    const mockCandidateSource = new MockCandidateSource();
    (mockCandidateSource as any).savedSearchType = SavedSearchType.other; // Add savedSearchType property dynamically
    component.candidateSource = mockCandidateSource;
    mockSavedSearchService.clearSelection.and.returnValue(of(true));
    spyOn(component, 'doSearch');
    component.clearSelectionAndDoSearch();

    expect(mockSavedSearchService.clearSelection).toHaveBeenCalledWith(1, {userId: 1});
  });
  it('should handle error when clearing selection for saved search', () => {
    component.loggedInUser = new MockUser();
    mockSavedSearchService.clearSelection.and.returnValue(throwError('error'));
    const mockCandidateSource = new MockCandidateSource();
    (mockCandidateSource as any).savedSearchType = SavedSearchType.other; // Add savedSearchType property dynamically
    component.candidateSource = mockCandidateSource;
    component.clearSelectionAndDoSearch();
    expect(mockSavedSearchService.clearSelection).toHaveBeenCalledWith(1, {userId: 1});
    expect(component.error).toBe('error');
  });
});
