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

import {ShowCandidatesComponent} from "./show-candidates.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {
  NgbModal,
  NgbOffcanvas,
  NgbPaginationModule,
  NgbTypeaheadModule
} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
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
import {of, Subscription, throwError} from "rxjs";
import {LocalStorageService} from "../../../services/local-storage.service";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
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
import {CasiAdminService} from "../../../services/casi-admin.service";
import {ListAction, ServiceList} from "../../../model/service-list";
import {Status} from "../../../model/base";

describe('ShowCandidatesComponent', () => {
  let component: ShowCandidatesComponent;
  let fixture: ComponentFixture<ShowCandidatesComponent>;
  let formBuilder: UntypedFormBuilder;

  // Mock services
  const mockCandidateService = jasmine.createSpyObj('CandidateService', ['search', 'findByCandidateNumberOrName', 'downloadCv', 'createUpdateOppsFromCandidates', 'createUpdateOppsFromCandidateList', 'updateStatus', 'resolveOutstandingTasks']);
  const mockCandidateSourceService = jasmine.createSpyObj('CandidateSourceService', ['copy', 'starSourceForUser', 'unstarSourceForUser']);
  const mockSavedSearchService = jasmine.createSpyObj('SavedSearchService', ['clearSelection', 'selectCandidate', 'getSelectionCount', 'updateSelectedStatuses', 'getSavedSearchTypeInfos', 'addWatcher', 'removeWatcher', 'saveSelection']);
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
  const mockCasiAdminService = jasmine.createSpyObj('CasiAdminService', ['getServiceList', 'performServiceListAction']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ShowCandidatesComponent, SortedByComponent, AutosaveStatusComponent, CandidateSourceDescriptionComponent],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        NgbTypeaheadModule,
        NgbPaginationModule,
        FormsModule,
        ReactiveFormsModule,
        NgSelectModule
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
        {provide: PublishedDocColumnService, useValue: mockPublishedDocColumnService},
        {provide: CasiAdminService, useValue: mockCasiAdminService}
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
    mockCasiAdminService.getServiceList.and.returnValue(of(null));
    mockCasiAdminService.performServiceListAction.and.returnValue(of(null));

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

  it('should load service list when candidate source changes to a saved list', fakeAsync(() => {
    const mockServiceList: ServiceList = {
      id: 99,
      provider: 'LINKEDIN',
      serviceCode: 'PREMIUM_MEMBERSHIP',
      listRole: 'USER_ISSUE_REPORT',
      permittedActions: [ListAction.REASSIGN]
    };
    mockCandidateSourceCandidateService.searchPaged.and.returnValue(
      of({content: [], totalElements: 0, number: 0, size: 10})
    );
    mockCasiAdminService.getServiceList.and.returnValue(of(mockServiceList));

    component.ngOnChanges({
      candidateSource: {
        currentValue: component.candidateSource,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    });
    tick();

    expect(mockCasiAdminService.getServiceList).toHaveBeenCalledWith(component.candidateSource.id);
    expect(component.serviceList).toEqual(mockServiceList);
  }));

  it('should not call getServiceList and set serviceList to null when source is not a saved list', fakeAsync(() => {
    mockCandidateSourceCandidateService.searchPaged.and.returnValue(
      of({content: [], totalElements: 0, number: 0, size: 10})
    );
    mockCasiAdminService.getServiceList.calls.reset();
    component.candidateSource = new MockSavedSearch();

    component.ngOnChanges({
      candidateSource: {
        currentValue: component.candidateSource,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    });
    tick();

    expect(mockCasiAdminService.getServiceList).not.toHaveBeenCalled();
    expect(component.serviceList).toBeNull();
  }));

  it('should set serviceList to null when service list fetch fails', fakeAsync(() => {
    mockCandidateSourceCandidateService.searchPaged.and.returnValue(
      of({content: [], totalElements: 0, number: 0, size: 10})
    );
    mockCasiAdminService.getServiceList.and.returnValue(throwError('Not found'));

    component.ngOnChanges({
      candidateSource: {
        currentValue: component.candidateSource,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    });
    tick();

    expect(component.serviceList).toBeNull();
  }));

  it('should set actionSuccessMessage on successful service list action', fakeAsync(() => {
    component.serviceList = {
      id: 99,
      provider: 'LINKEDIN',
      serviceCode: 'PREMIUM_MEMBERSHIP',
      listRole: 'USER_ISSUE_REPORT',
      permittedActions: [ListAction.REASSIGN]
    };
    const mockCandidate = new MockCandidate();
    component.selectedCandidates = [mockCandidate];
    mockCasiAdminService.performServiceListAction.and.returnValue(of(null));

    component.onServiceListAction(ListAction.REASSIGN);
    tick();

    expect(mockCasiAdminService.performServiceListAction).toHaveBeenCalledWith(
      99, ListAction.REASSIGN, [mockCandidate.candidateNumber]
    );
    expect(component.actionSuccessMessage).not.toBeNull();
  }));

  it('should set error and leave actionSuccessMessage null on failed service list action', fakeAsync(() => {
    component.serviceList = {
      id: 99,
      provider: 'LINKEDIN',
      serviceCode: 'PREMIUM_MEMBERSHIP',
      listRole: 'USER_ISSUE_REPORT',
      permittedActions: [ListAction.REASSIGN]
    };
    component.selectedCandidates = [new MockCandidate()];
    mockCasiAdminService.performServiceListAction.and.returnValue(throwError('Action failed'));

    component.onServiceListAction(ListAction.REASSIGN);
    tick();

    expect(component.error).toBe('Action failed');
    expect(component.actionSuccessMessage).toBeNull();
  }));

  describe('page size preference', () => {
    const triggerSourceChange = () => component.ngOnChanges({
      candidateSource: {
        currentValue: component.candidateSource,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    });

    beforeEach(() => {
      mockCandidateSourceCandidateService.searchPaged.and.returnValue(
        of({content: [], totalElements: 0, number: 0, size: 20})
      );
    });

    it('should fall back to 20 when no preference is stored and @Input is not set', fakeAsync(() => {
      mockLocalStorageService.get.and.returnValue(null);
      component.pageSize = undefined as any;

      triggerSourceChange();
      tick();

      expect(component.pageSize).toBe(20);
    }));

    it('should fall back to 20 when stored value is not a valid option', fakeAsync(() => {
      mockLocalStorageService.get.and.callFake((key: string) =>
        key.endsWith('PageSize') ? '42' : null
      );
      component.pageSize = undefined as any;

      triggerSourceChange();
      tick();

      expect(component.pageSize).toBe(20);
    }));

    it('should restore a valid stored preference', fakeAsync(() => {
      mockLocalStorageService.get.and.callFake((key: string) =>
        key.endsWith('PageSize') ? '50' : null
      );

      triggerSourceChange();
      tick();

      expect(component.pageSize).toBe(50);
    }));

    it('should keep a valid @Input value when no preference is stored', fakeAsync(() => {
      mockLocalStorageService.get.and.returnValue(null);
      component.pageSize = 50;

      triggerSourceChange();
      tick();

      expect(component.pageSize).toBe(50);
    }));

    it('onPageSizeChange should update pageSize, reset pageNumber, persist preference and trigger search',
      () => {
      spyOn(component, 'doSearch');
      mockLocalStorageService.set.calls.reset();

      component.onPageSizeChange(50);

      expect(component.pageSize).toBe(50);
      expect(component.pageNumber).toBe(1);
      expect(mockLocalStorageService.set).toHaveBeenCalledWith(
        jasmine.stringContaining('PageSize'), '50'
      );
      expect(component.doSearch).toHaveBeenCalledWith(true);
    });
  });

  describe('additional branch coverage', () => {
    const savedSearchSource = () => {
      const source = new MockSavedSearch();
      (source as any).id = 1;
      return source;
    };

    const modalRef = (result: Promise<any> = Promise.resolve(undefined)) => ({
      componentInstance: {},
      result,
      close: jasmine.createSpy('close')
    } as any);

    beforeEach(() => {
      mockNgbModal.open.calls.reset();
      mockCandidateService.search.calls.reset();
      mockCandidateService.findByCandidateNumberOrName.calls.reset();
      mockSavedSearchService.selectCandidate.calls.reset();
      mockSavedSearchService.getSelectionCount.calls.reset();
      mockSavedSearchService.updateSelectedStatuses.calls.reset();
      mockSavedSearchService.addWatcher.calls.reset();
      mockSavedSearchService.removeWatcher.calls.reset();
      mockCandidateSourceService.copy.calls.reset();
      mockCandidateSourceService.starSourceForUser.calls.reset();
      mockCandidateSourceService.unstarSourceForUser.calls.reset();
      mockSavedListCandidateService.remove.calls.reset();
      mockSavedListCandidateService.saveSelection.calls.reset();
      mockSavedListCandidateService.create.calls.reset();
      mockSavedListService.publish.calls.reset();
      mockSavedListService.importEmployerFeedback.calls.reset();
      mockSavedListService.createFolder.calls.reset();
      component.selectedCandidates = [];
      component.loggedInUser = new MockUser();
      component.pageNumber = 1;
      component.pageSize = 20;
    });

    it('should expose list/search derived values', () => {
      component.candidateSource = new MockCandidateSource();
      component.searchInResultsForm = formBuilder.group({keyword: ['abc'], showClosedOpps: [true]});
      component.selectedCandidates = [new MockCandidate()];
      expect(component.pluralType).toBe('lists');
      expect(component.keyword).toBe('abc');
      expect(component.numberSelections).toBe(1);
      expect(component.isSavedList()).toBeTrue();
      expect(component.isSavedSearch()).toBeFalse();
      expect(component.isSwapSelectionSupported()).toBeTrue();
      expect(component.isSelection()).toBeTrue();
      expect(component.displayTextMatchRank()).toBeFalse();

      component.candidateSource = savedSearchSource();
      component.isKeywordSearch = true;
      expect(component.pluralType).toBe('searches');
      expect(component.isSavedList()).toBeFalse();
      expect(component.isSavedSearch()).toBeTrue();
      expect(component.isSwapSelectionSupported()).toBeFalse();
      expect(component.displayTextMatchRank()).toBeTrue();
    });

    it('should return empty keyword without a form', () => {
      component.searchInResultsForm = null;
      expect(component.keyword).toBe('');
    });

    it('should restore showClosedOpps from local storage and default to true', () => {
      component.candidateSource = new MockCandidateSource();
      mockLocalStorageService.get.and.returnValues(null, 'false', 'true');
      expect(component.showClosedOpps).toBeTrue();
      expect(component.showClosedOpps).toBeFalse();
      expect(component.showClosedOpps).toBeTrue();
    });

    it('should persist filters and search after debounce', fakeAsync(() => {
      component.candidateSource = new MockCandidateSource();
      component.searchInResultsForm = formBuilder.group({keyword: [''], showClosedOpps: [true]});
      spyOn(component, 'doSearch');
      component.subscribeToFilterChanges();
      component.searchInResultsForm.patchValue({keyword: 'new'});
      tick(801);
      expect(component.filterSearch).toBeTrue();
      expect(mockLocalStorageService.set).toHaveBeenCalledWith('ListKey1ShowClosedOpps', 'true');
      expect(component.doSearch).toHaveBeenCalledWith(true);
    }));

    it('should unsubscribe on destroy', () => {
      component.subscription = new Subscription();
      spyOn(component.subscription, 'unsubscribe');
      component.ngOnDestroy();
      expect(component.subscription.unsubscribe).toHaveBeenCalled();
    });

    it('should select candidates according to source type', () => {
      const candidate = new MockCandidate();
      candidate.selected = true;
      component.candidateSource = savedSearchSource();
      expect(component.isSelected(candidate)).toBeTrue();
      component.candidateSource = new MockCandidateSource();
      component.selectedCandidates = [candidate];
      expect(component.isSelected(candidate)).toBeTrue();
      component.selectedCandidates = [];
      expect(component.isSelected(candidate)).toBeFalse();
    });

    it('should set current candidate and emit selection', () => {
      const candidate = new MockCandidate();
      candidate.selected = true;
      component.candidateSource = savedSearchSource();
      spyOn(component.candidateSelection, 'emit');
      component.setCurrentCandidate(candidate);
      expect(component.currentCandidate).toBe(candidate);
      expect(component.savedSearchSelectionChange).toBeTrue();
      expect(component.candidateSelection.emit).toHaveBeenCalledWith(candidate);
    });

    it('should toggle fetch and refresh', () => {
      component.useOldFetch = false;
      spyOn(component, 'doSearch');
      component.toggleFetch();
      expect(component.useOldFetch).toBeTrue();
      expect(component.doSearch).toHaveBeenCalledWith(true);
    });

    it('should apply review filter and search without page number', () => {
      component.searchInResultsForm = formBuilder.group({statusesDisplay: [['rejected']]});
      spyOn(component, 'doSearch');
      component.onReviewStatusFilterChange();
      expect(component.reviewStatusFilter).toEqual(['rejected']);
      expect(component.doSearch).toHaveBeenCalledWith(false, false);
    });

    it('should identify target list state', () => {
      component.targetListName = null;
      expect(component.haveTargetList()).toBeFalsy();
      component.targetListName = 'Target';
      expect(component.haveTargetList()).toBeTrue();
    });

    it('should render candidate rows with or without names', () => {
      const candidate = new MockCandidate();
      mockCandidateFieldService.isCandidateNameViewable.and.returnValue(true);
      expect(component.renderCandidateRow(candidate)).toContain(candidate.candidateNumber);
      expect(component.renderCandidateRow(candidate)).toContain(candidate.user.firstName);
      mockCandidateFieldService.isCandidateNameViewable.and.returnValue(false);
      expect(component.renderCandidateRow(candidate)).toBe(candidate.candidateNumber);
    });

    it('should clear typeahead input and add selected candidate', () => {
      const candidate = new MockCandidate();
      const event = {preventDefault: jasmine.createSpy('preventDefault'), item: candidate};
      const input = {value: 'old'};
      spyOn(component, 'addCandidateToList');
      component.selectCandidateToAdd(event, input);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(input.value).toBe('');
      expect(component.addCandidateToList).toHaveBeenCalledWith(candidate);
    });

    it('should remove one candidate and refresh on success', fakeAsync(() => {
      const first = new MockCandidate();
      const second = new MockCandidate();
      second.id = 2;
      component.selectedCandidates = [first, second];
      mockSavedListCandidateService.remove.and.returnValue(of({}));
      spyOn(component, 'doSearch');
      component.removeCandidateFromList(first);
      tick();
      expect(component.selectedCandidates).toEqual([second]);
      expect(mockSavedListCandidateService.remove).toHaveBeenCalledWith(1, {candidateIds: [first.id]});
      expect(component.doSearch).toHaveBeenCalledWith(true);
    }));

    it('should expose remove error', fakeAsync(() => {
      const candidate = new MockCandidate();
      component.selectedCandidates = [candidate];
      mockSavedListCandidateService.remove.and.returnValue(throwError('remove failed'));
      component.removeSelectedCandidatesFromList();
      tick();
      expect(component.error).toBe('remove failed');
    }));

    it('should swap saved-list selection successfully', fakeAsync(() => {
      const first = new MockCandidate();
      const second = new MockCandidate();
      second.id = 2;
      component.selectedCandidates = [first];
      mockCandidateSourceCandidateService.search.and.returnValue(of([first, second]));
      spyOn(component.selectedCandidatesChange, 'emit');
      component.swapSelection();
      tick();
      expect(component.selectedCandidates).toEqual([second]);
      expect(component.searching).toBeFalse();
      expect(component.selectedCandidatesChange.emit).toHaveBeenCalledWith([second]);
    }));

    it('should handle swap selection failure', fakeAsync(() => {
      mockCandidateSourceCandidateService.search.and.returnValue(throwError('swap failed'));
      component.swapSelection();
      tick();
      expect(component.error).toBe('swap failed');
      expect(component.searching).toBeFalse();
    }));

    it('should reject exports over 5000 candidates', () => {
      component.results = {totalElements: 5001} as SearchResults<Candidate>;
      expect(() => component.exportCandidates()).toThrowError(/capped at 5,000/);
      expect(component.error).toContain('capped at 5,000');
    });

    it('should publish candidates and update source', fakeAsync(() => {
      const result: any = {...component.candidateSource, publishedDocLink: 'doc', exportColumns: ['a']};
      mockSavedListService.publish.and.returnValue(of(result));
      (component as any).publishCandidates([{key: 'a'}]);
      tick();
      expect((component.candidateSource as any).publishedDocLink).toBe('doc');
      expect(component.candidateSource.exportColumns as unknown[]).toEqual(['a']);
      expect(component.publishing).toBeFalse();
    }));

    it('should expose publish failure', fakeAsync(() => {
      mockSavedListService.publish.and.returnValue(throwError('publish failed'));
      (component as any).publishCandidates([]);
      tick();
      expect(component.error).toBe('publish failed');
      expect(component.publishing).toBeFalse();
    }));

    it('should import employer feedback and refresh', fakeAsync(() => {
      const report: any = {message: 'Done', numEmployerFeedbacks: 0, numJobOffers: 0, numNoJobOffers: 0};
      mockSavedListService.importEmployerFeedback.and.returnValue(of(report));
      spyOn(component, 'doSearch');
      spyOn<any>(component, 'displayImportFeedbackReport');
      component.importEmployerFeedback();
      tick();
      expect(component.doSearch).toHaveBeenCalledWith(true);
      expect((component as any).displayImportFeedbackReport).toHaveBeenCalledWith(report);
      expect(component.importingFeedback).toBeFalse();
    }));

    it('should expose employer feedback import failure', fakeAsync(() => {
      mockSavedListService.importEmployerFeedback.and.returnValue(throwError('feedback failed'));
      component.importEmployerFeedback();
      tick();
      expect(component.error).toBe('feedback failed');
      expect(component.importingFeedback).toBeFalse();
    }));

    it('should build complete employer feedback report text', () => {
      const ref = modalRef();
      mockNgbModal.open.and.returnValue(ref);
      (component as any).displayImportFeedbackReport({
        message: 'Imported', numEmployerFeedbacks: 2, numJobOffers: 3, numNoJobOffers: 4
      });
      expect(ref.componentInstance.title).toBe('Feedback Import Report');
      expect(ref.componentInstance.message).toContain('2 candidates');
      expect(ref.componentInstance.message).toContain('3 candidates');
      expect(ref.componentInstance.message).toContain('4 candidates');
    });

    it('should open publish column selector and publish selected columns', fakeAsync(() => {
      const columns: any[] = [{key: 'one'}];
      const ref = modalRef(Promise.resolve(columns));
      mockNgbModal.open.and.returnValue(ref);
      mockPublishedDocColumnService.getColumnConfigFromAllColumns.and.returnValue(columns);
      mockPublishedDocColumnService.getColumnConfigFromExportColumns.and.returnValue(columns);
      spyOn<any>(component, 'publishCandidates');
      component.modifyExportColumns();
      tick();
      expect(ref.componentInstance.availableColumns).toBe(columns);
      expect((component as any).publishCandidates).toHaveBeenCalledWith(columns);
    }));

    it('should build breadcrumbs for lists and searches', () => {
      component.candidateSource = new MockCandidateSource();
      expect(component.getCandidateSourceBreadcrumb(component.candidateSource)).toContain(component.candidateSource.name);
      expect(component.getBreadcrumb()).toContain(component.candidateSource.name);
      component.candidateSource = savedSearchSource();
      mockSavedSearchService.getSavedSearchTypeInfos.and.returnValue([]);
      expect(component.getBreadcrumb()).toBeDefined();
    });

    it('should expose source flags and links', () => {
      component.candidateSource = new MockCandidateSource();
      (component.candidateSource as any).publishedDocLink = 'https://example.test/doc';
      (component.candidateSource as any).savedSearchSource = {id: 9};
      expect(component.hasPublishedDoc()).toBeTrue();
      expect(component.hasSavedSearchSource()).toBeTrue();
      expect(component.isDefaultSavedSearch()).toBeFalse();
      component.candidateSource = savedSearchSource();
      (component.candidateSource as any).defaultSearch = true;
      expect(component.isDefaultSavedSearch()).toBeTrue();
      expect(component.hasSavedSearchSource()).toBeFalse();
    });

    it('should open published and Salesforce links', () => {
      component.candidateSource = new MockCandidateSource();
      (component.candidateSource as any).publishedDocLink = 'https://example.test/doc';
      spyOn(window, 'open');
      component.doShowPublishedDoc();
      expect(window.open).toHaveBeenCalledWith('https://example.test/doc', '_blank');
      mockSalesforceService.joblink.and.returnValue('https://example.test/sf');
      component.doShowSalesforceLink();
      expect(window.open).toHaveBeenCalledWith('https://example.test/sf', '_blank');
    });

    it('should create a list folder when missing', fakeAsync(() => {
      const result: any = {...component.candidateSource, folderlink: 'https://example.test/folder'};
      (component.candidateSource as any).folderlink = null;
      mockSavedListService.createFolder.and.returnValue(of(result));
      spyOn(window, 'open');
      component.doShowListFolder();
      tick();
      expect(component.candidateSource).toBe(result);
      expect(window.open).toHaveBeenCalledWith(result.folderlink, '_blank');
      expect(component.searching).toBeFalse();
    }));

    it('should expose folder creation failure', fakeAsync(() => {
      (component.candidateSource as any).folderlink = null;
      mockSavedListService.createFolder.and.returnValue(throwError('folder failed'));
      component.doShowListFolder();
      tick();
      expect(component.error).toBe('folder failed');
      expect(component.searching).toBeFalse();
    }));

    it('should emit edit source', () => {
      spyOn(component.editSource, 'emit');
      component.doEditSource();
      expect(component.editSource.emit).toHaveBeenCalledWith(component.candidateSource);
    });

    it('should test task assignment helpers', () => {
      const candidate: any = new MockCandidate();
      candidate.taskAssignments = [
        {status: Status.active, completedDate: new Date(), abandonedDate: null, task: {id: 1, optional: false}},
        {status: Status.active, completedDate: null, abandonedDate: null, task: {id: 2, optional: true}},
        {status: Status.inactive, completedDate: new Date(), abandonedDate: null, task: {id: 1, optional: false}}
      ];
      expect(component.hasTaskAssignments(candidate)).toBeTrue();
      expect(component.getCompletedMonitoredTasks(candidate).length).toBe(1);
      expect(component.getTotalMonitoredTasks(candidate).length).toBe(1);
      component.monitoredTask = {id: 1} as any;
      expect(component.getCompletedMonitoredTasks(candidate).length).toBe(1);
      expect(component.getTotalMonitoredTasks(candidate).length).toBe(1);
    });

    it('should resolve outstanding tasks and refresh', fakeAsync(() => {
      component.selectedCandidates = [new MockCandidate()];
      mockCandidateService.resolveOutstandingTasks.and.returnValue(of({}));
      spyOn(component, 'doSearch');
      component.resolveTaskAssignments();
      tick();
      expect(component.doSearch).toHaveBeenCalledWith(true);
      expect(component.updatingTasks).toBeFalse();
    }));

    it('should reject resolving tasks without selection', () => {
      component.selectedCandidates = [];
      component.resolveTaskAssignments();
      expect(component.error).toBe('No candidates are selected');
    });

    it('should expose resolve task failure', fakeAsync(() => {
      component.selectedCandidates = [new MockCandidate()];
      mockCandidateService.resolveOutstandingTasks.and.returnValue(throwError('task failed'));
      component.resolveTaskAssignments();
      tick();
      expect(component.error).toBe('task failed');
      expect(component.updatingTasks).toBeFalse();
    }));

    it('should calculate review status', () => {
      const candidate: any = new MockCandidate();
      candidate.candidateReviewStatusItems = [];
      expect(component.getReviewStatus(candidate)).toBe(component.ReviewStatus.unverified);
      candidate.candidateReviewStatusItems = [{savedSearch: {id: component.candidateSource.id}, reviewStatus: 'verified'}];
      expect(component.getReviewStatus(candidate)).toBe(component.ReviewStatus.verified);
    });

    it('should detect watching state', () => {
      component.loggedInUser = new MockUser();
      (component.candidateSource as any).watcherUserIds = undefined;
      expect(component.isWatching()).toBeFalse();
      (component.candidateSource as any).watcherUserIds = [component.loggedInUser.id];
      expect(component.isWatching()).toBeTrue();
    });

    it('should add and remove watcher', fakeAsync(() => {
      const source: any = savedSearchSource();
      source.watcherUserIds = [];
      component.candidateSource = source;
      mockSavedSearchService.addWatcher.and.returnValue(of({...source, watcherUserIds: [component.loggedInUser.id]}));
      component.doToggleWatch();
      tick();
      expect(mockSavedSearchService.addWatcher).toHaveBeenCalled();
      expect(component.loading).toBeFalse();

      mockSavedSearchService.removeWatcher.and.returnValue(of({...source, watcherUserIds: []}));
      component.doToggleWatch();
      tick();
      expect(mockSavedSearchService.removeWatcher).toHaveBeenCalled();
      expect(component.loading).toBeFalse();
    }));

    it('should expose watcher errors', fakeAsync(() => {
      const source: any = savedSearchSource();
      source.watcherUserIds = [];
      component.candidateSource = source;
      mockSavedSearchService.addWatcher.and.returnValue(throwError('watch failed'));
      component.doToggleWatch();
      tick();
      expect(component.error).toBe('watch failed');
      expect(component.loading).toBeFalse();
    }));
  });

  describe('expanded workflow coverage', () => {
    const savedSearchSource = (): any => {
      const source: any = new MockSavedSearch();
      source.id = 1;
      source.reviewable = true;
      source.defaultSearch = false;
      source.watcherUserIds = [];
      return source;
    };

    const modalRef = (result: Promise<any> = Promise.resolve(undefined)): any => ({
      componentInstance: {
        onAction: {
          subscribe: (callback: () => void) => callback()
        },
        setTasks: jasmine.createSpy('setTasks')
      },
      result,
      close: jasmine.createSpy('close')
    });

    beforeEach(() => {
      component.selectedCandidates = [];
      component.loggedInUser = new MockUser();
      component.pageNumber = 1;
      component.pageSize = 20;
      mockCandidateService.downloadCv.calls.reset();
      mockNgbModal.open.calls.reset();
      component.results = {
        content: [],
        totalElements: 0,
        number: 0,
        size: 20
      } as SearchResults<Candidate>;
    });

    it('should initialize a saved search and emit loaded selections', fakeAsync(() => {
      const source = savedSearchSource();
      const selected = [new MockCandidate()];
      component.candidateSource = source;
      mockSavedListCandidateService.getSelectionListCandidates.and.returnValue(of(selected));
      spyOn(component.selectedCandidatesChange, 'emit');

      component.ngOnInit();
      tick();

      expect(mockSavedListCandidateService.getSelectionListCandidates).toHaveBeenCalledWith(source.id);
      expect(component.selectedCandidates).toEqual(selected);
      expect(component.selectedCandidatesChange.emit).toHaveBeenCalledWith(selected);
      expect(component.searchInResultsForm.get('statusesDisplay')).toBeTruthy();
      expect(component.statuses.length).toBe(3);
    }));

    it('should expose saved-search initialization errors', fakeAsync(() => {
      component.candidateSource = savedSearchSource();
      mockSavedListCandidateService.getSelectionListCandidates.and.returnValue(
        throwError('selection load failed')
      );

      component.ngOnInit();
      tick();

      expect(component.error).toBe('selection load failed');
    }));

    it('should search candidate names through the typeahead and recover from errors', fakeAsync(() => {
      component.candidateSource = new MockCandidateSource();
      const candidate = new MockCandidate();
      mockCandidateService.findByCandidateNumberOrName.and.returnValue(
        of({content: [candidate]})
      );

      component.ngOnInit();

      let output: Candidate[];
      component.doNumberOrNameSearch(of('abc')).subscribe((value: Candidate[]) => output = value);
      tick(301);

      expect(mockCandidateService.findByCandidateNumberOrName).toHaveBeenCalledWith({
        candidateNumberOrName: 'abc',
        pageSize: 10
      });
      expect(output).toEqual([candidate]);
      expect(component.searchFailed).toBeFalse();

      mockCandidateService.findByCandidateNumberOrName.and.returnValue(
        throwError('lookup failed')
      );
      component.doNumberOrNameSearch(of('bad')).subscribe((value: Candidate[]) => output = value);
      tick(301);

      expect(output).toEqual([]);
      expect(component.searchFailed).toBeTrue();
    }));

    it('should process searchRequest changes and selected-candidate clearing', () => {
      component.searchRequest = {
        simpleQueryString: 'test'
      } as any;
      spyOn<any>(component, 'updatedSearch');
      spyOn(component, 'doSearch');

      component.ngOnChanges({
        searchRequest: {
          previousValue: null,
          currentValue: component.searchRequest,
          firstChange: false,
          isFirstChange: () => false
        },
        selectedCandidates: {
          previousValue: [new MockCandidate()],
          currentValue: [],
          firstChange: false,
          isFirstChange: () => false
        }
      });

      expect(component.pageNumber).toBe(1);
      expect((component as any).updatedSearch).toHaveBeenCalled();
      expect(component.doSearch).toHaveBeenCalledWith(true);
    });

    it('should execute updated searches and normalize invalid text-match sorting', fakeAsync(() => {
      component.searchRequest = {
        simpleQueryString: ' ',
        reviewStatusFilter: null
      } as any;
      component.sortField = 'text_match';
      component.sortDirection = 'ASC';
      component.pageNumber = 2;
      component.pageSize = 50;
      component.reviewStatusFilter = ['verified'];
      component.useOldFetch = true;

      spyOn(component, 'isReviewable').and.returnValue(true);
      spyOn<any>(component, 'cacheResults');
      mockCandidateService.search.and.returnValue(of({
        content: [],
        totalElements: 0,
        number: 1,
        size: 50
      }));

      (component as any).updatedSearch();
      tick();

      expect(component.sortField).toBe('id');
      expect(component.sortDirection).toBe('DESC');
      expect(component.searchRequest.pageNumber).toBe(1);
      expect(component.searchRequest.pageSize).toBe(50);
      expect(component.searchRequest.reviewStatusFilter).toEqual(['verified']);
      expect(mockCandidateService.search).toHaveBeenCalledWith(component.searchRequest, true);
      expect((component as any).cacheResults).toHaveBeenCalled();
      expect(component.searching).toBeFalse();
    }));

    it('should expose updated-search failures', fakeAsync(() => {
      component.searchRequest = {simpleQueryString: 'query'} as any;
      mockCandidateService.search.and.returnValue(throwError('request failed'));

      (component as any).updatedSearch();
      tick();

      expect(component.error).toBe('request failed');
      expect(component.searching).toBeFalse();
    }));

    it('should delegate doSearch to updatedSearch when a request is supplied', () => {
      component.searchRequest = {simpleQueryString: 'query'} as any;
      spyOn<any>(component, 'checkCache').and.returnValue(false);
      spyOn<any>(component, 'updatedSearch');

      component.doSearch(true);

      expect((component as any).updatedSearch).toHaveBeenCalled();
    });

    it('should restore the current candidate after a normal search', fakeAsync(() => {
      const candidate = new MockCandidate();
      component.currentCandidate = candidate;
      component.results = {
        content: [candidate],
        totalElements: 1,
        number: 0,
        size: 20
      } as SearchResults<Candidate>;
      spyOn<any>(component, 'checkCache').and.returnValue(false);
      spyOn<any>(component, 'performSearch').and.returnValue(of(null));
      spyOn(component, 'setCurrentCandidate').and.callThrough();

      component.doSearch(true);
      tick();

      expect((component as any).performSearch).toHaveBeenCalled();
      expect(component.setCurrentCandidate).toHaveBeenCalledWith(candidate);
    }));

    it('should restore saved search card scroll position', fakeAsync(() => {
      const card = {scrollTo: jasmine.createSpy('scrollTo')};
      (component as any).searchCard = card;
      (component as any).searchCardScrollTop = 75;

      component.setSearchCardScrollTop();
      tick(201);

      expect(card.scrollTo).toHaveBeenCalledWith({top: 75, behavior: 'smooth'});
    }));

    it('should preserve profile scroll position when selecting another candidate', () => {
      const current = new MockCandidate();
      const next = new MockCandidate();
      next.id = 2;
      component.currentCandidate = current;
      const profile = document.createElement('div');
      profile.className = 'profile';
      Object.defineProperty(profile, 'scrollTop', {value: 45, configurable: true});
      document.body.appendChild(profile);
      spyOn(component, 'setCurrentCandidate');

      component.selectCandidate(next);

      expect((component as any).searchCardScrollTop).toBe(45);
      expect(component.setCurrentCandidate).toHaveBeenCalledWith(next);
      profile.remove();
    });

    it('should open import modal and import the selected file', fakeAsync(() => {
      const file = new File(['1'], 'candidates.csv', {type: 'text/csv'});
      const ref = modalRef(Promise.resolve([file]));
      mockNgbModal.open.and.returnValue(ref);
      spyOn<any>(component, 'doImport');

      component.importCandidates();
      tick();

      expect(ref.componentInstance.validExtensions).toEqual(['csv', 'txt']);
      expect(ref.componentInstance.maxFiles).toBe(1);
      expect((component as any).doImport).toHaveBeenCalledWith([file]);
    }));

    it('should complete and fail file imports correctly', fakeAsync(() => {
      const file = new File(['1'], 'candidates.csv', {type: 'text/csv'});
      mockSavedListCandidateService.mergeFromFile.and.returnValue(of({}));
      spyOn(component, 'doSearch');

      (component as any).doImport([file]);
      tick();

      expect(mockSavedListCandidateService.mergeFromFile).toHaveBeenCalled();
      expect(component.doSearch).toHaveBeenCalledWith(true);
      expect(component.importing).toBeFalse();

      mockSavedListCandidateService.mergeFromFile.and.returnValue(throwError('import failed'));
      (component as any).doImport([file]);
      tick();

      expect(component.error).toBe('import failed');
      expect(component.importing).toBeFalse();
    }));

    it('should open the named CV modal when names are viewable', fakeAsync(() => {
      const candidate = new MockCandidate();
      const ref = modalRef(Promise.resolve(null));
      mockNgbModal.open.and.returnValue(ref);
      spyOn(component, 'canViewCandidateName').and.returnValue(true);

      component.downloadGeneratedCV(candidate);
      tick();

      expect(ref.componentInstance.candidateId).toBe(candidate.id);
      expect(mockCandidateService.downloadCv).not.toHaveBeenCalled();
    }));

    it('should download an anonymised CV and expose download errors', fakeAsync(() => {
      const candidate = new MockCandidate();
      const tab: any = {location: {href: ''}};
      spyOn(component, 'canViewCandidateName').and.returnValue(false);
      spyOn(window, 'open').and.returnValue(tab);
      spyOn(URL, 'createObjectURL').and.returnValue('blob:test');
      mockCandidateService.downloadCv.and.returnValue(of(new Blob(['cv'])));

      component.downloadGeneratedCV(candidate);
      tick();

      expect(mockCandidateService.downloadCv).toHaveBeenCalledWith({
        candidateId: candidate.id,
        showName: false,
        showContact: false
      });
      expect(tab.location.href).toBe('blob:test');

      mockCandidateService.downloadCv.and.returnValue(throwError('cv failed'));
      component.downloadGeneratedCV(candidate);
      tick();
      expect(component.error).toBe('cv failed');
    }));

    it('should update a saved-search selection and expose server failure', fakeAsync(() => {
      const candidate = new MockCandidate();
      component.candidateSource = savedSearchSource();
      component.selectedCandidates = [];
      mockSavedSearchService.selectCandidate.and.returnValue(of(null));

      component.onSelectionChange(candidate, true);
      tick();

      expect(mockSavedSearchService.selectCandidate).toHaveBeenCalledWith(
        component.candidateSource.id,
        {userId: component.loggedInUser.id, candidateId: candidate.id, selected: true}
      );
      expect(component.savedSearchSelectionChange).toBeTrue();

      mockSavedSearchService.selectCandidate.and.returnValue(throwError('selection failed'));
      component.onSelectionChange(candidate, false);
      tick();
      expect(component.error).toBe('selection failed');
    }));

    it('should confirm context-note loss before deselecting a saved-search candidate', fakeAsync(() => {
      const candidate = new MockCandidate();
      candidate.selected = true;
      candidate.contextNote = 'Important';
      component.candidateSource = savedSearchSource();
      component.selectedCandidates = [candidate];
      mockNgbModal.open.and.returnValue(modalRef(Promise.resolve(true)));
      mockSavedSearchService.selectCandidate.and.returnValue(of(null));

      component.onSelectionChange(candidate, false);
      tick();

      expect(candidate.contextNote).toBeNull();
      expect(component.savedSearchSelectionChange).toBeFalse();
      expect(mockSavedSearchService.selectCandidate).toHaveBeenCalled();
    }));

    it('should restore selection when context-note deselection is declined or dismissed', fakeAsync(() => {
      const candidate = new MockCandidate();
      candidate.contextNote = 'Important';
      component.candidateSource = savedSearchSource();
      component.selectedCandidates = [candidate];

      mockNgbModal.open.and.returnValue(modalRef(Promise.resolve(false)));
      component.onSelectionChange(candidate, false);
      tick();
      expect(candidate.selected).toBeTrue();

      mockNgbModal.open.and.returnValue(modalRef(Promise.reject('dismissed')));
      component.onSelectionChange(candidate, false);
      tick();
      expect(candidate.selected).toBeTrue();
    }));

    it('should validate save selection for list and saved search sources', fakeAsync(() => {
      spyOn<any>(component, 'requestSaveSelection');

      component.candidateSource = new MockCandidateSource();
      component.selectedCandidates = [];
      component.saveSelection();
      expect(component.error).toBe('No candidates are selected');

      component.selectedCandidates = [new MockCandidate()];
      component.saveSelection();
      expect((component as any).requestSaveSelection).toHaveBeenCalled();

      component.candidateSource = savedSearchSource();
      mockSavedSearchService.getSelectionCount.and.returnValue(of(0));
      component.saveSelection();
      tick();
      expect(component.error).toBe('No candidates are selected');

      mockSavedSearchService.getSelectionCount.and.returnValue(of(2));
      component.saveSelection();
      tick();
      expect((component as any).requestSaveSelection).toHaveBeenCalled();

      mockSavedSearchService.getSelectionCount.and.returnValue(throwError('count failed'));
      component.saveSelection();
      tick();
      expect(component.error).toBe('count failed');
    }));

    it('should configure and process the save-selection modal', fakeAsync(() => {
      const selection = {savedListId: 2, replace: false};
      const ref = modalRef(Promise.resolve(selection));
      mockNgbModal.open.and.returnValue(ref);
      (component.candidateSource as any).sfJobOpp = {id: 8};
      spyOn<any>(component, 'doSaveSelection');

      (component as any).requestSaveSelection();
      tick();

      expect(ref.componentInstance.action).toBe('Save');
      expect(ref.componentInstance.jobId).toBe(8);
      expect(ref.componentInstance.excludeList).toBe(component.candidateSource);
      expect((component as any).doSaveSelection).toHaveBeenCalledWith(selection);
    }));

    it('should save saved-search selections successfully and handle failure', fakeAsync(() => {
      component.candidateSource = savedSearchSource();
      const target = {
        savedListId: 5,
        newListName: 'Target',
        replace: true,
        jobId: 9,
        statusUpdateInfo: {status: 'active'}
      };
      const result: any = {id: 5, name: 'Target'};
      mockSavedSearchService.saveSelection.and.returnValue(of(result));
      spyOn<any>(component, 'cacheTargetList');
      spyOn(component, 'doSearch');
      const cacheService = (component as any).candidateSourceResultsCacheService;
      spyOn(cacheService, 'removeFromCache');

      (component as any).doSaveSelection(target);
      tick();

      expect(mockSavedSearchService.saveSelection).toHaveBeenCalled();
      expect(component.targetListId).toBe(5);
      expect(component.targetListName).toBe('Target');
      expect(component.savedSelection).toBeTrue();
      expect(component.savingSelection).toBeFalse();
      expect(component.doSearch).toHaveBeenCalledWith(true);

      mockSavedSearchService.saveSelection.and.returnValue(throwError('save failed'));
      (component as any).doSaveSelection(target);
      tick();
      expect(component.error).toBe('save failed');
      expect(component.savingSelection).toBeFalse();
    }));

    it('should merge into an existing list and create a new list', fakeAsync(() => {
      const candidate = new MockCandidate();
      component.selectedCandidates = [candidate];
      mockSavedListService.get.and.returnValue(of({id: 7, name: 'Existing'} as any));
      mockSavedListCandidateService.saveSelection.and.returnValue(of(null));
      const cacheService = (component as any).candidateSourceResultsCacheService;
      spyOn(cacheService, 'removeFromCache');

      (component as any).doSaveSelection({
        savedListId: 7,
        replace: true,
        statusUpdateInfo: null
      });
      tick();

      expect(component.targetListId).toBe(7);
      expect(component.targetListName).toBe('Existing');
      expect(component.targetListReplace).toBeTrue();
      expect(component.savedSelection).toBeTrue();

      mockSavedListCandidateService.create.and.returnValue(
        of({id: 8, name: 'Created'} as any)
      );
      spyOn<any>(component, 'cacheTargetList');
      (component as any).doSaveSelection({
        savedListId: 0,
        newListName: 'Created',
        replace: false,
        statusUpdateInfo: null
      });
      tick();

      expect(component.targetListId).toBe(8);
      expect(component.targetListName).toBe('Created');
      expect(component.targetListReplace).toBeFalse();
      expect((component as any).cacheTargetList).toHaveBeenCalled();
    }));

    it('should expose merge and create list failures', fakeAsync(() => {
      component.selectedCandidates = [new MockCandidate()];
      mockSavedListService.get.and.returnValue(of({id: 7, name: 'Existing'} as any));
      mockSavedListCandidateService.saveSelection.and.returnValue(throwError('merge failed'));

      (component as any).doSaveSelection({savedListId: 7, replace: false});
      tick();
      expect(component.error).toBe('merge failed');
      expect(component.savingSelection).toBeFalse();

      mockSavedListCandidateService.create.and.returnValue(throwError('create failed'));
      (component as any).doSaveSelection({savedListId: 0, replace: false});
      tick();
      expect(component.error).toBe('create failed');
      expect(component.savingSelection).toBeFalse();
    }));

    it('should cache and restore target-list details', () => {
      component.targetListId = 4;
      component.targetListName = 'Cached';
      component.targetListReplace = true;

      (component as any).cacheTargetList();

      expect(mockLocalStorageService.set).toHaveBeenCalledWith('Target1', {
        sourceID: 1,
        listID: 4,
        name: 'Cached',
        replace: true
      });

      mockLocalStorageService.get.and.returnValue({
        sourceID: 1,
        listID: 9,
        name: 'Restored',
        replace: false
      });
      (component as any).restoreTargetListFromCache();

      expect(component.targetListId).toBe(9);
      expect(component.targetListName).toBe('Restored');
      expect(component.targetListReplace).toBeFalse();
    });

    it('should configure review modal and refresh after completion', fakeAsync(() => {
      const candidate: any = new MockCandidate();
      candidate.candidateReviewStatusItems = [{
        id: 22,
        savedSearch: {id: component.candidateSource.id}
      }];
      const ref = modalRef(Promise.resolve(null));
      mockNgbModal.open.and.returnValue(ref);
      spyOn(component, 'onReviewStatusChange');

      component.review(candidate);
      tick();

      expect(ref.componentInstance.candidateReviewStatusItemId).toBe(22);
      expect(ref.componentInstance.candidateId).toBe(candidate.id);
      expect(component.onReviewStatusChange).toHaveBeenCalled();
    }));

    it('should perform selected and whole-list Salesforce updates', fakeAsync(() => {
      (component.candidateSource as any).sfJobOpp = {id: 2, sfId: 'SF-2'};
      component.selectedCandidates = [new MockCandidate()];
      mockCandidateService.createUpdateOppsFromCandidates.and.returnValue(of(null));
      mockCandidateService.createUpdateOppsFromCandidateList.and.returnValue(of(null));
      spyOn(component, 'doSearch');

      (component as any).doCreateUpdateSalesforceOnList2({stage: 'Prospect'}, true);
      tick();

      expect(mockCandidateService.createUpdateOppsFromCandidates).toHaveBeenCalled();
      expect(component.updating).toBeFalse();

      (component as any).doCreateUpdateSalesforceOnList2(null, false);
      tick();

      expect(mockCandidateService.createUpdateOppsFromCandidateList).toHaveBeenCalled();
      expect(component.doSearch).toHaveBeenCalledWith(true);
    }));

    it('should expose Salesforce update failures', fakeAsync(() => {
      (component.candidateSource as any).sfJobOpp = {id: 2, sfId: 'SF-2'};
      component.selectedCandidates = [new MockCandidate()];
      mockCandidateService.createUpdateOppsFromCandidates.and.returnValue(
        throwError('selected SF failed')
      );

      (component as any).doCreateUpdateSalesforceOnList2(null, true);
      tick();

      expect(component.error).toBe('selected SF failed');
      expect(component.updating).toBeFalse();

      mockCandidateService.createUpdateOppsFromCandidateList.and.returnValue(
        throwError('list SF failed')
      );
      (component as any).doCreateUpdateSalesforceOnList2(null, false);
      tick();

      expect(component.error).toBe('list SF failed');
      expect(component.updating).toBeFalse();
    }));

    it('should request a whole-list Salesforce update when no candidates are selected', fakeAsync(() => {
      component.selectedCandidates = [];
      const ref = modalRef(Promise.resolve(true));
      mockNgbModal.open.and.returnValue(ref);
      spyOn<any>(component, 'doCreateUpdateSalesforceOnList');

      component.createUpdateSalesforce();
      tick();

      expect((component as any).doCreateUpdateSalesforceOnList).toHaveBeenCalledWith(false);
    }));

    it('should validate and update candidate statuses for lists and searches', fakeAsync(() => {
      const candidate = new MockCandidate();
      component.selectedCandidates = [candidate];
      spyOn<any>(component, 'requestNewStatusInfo');

      component.updateStatusOfSelection();
      expect((component as any).requestNewStatusInfo).toHaveBeenCalledWith(1);

      component.candidateSource = savedSearchSource();
      mockSavedSearchService.getSelectionCount.and.returnValue(of(3));
      component.updateStatusOfSelection();
      tick();
      expect((component as any).requestNewStatusInfo).toHaveBeenCalledWith(3);

      mockSavedSearchService.getSelectionCount.and.returnValue(throwError('status count failed'));
      component.updateStatusOfSelection();
      tick();
      expect(component.error).toBe('status count failed');
    }));

    it('should update statuses for saved lists and saved searches', fakeAsync(() => {
      const candidate = new MockCandidate();
      component.selectedCandidates = [candidate];
      const info: any = {status: 'active'};
      mockCandidateService.updateStatus.and.returnValue(of(null));
      spyOn(component, 'doSearch');

      (component as any).updateCandidateStatuses(info);
      tick();

      expect(candidate.status).toBe(info.status);
      expect(component.updatingStatuses).toBeFalse();

      component.candidateSource = savedSearchSource();
      mockSavedSearchService.updateSelectedStatuses.and.returnValue(of(null));
      (component as any).updateCandidateStatuses(info);
      tick();

      expect(mockSavedSearchService.updateSelectedStatuses).toHaveBeenCalledWith(
        component.candidateSource.id,
        info
      );
      expect(component.updatingStatuses).toBeFalse();
    }));

    it('should expose status update failures', fakeAsync(() => {
      component.selectedCandidates = [new MockCandidate()];
      mockCandidateService.updateStatus.and.returnValue(throwError('list status failed'));

      (component as any).updateCandidateStatuses({status: 'active'});
      tick();
      expect(component.error).toBe('list status failed');

      component.candidateSource = savedSearchSource();
      mockSavedSearchService.updateSelectedStatuses.and.returnValue(
        throwError('search status failed')
      );
      (component as any).updateCandidateStatuses({status: 'active'});
      tick();
      expect(component.error).toBe('search status failed');
    }));

    it('should assign tasks and refresh for both modal result paths', fakeAsync(() => {
      const task: any = {id: 3};
      const ref = modalRef(Promise.resolve(task));
      mockNgbModal.open.and.returnValue(ref);
      spyOn(component, 'doSearch');

      component.assignTasks();
      tick();

      expect(ref.componentInstance.setTasks).toHaveBeenCalledWith(component.candidateSource);
      expect(component.monitoredTask).toBe(task);
      expect(component.doSearch).toHaveBeenCalledWith(true);

      mockNgbModal.open.and.returnValue(modalRef(Promise.resolve(null)));
      component.assignTasks();
      tick();
      expect(component.doSearch).toHaveBeenCalledWith(true);
    }));

    it('should copy a source and expose copy failures', fakeAsync(() => {
      const target: any = {id: 9, name: 'Copied'};
      mockCandidateSourceService.copy.and.returnValue(of(target));
      const ref = modalRef(Promise.resolve({
        savedListId: 9,
        newListName: 'Copied',
        replace: false,
        statusUpdateInfo: null
      }));
      mockNgbModal.open.and.returnValue(ref);
      const cacheService = (component as any).candidateSourceResultsCacheService;
      spyOn(cacheService, 'removeFromCache');

      component.doCopySource();
      tick();

      expect(component.targetListId).toBe(9);
      expect(component.targetListName).toBe('Copied');
      expect(component.savedSelection).toBeFalse();
      expect(component.loading).toBeFalse();

      mockCandidateSourceService.copy.and.returnValue(throwError('copy failed'));
      mockNgbModal.open.and.returnValue(ref);
      component.doCopySource();
      tick();

      expect(component.error).toBe('copy failed');
      expect(component.loading).toBeFalse();
    }));

    it('should toggle starred state through both service paths', fakeAsync(() => {
      const source: any = component.candidateSource;
      spyOn(component, 'isStarred').and.returnValue(false);
      mockCandidateSourceService.starSourceForUser.and.returnValue(of(source));

      component.doToggleStarred();
      tick();

      expect(mockCandidateSourceService.starSourceForUser).toHaveBeenCalled();
      expect(component.loading).toBeFalse();

      (component.isStarred as jasmine.Spy).and.returnValue(true);
      mockCandidateSourceService.unstarSourceForUser.and.returnValue(of(source));
      component.doToggleStarred();
      tick();

      expect(mockCandidateSourceService.unstarSourceForUser).toHaveBeenCalled();
      expect(component.loading).toBeFalse();
    }));

    it('should expose starred service errors', fakeAsync(() => {
      spyOn(component, 'isStarred').and.returnValue(false);
      mockCandidateSourceService.starSourceForUser.and.returnValue(throwError('star failed'));

      component.doToggleStarred();
      tick();

      expect(component.error).toBe('star failed');
      expect(component.loading).toBeFalse();

      (component.isStarred as jasmine.Spy).and.returnValue(true);
      mockCandidateSourceService.unstarSourceForUser.and.returnValue(throwError('unstar failed'));
      component.doToggleStarred();
      tick();

      expect(component.error).toBe('unstar failed');
      expect(component.loading).toBeFalse();
    }));

    it('should close selected opportunities and deselect affected candidates', fakeAsync(() => {
      const first = new MockCandidate();
      const second = new MockCandidate();
      second.id = 2;
      component.selectedCandidates = [first, second];
      const job: any = {sfId: 'SF-JOB'};
      mockCandidateService.createUpdateOppsFromCandidates.and.returnValue(of(null));
      spyOn(component, 'doSearch');

      (component as any).doCloseOpps([first], job, {stage: 'Closed'});
      tick();

      expect(component.selectedCandidates).toEqual([second]);
      expect(component.doSearch).toHaveBeenCalledWith(true);
      expect(component.closing).toBeFalse();

      mockCandidateService.createUpdateOppsFromCandidates.and.returnValue(
        throwError('close failed')
      );
      (component as any).doCloseOpps([second], job, null);
      tick();

      expect(component.error).toBe('close failed');
      expect(component.closing).toBeFalse();
    }));

    it('should configure close-opportunity modals', fakeAsync(() => {
      const candidate = new MockCandidate();
      const info: any = {stage: 'Closed'};
      const job: any = {sfId: 'SF-JOB'};
      (component.candidateSource as any).sfJobOpp = job;
      component.selectedCandidates = [candidate];
      const ref = modalRef(Promise.resolve(info));
      mockNgbModal.open.and.returnValue(ref);
      spyOn<any>(component, 'doCloseOpps');

      component.closeSelectedOpportunities();
      tick();
      expect(ref.componentInstance.closing).toBeTrue();
      expect((component as any).doCloseOpps).toHaveBeenCalledWith([candidate], job, info);

      (component as any).doCloseOpps.calls.reset();
      component.closeOpportunity(candidate);
      tick();
      expect((component as any).doCloseOpps).toHaveBeenCalledWith([candidate], job, info);
    }));

    it('should replace an updated candidate and delegate authorization helpers', () => {
      const oldCandidate = new MockCandidate();
      const updated = new MockCandidate();

      updated.user = {
        ...updated.user,
        firstName: 'Updated'
      };

      component.results = {
        content: [oldCandidate],
        totalElements: 1
      } as SearchResults<Candidate>;

      component.updatedCandidate(updated);

      expect(component.results.content[0]).toBe(updated);

      mockAuthorizationService.canViewCandidateName.and.returnValue(true);
      mockAuthorizationService.isEmployerPartner.and.returnValue(true);
      mockAuthorizationService.isReadOnly.and.returnValue(true);

      expect(component.canViewCandidateName()).toBeTrue();
      expect(component.isEmployerPartner()).toBeTrue();
      expect(component.isReadOnly()).toBeTrue();
    });
    it('should open a candidate in a new tab', () => {
      spyOn(window, 'open');

      component.openCandidateInNewTab('12345');

      expect(window.open).toHaveBeenCalledWith('/candidate/12345', '_blank');
    });
  });
});
