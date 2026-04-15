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
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {
  NgbModal,
  NgbOffcanvas,
  NgbPaginationModule,
  NgbTypeaheadModule
} from "@ng-bootstrap/ng-bootstrap";
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
import {MockCandidate} from "../../../MockData/MockCandidate";
import {By} from "@angular/platform-browser";
import {
  CandidateSourceCandidateService
} from "../../../services/candidate-source-candidate.service";
import {SavedListCandidateService} from "../../../services/saved-list-candidate.service";
import {SearchResults} from "../../../model/search-results";
import {Candidate} from "../../../model/candidate";
import {SavedListService} from "../../../services/saved-list.service";
import {CandidateFieldService} from "../../../services/candidate-field.service";
import {PublishedDocColumnService} from "../../../services/published-doc-column.service";

describe('ShowCandidatesComponent', () => {
  let component: ShowCandidatesComponent;
  let fixture: ComponentFixture<ShowCandidatesComponent>;
  let formBuilder: UntypedFormBuilder;

  // Mock services
  const mockCandidateService = jasmine.createSpyObj('CandidateService', ['search', 'findByCandidateNumberOrName', 'downloadCv', 'createUpdateOppsFromCandidates', 'createUpdateOppsFromCandidateList', 'updateStatus', 'resolveOutstandingTasks']);
  const mockCandidateSourceService = jasmine.createSpyObj('CandidateSourceService', ['copy', 'starSourceForUser', 'unstarSourceForUser']);
  const mockSavedSearchService = jasmine.createSpyObj('SavedSearchService', ['clearSelection', 'selectCandidate', 'getSelectionCount', 'updateSelectedStatuses', 'getSavedSearchTypeInfos', 'addWatcher', 'removeWatcher']);
  const mockSavedListService = jasmine.createSpyObj('SavedListService', ['publish', 'importEmployerFeedback', 'createFolder', 'get']);
  const mockLocalStorageService = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
  const mockNgbModal = jasmine.createSpyObj('NgbModal', ['open']);
  const mockNgbOffcanvas = jasmine.createSpyObj('NgbOffcanvas', ['open', 'dismiss']);
  const mockAuthorizationService = jasmine.createSpyObj('AuthorizationService', [
    'canAssignTask', 'canAccessSalesforce', 'canEditCandidateSource', 'canPublishList',
    'canImportToList', 'canResolveTasks', 'canUpdateCandidateStatus', 'canUpdateSalesforce',
    'canManageCandidateTasks', 'isCandidateSourceMine', 'isEmployerPartner', 'canExportFromSource',
    'isReadOnly', 'isStarredByMe', 'canViewCandidateName', 'canAccessGoogleDrive'
  ]);
  const mockAuthenticationService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
  const mockSalesforceService = jasmine.createSpyObj('SalesforceService', ['joblink']);
  const mockLocation = jasmine.createSpyObj('Location', ['back']);
  const mockCandidateSourceCandidateService = jasmine.createSpyObj('CandidateSourceCandidateService', ['searchPaged', 'search', 'export']);
  const mockSavedListCandidateService = jasmine.createSpyObj('SavedListCandidateService', ['mergeFromFile', 'merge', 'remove', 'getSelectionListCandidates', 'saveSelection', 'create']);
  const mockCandidateFieldService = jasmine.createSpyObj('CandidateFieldService', ['getCandidateSourceFields', 'isCandidateNameViewable']);
  const mockPublishedDocColumnService = jasmine.createSpyObj('PublishedDocColumnService', ['getColumnConfigFromAllColumns', 'getColumnConfigFromExportColumns']);

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
        {provide: SavedListService, useValue: mockSavedListService},
        {provide: LocalStorageService, useValue: mockLocalStorageService},
        {provide: NgbModal, useValue: mockNgbModal},
        {provide: NgbOffcanvas, useValue: mockNgbOffcanvas},
        {provide: AuthorizationService, useValue: mockAuthorizationService},
        {provide: AuthenticationService, useValue: mockAuthenticationService},
        {provide: SalesforceService, useValue: mockSalesforceService},
        {provide: Location, useValue: mockLocation},
        {provide: CandidateSourceCandidateService, useValue: mockCandidateSourceCandidateService},
        {provide: SavedListCandidateService, useValue: mockSavedListCandidateService},
        {provide: CandidateFieldService, useValue: mockCandidateFieldService},
        {provide: PublishedDocColumnService, useValue: mockPublishedDocColumnService}
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShowCandidatesComponent);
    component = fixture.componentInstance;

    // Setup default mocks
    formBuilder = TestBed.inject(UntypedFormBuilder);
    component.candidateSource = new MockCandidateSource();
    component.searchInResultsForm = formBuilder.group({
      keyword: [''],
      showClosedOpps: [false],
      statusesDisplay: [[]]
    });
    mockAuthenticationService.getLoggedInUser.and.returnValue(new MockUser());
    mockCandidateFieldService.getCandidateSourceFields.and.returnValue([]);
    mockCandidateFieldService.isCandidateNameViewable.and.returnValue(true);
    mockAuthorizationService.isReadOnly.and.returnValue(false);
    mockAuthorizationService.isEmployerPartner.and.returnValue(false);
    mockAuthorizationService.canExportFromSource.and.returnValue(true);
    mockAuthorizationService.canImportToList.and.returnValue(true);
    mockAuthorizationService.canPublishList.and.returnValue(true);
    mockAuthorizationService.canUpdateCandidateStatus.and.returnValue(true);
    mockAuthorizationService.canAssignTask.and.returnValue(true);
    mockAuthorizationService.canResolveTasks.and.returnValue(true);
    mockAuthorizationService.canAccessSalesforce.and.returnValue(true);
    mockAuthorizationService.canAccessGoogleDrive.and.returnValue(true);
    mockAuthorizationService.isStarredByMe.and.returnValue(false);

    // Detect changes after a short delay to ensure template binding
    setTimeout(() => {
      fixture.detectChanges();
    }, 500);
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle error when clearing selection for saved search', () => {
    component.loggedInUser = new MockUser();
    mockSavedSearchService.clearSelection.and.returnValue(throwError('Selection clear error'));
    const mockCandidateSource = new MockCandidateSource();
    (mockCandidateSource as any).savedSearchType = SavedSearchType.other;
    component.candidateSource = mockCandidateSource;
    component.clearSelectionAndDoSearch();

    expect(mockSavedSearchService.clearSelection).toHaveBeenCalledWith(1, {userId: 1});
    expect(component.error).toBe('Selection clear error');
  });

  it('should clear selection for saved list and perform search', () => {
    component.loggedInUser = new MockUser();
    component.candidateSource = new MockCandidateSource();
    spyOn(component, 'doSearch');
    component.clearSelectionAndDoSearch();

    expect(component.doSearch).toHaveBeenCalledWith(true);
    expect(component.selectedCandidates).toEqual([]);
  });

  it('should initialize form and user correctly', () => {
    component.ngOnInit();
    expect(component.searchInResultsForm.get('keyword')).toBeTruthy();
    expect(component.searchInResultsForm.get('showClosedOpps')).toBeTruthy();
    expect(component.loggedInUser).toEqual(new MockUser());
  });

  it('should handle search error in doSearch', fakeAsync(() => {
    mockCandidateSourceCandidateService.searchPaged.and.returnValue(throwError('Search error'));
    component.candidateSource = new MockCandidateSource();
    component.doSearch(true);
    tick();

    expect(mockCandidateSourceCandidateService.searchPaged).toHaveBeenCalled();
    expect(component.error).toBe('Search error');
    expect(component.searching).toBeFalse();
  }));

  it('should toggle sort direction when same column is clicked', () => {
    component.sortField = 'id';
    component.sortDirection = 'ASC';
    spyOn(component, 'doSearch');

    component.toggleSort('id');

    expect(component.sortDirection).toBe('DESC');
    expect(component.doSearch).toHaveBeenCalledWith(true);
  });

  it('should change sort field and set direction to ASC when different column is clicked', () => {
    component.sortField = 'id';
    component.sortDirection = 'DESC';
    spyOn(component, 'doSearch');

    component.toggleSort('name');

    expect(component.sortField).toBe('name');
    expect(component.sortDirection).toBe('ASC');
    expect(component.doSearch).toHaveBeenCalledWith(true);
  });

  it('should export candidates successfully', fakeAsync(() => {
    const mockBlob = new Blob(['test'], {type: 'text/csv'});
    mockCandidateSourceCandidateService.export.and.returnValue(of(mockBlob));
    spyOn(component, 'createAndDownloadBlobFile');
    component.results = {totalElements: 100} as SearchResults<Candidate>;
    component.candidateSource = new MockCandidateSource();

    component.exportCandidates();
    tick();

    expect(mockCandidateSourceCandidateService.export).toHaveBeenCalled();
    expect(component.createAndDownloadBlobFile).toHaveBeenCalledWith(mockBlob, {type: 'text/csv;charset=utf-8;'}, 'candidates.csv');
    expect(component.exporting).toBeFalse();
  }));

  it('should add candidate to list', fakeAsync(() => {
    const mockCandidate = new MockCandidate();
    mockSavedListCandidateService.merge.and.returnValue(of({}));
    spyOn(component, 'doSearch');
    component.candidateSource = new MockCandidateSource();

    component.addCandidateToList(mockCandidate);
    tick();

    expect(mockSavedListCandidateService.merge).toHaveBeenCalledWith(1, {candidateIds: [mockCandidate.id]});
    expect(component.doSearch).toHaveBeenCalledWith(true);
    expect(component.adding).toBeFalse();
  }));

  it('should handle error when adding candidate to list', fakeAsync(() => {
    const mockCandidate = new MockCandidate();
    mockSavedListCandidateService.merge.and.returnValue(throwError('Add error'));
    component.candidateSource = new MockCandidateSource();

    component.addCandidateToList(mockCandidate);
    tick();

    expect(mockSavedListCandidateService.merge).toHaveBeenCalledWith(1, {candidateIds: [mockCandidate.id]});
    expect(component.error).toBe('Add error');
    expect(component.adding).toBeFalse();
  }));

  it('should handle candidate selection change for saved list', () => {
    const mockCandidate = new MockCandidate();
    component.candidateSource = new MockCandidateSource();
    component.selectedCandidates = [];
    spyOn(component.selectedCandidatesChange, 'emit');

    component.onSelectionChange(mockCandidate, true);

    expect(mockCandidate.selected).toBeTrue();
    expect(component.selectedCandidates).toContain(mockCandidate);
    expect(component.selectedCandidatesChange.emit).toHaveBeenCalledWith([mockCandidate]);
  });

  it('should handle candidate deselection for saved list', () => {
    const mockCandidate = new MockCandidate();
    component.candidateSource = new MockCandidateSource();
    component.selectedCandidates = [mockCandidate];
    spyOn(component.selectedCandidatesChange, 'emit');

    component.onSelectionChange(mockCandidate, false);

    expect(mockCandidate.selected).toBeFalse();
    expect(component.selectedCandidates).toEqual([]);
    expect(component.selectedCandidatesChange.emit).toHaveBeenCalledWith([]);
  });

  it('should trigger saveSelection when save button is clicked', () => {
    component.candidateSource = new MockCandidateSource();
    component.selectedCandidates = [new MockCandidate()];
    spyOn(component, 'saveSelection');
    fixture.detectChanges();

    const saveButton = fixture.debugElement.query(By.css('.save-selection-btn'));
    saveButton.triggerEventHandler('onClick', null);

    expect(component.saveSelection).toHaveBeenCalled();
  });

  it('should disable save button when no candidates are selected', () => {
    component.candidateSource = new MockCandidateSource();
    component.selectedCandidates = [];
    fixture.detectChanges();

    const saveButton = fixture.debugElement.query(By.css('.btn[title="Save selected candidates to a list"]'));
    expect(saveButton.nativeElement.disabled).toBeTrue();
  });

  it('should toggle description visibility when link is clicked', () => {
    fixture.detectChanges();

    const toggleLink = fixture.debugElement.query(By.css('.toggle-description'));
    expect(component.showDescription).toBeFalse();

    toggleLink.triggerEventHandler('onClick', null);
    fixture.detectChanges();

    expect(component.showDescription).toBeTrue();

    const icon = toggleLink.query(By.css('i'));
    expect(icon.nativeElement.classList).toContain('fa-chevron-up');
  });


  it('should call importCandidates when import button is clicked', () => {
    component.candidateSource = new MockCandidateSource();
    mockAuthorizationService.canImportToList.and.returnValue(true);
    spyOn(component, 'importCandidates');
    fixture.detectChanges();

    const importButton = fixture.debugElement.query(By.css('.import-candidates-btn'));
    importButton.triggerEventHandler('onClick', null);

    expect(component.importCandidates).toHaveBeenCalled();
  });

});
