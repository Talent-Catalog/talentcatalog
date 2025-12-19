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
import {CandidateSourceCandidateService} from './candidate-source-candidate.service';
import {environment} from '../../environments/environment';
import {Candidate} from '../model/candidate';
import {SearchResults} from '../model/search-results';
import {CandidateSource, PagedSearchRequest, SearchCandidateSourcesRequest} from '../model/base';
import {MockCandidate} from "../MockData/MockCandidate";

describe('CandidateSourceCandidateService', () => {
  let service: CandidateSourceCandidateService;
  let httpMock: HttpTestingController;

  const savedListApiUrl = environment.apiUrl + '/saved-list-candidate';
  const savedSearchApiUrl = environment.apiUrl + '/saved-search-candidate';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateSourceCandidateService]
    });
    service = TestBed.inject(CandidateSourceCandidateService);
    httpMock = TestBed.inject(HttpTestingController);

    // Mock the isSavedSearch function
    (service as any).isSavedSearch = jasmine.createSpy('isSavedSearch');
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('list', () => {
    it('should make a GET request to the correct URL for listing candidates', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const mockResponse: Candidate[] = [new MockCandidate()];

      (service as any).isSavedSearch.and.returnValue(false);

      service.list(source).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${savedListApiUrl}/${source.id}/list`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('isEmpty', () => {
    it('should make a GET request to the correct URL for checking if source is empty', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const mockResponse = true;

      (service as any).isSavedSearch.and.returnValue(false);

      service.isEmpty(source).subscribe(response => {
        expect(response).toBe(mockResponse);
      });

      const req = httpMock.expectOne(`${savedListApiUrl}/${source.id}/is-empty`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('search', () => {
    it('should make a POST request to the correct URL for searching candidates', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const request: SearchCandidateSourcesRequest = {
        keyword:'Test'
      };
      const mockResponse: Candidate[] = [
        new MockCandidate()
      ];

      (service as any).isSavedSearch.and.returnValue(false);

      service.search(source, request).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${savedListApiUrl}/${source.id}/search`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });

  describe('searchPaged', () => {
    it('should make a POST request to the correct URL for searching candidates with pagination', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const request: SearchCandidateSourcesRequest = { keyword:'test' };
      const mockResponse: SearchResults<Candidate> = {number:1,size:10,totalPages:1,totalElements:1,first:true,last:false, content: [new MockCandidate()] };

      (service as any).isSavedSearch.and.returnValue(false);

      service.searchPaged(source, request).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${savedListApiUrl}/${source.id}/search-paged`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });

  describe('export', () => {
    it('should make a POST request to the correct URL for exporting candidates', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const request: PagedSearchRequest = { pageNumber:1};
      const mockResponse = new Blob();

      (service as any).isSavedSearch.and.returnValue(false);

      service.export(source, request).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${savedListApiUrl}/${source.id}/export/csv`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      expect(req.request.responseType).toBe('blob');
      req.flush(mockResponse);
    });
  });
});
