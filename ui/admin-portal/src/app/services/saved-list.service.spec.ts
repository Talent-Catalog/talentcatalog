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
import {SavedListService} from './saved-list.service';
import {environment} from "../../environments/environment";
import {
  SavedList,
  UpdateSavedListInfoRequest,
  PublishListRequest,
  PublishedDocImportReport,
  SearchSavedListRequest, UpdateShortNameRequest
} from "../model/saved-list";
import {SearchResults} from "../model/search-results";
import {MockSavedList} from "../MockData/MockSavedList";

describe('SavedListService', () => {
  let service: SavedListService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/saved-list';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SavedListService]
    });
    service = TestBed.inject(SavedListService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create a saved list', () => {
    const request: UpdateSavedListInfoRequest = { name: 'New List' };
    const mockResponse: SavedList = { id: 1, name: 'New List' } as SavedList;

    service.create(request).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should create a folder for a saved list', () => {
    const savedListId = 1;
    const mockResponse: SavedList = { id: 1, name: 'New Folder' } as SavedList;

    service.createFolder(savedListId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${savedListId}/create-folder`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('should delete a saved list', () => {
    const savedListId = 1;

    service.delete(savedListId).subscribe(response => {
      expect(response).toBeTrue();
    });

    const req = httpMock.expectOne(`${apiUrl}/${savedListId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(true);
  });

  it('should get a saved list', () => {
    const savedListId = 1;
    const mockResponse: SavedList = { id: 1, name: 'Saved List' } as SavedList;

    service.get(savedListId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${savedListId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should publish a saved list', () => {
    const savedListId = 1;
    const request: PublishListRequest = { publishClosedOpps: true } as PublishListRequest;
    const mockResponse: SavedList = { id: 1, name: 'Published List' } as SavedList;

    service.publish(savedListId, request).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${savedListId}/publish`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('should import employer feedback', () => {
    const savedListId = 1;
    const mockResponse: PublishedDocImportReport = { numCandidates: 1 } as PublishedDocImportReport;

    service.importEmployerFeedback(savedListId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${savedListId}/feedback`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('should search saved lists', () => {
    const request:SearchSavedListRequest = { shortName: false };
    const mockResponse: SavedList[] = [MockSavedList];

    service.search(request).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should search saved lists with pagination', () => {
    const request:SearchSavedListRequest = { shortName: false };
    const mockResponse: SearchResults<SavedList> = { content: [MockSavedList], totalElements: 1 } as SearchResults<SavedList>;

    service.searchPaged(request).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/search-paged`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should update a saved list', () => {
    const savedListId = 1;
    const request: UpdateSavedListInfoRequest = { name: 'Updated List' };
    const mockResponse: SavedList = { id: 1, name: 'Updated List' } as SavedList;

    service.update(savedListId, request).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${savedListId}`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('should update the short name of a saved list', () => {
    const request: UpdateShortNameRequest = { tcShortName: 'Short Name' } as UpdateShortNameRequest;
    const mockResponse: SavedList =MockSavedList;

    service.updateShortName(request).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/short-name`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });
});
