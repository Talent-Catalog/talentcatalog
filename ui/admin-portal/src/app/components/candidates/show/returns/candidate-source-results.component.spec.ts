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
import {CandidateSourceResultsComponent} from "./candidate-source-results.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {
  CandidateSourceResultsCacheService
} from "../../../../services/candidate-source-results-cache.service";
import {
  CandidateSourceCandidateService
} from "../../../../services/candidate-source-candidate.service";
import {AuthorizationService} from "../../../../services/authorization.service";
import {CandidateFieldService} from "../../../../services/candidate-field.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Router} from "@angular/router";
import {NO_ERRORS_SCHEMA, SimpleChange} from '@angular/core';
import {of} from 'rxjs';
import {DtoType} from '../../../../model/base';

describe('CandidateSourceResultsComponent', () => {
  let component: CandidateSourceResultsComponent;
  let fixture: ComponentFixture<CandidateSourceResultsComponent>;

  let router: jasmine.SpyObj<Router>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  let cacheService: jasmine.SpyObj<CandidateSourceResultsCacheService>;
  let sourceCandidateService: jasmine.SpyObj<CandidateSourceCandidateService>;
  let candidateFieldService: jasmine.SpyObj<CandidateFieldService>;
  let modalService: jasmine.SpyObj<NgbModal>;

  const savedSearch = {
    id: 10,
    name: 'Test search',
    savedSearchType: 2,
    reviewable: false,
    users: []
  } as any;

  const savedList = {
    id: 20,
    name: 'Test list',
    users: []
  } as any;

  beforeEach(async () => {
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    authorizationService = jasmine.createSpyObj<AuthorizationService>(
      'AuthorizationService',
      [
        'canViewCandidateName',
        'canViewCandidateCountry',
        'canAccessSalesforce',
        'canAccessGoogleDrive'
      ]
    );

    cacheService = jasmine.createSpyObj<CandidateSourceResultsCacheService>(
      'CandidateSourceResultsCacheService',
      ['getFromCache', 'cache']
    );

    sourceCandidateService =
      jasmine.createSpyObj<CandidateSourceCandidateService>(
        'CandidateSourceCandidateService',
        ['searchPaged']
      );

    candidateFieldService = jasmine.createSpyObj<CandidateFieldService>(
      'CandidateFieldService',
      ['getCandidateSourceFields']
    );

    modalService = jasmine.createSpyObj<NgbModal>('NgbModal', ['open']);

    candidateFieldService.getCandidateSourceFields.and.returnValue([]);
    sourceCandidateService.searchPaged.and.returnValue(
      of({
        content: [],
        totalElements: 0
      } as any)
    );

    await TestBed.configureTestingModule({
      declarations: [CandidateSourceResultsComponent],
      providers: [
        {provide: Router, useValue: router},
        {provide: AuthorizationService, useValue: authorizationService},
        {provide: CandidateSourceResultsCacheService, useValue: cacheService},
        {provide: CandidateSourceCandidateService, useValue: sourceCandidateService},
        {provide: CandidateFieldService, useValue: candidateFieldService},
        {provide: NgbModal, useValue: modalService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .overrideTemplate(CandidateSourceResultsComponent, '')
    .compileComponents();

    fixture = TestBed.createComponent(CandidateSourceResultsComponent);
    component = fixture.componentInstance;
    component.candidateSource = savedSearch;

    fixture.detectChanges();
  });

  
  it('should create and initialize short format', () => {
    component.longFormat = true;

    component.ngOnInit();

    expect(component).toBeTruthy();
    expect(component.longFormat).toBeFalse();
  });

  describe('ngOnChanges', () => {
    it('should load fields and search when candidate source changes', () => {
      const loadFieldsSpy = spyOn<any>(component, 'loadSelectedFields');
      const searchSpy = spyOn(component, 'search');

      component.ngOnChanges({
        candidateSource: new SimpleChange(savedList, savedSearch, false)
      });

      expect(loadFieldsSpy).toHaveBeenCalledTimes(1);
      expect(searchSpy).toHaveBeenCalledOnceWith(false);
    });

    it('should do nothing when candidate source change is absent', () => {
      const loadFieldsSpy = spyOn<any>(component, 'loadSelectedFields');
      const searchSpy = spyOn(component, 'search');

      component.ngOnChanges({});

      expect(loadFieldsSpy).not.toHaveBeenCalled();
      expect(searchSpy).not.toHaveBeenCalled();
    });

    it('should do nothing when previous and current values are identical', () => {
      const loadFieldsSpy = spyOn<any>(component, 'loadSelectedFields');
      const searchSpy = spyOn(component, 'search');

      component.ngOnChanges({
        candidateSource: new SimpleChange(savedSearch, savedSearch, false)
      });

      expect(loadFieldsSpy).not.toHaveBeenCalled();
      expect(searchSpy).not.toHaveBeenCalled();
    });
  });

  describe('onOpenSource', () => {
    it('should navigate without query parameters from page one', () => {
      component.pageNumber = 1;
      component.candidateSource = savedSearch;

      component.onOpenSource();

      expect(router.navigate).toHaveBeenCalledWith(
        ['search', savedSearch.id],
        {}
      );
    });

    it('should preserve the current page number after page one', () => {
      component.pageNumber = 4;
      component.candidateSource = savedList;

      component.onOpenSource();

      expect(router.navigate).toHaveBeenCalledWith(
        ['list', savedList.id],
        {
          queryParams: {
            pageNumber: 4
          }
        }
      );
    });
  });

  describe('search', () => {
    it('should stop when cached results are available', () => {
      const checkCacheSpy = spyOn<any>(component, 'checkCache')
      .and.returnValue(true);
      const performSearchSpy = spyOn<any>(component, 'performSearch')
      .and.returnValue(of({}));

      component.search(false);

      expect(checkCacheSpy).toHaveBeenCalledWith(false, false);
      expect(performSearchSpy).not.toHaveBeenCalled();
    });

    it('should perform a preview search when cache is unavailable', () => {
      const checkCacheSpy = spyOn<any>(component, 'checkCache')
      .and.returnValue(false);
      const performSearchSpy = spyOn<any>(component, 'performSearch')
      .and.returnValue(of({}));

      component.search(true);

      expect(checkCacheSpy).toHaveBeenCalledWith(true, false);
      expect(performSearchSpy).toHaveBeenCalledWith(
        12,
        DtoType.PREVIEW
      );
    });
  });

  it('should toggle sorting and refresh the search', () => {
    component.sortField = 'name';
    component.sortDirection = 'ASC';

    const searchSpy = spyOn(component, 'search');

    component.toggleSort('name');

    expect(component.sortDirection).toBe('DESC');
    expect(searchSpy).toHaveBeenCalledOnceWith(true);
  });

  it('should emit toggleStarred', () => {
    const emitSpy = spyOn(component.toggleStarred, 'emit');

    component.onToggleStarred(savedSearch);

    expect(emitSpy).toHaveBeenCalledOnceWith(savedSearch);
  });

  it('should emit toggleWatch', () => {
    const emitSpy = spyOn(component.toggleWatch, 'emit');

    component.onToggleWatch(savedSearch);

    expect(emitSpy).toHaveBeenCalledOnceWith(savedSearch);
  });

  it('should emit deleteSource', () => {
    const emitSpy = spyOn(component.deleteSource, 'emit');

    component.onDeleteSource(savedSearch);

    expect(emitSpy).toHaveBeenCalledOnceWith(savedSearch);
  });

  it('should emit editSource', () => {
    const emitSpy = spyOn(component.editSource, 'emit');

    component.onEditSource(savedSearch);

    expect(emitSpy).toHaveBeenCalledOnceWith(savedSearch);
  });

  it('should emit copySource', () => {
    const emitSpy = spyOn(component.copySource, 'emit');

    component.onCopySource(savedSearch);

    expect(emitSpy).toHaveBeenCalledOnceWith(savedSearch);
  });

  it('should refresh after a page change', () => {
    const searchSpy = spyOn(component, 'search');

    component.onPageChange();

    expect(searchSpy).toHaveBeenCalledOnceWith(true);
  });

  it('should refresh when refresh is requested', () => {
    const searchSpy = spyOn(component, 'search');

    component.onRefreshRequest();

    expect(searchSpy).toHaveBeenCalledOnceWith(true);
  });
});
