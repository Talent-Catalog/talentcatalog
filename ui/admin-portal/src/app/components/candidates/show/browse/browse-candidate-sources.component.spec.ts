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
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Router} from '@angular/router';
import {of, throwError} from 'rxjs';

import {BrowseCandidateSourcesComponent} from './browse-candidate-sources.component';
import {AuthenticationService} from '../../../../services/authentication.service';
import {CandidateSourceService} from '../../../../services/candidate-source.service';
import {AuthorizationService} from '../../../../services/authorization.service';
import {
  CandidateSourceResultsCacheService
} from '../../../../services/candidate-source-results-cache.service';
import {SalesforceService} from '../../../../services/salesforce.service';
import {SavedSearchService} from '../../../../services/saved-search.service';
import {LocalStorageService} from '../../../../services/local-storage.service';
import {CandidateSource, CandidateSourceType, DtoType, SearchBy} from '../../../../model/base';
import {
  SavedSearchSubtype,
  SavedSearchType,
  SearchSavedSearchRequest
} from '../../../../model/saved-search';
import {ContentUpdateType, SearchSavedListRequest} from '../../../../model/saved-list';
import {
  CreateUpdateSearchComponent
} from '../../../search/create-update/create-update-search.component';
import {CreateUpdateListComponent} from '../../../list/create-update/create-update-list.component';
import {SelectListComponent} from '../../../list/select/select-list.component';
import {ConfirmationComponent} from '../../../util/confirm/confirmation.component';

