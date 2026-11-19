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
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Router} from '@angular/router';
import {of, throwError} from 'rxjs';

import {BrowseCandidateSourcesComponent} from './browse-candidate-sources.component';
import {AuthenticationService} from '../../../../services/authentication.service';
import {CandidateSourceService} from '../../../../services/candidate-source.service';
import {AuthorizationService} from '../../../../services/authorization.service';
import {CandidateSourceResultsCacheService} from '../../../../services/candidate-source-results-cache.service';
import {SalesforceService} from '../../../../services/salesforce.service';
import {SavedSearchService} from '../../../../services/saved-search.service';
import {LocalStorageService} from '../../../../services/local-storage.service';
import {CandidateSource, CandidateSourceType, DtoType, SearchBy} from '../../../../model/base';
import {SavedSearchSubtype, SavedSearchType, SearchSavedSearchRequest} from '../../../../model/saved-search';
import {ContentUpdateType, SearchSavedListRequest} from '../../../../model/saved-list';
import {CreateUpdateSearchComponent} from '../../../search/create-update/create-update-search.component';
import {CreateUpdateListComponent} from '../../../list/create-update/create-update-list.component';
import {SelectListComponent} from '../../../list/select/select-list.component';
import {ConfirmationComponent} from '../../../util/confirm/confirmation.component';
import {expect, vi} from 'vitest';

