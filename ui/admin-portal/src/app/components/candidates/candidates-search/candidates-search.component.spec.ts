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
import {CandidatesSearchComponent} from './candidates-search.component';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {SavedSearchService} from '../../../services/saved-search.service';
import {BehaviorSubject, of, throwError} from 'rxjs';
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";

function fakeParamMap(params: { [key: string]: string }): ParamMap {
  return {
    get: (name: string) => (name in params ? params[name] : null),
    has: (name: string) => name in params,
    getAll: (name: string) => (name in params ? [params[name]] : []),
    keys: Object.keys(params)
  } as ParamMap;
}

describe('CandidatesSearchComponent', () => {
  let component: CandidatesSearchComponent;
  let fixture: ComponentFixture<CandidatesSearchComponent>;
  let paramMap$: BehaviorSubject<ParamMap>;
  let queryParamMap$: BehaviorSubject<ParamMap>;
  let mockSavedSearchService: any;
  let modalService: NgbModal;

  beforeEach(async () => {
    paramMap$ = new BehaviorSubject<ParamMap>(fakeParamMap({}));
    queryParamMap$ = new BehaviorSubject<ParamMap>(fakeParamMap({}));

    const mockActivatedRoute: any = {
      paramMap: paramMap$,
      queryParamMap: queryParamMap$
    };
    mockSavedSearchService = jasmine.createSpyObj('SavedSearchService', ['get', 'getDefault', 'updateJob']);

    await TestBed.configureTestingModule({
      declarations: [CandidatesSearchComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: SavedSearchService, useValue: mockSavedSearchService },
        { provide: NgbModal  },
      ]
    })
    .compileComponents();
    mockSavedSearchService.getDefault.and.returnValue(of());
    modalService = TestBed.inject(NgbModal);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidatesSearchComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load default search when no ID is provided in the route', fakeAsync(() => {
    const mockDefaultSearch = new MockSavedSearch();
    mockSavedSearchService.getDefault.and.returnValue(of(mockDefaultSearch));

    fixture.detectChanges();
    tick();

    expect(component.loading).toBe(false);
    expect(component.savedSearch).toEqual(mockDefaultSearch);
    expect(component.error).toBeUndefined();
  }));

  it('should handle error from saved search service when loading default search', fakeAsync(() => {
    const mockError = 'Error loading default search';
    mockSavedSearchService.getDefault.and.returnValue(throwError(mockError));

    fixture.detectChanges();
    tick();

    expect(component.loading).toBe(false);
    expect(component.savedSearch).toBeUndefined();
    expect(component.error).toEqual(mockError);
  }));

  it('should update page number and page size when route parameters change', () => {
    mockSavedSearchService.getDefault.and.returnValue(of(new MockSavedSearch()));
    queryParamMap$.next(fakeParamMap({pageNumber: '2', pageSize: '10'}));

    fixture.detectChanges();

    expect(component.pageNumber).toEqual(2);
    expect(component.pageSize).toEqual(10);
  });

  it('should load a saved search by id when the :id route param is present', fakeAsync(() => {
    const mockSavedSearch = new MockSavedSearch();
    mockSavedSearchService.get.and.returnValue(of(mockSavedSearch));
    paramMap$.next(fakeParamMap({id: '5'}));

    fixture.detectChanges();
    tick();

    expect(mockSavedSearchService.get).toHaveBeenCalledWith(5);
    expect(component.savedSearch).toEqual(mockSavedSearch);
    expect(component.loading).toBe(false);
  }));

  it('should not reload the saved search when only an unrelated query param changes', fakeAsync(() => {
    const mockDefaultSearch = new MockSavedSearch();
    mockSavedSearchService.getDefault.and.returnValue(of(mockDefaultSearch));

    fixture.detectChanges();
    tick();
    expect(mockSavedSearchService.getDefault).toHaveBeenCalledTimes(1);

    // Simulate Clear Search removing the 'job' query param from the URL - the :id route param
    // (and therefore which saved search is active) hasn't changed, so this must not retrigger a
    // reload (which would overwrite the just-cleared form with the persisted search contents).
    queryParamMap$.next(fakeParamMap({}));
    tick();

    expect(mockSavedSearchService.getDefault).toHaveBeenCalledTimes(1);
  }));

  it('should reload when the :id route param actually changes', fakeAsync(() => {
    mockSavedSearchService.getDefault.and.returnValue(of(new MockSavedSearch()));
    const otherSearch = new MockSavedSearch();
    mockSavedSearchService.get.and.returnValue(of(otherSearch));

    fixture.detectChanges();
    tick();
    expect(mockSavedSearchService.getDefault).toHaveBeenCalledTimes(1);

    paramMap$.next(fakeParamMap({id: '9'}));
    tick();

    expect(mockSavedSearchService.get).toHaveBeenCalledWith(9);
    expect(component.savedSearch).toEqual(otherSearch);
  }));

  describe('explicit job association on the default search', () => {

    it('should call updateJob when a job query param is present and the default search has no existing job', fakeAsync(() => {
      const defaultSearch = {...new MockSavedSearch(), id: 1, defaultSearch: true, sfJobOpp: null};
      const updatedSearch = {...defaultSearch, sfJobOpp: {id: 123}};
      mockSavedSearchService.getDefault.and.returnValue(of(defaultSearch));
      mockSavedSearchService.updateJob.and.returnValue(of(updatedSearch));
      queryParamMap$.next(fakeParamMap({job: '123'}));

      fixture.detectChanges();
      tick();

      expect(mockSavedSearchService.updateJob).toHaveBeenCalledWith(defaultSearch.id, 123);
      expect(component.savedSearch).toEqual(updatedSearch);
    }));

    it('should call updateJob to replace a different existing job association', fakeAsync(() => {
      const defaultSearch = {...new MockSavedSearch(), id: 1, defaultSearch: true, sfJobOpp: {id: 1}};
      const updatedSearch = {...defaultSearch, sfJobOpp: {id: 123}};
      mockSavedSearchService.getDefault.and.returnValue(of(defaultSearch));
      mockSavedSearchService.updateJob.and.returnValue(of(updatedSearch));
      queryParamMap$.next(fakeParamMap({job: '123'}));

      fixture.detectChanges();
      tick();

      expect(mockSavedSearchService.updateJob).toHaveBeenCalledWith(defaultSearch.id, 123);
      expect(component.savedSearch).toEqual(updatedSearch);
    }));

    it('should NOT call updateJob when the default search is already associated with that same job', fakeAsync(() => {
      const defaultSearch = {...new MockSavedSearch(), defaultSearch: true, sfJobOpp: {id: 123}};
      mockSavedSearchService.getDefault.and.returnValue(of(defaultSearch));
      queryParamMap$.next(fakeParamMap({job: '123'}));

      fixture.detectChanges();
      tick();

      expect(mockSavedSearchService.updateJob).not.toHaveBeenCalled();
      expect(component.savedSearch).toEqual(defaultSearch);
    }));

    it('should NOT call updateJob when there is no job query param', fakeAsync(() => {
      const defaultSearch = {...new MockSavedSearch(), defaultSearch: true, sfJobOpp: {id: 123}};
      mockSavedSearchService.getDefault.and.returnValue(of(defaultSearch));

      fixture.detectChanges();
      tick();

      expect(mockSavedSearchService.updateJob).not.toHaveBeenCalled();
      expect(component.savedSearch.sfJobOpp).toEqual({id: 123});
    }));

    it('should NOT call updateJob when loading a non-default saved search by id, even with a job query param', fakeAsync(() => {
      const namedSearch = {...new MockSavedSearch(), defaultSearch: false};
      mockSavedSearchService.get.and.returnValue(of(namedSearch));
      paramMap$.next(fakeParamMap({id: '5'}));
      queryParamMap$.next(fakeParamMap({job: '123'}));

      fixture.detectChanges();
      tick();

      expect(mockSavedSearchService.updateJob).not.toHaveBeenCalled();
    }));

    it('should surface a job assignment failure without blocking the rest of the page', fakeAsync(() => {
      const defaultSearch = {...new MockSavedSearch(), defaultSearch: true, sfJobOpp: null};
      mockSavedSearchService.getDefault.and.returnValue(of(defaultSearch));
      mockSavedSearchService.updateJob.and.returnValue(throwError('Job assignment failed'));
      queryParamMap$.next(fakeParamMap({job: '123'}));

      fixture.detectChanges();
      tick();

      expect(component.jobAssignmentError).toEqual('Job assignment failed');
      expect(component.error).toBeUndefined();
      expect(component.loading).toBe(false);
    }));
  });

  it('should display confirmation modal if form dirty and navigate away', () => {
    fixture.detectChanges();
    spyOn(modalService, 'open').and.returnValue({
      componentInstance: {},
      result: Promise.resolve('saved')
    } as NgbModalRef);

    component.formDirty = true;

    component.canExit();

    expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, jasmine.any(Object));
  });

});
