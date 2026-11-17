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

import type {Mock} from "vitest";
import {ShowCandidatesComponent} from "./show-candidates.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {SimpleChange} from "@angular/core";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {RouterTestingModule} from "@angular/router/testing";
import {Router} from "@angular/router";
import {NgbModal, NgbOffcanvas, NgbPaginationModule, NgbTypeaheadModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {DatePipe, TitleCasePipe} from "@angular/common";
import {CandidateService} from "../../../services/candidate.service";
import {CandidateSourceDescriptionComponent} from "../../util/candidate-source-description/candidate-source-description.component";
import {AutosaveStatusComponent} from "../../util/autosave-status/autosave-status.component";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {SavedSearchService} from "../../../services/saved-search.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {MockCandidateSource} from "../../../MockData/MockCandidateSource";
import {MockUser} from "../../../MockData/MockUser";
import {of, Subject, Subscription, throwError} from "rxjs";
import {LocalStorageService} from "../../../services/local-storage.service";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {By} from "@angular/platform-browser";
import {CandidateSourceCandidateService} from "../../../services/candidate-source-candidate.service";
import {SavedListCandidateService} from "../../../services/saved-list-candidate.service";
import {SearchResults} from "../../../model/search-results";
import {Candidate} from "../../../model/candidate";
import {SavedListService} from "../../../services/saved-list.service";
import {CandidateFieldService} from "../../../services/candidate-field.service";
import {PublishedDocColumnService} from "../../../services/published-doc-column.service";
import {CasiAdminService} from "../../../services/casi-admin.service";
import {ListAction, ServiceList} from "../../../model/service-list";
import {Status} from "../../../model/base";
import {expect, vi} from 'vitest';

describe('ShowCandidatesComponent', () => {
    let component: ShowCandidatesComponent;
    let fixture: ComponentFixture<ShowCandidatesComponent>;
    let formBuilder: UntypedFormBuilder;

    // Mock services
    const mockCandidateService = {
        searchOrMatch: vi.fn().mockName("CandidateService.searchOrMatch"),
        findByCandidateNumberOrName: vi.fn().mockName("CandidateService.findByCandidateNumberOrName"),
        downloadCv: vi.fn().mockName("CandidateService.downloadCv"),
        createUpdateOppsFromCandidates: vi.fn().mockName("CandidateService.createUpdateOppsFromCandidates"),
        createUpdateOppsFromCandidateList: vi.fn().mockName("CandidateService.createUpdateOppsFromCandidateList"),
        updateStatus: vi.fn().mockName("CandidateService.updateStatus"),
        resolveOutstandingTasks: vi.fn().mockName("CandidateService.resolveOutstandingTasks")
    };
    const mockCandidateSourceService = {
        copy: vi.fn().mockName("CandidateSourceService.copy"),
        starSourceForUser: vi.fn().mockName("CandidateSourceService.starSourceForUser"),
        unstarSourceForUser: vi.fn().mockName("CandidateSourceService.unstarSourceForUser")
    };
    const mockSavedSearchService = {
        clearSelection: vi.fn().mockName("SavedSearchService.clearSelection"),
        selectCandidate: vi.fn().mockName("SavedSearchService.selectCandidate"),
        getSelectionCount: vi.fn().mockName("SavedSearchService.getSelectionCount"),
        updateSelectedStatuses: vi.fn().mockName("SavedSearchService.updateSelectedStatuses"),
        getSavedSearchTypeInfos: vi.fn().mockName("SavedSearchService.getSavedSearchTypeInfos"),
        addWatcher: vi.fn().mockName("SavedSearchService.addWatcher"),
        removeWatcher: vi.fn().mockName("SavedSearchService.removeWatcher"),
        saveSelection: vi.fn().mockName("SavedSearchService.saveSelection"),
        resolveMatching: vi.fn().mockName("SavedSearchService.resolveMatching")
    };
    const mockSavedListService = {
        publish: vi.fn().mockName("SavedListService.publish"),
        importEmployerFeedback: vi.fn().mockName("SavedListService.importEmployerFeedback"),
        createFolder: vi.fn().mockName("SavedListService.createFolder"),
        get: vi.fn().mockName("SavedListService.get"),
        resolveMatching: vi.fn().mockName("SavedListService.resolveMatching")
    };
    const mockLocalStorageService = {
        get: vi.fn().mockName("LocalStorageService.get"),
        set: vi.fn().mockName("LocalStorageService.set")
    };
    const mockNgbModal = {
        open: vi.fn().mockName("NgbModal.open")
    };
    const mockNgbOffcanvas = {
        open: vi.fn().mockName("NgbOffcanvas.open"),
        dismiss: vi.fn().mockName("NgbOffcanvas.dismiss")
    };
    const mockAuthorizationService = {
        canAssignTask: vi.fn().mockName("AuthorizationService.canAssignTask"),
        canAccessSalesforce: vi.fn().mockName("AuthorizationService.canAccessSalesforce"),
        canEditCandidateSource: vi.fn().mockName("AuthorizationService.canEditCandidateSource"),
        canPublishList: vi.fn().mockName("AuthorizationService.canPublishList"),
        canImportToList: vi.fn().mockName("AuthorizationService.canImportToList"),
        canResolveTasks: vi.fn().mockName("AuthorizationService.canResolveTasks"),
        canUpdateCandidateStatus: vi.fn().mockName("AuthorizationService.canUpdateCandidateStatus"),
        canUpdateSalesforce: vi.fn().mockName("AuthorizationService.canUpdateSalesforce"),
        canManageCandidateTasks: vi.fn().mockName("AuthorizationService.canManageCandidateTasks"),
        isCandidateSourceMine: vi.fn().mockName("AuthorizationService.isCandidateSourceMine"),
        isEmployerPartner: vi.fn().mockName("AuthorizationService.isEmployerPartner"),
        canExportFromSource: vi.fn().mockName("AuthorizationService.canExportFromSource"),
        isReadOnly: vi.fn().mockName("AuthorizationService.isReadOnly"),
        isStarredByMe: vi.fn().mockName("AuthorizationService.isStarredByMe"),
        canViewCandidateName: vi.fn().mockName("AuthorizationService.canViewCandidateName"),
        canAccessGoogleDrive: vi.fn().mockName("AuthorizationService.canAccessGoogleDrive")
    };
    const mockAuthenticationService = {
        getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
    };
    const mockSalesforceService = {
        joblink: vi.fn().mockName("SalesforceService.joblink")
    };
    const mockLocation = {
        back: vi.fn().mockName("Location.back")
    };
    const mockCandidateSourceCandidateService = {
        searchPaged: vi.fn().mockName("CandidateSourceCandidateService.searchPaged"),
        search: vi.fn().mockName("CandidateSourceCandidateService.search"),
        export: vi.fn().mockName("CandidateSourceCandidateService.export")
    };
    const mockSavedListCandidateService = {
        mergeFromFile: vi.fn().mockName("SavedListCandidateService.mergeFromFile"),
        merge: vi.fn().mockName("SavedListCandidateService.merge"),
        remove: vi.fn().mockName("SavedListCandidateService.remove"),
        getSelectionListCandidates: vi.fn().mockName("SavedListCandidateService.getSelectionListCandidates"),
        saveSelection: vi.fn().mockName("SavedListCandidateService.saveSelection"),
        create: vi.fn().mockName("SavedListCandidateService.create")
    };
    const mockCandidateFieldService = {
        getCandidateSourceFields: vi.fn().mockName("CandidateFieldService.getCandidateSourceFields"),
        isCandidateNameViewable: vi.fn().mockName("CandidateFieldService.isCandidateNameViewable")
    };
    const mockPublishedDocColumnService = {
        getColumnConfigFromAllColumns: vi.fn().mockName("PublishedDocColumnService.getColumnConfigFromAllColumns"),
        getColumnConfigFromExportColumns: vi.fn().mockName("PublishedDocColumnService.getColumnConfigFromExportColumns")
    };
    const mockCasiAdminService = {
        getServiceList: vi.fn().mockName("CasiAdminService.getServiceList"),
        performServiceListAction: vi.fn().mockName("CasiAdminService.performServiceListAction")
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ShowCandidatesComponent, SortedByComponent, AutosaveStatusComponent, CandidateSourceDescriptionComponent],
            imports: [
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
                { provide: CandidateService, useValue: mockCandidateService },
                { provide: CandidateSourceService, useValue: mockCandidateSourceService },
                { provide: SavedSearchService, useValue: mockSavedSearchService },
                { provide: SavedListService, useValue: mockSavedListService },
                { provide: LocalStorageService, useValue: mockLocalStorageService },
                { provide: NgbModal, useValue: mockNgbModal },
                { provide: NgbOffcanvas, useValue: mockNgbOffcanvas },
                { provide: AuthorizationService, useValue: mockAuthorizationService },
                { provide: AuthenticationService, useValue: mockAuthenticationService },
                { provide: SalesforceService, useValue: mockSalesforceService },
                { provide: Location, useValue: mockLocation },
                { provide: CandidateSourceCandidateService, useValue: mockCandidateSourceCandidateService },
                { provide: SavedListCandidateService, useValue: mockSavedListCandidateService },
                { provide: CandidateFieldService, useValue: mockCandidateFieldService },
                { provide: PublishedDocColumnService, useValue: mockPublishedDocColumnService },
                { provide: CasiAdminService, useValue: mockCasiAdminService },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
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
        mockAuthenticationService.getLoggedInUser.mockReturnValue(new MockUser());
        mockCandidateFieldService.getCandidateSourceFields.mockReturnValue([]);
        mockCandidateFieldService.isCandidateNameViewable.mockReturnValue(true);
        mockAuthorizationService.isReadOnly.mockReturnValue(false);
        mockAuthorizationService.isEmployerPartner.mockReturnValue(false);
        mockAuthorizationService.canExportFromSource.mockReturnValue(true);
        mockAuthorizationService.canImportToList.mockReturnValue(true);
        mockAuthorizationService.canPublishList.mockReturnValue(true);
        mockAuthorizationService.canUpdateCandidateStatus.mockReturnValue(true);
        mockAuthorizationService.canAssignTask.mockReturnValue(true);
        mockAuthorizationService.canResolveTasks.mockReturnValue(true);
        mockAuthorizationService.canAccessSalesforce.mockReturnValue(true);
        mockAuthorizationService.canAccessGoogleDrive.mockReturnValue(true);
        mockAuthorizationService.isStarredByMe.mockReturnValue(false);
        mockCasiAdminService.getServiceList.mockReturnValue(of(null));
        mockCasiAdminService.performServiceListAction.mockReturnValue(of(null));
        mockSavedSearchService.resolveMatching.mockReturnValue(of(null));
        mockSavedListService.resolveMatching.mockReturnValue(of(null));

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
        mockSavedSearchService.clearSelection.mockReturnValue(throwError('Selection clear error'));
        const mockCandidateSource = new MockCandidateSource();
        (mockCandidateSource as any).defaultSearch = false; //Any value makes it look like a saved search
        component.candidateSource = mockCandidateSource;
        component.clearSelectionAndDoSearch();

        expect(mockSavedSearchService.clearSelection).toHaveBeenCalledWith(1, { userId: 1 });
        expect(component.error).toBe('Selection clear error');
    });

    it('should clear selection for saved list and perform search', () => {
        component.loggedInUser = new MockUser();
        component.candidateSource = new MockCandidateSource();
        vi.spyOn(component, 'doSearch').mockReturnValue(undefined);
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
        mockCandidateSourceCandidateService.searchPaged.mockReturnValue(throwError('Search error'));
        component.candidateSource = new MockCandidateSource();
        component.doSearch(true);
        tick();

        expect(mockCandidateSourceCandidateService.searchPaged).toHaveBeenCalled();
        expect(component.error).toBe('Search error');
        expect(component.searching).toBe(false);
    }));

    it('should toggle sort direction when same column is clicked', () => {
        component.sortField = 'id';
        component.sortDirection = 'ASC';
        vi.spyOn(component, 'doSearch').mockReturnValue(undefined);

        component.toggleSort('id');

        expect(component.sortDirection).toBe('DESC');
        expect(component.doSearch).toHaveBeenCalledWith(true);
    });

    it('should change sort field and set direction to ASC when different column is clicked', () => {
        component.sortField = 'id';
        component.sortDirection = 'DESC';
        vi.spyOn(component, 'doSearch').mockReturnValue(undefined);

        component.toggleSort('name');

        expect(component.sortField).toBe('name');
        expect(component.sortDirection).toBe('ASC');
        expect(component.doSearch).toHaveBeenCalledWith(true);
    });

    it('should export candidates successfully', fakeAsync(() => {
        const mockBlob = new Blob(['test'], { type: 'text/csv' });
        mockCandidateSourceCandidateService.export.mockReturnValue(of(mockBlob));
        vi.spyOn(component, 'createAndDownloadBlobFile').mockReturnValue(undefined);
        component.results = { totalElements: 100 } as SearchResults<Candidate>;
        component.candidateSource = new MockCandidateSource();

        component.exportCandidates();
        tick();

        expect(mockCandidateSourceCandidateService.export).toHaveBeenCalled();
        expect(component.createAndDownloadBlobFile).toHaveBeenCalledWith(mockBlob, { type: 'text/csv;charset=utf-8;' }, 'candidates.csv');
        expect(component.exporting).toBe(false);
    }));

    it('should add candidate to list', fakeAsync(() => {
        const mockCandidate = new MockCandidate();
        mockSavedListCandidateService.merge.mockReturnValue(of({}));
        vi.spyOn(component, 'doSearch').mockReturnValue(undefined);
        component.candidateSource = new MockCandidateSource();

        component.addCandidateToList(mockCandidate);
        tick();

        expect(mockSavedListCandidateService.merge).toHaveBeenCalledWith(1, { candidateIds: [mockCandidate.id] });
        expect(component.doSearch).toHaveBeenCalledWith(true);
        expect(component.adding).toBe(false);
    }));

    it('should handle error when adding candidate to list', fakeAsync(() => {
        const mockCandidate = new MockCandidate();
        mockSavedListCandidateService.merge.mockReturnValue(throwError('Add error'));
        component.candidateSource = new MockCandidateSource();

        component.addCandidateToList(mockCandidate);
        tick();

        expect(mockSavedListCandidateService.merge).toHaveBeenCalledWith(1, { candidateIds: [mockCandidate.id] });
        expect(component.error).toBe('Add error');
        expect(component.adding).toBe(false);
    }));

    it('should handle candidate selection change for saved list', () => {
        const mockCandidate = new MockCandidate();
        component.candidateSource = new MockCandidateSource();
        component.selectedCandidates = [];
        vi.spyOn(component.selectedCandidatesChange, 'emit').mockReturnValue(undefined);

        component.onSelectionChange(mockCandidate, true);

        expect(mockCandidate.selected).toBe(true);
        expect(component.selectedCandidates).toContain(mockCandidate);
        expect(component.selectedCandidatesChange.emit).toHaveBeenCalledWith([mockCandidate]);
    });

    it('should handle candidate deselection for saved list', () => {
        const mockCandidate = new MockCandidate();
        component.candidateSource = new MockCandidateSource();
        component.selectedCandidates = [mockCandidate];
        vi.spyOn(component.selectedCandidatesChange, 'emit').mockReturnValue(undefined);

        component.onSelectionChange(mockCandidate, false);

        expect(mockCandidate.selected).toBe(false);
        expect(component.selectedCandidates).toEqual([]);
        expect(component.selectedCandidatesChange.emit).toHaveBeenCalledWith([]);
    });

    it('should trigger saveSelection when save button is clicked', () => {
        component.candidateSource = new MockCandidateSource();
        component.selectedCandidates = [new MockCandidate()];
        vi.spyOn(component, 'saveSelection').mockReturnValue(undefined);
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
        expect(saveButton.nativeElement.disabled).toBe(true);
    });

    it('should toggle description visibility when link is clicked', () => {
        fixture.detectChanges();

        const toggleLink = fixture.debugElement.query(By.css('.toggle-description'));
        expect(component.showDescription).toBe(false);

        toggleLink.triggerEventHandler('onClick', null);
        fixture.detectChanges();

        expect(component.showDescription).toBe(true);

        const icon = toggleLink.query(By.css('i'));
        expect(icon.nativeElement.classList).toContain('fa-chevron-up');
    });


    it('should call importCandidates when import button is clicked', () => {
        component.candidateSource = new MockCandidateSource();
        mockAuthorizationService.canImportToList.mockReturnValue(true);
        vi.spyOn(component, 'importCandidates').mockReturnValue(undefined);
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
        mockCandidateSourceCandidateService.searchPaged.mockReturnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));
        mockCasiAdminService.getServiceList.mockReturnValue(of(mockServiceList));

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
        mockCandidateSourceCandidateService.searchPaged.mockReturnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));
        mockCasiAdminService.getServiceList.mockClear();
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
        mockCandidateSourceCandidateService.searchPaged.mockReturnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));
        mockCasiAdminService.getServiceList.mockReturnValue(throwError('Not found'));

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
        mockCasiAdminService.performServiceListAction.mockReturnValue(of(null));

        component.onServiceListAction(ListAction.REASSIGN);
        tick();

        expect(mockCasiAdminService.performServiceListAction).toHaveBeenCalledWith(99, ListAction.REASSIGN, [mockCandidate.candidateNumber]);
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
        mockCasiAdminService.performServiceListAction.mockReturnValue(throwError('Action failed'));

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
            mockCandidateSourceCandidateService.searchPaged.mockReturnValue(of({ content: [], totalElements: 0, number: 0, size: 20 }));
        });

        it('should fall back to 20 when no preference is stored and @Input is not set', fakeAsync(() => {
            mockLocalStorageService.get.mockReturnValue(null);
            component.pageSize = undefined as any;

            triggerSourceChange();
            tick();

            expect(component.pageSize).toBe(20);
        }));

        it('should fall back to 20 when stored value is not a valid option', fakeAsync(() => {
            mockLocalStorageService.get.mockImplementation((key: string) => key.endsWith('PageSize') ? '42' : null);
            component.pageSize = undefined as any;

            triggerSourceChange();
            tick();

            expect(component.pageSize).toBe(20);
        }));

        it('should restore a valid stored preference', fakeAsync(() => {
            mockLocalStorageService.get.mockImplementation((key: string) => key.endsWith('PageSize') ? '50' : null);

            triggerSourceChange();
            tick();

            expect(component.pageSize).toBe(50);
        }));

        it('should keep a valid @Input value when no preference is stored', fakeAsync(() => {
            mockLocalStorageService.get.mockReturnValue(null);
            component.pageSize = 50;

            triggerSourceChange();
            tick();

            expect(component.pageSize).toBe(50);
        }));

        it('onPageSizeChange should update pageSize, reset pageNumber, persist preference and trigger search', () => {
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);
            mockLocalStorageService.set.mockClear();

            component.onPageSizeChange(50);

            expect(component.pageSize).toBe(50);
            expect(component.pageNumber).toBe(1);
            expect(mockLocalStorageService.set).toHaveBeenCalledWith(expect.stringContaining('PageSize'), '50');
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
            close: vi.fn().mockName('close')
        } as any);

        beforeEach(() => {
            mockNgbModal.open.mockClear();
            mockCandidateService.searchOrMatch.mockClear();
            mockCandidateService.findByCandidateNumberOrName.mockClear();
            mockSavedSearchService.selectCandidate.mockClear();
            mockSavedSearchService.getSelectionCount.mockClear();
            mockSavedSearchService.updateSelectedStatuses.mockClear();
            mockSavedSearchService.addWatcher.mockClear();
            mockSavedSearchService.removeWatcher.mockClear();
            mockCandidateSourceService.copy.mockClear();
            mockCandidateSourceService.starSourceForUser.mockClear();
            mockCandidateSourceService.unstarSourceForUser.mockClear();
            mockSavedListCandidateService.remove.mockClear();
            mockSavedListCandidateService.saveSelection.mockClear();
            mockSavedListCandidateService.create.mockClear();
            mockSavedListService.publish.mockClear();
            mockSavedListService.importEmployerFeedback.mockClear();
            mockSavedListService.createFolder.mockClear();
            component.selectedCandidates = [];
            component.loggedInUser = new MockUser();
            component.pageNumber = 1;
            component.pageSize = 20;
        });

        it('should expose list/search derived values', () => {
            component.candidateSource = new MockCandidateSource();
            component.searchInResultsForm = formBuilder.group({ keyword: ['abc'], showClosedOpps: [true] });
            component.selectedCandidates = [new MockCandidate()];
            expect(component.pluralType).toBe('lists');
            expect(component.keyword).toBe('abc');
            expect(component.numberSelections).toBe(1);
            expect(component.isSavedList()).toBe(true);
            expect(component.isSavedSearch()).toBe(false);
            expect(component.isSwapSelectionSupported()).toBe(true);
            expect(component.isSelection()).toBe(true);
            expect(component.displayScore()).toBe(false);

            component.candidateSource = savedSearchSource();
            component.isKeywordSearch = true;
            expect(component.pluralType).toBe('searches');
            expect(component.isSavedList()).toBe(false);
            expect(component.isSavedSearch()).toBe(true);
            expect(component.isSwapSelectionSupported()).toBe(false);
            expect(component.displayScore()).toBe(true);
        });

        it('should return empty keyword without a form', () => {
            component.searchInResultsForm = null;
            expect(component.keyword).toBe('');
        });

        it('should restore showClosedOpps from local storage and default to true', () => {
            component.candidateSource = new MockCandidateSource();
            mockLocalStorageService.get.mockReturnValueOnce(null).mockReturnValueOnce('false').mockReturnValueOnce('true');
            expect(component.showClosedOpps).toBe(true);
            expect(component.showClosedOpps).toBe(false);
            expect(component.showClosedOpps).toBe(true);
        });

        it('should persist filters and search after debounce', fakeAsync(() => {
            component.candidateSource = new MockCandidateSource();
            component.searchInResultsForm = formBuilder.group({ keyword: [''], showClosedOpps: [true] });
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);
            component.subscribeToFilterChanges();
            component.searchInResultsForm.patchValue({ keyword: 'new' });
            tick(801);
            expect(component.filterSearch).toBe(true);
            expect(mockLocalStorageService.set).toHaveBeenCalledWith('ListKey1ShowClosedOpps', 'true');
            expect(component.doSearch).toHaveBeenCalledWith(true);
        }));

        it('should unsubscribe on destroy', () => {
            component.subscription = new Subscription();
            vi.spyOn(component.subscription, 'unsubscribe').mockReturnValue(undefined);
            component.ngOnDestroy();
            expect(component.subscription.unsubscribe).toHaveBeenCalled();
        });

        it('should select candidates according to source type', () => {
            const candidate = new MockCandidate();
            candidate.selected = true;
            component.candidateSource = savedSearchSource();
            expect(component.isSelected(candidate)).toBe(true);
            component.candidateSource = new MockCandidateSource();
            component.selectedCandidates = [candidate];
            expect(component.isSelected(candidate)).toBe(true);
            component.selectedCandidates = [];
            expect(component.isSelected(candidate)).toBe(false);
        });

        it('should set current candidate and emit selection', () => {
            const candidate = new MockCandidate();
            candidate.selected = true;
            component.candidateSource = savedSearchSource();
            vi.spyOn(component.candidateSelection, 'emit').mockReturnValue(undefined);
            component.setCurrentCandidate(candidate);
            expect(component.currentCandidate).toBe(candidate);
            expect(component.savedSearchSelectionChange).toBe(true);
            expect(component.candidateSelection.emit).toHaveBeenCalledWith(candidate);
        });

        it('should pass the resolved matching context through to the candidate search card', () => {
            const matching = { id: 5, matchingDescription: '<p>ICU nurse</p>', updatedDate: '2026-10-01T10:15:30Z' };
            mockSavedListService.resolveMatching.mockReturnValue(of(matching));
            component.candidateSource = new MockCandidateSource();
            fixture.detectChanges(); // triggers ngOnInit, which resets currentCandidate to null

            component.setCurrentCandidate(new MockCandidate());
            fixture.detectChanges();

            const searchCardEl = fixture.debugElement.query(By.css('app-candidate-search-card'));
            expect(searchCardEl.properties['matching']).toEqual(matching);
            expect(searchCardEl.properties['requirementsPendingMessage']).toBeUndefined();
        });

        it('should apply review filter and search without page number', () => {
            component.searchInResultsForm = formBuilder.group({ statusesDisplay: [['rejected']] });
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);
            component.onReviewStatusFilterChange();
            expect(component.reviewStatusFilter).toEqual(['rejected']);
            expect(component.doSearch).toHaveBeenCalledWith(false, false);
        });

        it('should identify target list state', () => {
            component.targetListName = null;
            expect(component.haveTargetList()).toBeFalsy();
            component.targetListName = 'Target';
            expect(component.haveTargetList()).toBe(true);
        });

        it('should render candidate rows with or without names', () => {
            const candidate = new MockCandidate();
            mockCandidateFieldService.isCandidateNameViewable.mockReturnValue(true);
            expect(component.renderCandidateRow(candidate)).toContain(candidate.candidateNumber);
            expect(component.renderCandidateRow(candidate)).toContain(candidate.user.firstName);
            mockCandidateFieldService.isCandidateNameViewable.mockReturnValue(false);
            expect(component.renderCandidateRow(candidate)).toBe(candidate.candidateNumber);
        });

        it('should clear typeahead input and add selected candidate', () => {
            const candidate = new MockCandidate();
            const event = { preventDefault: vi.fn().mockName('preventDefault'), item: candidate };
            const input = { value: 'old' };
            vi.spyOn(component, 'addCandidateToList').mockReturnValue(undefined);
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
            mockSavedListCandidateService.remove.mockReturnValue(of({}));
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);
            component.removeCandidateFromList(first);
            tick();
            expect(component.selectedCandidates).toEqual([second]);
            expect(mockSavedListCandidateService.remove).toHaveBeenCalledWith(1, { candidateIds: [first.id] });
            expect(component.doSearch).toHaveBeenCalledWith(true);
        }));

        it('should expose remove error', fakeAsync(() => {
            const candidate = new MockCandidate();
            component.selectedCandidates = [candidate];
            mockSavedListCandidateService.remove.mockReturnValue(throwError('remove failed'));
            component.removeSelectedCandidatesFromList();
            tick();
            expect(component.error).toBe('remove failed');
        }));

        it('should swap saved-list selection successfully', fakeAsync(() => {
            const first = new MockCandidate();
            const second = new MockCandidate();
            second.id = 2;
            component.selectedCandidates = [first];
            mockCandidateSourceCandidateService.search.mockReturnValue(of([first, second]));
            vi.spyOn(component.selectedCandidatesChange, 'emit').mockReturnValue(undefined);
            component.swapSelection();
            tick();
            expect(component.selectedCandidates).toEqual([second]);
            expect(component.searching).toBe(false);
            expect(component.selectedCandidatesChange.emit).toHaveBeenCalledWith([second]);
        }));

        it('should handle swap selection failure', fakeAsync(() => {
            mockCandidateSourceCandidateService.search.mockReturnValue(throwError('swap failed'));
            component.swapSelection();
            tick();
            expect(component.error).toBe('swap failed');
            expect(component.searching).toBe(false);
        }));

        it('should reject exports over 5000 candidates', () => {
            component.results = { totalElements: 5001 } as SearchResults<Candidate>;
            expect(() => component.exportCandidates()).toThrowError(/capped at 5,000/);
            expect(component.error).toContain('capped at 5,000');
        });

        it('should publish candidates and update source', fakeAsync(() => {
            const result: any = { ...component.candidateSource, publishedDocLink: 'doc', exportColumns: ['a'] };
            mockSavedListService.publish.mockReturnValue(of(result));
            (component as any).publishCandidates([{ key: 'a' }]);
            tick();
            expect((component.candidateSource as any).publishedDocLink).toBe('doc');
            expect(component.candidateSource.exportColumns as unknown[]).toEqual(['a']);
            expect(component.publishing).toBe(false);
        }));

        it('should expose publish failure', fakeAsync(() => {
            mockSavedListService.publish.mockReturnValue(throwError('publish failed'));
            (component as any).publishCandidates([]);
            tick();
            expect(component.error).toBe('publish failed');
            expect(component.publishing).toBe(false);
        }));

        it('should import employer feedback and refresh', fakeAsync(() => {
            const report: any = { message: 'Done', numEmployerFeedbacks: 0, numJobOffers: 0, numNoJobOffers: 0 };
            mockSavedListService.importEmployerFeedback.mockReturnValue(of(report));
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);
            vi.spyOn((component as any), 'displayImportFeedbackReport').mockReturnValue(undefined);
            component.importEmployerFeedback();
            tick();
            expect(component.doSearch).toHaveBeenCalledWith(true);
            expect((component as any).displayImportFeedbackReport).toHaveBeenCalledWith(report);
            expect(component.importingFeedback).toBe(false);
        }));

        it('should expose employer feedback import failure', fakeAsync(() => {
            mockSavedListService.importEmployerFeedback.mockReturnValue(throwError('feedback failed'));
            component.importEmployerFeedback();
            tick();
            expect(component.error).toBe('feedback failed');
            expect(component.importingFeedback).toBe(false);
        }));

        it('should build complete employer feedback report text', () => {
            const ref = modalRef();
            mockNgbModal.open.mockReturnValue(ref);
            (component as any).displayImportFeedbackReport({
                message: 'Imported', numEmployerFeedbacks: 2, numJobOffers: 3, numNoJobOffers: 4
            });
            expect(ref.componentInstance.title).toBe('Feedback Import Report');
            expect(ref.componentInstance.message).toContain('2 candidates');
            expect(ref.componentInstance.message).toContain('3 candidates');
            expect(ref.componentInstance.message).toContain('4 candidates');
        });

        it('should open publish column selector and publish selected columns', fakeAsync(() => {
            const columns: any[] = [{ key: 'one' }];
            const ref = modalRef(Promise.resolve(columns));
            mockNgbModal.open.mockReturnValue(ref);
            mockPublishedDocColumnService.getColumnConfigFromAllColumns.mockReturnValue(columns);
            mockPublishedDocColumnService.getColumnConfigFromExportColumns.mockReturnValue(columns);
            vi.spyOn((component as any), 'publishCandidates').mockReturnValue(undefined);
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
            mockSavedSearchService.getSavedSearchTypeInfos.mockReturnValue([]);
            expect(component.getBreadcrumb()).toBeDefined();
        });

        it('should expose source flags and links', () => {
            component.candidateSource = new MockCandidateSource();
            (component.candidateSource as any).publishedDocLink = 'https://example.test/doc';
            (component.candidateSource as any).savedSearchSource = { id: 9 };
            expect(component.hasPublishedDoc()).toBe(true);
            expect(component.hasSavedSearchSource()).toBe(true);
            expect(component.isDefaultSavedSearch()).toBe(false);
            component.candidateSource = savedSearchSource();
            (component.candidateSource as any).defaultSearch = true;
            expect(component.isDefaultSavedSearch()).toBe(true);
            expect(component.hasSavedSearchSource()).toBe(false);
        });

        it('should delegate Salesforce and Google Drive access checks', () => {
            mockAuthorizationService.canAccessSalesforce.mockReturnValue(true);
            expect(component.canAccessSalesforce()).toBe(true);
            expect(mockAuthorizationService.canAccessSalesforce).toHaveBeenCalled();

            mockAuthorizationService.canAccessSalesforce.mockReturnValue(false);
            expect(component.canAccessSalesforce()).toBe(false);

            mockAuthorizationService.canAccessGoogleDrive.mockReturnValue(true);
            expect(component.canAccessGoogleDrive()).toBe(true);
            expect(mockAuthorizationService.canAccessGoogleDrive).toHaveBeenCalled();
        });

        it('should open published and Salesforce links', () => {
            component.candidateSource = new MockCandidateSource();
            (component.candidateSource as any).publishedDocLink = 'https://example.test/doc';
            vi.spyOn(window, 'open').mockReturnValue(undefined);
            component.doShowPublishedDoc();
            expect(window.open).toHaveBeenCalledWith('https://example.test/doc', '_blank');
            mockSalesforceService.joblink.mockReturnValue('https://example.test/sf');
            component.doShowSalesforceLink();
            expect(window.open).toHaveBeenCalledWith('https://example.test/sf', '_blank');
        });

        it('should navigate to the new search page with the list id', () => {
            component.candidateSource = new MockCandidateSource();
            const router = TestBed.inject(Router);
            vi.spyOn(router, 'navigate').mockReturnValue(undefined);

            component.doSearchList();

            expect(router.navigate).toHaveBeenCalledWith(['/searches'], { queryParams: { tab: 'NewSearch', list: component.candidateSource.id } });
        });

        it('should not navigate when there is no list id', () => {
            component.candidateSource = { ...new MockCandidateSource(), id: null };
            const router = TestBed.inject(Router);
            vi.spyOn(router, 'navigate').mockReturnValue(undefined);

            component.doSearchList();

            expect(router.navigate).not.toHaveBeenCalled();
        });

        it('should create a list folder when missing', fakeAsync(() => {
            const result: any = { ...component.candidateSource, folderlink: 'https://example.test/folder' };
            (component.candidateSource as any).folderlink = null;
            mockSavedListService.createFolder.mockReturnValue(of(result));
            vi.spyOn(window, 'open').mockReturnValue(undefined);
            component.doShowListFolder();
            tick();
            expect(component.candidateSource).toBe(result);
            expect(window.open).toHaveBeenCalledWith(result.folderlink, '_blank');
            expect(component.searching).toBe(false);
        }));

        it('should expose folder creation failure', fakeAsync(() => {
            (component.candidateSource as any).folderlink = null;
            mockSavedListService.createFolder.mockReturnValue(throwError('folder failed'));
            component.doShowListFolder();
            tick();
            expect(component.error).toBe('folder failed');
            expect(component.searching).toBe(false);
        }));

        it('should emit edit source', () => {
            vi.spyOn(component.editSource, 'emit').mockReturnValue(undefined);
            component.doEditSource();
            expect(component.editSource.emit).toHaveBeenCalledWith(component.candidateSource);
        });

        it('should test task assignment helpers', () => {
            const candidate: any = new MockCandidate();
            candidate.taskAssignments = [
                { status: Status.active, completedDate: new Date(), abandonedDate: null, task: { id: 1, optional: false } },
                { status: Status.active, completedDate: null, abandonedDate: null, task: { id: 2, optional: true } },
                { status: Status.inactive, completedDate: new Date(), abandonedDate: null, task: { id: 1, optional: false } }
            ];
            expect(component.hasTaskAssignments(candidate)).toBe(true);
            expect(component.getCompletedMonitoredTasks(candidate).length).toBe(1);
            expect(component.getTotalMonitoredTasks(candidate).length).toBe(1);
            component.monitoredTask = { id: 1 } as any;
            expect(component.getCompletedMonitoredTasks(candidate).length).toBe(1);
            expect(component.getTotalMonitoredTasks(candidate).length).toBe(1);
        });

        it('should resolve outstanding tasks and refresh', fakeAsync(() => {
            component.selectedCandidates = [new MockCandidate()];
            mockCandidateService.resolveOutstandingTasks.mockReturnValue(of({}));
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);
            component.resolveTaskAssignments();
            tick();
            expect(component.doSearch).toHaveBeenCalledWith(true);
            expect(component.updatingTasks).toBe(false);
        }));

        it('should reject resolving tasks without selection', () => {
            component.selectedCandidates = [];
            component.resolveTaskAssignments();
            expect(component.error).toBe('No candidates are selected');
        });

        it('should expose resolve task failure', fakeAsync(() => {
            component.selectedCandidates = [new MockCandidate()];
            mockCandidateService.resolveOutstandingTasks.mockReturnValue(throwError('task failed'));
            component.resolveTaskAssignments();
            tick();
            expect(component.error).toBe('task failed');
            expect(component.updatingTasks).toBe(false);
        }));

        it('should calculate review status', () => {
            const candidate: any = new MockCandidate();
            candidate.candidateReviewStatusItems = [];
            expect(component.getReviewStatus(candidate)).toBe(component.ReviewStatus.unverified);
            candidate.candidateReviewStatusItems = [{ savedSearch: { id: component.candidateSource.id }, reviewStatus: 'verified' }];
            expect(component.getReviewStatus(candidate)).toBe(component.ReviewStatus.verified);
        });

        it('should detect watching state', () => {
            component.loggedInUser = new MockUser();
            (component.candidateSource as any).watcherUserIds = undefined;
            expect(component.isWatching()).toBe(false);
            (component.candidateSource as any).watcherUserIds = [component.loggedInUser.id];
            expect(component.isWatching()).toBe(true);
        });

        it('should add and remove watcher', fakeAsync(() => {
            const source: any = savedSearchSource();
            source.watcherUserIds = [];
            component.candidateSource = source;
            mockSavedSearchService.addWatcher.mockReturnValue(of({ ...source, watcherUserIds: [component.loggedInUser.id] }));
            component.doToggleWatch();
            tick();
            expect(mockSavedSearchService.addWatcher).toHaveBeenCalled();
            expect(component.loading).toBe(false);

            mockSavedSearchService.removeWatcher.mockReturnValue(of({ ...source, watcherUserIds: [] }));
            component.doToggleWatch();
            tick();
            expect(mockSavedSearchService.removeWatcher).toHaveBeenCalled();
            expect(component.loading).toBe(false);
        }));

        it('should expose watcher errors', fakeAsync(() => {
            const source: any = savedSearchSource();
            source.watcherUserIds = [];
            component.candidateSource = source;
            mockSavedSearchService.addWatcher.mockReturnValue(throwError('watch failed'));
            component.doToggleWatch();
            tick();
            expect(component.error).toBe('watch failed');
            expect(component.loading).toBe(false);
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
                setTasks: vi.fn().mockName('setTasks')
            },
            result,
            close: vi.fn().mockName('close')
        });

        beforeEach(() => {
            component.selectedCandidates = [];
            component.loggedInUser = new MockUser();
            component.pageNumber = 1;
            component.pageSize = 20;
            mockCandidateService.downloadCv.mockClear();
            mockNgbModal.open.mockClear();
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
            mockSavedListCandidateService.getSelectionListCandidates.mockReturnValue(of(selected));
            vi.spyOn(component.selectedCandidatesChange, 'emit').mockReturnValue(undefined);

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
            mockSavedListCandidateService.getSelectionListCandidates.mockReturnValue(throwError('selection load failed'));

            component.ngOnInit();
            tick();

            expect(component.error).toBe('selection load failed');
        }));

        it('should search candidate names through the typeahead and recover from errors', fakeAsync(() => {
            component.candidateSource = new MockCandidateSource();
            const candidate = new MockCandidate();
            mockCandidateService.findByCandidateNumberOrName.mockReturnValue(of({ content: [candidate] }));

            component.ngOnInit();

            let output: Candidate[];
            component.doNumberOrNameSearch(of('abc')).subscribe((value: Candidate[]) => output = value);
            tick(301);

            expect(mockCandidateService.findByCandidateNumberOrName).toHaveBeenCalledWith({
                candidateNumberOrName: 'abc',
                pageSize: 10
            });
            expect(output).toEqual([candidate]);
            expect(component.searchFailed).toBe(false);

            mockCandidateService.findByCandidateNumberOrName.mockReturnValue(throwError('lookup failed'));
            component.doNumberOrNameSearch(of('bad')).subscribe((value: Candidate[]) => output = value);
            tick(301);

            expect(output).toEqual([]);
            expect(component.searchFailed).toBe(true);
        }));

        it('should process searchRequest changes and selected-candidate clearing', () => {
            component.searchRequest = {
                simpleQueryString: 'test'
            } as any;
            vi.spyOn((component as any), 'updatedSearch').mockReturnValue(undefined);
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);

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

        it('should execute updated searches', fakeAsync(() => {
            component.searchRequest = {
                simpleQueryString: ' ',
                reviewStatusFilter: null
            } as any;
            component.sortField = 'id';
            component.sortDirection = 'DESC';
            component.pageNumber = 2;
            component.pageSize = 50;
            component.reviewStatusFilter = ['verified'];

            vi.spyOn(component, 'isReviewable').mockReturnValue(true);
            vi.spyOn((component as any), 'cacheResults').mockReturnValue(undefined);
            mockCandidateService.searchOrMatch.mockReturnValue(of({
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
            expect(mockCandidateService.searchOrMatch).toHaveBeenCalledWith(component.searchRequest, false);
            expect((component as any).cacheResults).toHaveBeenCalled();
            expect(component.searching).toBe(false);
        }));

        it('should expose updated-search failures', fakeAsync(() => {
            component.searchRequest = { simpleQueryString: 'query' } as any;
            mockCandidateService.searchOrMatch.mockReturnValue(throwError('request failed'));

            (component as any).updatedSearch();
            tick();

            expect(component.error).toBe('request failed');
            expect(component.searching).toBe(false);
        }));

        it('should delegate doSearch to updatedSearch when a request is supplied', () => {
            component.searchRequest = { simpleQueryString: 'query' } as any;
            vi.spyOn((component as any), 'checkCache').mockReturnValue(false);
            vi.spyOn((component as any), 'updatedSearch').mockReturnValue(undefined);

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
            vi.spyOn((component as any), 'checkCache').mockReturnValue(false);
            vi.spyOn((component as any), 'performSearch').mockReturnValue(of(null));
            vi.spyOn(component, 'setCurrentCandidate');

            component.doSearch(true);
            tick();

            expect((component as any).performSearch).toHaveBeenCalled();
            expect(component.setCurrentCandidate).toHaveBeenCalledWith(candidate);
        }));

        it('should restore saved search card scroll position', fakeAsync(() => {
            const card = { scrollTo: vi.fn().mockName('scrollTo') };
            (component as any).searchCard = card;
            (component as any).searchCardScrollTop = 75;

            component.setSearchCardScrollTop();
            tick(201);

            expect(card.scrollTo).toHaveBeenCalledWith({ top: 75, behavior: 'smooth' });
        }));

        it('should preserve profile scroll position when selecting another candidate', () => {
            const current = new MockCandidate();
            const next = new MockCandidate();
            next.id = 2;
            component.currentCandidate = current;
            const profile = document.createElement('div');
            profile.className = 'profile';
            Object.defineProperty(profile, 'scrollTop', { value: 45, configurable: true });
            document.body.appendChild(profile);
            vi.spyOn(component, 'setCurrentCandidate').mockReturnValue(undefined);

            component.selectCandidate(next);

            expect((component as any).searchCardScrollTop).toBe(45);
            expect(component.setCurrentCandidate).toHaveBeenCalledWith(next);
            profile.remove();
        });

        it('should open import modal and import the selected file', fakeAsync(() => {
            const file = new File(['1'], 'candidates.csv', { type: 'text/csv' });
            const ref = modalRef(Promise.resolve([file]));
            mockNgbModal.open.mockReturnValue(ref);
            vi.spyOn((component as any), 'doImport').mockReturnValue(undefined);

            component.importCandidates();
            tick();

            expect(ref.componentInstance.validExtensions).toEqual(['csv', 'txt']);
            expect(ref.componentInstance.maxFiles).toBe(1);
            expect((component as any).doImport).toHaveBeenCalledWith([file]);
        }));

        it('should complete and fail file imports correctly', fakeAsync(() => {
            const file = new File(['1'], 'candidates.csv', { type: 'text/csv' });
            mockSavedListCandidateService.mergeFromFile.mockReturnValue(of({}));
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);

            (component as any).doImport([file]);
            tick();

            expect(mockSavedListCandidateService.mergeFromFile).toHaveBeenCalled();
            expect(component.doSearch).toHaveBeenCalledWith(true);
            expect(component.importing).toBe(false);

            mockSavedListCandidateService.mergeFromFile.mockReturnValue(throwError('import failed'));
            (component as any).doImport([file]);
            tick();

            expect(component.error).toBe('import failed');
            expect(component.importing).toBe(false);
        }));

        it('should open the named CV modal when names are viewable', fakeAsync(() => {
            const candidate = new MockCandidate();
            const ref = modalRef(Promise.resolve(null));
            mockNgbModal.open.mockReturnValue(ref);
            vi.spyOn(component, 'canViewCandidateName').mockReturnValue(true);

            component.downloadGeneratedCV(candidate);
            tick();

            expect(ref.componentInstance.candidateId).toBe(candidate.id);
            expect(mockCandidateService.downloadCv).not.toHaveBeenCalled();
        }));

        it('should download an anonymised CV and expose download errors', fakeAsync(() => {
            const candidate = new MockCandidate();
            const tab: any = { location: { href: '' } };
            vi.spyOn(component, 'canViewCandidateName').mockReturnValue(false);
            vi.spyOn(window, 'open').mockReturnValue(tab);
            vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:test');
            mockCandidateService.downloadCv.mockReturnValue(of(new Blob(['cv'])));

            component.downloadGeneratedCV(candidate);
            tick();

            expect(mockCandidateService.downloadCv).toHaveBeenCalledWith({
                candidateId: candidate.id,
                showName: false,
                showContact: false
            });
            expect(tab.location.href).toBe('blob:test');

            mockCandidateService.downloadCv.mockReturnValue(throwError('cv failed'));
            component.downloadGeneratedCV(candidate);
            tick();
            expect(component.error).toBe('cv failed');
        }));

        it('should update a saved-search selection and expose server failure', fakeAsync(() => {
            const candidate = new MockCandidate();
            component.candidateSource = savedSearchSource();
            component.selectedCandidates = [];
            mockSavedSearchService.selectCandidate.mockReturnValue(of(null));

            component.onSelectionChange(candidate, true);
            tick();

            expect(mockSavedSearchService.selectCandidate).toHaveBeenCalledWith(component.candidateSource.id, { userId: component.loggedInUser.id, candidateId: candidate.id, selected: true });
            expect(component.savedSearchSelectionChange).toBe(true);

            mockSavedSearchService.selectCandidate.mockReturnValue(throwError('selection failed'));
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
            mockNgbModal.open.mockReturnValue(modalRef(Promise.resolve(true)));
            mockSavedSearchService.selectCandidate.mockReturnValue(of(null));

            component.onSelectionChange(candidate, false);
            tick();

            expect(candidate.contextNote).toBeNull();
            expect(component.savedSearchSelectionChange).toBe(false);
            expect(mockSavedSearchService.selectCandidate).toHaveBeenCalled();
        }));

        it('should restore selection when context-note deselection is declined or dismissed', fakeAsync(() => {
            const candidate = new MockCandidate();
            candidate.contextNote = 'Important';
            component.candidateSource = savedSearchSource();
            component.selectedCandidates = [candidate];

            mockNgbModal.open.mockReturnValue(modalRef(Promise.resolve(false)));
            component.onSelectionChange(candidate, false);
            tick();
            expect(candidate.selected).toBe(true);

            mockNgbModal.open.mockReturnValue(modalRef(Promise.reject('dismissed')));
            component.onSelectionChange(candidate, false);
            tick();
            expect(candidate.selected).toBe(true);
        }));

        it('should validate save selection for list and saved search sources', fakeAsync(() => {
            vi.spyOn((component as any), 'requestSaveSelection').mockReturnValue(undefined);

            component.candidateSource = new MockCandidateSource();
            component.selectedCandidates = [];
            component.saveSelection();
            expect(component.error).toBe('No candidates are selected');

            component.selectedCandidates = [new MockCandidate()];
            component.saveSelection();
            expect((component as any).requestSaveSelection).toHaveBeenCalled();

            component.candidateSource = savedSearchSource();
            mockSavedSearchService.getSelectionCount.mockReturnValue(of(0));
            component.saveSelection();
            tick();
            expect(component.error).toBe('No candidates are selected');

            mockSavedSearchService.getSelectionCount.mockReturnValue(of(2));
            component.saveSelection();
            tick();
            expect((component as any).requestSaveSelection).toHaveBeenCalled();

            mockSavedSearchService.getSelectionCount.mockReturnValue(throwError('count failed'));
            component.saveSelection();
            tick();
            expect(component.error).toBe('count failed');
        }));

        it('should configure and process the save-selection modal', fakeAsync(() => {
            const selection = { savedListId: 2, replace: false };
            const ref = modalRef(Promise.resolve(selection));
            mockNgbModal.open.mockReturnValue(ref);
            (component.candidateSource as any).sfJobOpp = { id: 8 };
            vi.spyOn((component as any), 'doSaveSelection').mockReturnValue(undefined);

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
                statusUpdateInfo: { status: 'active' }
            };
            const result: any = { id: 5, name: 'Target' };
            mockSavedSearchService.saveSelection.mockReturnValue(of(result));
            vi.spyOn((component as any), 'cacheTargetList').mockReturnValue(undefined);
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);
            const cacheService = (component as any).candidateSourceResultsCacheService;
            vi.spyOn(cacheService, 'removeFromCache').mockReturnValue(undefined);

            (component as any).doSaveSelection(target);
            tick();

            expect(mockSavedSearchService.saveSelection).toHaveBeenCalled();
            expect(component.targetListId).toBe(5);
            expect(component.targetListName).toBe('Target');
            expect(component.savedSelection).toBe(true);
            expect(component.savingSelection).toBe(false);
            expect(component.doSearch).toHaveBeenCalledWith(true);

            mockSavedSearchService.saveSelection.mockReturnValue(throwError('save failed'));
            (component as any).doSaveSelection(target);
            tick();
            expect(component.error).toBe('save failed');
            expect(component.savingSelection).toBe(false);
        }));

        it('should merge into an existing list and create a new list', fakeAsync(() => {
            const candidate = new MockCandidate();
            component.selectedCandidates = [candidate];
            mockSavedListService.get.mockReturnValue(of({ id: 7, name: 'Existing' } as any));
            mockSavedListCandidateService.saveSelection.mockReturnValue(of(null));
            const cacheService = (component as any).candidateSourceResultsCacheService;
            vi.spyOn(cacheService, 'removeFromCache').mockReturnValue(undefined);

            (component as any).doSaveSelection({
                savedListId: 7,
                replace: true,
                statusUpdateInfo: null
            });
            tick();

            expect(component.targetListId).toBe(7);
            expect(component.targetListName).toBe('Existing');
            expect(component.targetListReplace).toBe(true);
            expect(component.savedSelection).toBe(true);

            mockSavedListCandidateService.create.mockReturnValue(of({ id: 8, name: 'Created' } as any));
            vi.spyOn((component as any), 'cacheTargetList').mockReturnValue(undefined);
            (component as any).doSaveSelection({
                savedListId: 0,
                newListName: 'Created',
                replace: false,
                statusUpdateInfo: null
            });
            tick();

            expect(component.targetListId).toBe(8);
            expect(component.targetListName).toBe('Created');
            expect(component.targetListReplace).toBe(false);
            expect((component as any).cacheTargetList).toHaveBeenCalled();
        }));

        it('should identify a list source by sourceListId when saving a selection', () => {
            vi.spyOn((component as any).candidateSourceResultsCacheService, 'removeFromCache').mockReturnValue(undefined);
            component.candidateSource = new MockCandidateSource();
            component.selectedCandidates = [new MockCandidate()];
            mockSavedListService.get.mockReturnValue(of({ id: 7, name: 'Existing' } as any));
            mockSavedListCandidateService.saveSelection.mockReturnValue(of(null));

            (component as any).doSaveSelection({ savedListId: 7, replace: false });

            const request = vi.mocked(mockSavedListCandidateService.saveSelection).mock.lastCall[1];
            expect(request.sourceListId).toBe(component.candidateSource.id);
            expect(request.sourceSearchId).toBeUndefined();
        });

        it('should identify a matching search source by sourceSearchId, not sourceListId', () => {
            vi.spyOn((component as any).candidateSourceResultsCacheService, 'removeFromCache').mockReturnValue(undefined);
            component.candidateSource = { id: 1, name: 'Search', defaultSearch: false } as any;
            component.isMatchingSearch = true;
            component.results = { content: [new MockCandidate()] } as any;
            mockSavedListService.get.mockReturnValue(of({ id: 7, name: 'Existing' } as any));
            mockSavedListCandidateService.saveSelection.mockReturnValue(of(null));

            (component as any).doSaveSelection({ savedListId: 7, replace: false });

            const request = vi.mocked(mockSavedListCandidateService.saveSelection).mock.lastCall[1];
            expect(request.sourceSearchId).toBe(component.candidateSource.id);
            expect(request.sourceListId).toBeUndefined();
        });

        it('should expose merge and create list failures', fakeAsync(() => {
            component.selectedCandidates = [new MockCandidate()];
            mockSavedListService.get.mockReturnValue(of({ id: 7, name: 'Existing' } as any));
            mockSavedListCandidateService.saveSelection.mockReturnValue(throwError('merge failed'));

            (component as any).doSaveSelection({ savedListId: 7, replace: false });
            tick();
            expect(component.error).toBe('merge failed');
            expect(component.savingSelection).toBe(false);

            mockSavedListCandidateService.create.mockReturnValue(throwError('create failed'));
            (component as any).doSaveSelection({ savedListId: 0, replace: false });
            tick();
            expect(component.error).toBe('create failed');
            expect(component.savingSelection).toBe(false);
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

            mockLocalStorageService.get.mockReturnValue({
                sourceID: 1,
                listID: 9,
                name: 'Restored',
                replace: false
            });
            (component as any).restoreTargetListFromCache();

            expect(component.targetListId).toBe(9);
            expect(component.targetListName).toBe('Restored');
            expect(component.targetListReplace).toBe(false);
        });

        it('should configure review modal and refresh after completion', fakeAsync(() => {
            const candidate: any = new MockCandidate();
            candidate.candidateReviewStatusItems = [{
                    id: 22,
                    savedSearch: { id: component.candidateSource.id }
                }];
            const ref = modalRef(Promise.resolve(null));
            mockNgbModal.open.mockReturnValue(ref);
            vi.spyOn(component, 'onReviewStatusChange').mockReturnValue(undefined);

            component.review(candidate);
            tick();

            expect(ref.componentInstance.candidateReviewStatusItemId).toBe(22);
            expect(ref.componentInstance.candidateId).toBe(candidate.id);
            expect(component.onReviewStatusChange).toHaveBeenCalled();
        }));

        it('should perform selected and whole-list Salesforce updates', fakeAsync(() => {
            (component.candidateSource as any).sfJobOpp = { id: 2, sfId: 'SF-2' };
            component.selectedCandidates = [new MockCandidate()];
            mockCandidateService.createUpdateOppsFromCandidates.mockReturnValue(of(null));
            mockCandidateService.createUpdateOppsFromCandidateList.mockReturnValue(of(null));
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);

            (component as any).doCreateUpdateSalesforceOnList2({ stage: 'Prospect' }, true);
            tick();

            expect(mockCandidateService.createUpdateOppsFromCandidates).toHaveBeenCalled();
            expect(component.updating).toBe(false);

            (component as any).doCreateUpdateSalesforceOnList2(null, false);
            tick();

            expect(mockCandidateService.createUpdateOppsFromCandidateList).toHaveBeenCalled();
            expect(component.doSearch).toHaveBeenCalledWith(true);
        }));

        it('should expose Salesforce update failures', fakeAsync(() => {
            (component.candidateSource as any).sfJobOpp = { id: 2, sfId: 'SF-2' };
            component.selectedCandidates = [new MockCandidate()];
            mockCandidateService.createUpdateOppsFromCandidates.mockReturnValue(throwError('selected SF failed'));

            (component as any).doCreateUpdateSalesforceOnList2(null, true);
            tick();

            expect(component.error).toBe('selected SF failed');
            expect(component.updating).toBe(false);

            mockCandidateService.createUpdateOppsFromCandidateList.mockReturnValue(throwError('list SF failed'));
            (component as any).doCreateUpdateSalesforceOnList2(null, false);
            tick();

            expect(component.error).toBe('list SF failed');
            expect(component.updating).toBe(false);
        }));

        it('should request a whole-list Salesforce update when no candidates are selected', fakeAsync(() => {
            component.selectedCandidates = [];
            const ref = modalRef(Promise.resolve(true));
            mockNgbModal.open.mockReturnValue(ref);
            vi.spyOn((component as any), 'doCreateUpdateSalesforceOnList').mockReturnValue(undefined);

            component.createUpdateSalesforce();
            tick();

            expect((component as any).doCreateUpdateSalesforceOnList).toHaveBeenCalledWith(false);
        }));

        it('should validate and update candidate statuses for lists and searches', fakeAsync(() => {
            const candidate = new MockCandidate();
            component.selectedCandidates = [candidate];
            vi.spyOn((component as any), 'requestNewStatusInfo').mockReturnValue(undefined);

            component.updateStatusOfSelection();
            expect((component as any).requestNewStatusInfo).toHaveBeenCalledWith(1);

            component.candidateSource = savedSearchSource();
            mockSavedSearchService.getSelectionCount.mockReturnValue(of(3));
            component.updateStatusOfSelection();
            tick();
            expect((component as any).requestNewStatusInfo).toHaveBeenCalledWith(3);

            mockSavedSearchService.getSelectionCount.mockReturnValue(throwError('status count failed'));
            component.updateStatusOfSelection();
            tick();
            expect(component.error).toBe('status count failed');
        }));

        it('should update statuses for saved lists and saved searches', fakeAsync(() => {
            const candidate = new MockCandidate();
            component.selectedCandidates = [candidate];
            const info: any = { status: 'active' };
            mockCandidateService.updateStatus.mockReturnValue(of(null));
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);

            (component as any).updateCandidateStatuses(info);
            tick();

            expect(candidate.status).toBe(info.status);
            expect(component.updatingStatuses).toBe(false);

            component.candidateSource = savedSearchSource();
            mockSavedSearchService.updateSelectedStatuses.mockReturnValue(of(null));
            (component as any).updateCandidateStatuses(info);
            tick();

            expect(mockSavedSearchService.updateSelectedStatuses).toHaveBeenCalledWith(component.candidateSource.id, info);
            expect(component.updatingStatuses).toBe(false);
        }));

        it('should expose status update failures', fakeAsync(() => {
            component.selectedCandidates = [new MockCandidate()];
            mockCandidateService.updateStatus.mockReturnValue(throwError('list status failed'));

            (component as any).updateCandidateStatuses({ status: 'active' });
            tick();
            expect(component.error).toBe('list status failed');

            component.candidateSource = savedSearchSource();
            mockSavedSearchService.updateSelectedStatuses.mockReturnValue(throwError('search status failed'));
            (component as any).updateCandidateStatuses({ status: 'active' });
            tick();
            expect(component.error).toBe('search status failed');
        }));

        it('should assign tasks and refresh for both modal result paths', fakeAsync(() => {
            const task: any = { id: 3 };
            const ref = modalRef(Promise.resolve(task));
            mockNgbModal.open.mockReturnValue(ref);
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);

            component.assignTasks();
            tick();

            expect(ref.componentInstance.setTasks).toHaveBeenCalledWith(component.candidateSource);
            expect(component.monitoredTask).toBe(task);
            expect(component.doSearch).toHaveBeenCalledWith(true);

            mockNgbModal.open.mockReturnValue(modalRef(Promise.resolve(null)));
            component.assignTasks();
            tick();
            expect(component.doSearch).toHaveBeenCalledWith(true);
        }));

        it('should copy a source and expose copy failures', fakeAsync(() => {
            const target: any = { id: 9, name: 'Copied' };
            mockCandidateSourceService.copy.mockReturnValue(of(target));
            const ref = modalRef(Promise.resolve({
                savedListId: 9,
                newListName: 'Copied',
                replace: false,
                statusUpdateInfo: null
            }));
            mockNgbModal.open.mockReturnValue(ref);
            const cacheService = (component as any).candidateSourceResultsCacheService;
            vi.spyOn(cacheService, 'removeFromCache').mockReturnValue(undefined);

            component.doCopySource();
            tick();

            expect(component.targetListId).toBe(9);
            expect(component.targetListName).toBe('Copied');
            expect(component.savedSelection).toBe(false);
            expect(component.loading).toBe(false);

            mockCandidateSourceService.copy.mockReturnValue(throwError('copy failed'));
            mockNgbModal.open.mockReturnValue(ref);
            component.doCopySource();
            tick();

            expect(component.error).toBe('copy failed');
            expect(component.loading).toBe(false);
        }));

        it('should toggle starred state through both service paths', fakeAsync(() => {
            const source: any = component.candidateSource;
            vi.spyOn(component, 'isStarred').mockReturnValue(false);
            mockCandidateSourceService.starSourceForUser.mockReturnValue(of(source));

            component.doToggleStarred();
            tick();

            expect(mockCandidateSourceService.starSourceForUser).toHaveBeenCalled();
            expect(component.loading).toBe(false);

            (component.isStarred as Mock).mockReturnValue(true);
            mockCandidateSourceService.unstarSourceForUser.mockReturnValue(of(source));
            component.doToggleStarred();
            tick();

            expect(mockCandidateSourceService.unstarSourceForUser).toHaveBeenCalled();
            expect(component.loading).toBe(false);
        }));

        it('should expose starred service errors', fakeAsync(() => {
            vi.spyOn(component, 'isStarred').mockReturnValue(false);
            mockCandidateSourceService.starSourceForUser.mockReturnValue(throwError('star failed'));

            component.doToggleStarred();
            tick();

            expect(component.error).toBe('star failed');
            expect(component.loading).toBe(false);

            (component.isStarred as Mock).mockReturnValue(true);
            mockCandidateSourceService.unstarSourceForUser.mockReturnValue(throwError('unstar failed'));
            component.doToggleStarred();
            tick();

            expect(component.error).toBe('unstar failed');
            expect(component.loading).toBe(false);
        }));

        it('should close selected opportunities and deselect affected candidates', fakeAsync(() => {
            const first = new MockCandidate();
            const second = new MockCandidate();
            second.id = 2;
            component.selectedCandidates = [first, second];
            const job: any = { sfId: 'SF-JOB' };
            mockCandidateService.createUpdateOppsFromCandidates.mockReturnValue(of(null));
            vi.spyOn(component, 'doSearch').mockReturnValue(undefined);

            (component as any).doCloseOpps([first], job, { stage: 'Closed' });
            tick();

            expect(component.selectedCandidates).toEqual([second]);
            expect(component.doSearch).toHaveBeenCalledWith(true);
            expect(component.closing).toBe(false);

            mockCandidateService.createUpdateOppsFromCandidates.mockReturnValue(throwError('close failed'));
            (component as any).doCloseOpps([second], job, null);
            tick();

            expect(component.error).toBe('close failed');
            expect(component.closing).toBe(false);
        }));

        it('should configure close-opportunity modals', fakeAsync(() => {
            const candidate = new MockCandidate();
            const info: any = { stage: 'Closed' };
            const job: any = { sfId: 'SF-JOB' };
            (component.candidateSource as any).sfJobOpp = job;
            component.selectedCandidates = [candidate];
            const ref = modalRef(Promise.resolve(info));
            mockNgbModal.open.mockReturnValue(ref);
            vi.spyOn((component as any), 'doCloseOpps').mockReturnValue(undefined);

            component.closeSelectedOpportunities();
            tick();
            expect(ref.componentInstance.closing).toBe(true);
            expect((component as any).doCloseOpps).toHaveBeenCalledWith([candidate], job, info);

            (component as any).doCloseOpps.mockClear();
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

            mockAuthorizationService.canViewCandidateName.mockReturnValue(true);
            mockAuthorizationService.isEmployerPartner.mockReturnValue(true);
            mockAuthorizationService.isReadOnly.mockReturnValue(true);

            expect(component.canViewCandidateName()).toBe(true);
            expect(component.isEmployerPartner()).toBe(true);
            expect(component.isReadOnly()).toBe(true);
        });
        it('should open a candidate in a new tab', () => {
            vi.spyOn(window, 'open').mockReturnValue(undefined);

            component.openCandidateInNewTab('12345');

            expect(window.open).toHaveBeenCalledWith('/candidate/12345', '_blank');
        });
    });

    describe('matching context for match explanations', () => {

        const matching = (id: number, description: string, updatedDate = '2026-10-01T10:15:30Z') => ({ id, matchingDescription: description, updatedDate });

        function listSource(id: number): any {
            const source = new MockCandidateSource();
            source.id = id;
            return source;
        }

        function searchSource(id: number, defaultSearch: boolean, autoUpdateOnSearch = false): any {
            const search: any = new MockSavedSearch();
            search.id = id;
            search.defaultSearch = defaultSearch;
            search.autoUpdateOnSearch = autoUpdateOnSearch;
            return search;
        }

        function changeSource(source: any) {
            const previous = component.candidateSource;
            component.candidateSource = source;
            component.ngOnChanges({
                candidateSource: new SimpleChange(previous, source, false)
            });
        }

        function runSearch() {
            component.searchRequest = {} as any;
            (component as any).updatedSearch();
        }

        beforeEach(() => {
            mockCandidateSourceCandidateService.searchPaged.mockReturnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));
            mockCandidateService.searchOrMatch.mockReturnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));
            mockCasiAdminService.getServiceList.mockReturnValue(of([]));
            mockSavedListService.resolveMatching.mockClear();
            mockSavedSearchService.resolveMatching.mockClear();
            fixture.detectChanges(); // ngOnInit
        });

        it('should not resolve the matching context just because a source is shown', () => {
            changeSource(listSource(1));

            expect(mockSavedListService.resolveMatching).not.toHaveBeenCalled();
            expect(component.sourceMatching).toBeUndefined();
        });

        it('should resolve a list\'s matching context once, when a candidate is first selected', () => {
            mockSavedListService.resolveMatching.mockReturnValue(of(matching(5, '<p>ICU nurse</p>')));
            changeSource(listSource(1));

            component.setCurrentCandidate(new MockCandidate());
            component.setCurrentCandidate(new MockCandidate());

            expect(mockSavedListService.resolveMatching).toHaveBeenCalledTimes(1);

            expect(mockSavedListService.resolveMatching).toHaveBeenCalledWith(1);
            expect(component.sourceMatching).toEqual(matching(5, '<p>ICU nurse</p>'));
        });

        it('should resolve a search\'s matching context through the saved search service', () => {
            mockSavedSearchService.resolveMatching.mockReturnValue(of(matching(6, '<p>Welder</p>')));
            changeSource(searchSource(3, false));

            component.setCurrentCandidate(new MockCandidate());

            expect(mockSavedSearchService.resolveMatching).toHaveBeenCalledTimes(1);

            expect(mockSavedSearchService.resolveMatching).toHaveBeenCalledWith(3);
            expect(mockSavedListService.resolveMatching).not.toHaveBeenCalled();
            expect(component.sourceMatching).toEqual(matching(6, '<p>Welder</p>'));
        });

        it('should record a source with no matching context as null', () => {
            mockSavedListService.resolveMatching.mockReturnValue(of(null));
            changeSource(listSource(1));

            component.setCurrentCandidate(new MockCandidate());

            expect(component.sourceMatching).toBeNull();
        });

        it('should not resolve a matching context when a candidate is selected without a source', () => {
            component.candidateSource = undefined;

            component.setCurrentCandidate(new MockCandidate());

            expect(mockSavedListService.resolveMatching).not.toHaveBeenCalled();
            expect(mockSavedSearchService.resolveMatching).not.toHaveBeenCalled();
            expect(component.sourceMatching).toBeUndefined();
        });

        it('should discard the matching context when the source changes, ignoring a slow response for the old source', () => {
            const slowFirst = new Subject<any>();
            mockSavedListService.resolveMatching.mockImplementation((id: number) => id === 1 ? slowFirst : of(matching(9, '<p>Second</p>')));
            changeSource(listSource(1));
            component.setCurrentCandidate(new MockCandidate());

            changeSource(listSource(2));
            expect(component.sourceMatching).toBeUndefined();
            component.setCurrentCandidate(new MockCandidate());
            slowFirst.next(matching(8, '<p>First</p>'));

            expect(component.sourceMatching).toEqual(matching(9, '<p>Second</p>'));
        });

        it('should refresh the matching context after a default search is run, since running it saves its requirements', () => {
            mockSavedSearchService.resolveMatching.mockReturnValueOnce(of(matching(6, '<p>First</p>', '2026-10-01T10:00:00Z'))).mockReturnValueOnce(of(matching(6, '<p>Second</p>', '2026-10-01T11:00:00Z')));
            changeSource(searchSource(3, true));
            component.setCurrentCandidate(new MockCandidate());

            runSearch();

            expect(mockSavedSearchService.resolveMatching).toHaveBeenCalledTimes(2);
            expect(component.sourceMatching).toEqual(matching(6, '<p>Second</p>', '2026-10-01T11:00:00Z'));
        });

        it('should not refresh the matching context after running a named search which doesn\'t update automatically', () => {
            mockSavedSearchService.resolveMatching.mockReturnValue(of(matching(6, '<p>First</p>')));
            changeSource(searchSource(3, false));
            component.setCurrentCandidate(new MockCandidate());

            runSearch();

            expect(mockSavedSearchService.resolveMatching).toHaveBeenCalledTimes(1);
        });

        it('should not resolve the matching context after a search run if it is not yet in use', () => {
            changeSource(searchSource(3, true));

            runSearch();

            expect(mockSavedSearchService.resolveMatching).not.toHaveBeenCalled();
        });

        it('should not report pending requirements when they have the same text content as the Matching', () => {
            mockSavedSearchService.resolveMatching.mockReturnValue(of(matching(6, '<p>ICU nurse</p>')));
            changeSource(searchSource(3, true));
            component.setCurrentCandidate(new MockCandidate());

            component.requirements = 'ICU  nurse';

            expect(component.requirementsPendingMessage).toBeUndefined();
        });

        it('should ask the user to run a default search whose requirements have changed', () => {
            mockSavedSearchService.resolveMatching.mockReturnValue(of(matching(6, '<p>ICU nurse</p>')));
            changeSource(searchSource(3, true));
            component.setCurrentCandidate(new MockCandidate());

            component.requirements = '<p>Paediatric nurse</p>';

            expect(component.requirementsPendingMessage).toContain('Run the search');
        });

        it('should keep reporting pending requirements consistently until the requirements change', () => {
            mockSavedSearchService.resolveMatching.mockReturnValue(of(matching(6, '<p>ICU nurse</p>')));
            changeSource(searchSource(3, true));
            component.setCurrentCandidate(new MockCandidate());
            component.requirements = '<p>Paediatric nurse</p>';

            const first = component.requirementsPendingMessage;
            const second = component.requirementsPendingMessage;
            expect(first).toContain('Run the search');
            expect(second).toBe(first);

            component.requirements = '<p>ICU nurse</p>';
            expect(component.requirementsPendingMessage).toBeUndefined();
        });

        it('should ask the user to save a named search whose requirements have unsaved changes', () => {
            mockSavedSearchService.resolveMatching.mockReturnValue(of(null));
            changeSource(searchSource(3, false));
            component.setCurrentCandidate(new MockCandidate());

            component.requirements = '<p>Paediatric nurse</p>';

            expect(component.requirementsPendingMessage).toContain('Save the search');
        });

        it('should never report pending requirements for a list, or when no requirements are being edited', () => {
            mockSavedListService.resolveMatching.mockReturnValue(of(matching(5, '<p>ICU nurse</p>')));
            changeSource(listSource(1));
            component.setCurrentCandidate(new MockCandidate());
            component.requirements = '<p>Paediatric nurse</p>';
            expect(component.requirementsPendingMessage).toBeUndefined();

            mockSavedSearchService.resolveMatching.mockReturnValue(of(matching(6, '<p>ICU nurse</p>')));
            changeSource(searchSource(3, true));
            component.setCurrentCandidate(new MockCandidate());
            component.requirements = undefined;
            expect(component.requirementsPendingMessage).toBeUndefined();
        });

        it('should not surface a page error when the matching context cannot be resolved', () => {
            mockSavedListService.resolveMatching.mockReturnValue(throwError('lookup failed'));
            vi.spyOn(console, 'warn').mockReturnValue(undefined);
            component.error = null;
            changeSource(listSource(1));

            component.setCurrentCandidate(new MockCandidate());

            expect(component.error).toBeNull();
            expect(component.sourceMatching).toBeUndefined();
            expect(console.warn).toHaveBeenCalled();
        });
    });
});
