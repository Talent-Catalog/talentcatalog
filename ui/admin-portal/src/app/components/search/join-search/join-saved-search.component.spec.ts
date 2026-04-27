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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {NgbTypeaheadModule} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';
import {JoinSavedSearchComponent} from './join-saved-search.component';
import {SavedSearchService} from "../../../services/saved-search.service";
import {RouterTestingModule} from "@angular/router/testing";
import {SavedSearch} from "../../../model/saved-search";
import {SearchResults} from "../../../model/search-results";
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {CandidateSourceComponent} from "../../util/candidate-source/candidate-source.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UpdatedByComponent} from "../../util/user/updated-by/updated-by.component";

describe('JoinSavedSearchComponent', () => {
  let component: JoinSavedSearchComponent;
  let fixture: ComponentFixture<JoinSavedSearchComponent>;
  let savedSearchServiceSpy: jasmine.SpyObj<SavedSearchService>;
  const mockSearchResult:SearchResults<SavedSearch> = {
    number : 1,
    size : 10,
    totalElements : 1,
    totalPages : 1,
    first : true,
    last : true,
    content : [new MockSavedSearch()] // Use the MockCandidateSource as content
  }
  beforeEach(async () => {
    const savedSearchServiceSpyObj = jasmine.createSpyObj('SavedSearchService', ['searchPaged', 'get']);

    await TestBed.configureTestingModule({
      declarations: [JoinSavedSearchComponent,UpdatedByComponent,CandidateSourceComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgbTypeaheadModule,RouterTestingModule],
      providers: [{ provide: SavedSearchService, useValue: savedSearchServiceSpyObj }, UntypedFormBuilder],
    }).compileComponents();

    savedSearchServiceSpy = TestBed.inject(SavedSearchService) as jasmine.SpyObj<SavedSearchService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JoinSavedSearchComponent);
    component = fixture.componentInstance;
    component.results = mockSearchResult;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load saved searches on initialization', () => {
    savedSearchServiceSpy.searchPaged.and.returnValue(of(mockSearchResult));
    fixture.detectChanges();
    expect(component.results).toEqual(mockSearchResult);
  });

  it('should emit selected base search on selection', () => {
    savedSearchServiceSpy.get.and.returnValue(of(new MockSavedSearch()));
    component.selected(1);
    // @ts-ignore: The TypeScript compiler requires the second overload signature, but the first overload is valid here
    expect(savedSearchServiceSpy.get).toHaveBeenCalledWith(1);
    expect(component.selectedBaseSearch).toEqual(new MockSavedSearch());
  })

  it('should delete selected search', () => {
    component.selectedBaseSearch = {
      defaultSearch: false,
      fixed: false,
      global: false,
      reviewable: false,
      savedSearchSubtype: undefined,
      savedSearchType: undefined,
      id: 1, name: 'Selected Search' };

    component.deleteSearch();

    expect(component.selectedBaseSearch).toBeNull();
  });

  it('should set searchFailed to true on search failure', () => {
    savedSearchServiceSpy.searchPaged.and.returnValue(throwError('error'));

    const term = 'test';
    component.doSavedSearchSearch(of(term)).subscribe(() => {
      expect(component.searchFailed).toBeTrue();
    });
  });
});

