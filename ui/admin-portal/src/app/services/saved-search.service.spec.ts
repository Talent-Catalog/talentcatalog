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

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {SavedSearchService} from './saved-search.service';
import {environment} from '../../environments/environment';
import {SavedSearch, SavedSearchRequest, SavedSearchType} from '../model/saved-search';
import {MockSavedSearch} from "../MockData/MockSavedSearch";

describe('SavedSearchService', () => {
  let service: SavedSearchService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/saved-search';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SavedSearchService],
    });

    service = TestBed.inject(SavedSearchService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch saved search by ID', () => {
    const dummySavedSearch: SavedSearch = new MockSavedSearch();
    service.get(1).subscribe(savedSearch => {
      expect(savedSearch).toEqual(dummySavedSearch);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(dummySavedSearch);
  });

  it('should create a new saved search', () => {
    const newSavedSearchRequest: SavedSearchRequest = { name: 'New Search', savedSearchType: SavedSearchType.job };
    const createdSavedSearch: SavedSearch = { id: 2, name: 'New Search', savedSearchType: SavedSearchType.job } as SavedSearch;

    service.create(newSavedSearchRequest).subscribe(savedSearch => {
      expect(savedSearch).toEqual(createdSavedSearch);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(createdSavedSearch);
  });

  it('should update an existing saved search', () => {
    const updatedSavedSearchRequest: SavedSearchRequest = { id: 1, name: 'Updated Search', savedSearchType: SavedSearchType.other };
    const updatedSavedSearch: SavedSearch = { id: 1, name: 'Updated Search', savedSearchType: SavedSearchType.profession} as SavedSearch;

    service.update(updatedSavedSearchRequest).subscribe(savedSearch => {
      expect(savedSearch).toEqual(updatedSavedSearch);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(updatedSavedSearch);
  });

  it('should delete a saved search by ID', () => {
    service.delete(1).subscribe(result => {
      expect(result).toBeTrue();
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(true);
  });

  it('should fetch the default saved search', () => {
    const defaultSavedSearch: SavedSearch = { id: 1, name: 'Default Search', savedSearchType:SavedSearchType.job } as SavedSearch;

    service.getDefault().subscribe(savedSearch => {
      expect(savedSearch).toEqual(defaultSavedSearch);
    });

    const req = httpMock.expectOne(`${apiUrl}/default`);
    expect(req.request.method).toBe('GET');
    req.flush(defaultSavedSearch);
  });
});