describe('BrowseCandidateSourcesComponent', () => {
    let component: BrowseCandidateSourcesComponent;
    let fixture: ComponentFixture<BrowseCandidateSourcesComponent>;

    let candidateSourceService: MockedObject<CandidateSourceService>;
    let localStorageService: MockedObject<LocalStorageService>;
    let authenticationService: MockedObject<AuthenticationService>;
    let authorizationService: MockedObject<AuthorizationService>;
    let modalService: MockedObject<NgbModal>;
    let cacheService: MockedObject<CandidateSourceResultsCacheService>;
    let router: MockedObject<Router>;
    let savedSearchService: MockedObject<SavedSearchService>;

    const loggedInUser = { id: 99, firstName: 'Test', lastName: 'User' } as any;

    const savedList = (id = 1, name = 'List one'): CandidateSource => ({
        id,
        name,
        users: [],
        watcherUserIds: [],
        sfJobOpp: { id: 55 },
        sfJobCountry: 'AU',
        sfJobStage: 'Prospect'
    } as any);

    const savedSearch = (id = 2, name = 'Search one'): CandidateSource => ({
        id,
        name,
        users: [],
        watcherUserIds: [],
        savedSearchType: SavedSearchType.other,
        savedSearchSubtype: SavedSearchSubtype.other,
        defaultSearch: false,
        reviewable: true
    } as any);

    const searchResults = (content: CandidateSource[] = []): any => ({
        content,
        totalElements: content.length,
        number: 0,
        size: 30
    });

    const modalRef = (result: Promise<any> = Promise.resolve(undefined)): any => ({
        componentInstance: {},
        result,
        dismiss: vi.fn().mockName('dismiss'),
        close: vi.fn().mockName('close')
    });

    beforeEach(async () => {
        candidateSourceService = ({
            searchPaged: vi.fn().mockName("CandidateSourceService.searchPaged"),
            copy: vi.fn().mockName("CandidateSourceService.copy"),
            delete: vi.fn().mockName("CandidateSourceService.delete"),
            starSourceForUser: vi.fn().mockName("CandidateSourceService.starSourceForUser"),
            unstarSourceForUser: vi.fn().mockName("CandidateSourceService.unstarSourceForUser")
        } as unknown as typeof candidateSourceService);
        localStorageService = ({
            get: vi.fn().mockName("LocalStorageService.get"),
            set: vi.fn().mockName("LocalStorageService.set")
        } as unknown as typeof localStorageService);
        authenticationService = ({
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        } as unknown as typeof authenticationService);
        authorizationService = ({
            isReadOnly: vi.fn().mockName("AuthorizationService.isReadOnly"),
            isEmployerPartner: vi.fn().mockName("AuthorizationService.isEmployerPartner"),
            isCandidateSourceMine: vi.fn().mockName("AuthorizationService.isCandidateSourceMine"),
            isStarredByMe: vi.fn().mockName("AuthorizationService.isStarredByMe")
        } as unknown as typeof authorizationService);
        modalService = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalService);
        cacheService = ({
            removeFromCache: vi.fn().mockName("CandidateSourceResultsCacheService.removeFromCache")
        } as unknown as typeof cacheService);
        router = ({
            navigate: vi.fn().mockName("Router.navigate")
        } as unknown as typeof router);
        savedSearchService = ({
            addWatcher: vi.fn().mockName("SavedSearchService.addWatcher"),
            removeWatcher: vi.fn().mockName("SavedSearchService.removeWatcher")
        } as unknown as typeof savedSearchService);

        authenticationService.getLoggedInUser.mockReturnValue(loggedInUser);
        authorizationService.isReadOnly.mockReturnValue(false);
        authorizationService.isEmployerPartner.mockReturnValue(false);
        authorizationService.isCandidateSourceMine.mockReturnValue(true);
        authorizationService.isStarredByMe.mockReturnValue(false);
        localStorageService.get.mockReturnValue(null);
        candidateSourceService.searchPaged.mockReturnValue(of(searchResults()));

        await TestBed.configureTestingModule({
            imports: [ReactiveFormsModule],
            declarations: [BrowseCandidateSourcesComponent],
            providers: [
                UntypedFormBuilder,
                { provide: CandidateSourceService, useValue: candidateSourceService },
                { provide: LocalStorageService, useValue: localStorageService },
                { provide: AuthenticationService, useValue: authenticationService },
                { provide: AuthorizationService, useValue: authorizationService },
                { provide: NgbModal, useValue: modalService },
                { provide: CandidateSourceResultsCacheService, useValue: cacheService },
                { provide: Router, useValue: router },
                { provide: SalesforceService, useValue: {} },
                { provide: SavedSearchService, useValue: savedSearchService }
            ]
        })
            .overrideComponent(BrowseCandidateSourcesComponent, {
            set: { template: '' }
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(BrowseCandidateSourcesComponent);
        component = fixture.componentInstance;
        component.sourceType = CandidateSourceType.SavedSearch;
        component.searchBy = SearchBy.mine;
        component.loggedInUser = loggedInUser;
        component.pageNumber = 1;
        component.pageSize = 30;
        component.results = searchResults();
        component.searchForm = TestBed.inject(UntypedFormBuilder).group({
            keyword: [''],
            selectedStages: [['candidateSearch']]
        });
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize user, restored filter, paging, subscription and search', () => {
        localStorageService.get.mockReturnValue('restored');
        vi.spyOn(component, 'subscribeToFilterChanges').mockReturnValue(undefined);
        vi.spyOn(component, 'search').mockReturnValue(undefined);

        component.ngOnInit();

        expect(component.loggedInUser).toBe(loggedInUser);
        expect(component.keyword).toBe('restored');
        expect(component.selectedStages).toEqual(['candidateSearch']);
        expect(component.pageNumber).toBe(1);
        expect(component.pageSize).toBe(30);
        expect(component.subscribeToFilterChanges).toHaveBeenCalled();
        expect(component.search).toHaveBeenCalled();
    });

    it('should return empty getter values when the form is absent', () => {
        component.searchForm = null;
        expect(component.keyword).toBe('');
        expect(component.selectedStages).toBe('' as any);
    });

    it('should react to a later saved-search subtype change', () => {
        localStorageService.get.mockReturnValue('health');
        vi.spyOn(component, 'search').mockReturnValue(undefined);

        component.ngOnChanges({
            savedSearchSubtype: {
                previousValue: SavedSearchSubtype.business,
                currentValue: SavedSearchSubtype.healthcare,
                firstChange: false,
                isFirstChange: () => false
            }
        });

        expect(component.keyword).toBe('health');
        expect(component.search).toHaveBeenCalled();
    });

    it('should ignore first subtype changes and non-saved-search changes', () => {
        vi.spyOn(component, 'search').mockReturnValue(undefined);

        component.ngOnChanges({
            savedSearchSubtype: {
                previousValue: undefined,
                currentValue: SavedSearchSubtype.business,
                firstChange: true,
                isFirstChange: () => true
            }
        });
        expect(component.search).not.toHaveBeenCalled();

        component.sourceType = CandidateSourceType.SavedList;
        component.ngOnChanges({
            savedSearchSubtype: {
                previousValue: SavedSearchSubtype.business,
                currentValue: SavedSearchSubtype.healthcare,
                firstChange: false,
                isFirstChange: () => false
            }
        });
        expect(component.search).not.toHaveBeenCalled();
    });

    it('should display only the name for a normal source', () => {
        expect(component.getBrowserDisplayString(savedList())).toBe('List one');
    });

    it('should append country and stage for registered-job saved lists', () => {
        component.searchBy = SearchBy.registeredJob;
        expect(component.getBrowserDisplayString(savedList())).toBe('List one(AU) - Prospect');
    });

    it('should not append job data to saved searches', () => {
        component.searchBy = SearchBy.registeredJob;
        expect(component.getBrowserDisplayString(savedSearch())).toBe('Search one');
    });

    it('should debounce form changes before searching', fakeAsync(() => {
        vi.spyOn(component, 'search').mockReturnValue(undefined);
        component.subscribeToFilterChanges();

        component.searchForm.patchValue({ keyword: 'abc' });
        tick(399);
        expect(component.search).not.toHaveBeenCalled();

        tick(1);
        expect(component.search).toHaveBeenCalledTimes(1);
    }));

    [
        { searchBy: SearchBy.mine, property: 'owned' },
        { searchBy: SearchBy.sharedWithMe, property: 'shared' },
        { searchBy: SearchBy.watched, property: 'watched' },
        { searchBy: SearchBy.externalLink, property: 'shortName' },
        { searchBy: SearchBy.registeredJob, property: 'registeredJob' }
    ].forEach(({ searchBy, property }) => {
        it(`should create the ${SearchBy[searchBy]} search request`, () => {
            component.searchBy = searchBy;
            component.search();

            const request = vi.mocked(candidateSourceService.searchPaged).mock.lastCall[1] as any;
            expect(request[property]).toBe(true);
            expect(request.keyword).toBe('');
            expect(request.pageNumber).toBe(0);
            expect(request.pageSize).toBe(30);
            expect(request.sortFields).toEqual(['name']);
            expect(request.sortDirection).toBe('ASC');
            expect(request.dtoType).toBe(DtoType.MINIMAL);
        });
    });

    it('should set all visibility flags for all sources', () => {
        component.searchBy = SearchBy.all;
        component.search();

        const request = vi.mocked(candidateSourceService.searchPaged).mock.lastCall[1] as any;
        expect(request.global).toBe(true);
        expect(request.owned).toBe(true);
        expect(request.shared).toBe(true);
    });

    it('should create a saved-list request', () => {
        component.sourceType = CandidateSourceType.SavedList;
        component.search();

        const request = vi.mocked(candidateSourceService.searchPaged).mock.lastCall[1];
        expect(request instanceof SearchSavedListRequest).toBe(true);
    });

    it('should add saved-search type filters only to saved-search requests', () => {
        component.savedSearchType = SavedSearchType.profession;
        component.savedSearchSubtype = SavedSearchSubtype.healthcare;
        component.search();

        const request = vi.mocked(candidateSourceService.searchPaged).mock.lastCall[1] as SearchSavedSearchRequest;
        expect(request.savedSearchType).toBe(SavedSearchType.profession);
        expect(request.savedSearchSubtype).toBe(SavedSearchSubtype.healthcare);
        expect(request.global).toBe(true);
        expect(request.owned).toBe(true);
        expect(request.shared).toBe(true);
    });

    it('should restore a previously selected source', () => {
        const first = savedSearch(1, 'First');
        const second = savedSearch(2, 'Second');

        localStorageService.get.mockReturnValue(2);

        candidateSourceService.searchPaged.mockReturnValue(of(searchResults([first, second])));

        component.search();

        expect(component.selectedIndex).toBe(1);
        expect(component.selectedSource).toBe(second);
        expect(component.loading).toBe(false);
    });

    it('should select the first source when there is no stored source', () => {
        const first = savedSearch(1, 'First');
        candidateSourceService.searchPaged.mockReturnValue(of(searchResults([first])));
        vi.spyOn(component, 'select');

        component.search();

        expect(component.select).toHaveBeenCalledWith(first);
    });

    it('should select the first source when stored source is not in the results', () => {
        const first = savedSearch(1, 'First');

        localStorageService.get.mockReturnValueOnce('').mockReturnValueOnce(999);

        candidateSourceService.searchPaged.mockReturnValue(of(searchResults([first])));

        const selectSpy = vi.spyOn(component, 'select');

        component.search();

        expect(selectSpy).toHaveBeenCalledWith(first);
    });

    it('should handle empty search results', () => {
        candidateSourceService.searchPaged.mockReturnValue(of(searchResults()));
        component.selectedSource = savedSearch();

        component.search();

        expect(component.results.content).toEqual([]);
        expect(component.loading).toBe(false);
    });

    it('should expose search failures', () => {
        candidateSourceService.searchPaged.mockReturnValue(throwError('search failed'));

        component.search();

        expect(component.error).toBe('search failed');
        expect(component.loading).toBe(false);
    });

    it('should select and persist a source', () => {
        const first = savedSearch(1);
        const second = savedSearch(2);
        component.results = searchResults([first, second]);

        component.select(second);

        expect(component.selectedSource).toBe(second);
        expect(component.selectedIndex).toBe(1);
        expect(localStorageService.set).toHaveBeenCalledWith(expect.stringContaining('BrowseKey'), 2);
    });

    it('should build standard and type-specific saved-state keys', () => {
        expect((component as any).savedStateKey()).toContain('BrowseKey');

        component.searchBy = SearchBy.type;
        component.savedSearchType = SavedSearchType.profession;
        component.savedSearchSubtype = SavedSearchSubtype.healthcare;
        expect((component as any).savedStateKey()).toContain(`${SavedSearchType.profession}/${SavedSearchSubtype.healthcare}`);

        component.savedSearchSubtype = null;
        expect((component as any).savedStateKey()).toContain(`${SavedSearchType.profession}`);
    });

    it('should move selection with arrow keys and respect boundaries', () => {
        const first = savedSearch(1);
        const second = savedSearch(2);
        component.results = searchResults([first, second]);
        component.selectedIndex = 0;
        vi.spyOn(component, 'select');

        component.keyDown({ key: 'ArrowUp' } as KeyboardEvent);
        expect(component.selectedIndex).toBe(0);

        component.keyDown({ key: 'ArrowDown' } as KeyboardEvent);
        expect(component.selectedIndex).toBe(1);
        expect(component.select).toHaveBeenCalledWith(second);

        component.keyDown({ key: 'ArrowDown' } as KeyboardEvent);
        expect(component.selectedIndex).toBe(1);

        component.keyDown({ key: 'ArrowUp' } as KeyboardEvent);
        expect(component.selectedIndex).toBe(0);
        expect(component.select).toHaveBeenCalledWith(first);

        component.keyDown({ key: 'Other' } as KeyboardEvent);
        expect(component.selectedIndex).toBe(0);
    });

    it('should open and process saved-search copy modal', fakeAsync(() => {
        const source = savedSearch();
        const ref = modalRef(Promise.resolve(true));
        modalService.open.mockReturnValue(ref);
        vi.spyOn(component, 'search').mockReturnValue(undefined);

        component.onCopySource(source);
        tick();

        expect(modalService.open).toHaveBeenCalledWith(CreateUpdateSearchComponent);
        expect(ref.componentInstance.savedSearch).toBe(source);
        expect(ref.componentInstance.copy).toBe(true);
        expect(component.search).toHaveBeenCalled();
    }));

    it('should tolerate saved-search copy modal dismissal', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.reject('dismissed')));
        component.onCopySource(savedSearch());
        tick();
        expect(component.loading).not.toBe(true);
    }));

    it('should configure and complete saved-list copy', fakeAsync(() => {
        const source = savedList();
        const target = savedList(9, 'Target');
        const selection = {
            savedListId: 9,
            newListName: 'Target',
            statusUpdateInfo: { status: 'active' },
            replace: true
        } as any;
        const ref = modalRef(Promise.resolve(selection));
        modalService.open.mockReturnValue(ref);
        candidateSourceService.copy.mockReturnValue(of(target));
        vi.spyOn(component, 'search').mockReturnValue(undefined);

        component.onCopySource(source);
        tick();

        expect(modalService.open).toHaveBeenCalledWith(SelectListComponent, { size: 'lg' });
        expect(ref.componentInstance.action).toBe('Copy');
        expect(ref.componentInstance.title).toBe('Copy to another List');
        expect(ref.componentInstance.readOnly).toBe(false);
        expect(ref.componentInstance.employerPartner).toBe(false);
        expect(ref.componentInstance.canChangeStatuses).toBe(true);
        expect(ref.componentInstance.excludeList).toBe(source);
        expect(candidateSourceService.copy).toHaveBeenCalledWith(source, {
            savedListId: 9,
            newListName: 'Target',
            sourceListId: source.id,
            statusUpdateInfo: selection.statusUpdateInfo,
            updateType: ContentUpdateType.replace,
            jobId: 55
        });
        expect(cacheService.removeFromCache).toHaveBeenCalledWith(target);
        expect(component.search).toHaveBeenCalled();
        expect(component.loading).toBe(false);
    }));

    it('should use add update type and expose saved-list copy failures', fakeAsync(() => {
        const source = savedList();
        modalService.open.mockReturnValue(modalRef(Promise.resolve({
            savedListId: 9,
            newListName: null,
            statusUpdateInfo: null,
            replace: false
        })));
        candidateSourceService.copy.mockReturnValue(throwError('copy failed'));

        component.onCopySource(source);
        tick();

        const request = vi.mocked(candidateSourceService.copy).mock.lastCall[1] as any;
        expect(request.updateType).toBe(ContentUpdateType.add);
        expect(component.error).toBe('copy failed');
        expect(component.loading).toBe(false);
    }));

    it('should reject deletion when source is not owned', () => {
        authorizationService.isCandidateSourceMine.mockReturnValue(false);
        component.onDeleteSource(savedList());
        expect(component.error).toBe('You can not delete this saved search/list.');
        expect(modalService.open).not.toHaveBeenCalled();
    });

    it('should delete an owned source after confirmation', fakeAsync(() => {
        const source = savedList();
        const ref = modalRef(Promise.resolve(true));
        modalService.open.mockReturnValue(ref);
        candidateSourceService.delete.mockReturnValue(of(null));
        vi.spyOn(component, 'search').mockReturnValue(undefined);

        component.onDeleteSource(source);
        tick();

        expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, {
            centered: true,
            backdrop: 'static'
        });
        expect(ref.componentInstance.message).toContain(source.name);
        expect(candidateSourceService.delete).toHaveBeenCalledWith(source);
        expect(component.search).toHaveBeenCalled();
        expect(component.loading).toBe(false);
    }));

    it('should not delete when confirmation is false or dismissed', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.resolve(false)));
        component.onDeleteSource(savedList());
        tick();
        expect(candidateSourceService.delete).not.toHaveBeenCalled();

        modalService.open.mockReturnValue(modalRef(Promise.reject('dismissed')));
        component.onDeleteSource(savedList());
        tick();
        expect(candidateSourceService.delete).not.toHaveBeenCalled();
    }));

    it('should expose delete failures', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.resolve(true)));
        candidateSourceService.delete.mockReturnValue(throwError('delete failed'));

        component.onDeleteSource(savedList());
        tick();

        expect(component.error).toBe('delete failed');
        expect(component.loading).toBe(false);
    }));

    it('should edit saved searches and saved lists', fakeAsync(() => {
        const searchSpy = vi.spyOn(component, 'search').mockReturnValue(undefined);

        const searchRef = modalRef(Promise.resolve(true));
        modalService.open.mockReturnValue(searchRef);

        const search = savedSearch();

        component.onEditSource(search);
        tick();

        expect(modalService.open).toHaveBeenCalledWith(CreateUpdateSearchComponent);
        expect(searchRef.componentInstance.savedSearch).toBe(search);
        expect(searchSpy).toHaveBeenCalled();

        searchSpy.mockClear();
        modalService.open.mockClear();

        const listRef = modalRef(Promise.resolve(true));
        modalService.open.mockReturnValue(listRef);

        const list = savedList();

        component.onEditSource(list);
        tick();

        expect(modalService.open).toHaveBeenCalledWith(CreateUpdateListComponent);
        expect(listRef.componentInstance.savedList).toBe(list);
        expect(searchSpy).toHaveBeenCalled();
    }));

    it('should tolerate edit modal dismissals', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.reject('dismissed')));
        component.onEditSource(savedSearch());
        tick();
        component.onEditSource(savedList());
        tick();
        expect(component.error).toBeUndefined();
    }));

    it('should create a new list and navigate to it', fakeAsync(() => {
        const created = savedList(8, 'Created');
        modalService.open.mockReturnValue(modalRef(Promise.resolve(created)));

        component.onNewList();
        tick();

        expect(modalService.open).toHaveBeenCalledWith(CreateUpdateListComponent);
        expect(router.navigate).toHaveBeenCalledWith(['list', 8]);
    }));

    it('should dismiss a failed new-list modal result', fakeAsync(() => {
        const ref = modalRef(Promise.reject('dismissed'));
        modalService.open.mockReturnValue(ref);

        component.onNewList();
        tick();

        expect(ref.dismiss).toHaveBeenCalled();
    }));

    it('should star and unstar sources successfully', () => {
        const original = savedList(1);
        const updated = savedList(1, 'Updated');
        component.results = searchResults([original]);
        component.selectedIndex = 0;
        component.selectedSource = original;

        candidateSourceService.starSourceForUser.mockReturnValue(of(updated));
        component.onToggleStarred(original);
        expect(candidateSourceService.starSourceForUser).toHaveBeenCalledWith(original, { userId: 99 });
        expect(component.results.content[0]).toBe(updated);
        expect(component.selectedSource).toBe(updated);
        expect(component.loading).toBe(false);

        authorizationService.isStarredByMe.mockReturnValue(true);
        candidateSourceService.unstarSourceForUser.mockReturnValue(of(original));
        component.onToggleStarred(updated);
        expect(candidateSourceService.unstarSourceForUser).toHaveBeenCalledWith(updated, { userId: 99 });
        expect(component.loading).toBe(false);
    });

    it('should expose star and unstar failures', () => {
        const source = savedList();
        candidateSourceService.starSourceForUser.mockReturnValue(throwError('star failed'));
        component.onToggleStarred(source);
        expect(component.error).toBe('star failed');
        expect(component.loading).toBe(false);

        authorizationService.isStarredByMe.mockReturnValue(true);
        candidateSourceService.unstarSourceForUser.mockReturnValue(throwError('unstar failed'));
        component.onToggleStarred(source);
        expect(component.error).toBe('unstar failed');
        expect(component.loading).toBe(false);
    });

    it('should add and remove saved-search watchers', () => {
        const source = savedSearch() as any;
        component.results = searchResults([source]);
        component.selectedIndex = 0;
        component.selectedSource = source;

        const watched = { ...source, watcherUserIds: [99] };
        savedSearchService.addWatcher.mockReturnValue(of(watched));
        component.onToggleWatch(source);
        expect(savedSearchService.addWatcher).toHaveBeenCalledWith(2, { userId: 99 });
        expect(component.selectedSource).toBe(watched);
        expect(component.loading).toBe(false);

        savedSearchService.removeWatcher.mockReturnValue(of(source));
        component.onToggleWatch(watched);
        expect(savedSearchService.removeWatcher).toHaveBeenCalledWith(2, { userId: 99 });
        expect(component.loading).toBe(false);
    });

    it('should expose watcher failures and ignore saved lists', () => {
        const source = savedSearch() as any;
        savedSearchService.addWatcher.mockReturnValue(throwError('watch failed'));
        component.onToggleWatch(source);
        expect(component.error).toBe('watch failed');
        expect(component.loading).toBe(false);

        source.watcherUserIds = [99];
        savedSearchService.removeWatcher.mockReturnValue(throwError('unwatch failed'));
        component.onToggleWatch(source);
        expect(component.error).toBe('unwatch failed');
        expect(component.loading).toBe(false);

        savedSearchService.addWatcher.mockClear();
        savedSearchService.removeWatcher.mockClear();
        component.onToggleWatch(savedList());
        expect(savedSearchService.addWatcher).not.toHaveBeenCalled();
        expect(savedSearchService.removeWatcher).not.toHaveBeenCalled();
    });

    it('should update a non-selected source without replacing selectedSource', () => {
        const first = savedList(1, 'First');
        const second = savedList(2, 'Second');
        const updatedSecond = savedList(2, 'Updated second');
        component.results = searchResults([first, second]);
        component.selectedIndex = 0;
        component.selectedSource = first;

        (component as any).updateLocalCandidateSourceCopy(updatedSecond);

        expect(component.results.content[1]).toBe(updatedSecond);
        expect(component.selectedSource).toBe(first);
    });

    it('should ignore updates for sources not in the current results', () => {
        const first = savedList(1);
        component.results = searchResults([first]);
        component.selectedIndex = 0;
        component.selectedSource = first;

        (component as any).updateLocalCandidateSourceCopy(savedList(999));

        expect(component.results.content).toEqual([first]);
        expect(component.selectedSource).toBe(first);
    });

    it('should emit subtype changes', () => {
        const info = {
            title: 'Healthcare',
            savedSearchSubtype: SavedSearchSubtype.healthcare
        } as any;
        vi.spyOn(component.subtypeChange, 'emit').mockReturnValue(undefined);

        component.subtypeChangeEvent(info);

        expect(component.subtypeChange.emit).toHaveBeenCalledWith(info);
    });
});