describe('BrowseCandidateSourcesComponent', () => {
  let component: BrowseCandidateSourcesComponent;
  let fixture: ComponentFixture<BrowseCandidateSourcesComponent>;

  let candidateSourceService: jasmine.SpyObj<CandidateSourceService>;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let cacheService: jasmine.SpyObj<CandidateSourceResultsCacheService>;
  let router: jasmine.SpyObj<Router>;
  let savedSearchService: jasmine.SpyObj<SavedSearchService>;

  const loggedInUser = {id: 99, firstName: 'Test', lastName: 'User'} as any;

  const savedList = (id = 1, name = 'List one'): CandidateSource => ({
    id,
    name,
    users: [],
    watcherUserIds: [],
    sfJobOpp: {id: 55},
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
    dismiss: jasmine.createSpy('dismiss'),
    close: jasmine.createSpy('close')
  });

  beforeEach(async () => {
    candidateSourceService = jasmine.createSpyObj<CandidateSourceService>(
      'CandidateSourceService',
      [
        'searchPaged',
        'copy',
        'delete',
        'starSourceForUser',
        'unstarSourceForUser'
      ]
    );
    localStorageService = jasmine.createSpyObj<LocalStorageService>(
      'LocalStorageService',
      ['get', 'set']
    );
    authenticationService = jasmine.createSpyObj<AuthenticationService>(
      'AuthenticationService',
      ['getLoggedInUser']
    );
    authorizationService = jasmine.createSpyObj<AuthorizationService>(
      'AuthorizationService',
      [
        'isReadOnly',
        'isEmployerPartner',
        'isCandidateSourceMine',
        'isStarredByMe'
      ]
    );
    modalService = jasmine.createSpyObj<NgbModal>('NgbModal', ['open']);
    cacheService = jasmine.createSpyObj<CandidateSourceResultsCacheService>(
      'CandidateSourceResultsCacheService',
      ['removeFromCache']
    );
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    savedSearchService = jasmine.createSpyObj<SavedSearchService>(
      'SavedSearchService',
      ['addWatcher', 'removeWatcher']
    );

    authenticationService.getLoggedInUser.and.returnValue(loggedInUser);
    authorizationService.isReadOnly.and.returnValue(false);
    authorizationService.isEmployerPartner.and.returnValue(false);
    authorizationService.isCandidateSourceMine.and.returnValue(true);
    authorizationService.isStarredByMe.and.returnValue(false);
    localStorageService.get.and.returnValue(null);
    candidateSourceService.searchPaged.and.returnValue(of(searchResults()));

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [BrowseCandidateSourcesComponent],
      providers: [
        UntypedFormBuilder,
        {provide: CandidateSourceService, useValue: candidateSourceService},
        {provide: LocalStorageService, useValue: localStorageService},
        {provide: AuthenticationService, useValue: authenticationService},
        {provide: AuthorizationService, useValue: authorizationService},
        {provide: NgbModal, useValue: modalService},
        {provide: CandidateSourceResultsCacheService, useValue: cacheService},
        {provide: Router, useValue: router},
        {provide: SalesforceService, useValue: {}},
        {provide: SavedSearchService, useValue: savedSearchService}
      ]
    })
    .overrideComponent(BrowseCandidateSourcesComponent, {
      set: {template: ''}
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
    localStorageService.get.and.returnValue('restored');
    spyOn(component, 'subscribeToFilterChanges');
    spyOn(component, 'search');

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
    localStorageService.get.and.returnValue('health');
    spyOn(component, 'search');

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
    spyOn(component, 'search');

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
    expect(component.getBrowserDisplayString(savedList())).toBe(
      'List one(AU) - Prospect'
    );
  });

  it('should not append job data to saved searches', () => {
    component.searchBy = SearchBy.registeredJob;
    expect(component.getBrowserDisplayString(savedSearch())).toBe('Search one');
  });

  it('should debounce form changes before searching', fakeAsync(() => {
    spyOn(component, 'search');
    component.subscribeToFilterChanges();

    component.searchForm.patchValue({keyword: 'abc'});
    tick(399);
    expect(component.search).not.toHaveBeenCalled();

    tick(1);
    expect(component.search).toHaveBeenCalledTimes(1);
  }));

  [
    {searchBy: SearchBy.mine, property: 'owned'},
    {searchBy: SearchBy.sharedWithMe, property: 'shared'},
    {searchBy: SearchBy.watched, property: 'watched'},
    {searchBy: SearchBy.externalLink, property: 'shortName'},
    {searchBy: SearchBy.registeredJob, property: 'registeredJob'}
  ].forEach(({searchBy, property}) => {
    it(`should create the ${SearchBy[searchBy]} search request`, () => {
      component.searchBy = searchBy;
      component.search();

      const request = candidateSourceService.searchPaged.calls.mostRecent().args[1] as any;
      expect(request[property]).toBeTrue();
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

    const request = candidateSourceService.searchPaged.calls.mostRecent().args[1] as any;
    expect(request.global).toBeTrue();
    expect(request.owned).toBeTrue();
    expect(request.shared).toBeTrue();
  });

  it('should create a saved-list request', () => {
    component.sourceType = CandidateSourceType.SavedList;
    component.search();

    const request = candidateSourceService.searchPaged.calls.mostRecent().args[1];
    expect(request instanceof SearchSavedListRequest).toBeTrue();
  });

  it('should add saved-search type filters only to saved-search requests', () => {
    component.savedSearchType = SavedSearchType.profession;
    component.savedSearchSubtype = SavedSearchSubtype.healthcare;
    component.search();

    const request = candidateSourceService.searchPaged.calls.mostRecent().args[1] as SearchSavedSearchRequest;
    expect(request.savedSearchType).toBe(SavedSearchType.profession);
    expect(request.savedSearchSubtype).toBe(SavedSearchSubtype.healthcare);
    expect(request.global).toBeTrue();
    expect(request.owned).toBeTrue();
    expect(request.shared).toBeTrue();
  });

  it('should restore a previously selected source', () => {
    const first = savedSearch(1, 'First');
    const second = savedSearch(2, 'Second');

    localStorageService.get.and.returnValue(2);

    candidateSourceService.searchPaged.and.returnValue(
      of(searchResults([first, second]))
    );

    component.search();

    expect(component.selectedIndex).toBe(1);
    expect(component.selectedSource).toBe(second);
    expect(component.loading).toBeFalse();
  });
  
  it('should select the first source when there is no stored source', () => {
    const first = savedSearch(1, 'First');
    candidateSourceService.searchPaged.and.returnValue(of(searchResults([first])));
    spyOn(component, 'select').and.callThrough();

    component.search();

    expect(component.select).toHaveBeenCalledWith(first);
  });

  it('should select the first source when stored source is not in the results', () => {
    const first = savedSearch(1, 'First');

    localStorageService.get.and.returnValues('', 999);

    candidateSourceService.searchPaged.and.returnValue(
      of(searchResults([first]))
    );

    const selectSpy = spyOn(component, 'select').and.callThrough();

    component.search();

    expect(selectSpy).toHaveBeenCalledWith(first);
  });

  it('should handle empty search results', () => {
    candidateSourceService.searchPaged.and.returnValue(of(searchResults()));
    component.selectedSource = savedSearch();

    component.search();

    expect(component.results.content).toEqual([]);
    expect(component.loading).toBeFalse();
  });

  it('should expose search failures', () => {
    candidateSourceService.searchPaged.and.returnValue(throwError('search failed'));

    component.search();

    expect(component.error).toBe('search failed');
    expect(component.loading).toBeFalse();
  });

  it('should select and persist a source', () => {
    const first = savedSearch(1);
    const second = savedSearch(2);
    component.results = searchResults([first, second]);

    component.select(second);

    expect(component.selectedSource).toBe(second);
    expect(component.selectedIndex).toBe(1);
    expect(localStorageService.set).toHaveBeenCalledWith(
      jasmine.stringContaining('BrowseKey'),
      2
    );
  });

  it('should build standard and type-specific saved-state keys', () => {
    expect((component as any).savedStateKey()).toContain('BrowseKey');

    component.searchBy = SearchBy.type;
    component.savedSearchType = SavedSearchType.profession;
    component.savedSearchSubtype = SavedSearchSubtype.healthcare;
    expect((component as any).savedStateKey()).toContain(
      `${SavedSearchType.profession}/${SavedSearchSubtype.healthcare}`
    );

    component.savedSearchSubtype = null;
    expect((component as any).savedStateKey()).toContain(
      `${SavedSearchType.profession}`
    );
  });

  it('should move selection with arrow keys and respect boundaries', () => {
    const first = savedSearch(1);
    const second = savedSearch(2);
    component.results = searchResults([first, second]);
    component.selectedIndex = 0;
    spyOn(component, 'select').and.callThrough();

    component.keyDown({key: 'ArrowUp'} as KeyboardEvent);
    expect(component.selectedIndex).toBe(0);

    component.keyDown({key: 'ArrowDown'} as KeyboardEvent);
    expect(component.selectedIndex).toBe(1);
    expect(component.select).toHaveBeenCalledWith(second);

    component.keyDown({key: 'ArrowDown'} as KeyboardEvent);
    expect(component.selectedIndex).toBe(1);

    component.keyDown({key: 'ArrowUp'} as KeyboardEvent);
    expect(component.selectedIndex).toBe(0);
    expect(component.select).toHaveBeenCalledWith(first);

    component.keyDown({key: 'Other'} as KeyboardEvent);
    expect(component.selectedIndex).toBe(0);
  });

  it('should open and process saved-search copy modal', fakeAsync(() => {
    const source = savedSearch();
    const ref = modalRef(Promise.resolve(true));
    modalService.open.and.returnValue(ref);
    spyOn(component, 'search');

    component.onCopySource(source);
    tick();

    expect(modalService.open).toHaveBeenCalledWith(CreateUpdateSearchComponent);
    expect(ref.componentInstance.savedSearch).toBe(source);
    expect(ref.componentInstance.copy).toBeTrue();
    expect(component.search).toHaveBeenCalled();
  }));

  it('should tolerate saved-search copy modal dismissal', fakeAsync(() => {
    modalService.open.and.returnValue(modalRef(Promise.reject('dismissed')));
    component.onCopySource(savedSearch());
    tick();
    expect(component.loading).not.toBeTrue();
  }));

  it('should configure and complete saved-list copy', fakeAsync(() => {
    const source = savedList();
    const target = savedList(9, 'Target');
    const selection = {
      savedListId: 9,
      newListName: 'Target',
      statusUpdateInfo: {status: 'active'},
      replace: true
    } as any;
    const ref = modalRef(Promise.resolve(selection));
    modalService.open.and.returnValue(ref);
    candidateSourceService.copy.and.returnValue(of(target));
    spyOn(component, 'search');

    component.onCopySource(source);
    tick();

    expect(modalService.open).toHaveBeenCalledWith(SelectListComponent, {size: 'lg'});
    expect(ref.componentInstance.action).toBe('Copy');
    expect(ref.componentInstance.title).toBe('Copy to another List');
    expect(ref.componentInstance.readOnly).toBeFalse();
    expect(ref.componentInstance.employerPartner).toBeFalse();
    expect(ref.componentInstance.canChangeStatuses).toBeTrue();
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
    expect(component.loading).toBeFalse();
  }));

  it('should use add update type and expose saved-list copy failures', fakeAsync(() => {
    const source = savedList();
    modalService.open.and.returnValue(modalRef(Promise.resolve({
      savedListId: 9,
      newListName: null,
      statusUpdateInfo: null,
      replace: false
    })));
    candidateSourceService.copy.and.returnValue(throwError('copy failed'));

    component.onCopySource(source);
    tick();

    const request = candidateSourceService.copy.calls.mostRecent().args[1] as any;
    expect(request.updateType).toBe(ContentUpdateType.add);
    expect(component.error).toBe('copy failed');
    expect(component.loading).toBeFalse();
  }));

  it('should reject deletion when source is not owned', () => {
    authorizationService.isCandidateSourceMine.and.returnValue(false);
    component.onDeleteSource(savedList());
    expect(component.error).toBe('You can not delete this saved search/list.');
    expect(modalService.open).not.toHaveBeenCalled();
  });

  it('should delete an owned source after confirmation', fakeAsync(() => {
    const source = savedList();
    const ref = modalRef(Promise.resolve(true));
    modalService.open.and.returnValue(ref);
    candidateSourceService.delete.and.returnValue(of(null));
    spyOn(component, 'search');

    component.onDeleteSource(source);
    tick();

    expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });
    expect(ref.componentInstance.message).toContain(source.name);
    expect(candidateSourceService.delete).toHaveBeenCalledWith(source);
    expect(component.search).toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  }));

  it('should not delete when confirmation is false or dismissed', fakeAsync(() => {
    modalService.open.and.returnValue(modalRef(Promise.resolve(false)));
    component.onDeleteSource(savedList());
    tick();
    expect(candidateSourceService.delete).not.toHaveBeenCalled();

    modalService.open.and.returnValue(modalRef(Promise.reject('dismissed')));
    component.onDeleteSource(savedList());
    tick();
    expect(candidateSourceService.delete).not.toHaveBeenCalled();
  }));

  it('should expose delete failures', fakeAsync(() => {
    modalService.open.and.returnValue(modalRef(Promise.resolve(true)));
    candidateSourceService.delete.and.returnValue(throwError('delete failed'));

    component.onDeleteSource(savedList());
    tick();

    expect(component.error).toBe('delete failed');
    expect(component.loading).toBeFalse();
  }));

  it('should edit saved searches and saved lists', fakeAsync(() => {
    const searchSpy = spyOn(component, 'search');

    const searchRef = modalRef(Promise.resolve(true));
    modalService.open.and.returnValue(searchRef);

    const search = savedSearch();

    component.onEditSource(search);
    tick();

    expect(modalService.open).toHaveBeenCalledWith(
      CreateUpdateSearchComponent
    );
    expect(searchRef.componentInstance.savedSearch).toBe(search);
    expect(searchSpy).toHaveBeenCalled();

    searchSpy.calls.reset();
    modalService.open.calls.reset();

    const listRef = modalRef(Promise.resolve(true));
    modalService.open.and.returnValue(listRef);

    const list = savedList();

    component.onEditSource(list);
    tick();

    expect(modalService.open).toHaveBeenCalledWith(
      CreateUpdateListComponent
    );
    expect(listRef.componentInstance.savedList).toBe(list);
    expect(searchSpy).toHaveBeenCalled();
  }));

  it('should tolerate edit modal dismissals', fakeAsync(() => {
    modalService.open.and.returnValue(modalRef(Promise.reject('dismissed')));
    component.onEditSource(savedSearch());
    tick();
    component.onEditSource(savedList());
    tick();
    expect(component.error).toBeUndefined();
  }));

  it('should create a new list and navigate to it', fakeAsync(() => {
    const created = savedList(8, 'Created');
    modalService.open.and.returnValue(modalRef(Promise.resolve(created)));

    component.onNewList();
    tick();

    expect(modalService.open).toHaveBeenCalledWith(CreateUpdateListComponent);
    expect(router.navigate).toHaveBeenCalledWith(['list', 8]);
  }));

  it('should dismiss a failed new-list modal result', fakeAsync(() => {
    const ref = modalRef(Promise.reject('dismissed'));
    modalService.open.and.returnValue(ref);

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

    candidateSourceService.starSourceForUser.and.returnValue(of(updated));
    component.onToggleStarred(original);
    expect(candidateSourceService.starSourceForUser).toHaveBeenCalledWith(
      original,
      {userId: 99}
    );
    expect(component.results.content[0]).toBe(updated);
    expect(component.selectedSource).toBe(updated);
    expect(component.loading).toBeFalse();

    authorizationService.isStarredByMe.and.returnValue(true);
    candidateSourceService.unstarSourceForUser.and.returnValue(of(original));
    component.onToggleStarred(updated);
    expect(candidateSourceService.unstarSourceForUser).toHaveBeenCalledWith(
      updated,
      {userId: 99}
    );
    expect(component.loading).toBeFalse();
  });

  it('should expose star and unstar failures', () => {
    const source = savedList();
    candidateSourceService.starSourceForUser.and.returnValue(throwError('star failed'));
    component.onToggleStarred(source);
    expect(component.error).toBe('star failed');
    expect(component.loading).toBeFalse();

    authorizationService.isStarredByMe.and.returnValue(true);
    candidateSourceService.unstarSourceForUser.and.returnValue(throwError('unstar failed'));
    component.onToggleStarred(source);
    expect(component.error).toBe('unstar failed');
    expect(component.loading).toBeFalse();
  });

  it('should add and remove saved-search watchers', () => {
    const source = savedSearch() as any;
    component.results = searchResults([source]);
    component.selectedIndex = 0;
    component.selectedSource = source;

    const watched = {...source, watcherUserIds: [99]};
    savedSearchService.addWatcher.and.returnValue(of(watched));
    component.onToggleWatch(source);
    expect(savedSearchService.addWatcher).toHaveBeenCalledWith(2, {userId: 99});
    expect(component.selectedSource).toBe(watched);
    expect(component.loading).toBeFalse();

    savedSearchService.removeWatcher.and.returnValue(of(source));
    component.onToggleWatch(watched);
    expect(savedSearchService.removeWatcher).toHaveBeenCalledWith(2, {userId: 99});
    expect(component.loading).toBeFalse();
  });

  it('should expose watcher failures and ignore saved lists', () => {
    const source = savedSearch() as any;
    savedSearchService.addWatcher.and.returnValue(throwError('watch failed'));
    component.onToggleWatch(source);
    expect(component.error).toBe('watch failed');
    expect(component.loading).toBeFalse();

    source.watcherUserIds = [99];
    savedSearchService.removeWatcher.and.returnValue(throwError('unwatch failed'));
    component.onToggleWatch(source);
    expect(component.error).toBe('unwatch failed');
    expect(component.loading).toBeFalse();

    savedSearchService.addWatcher.calls.reset();
    savedSearchService.removeWatcher.calls.reset();
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
    spyOn(component.subtypeChange, 'emit');

    component.subtypeChangeEvent(info);

    expect(component.subtypeChange.emit).toHaveBeenCalledWith(info);
  });
});
