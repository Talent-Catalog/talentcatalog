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
import {CandidateSourceService} from './candidate-source.service';
import {environment} from '../../environments/environment';
import {CandidateSource, CandidateSourceType, SearchCandidateSourcesRequest, UpdateCandidateContextNoteRequest, UpdateCandidateSourceDescriptionRequest, UpdateDisplayedFieldPathsRequest} from "../model/base";
import {SearchResults} from '../model/search-results';
import {CandidateFieldService} from './candidate-field.service';
import {CopySourceContentsRequest} from "../model/saved-list";

describe('CandidateSourceService', () => {
  let service: CandidateSourceService;
  let httpMock: HttpTestingController;
  let candidateFieldService: jasmine.SpyObj<CandidateFieldService>;

  const savedListApiUrl = `${environment.apiUrl}/saved-list`;
  const savedSearchApiUrl = `${environment.apiUrl}/saved-search`;

  beforeEach(() => {
    const candidateFieldServiceSpy = jasmine.createSpyObj('CandidateFieldService', ['isDefault']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        CandidateSourceService,
        { provide: CandidateFieldService, useValue: candidateFieldServiceSpy }
      ]
    });

    service = TestBed.inject(CandidateSourceService);
    httpMock = TestBed.inject(HttpTestingController);
    candidateFieldService = TestBed.inject(CandidateFieldService) as jasmine.SpyObj<CandidateFieldService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('copy', () => {
    it('should make a PUT request to the correct URL for copying a source', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const selection: CopySourceContentsRequest = {
        savedListId: 1,
        newListName: 'Test'
      };
      const mockResponse: CandidateSource = { id: 1 } as CandidateSource;

      service.copy(source, selection).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const apiUrl = savedListApiUrl;
      const req = httpMock.expectOne(`${apiUrl}/copy/${source.id}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(selection);
      req.flush(mockResponse);
    });
  });

  describe('delete', () => {
    it('should make a DELETE request to the correct URL for deleting a source', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const mockResponse = true;

      service.delete(source).subscribe((response) => {
        expect(response).toBe(mockResponse);
      });

      const apiUrl = savedListApiUrl;
      const req = httpMock.expectOne(`${apiUrl}/${source.id}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });
  });

  describe('starSourceForUser', () => {
    it('should make a PUT request to the correct URL for starring a source', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const request = { userId: 123 };
      const mockResponse: CandidateSource = { id: 1 } as CandidateSource;

      service.starSourceForUser(source, request).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const apiUrl = savedListApiUrl;
      const req = httpMock.expectOne(`${apiUrl}/shared-add/${source.id}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });

  describe('unstarSourceForUser', () => {
    it('should make a PUT request to the correct URL for unstarring a source', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const request = { userId: 123 };
      const mockResponse: CandidateSource = { id: 1 } as CandidateSource;

      service.unstarSourceForUser(source, request).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const apiUrl = savedListApiUrl;
      const req = httpMock.expectOne(`${apiUrl}/shared-remove/${source.id}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });
  //
  describe('searchPaged', () => {
    it('should make a POST request to the correct URL for searching paged sources', () => {
      const sourceType = CandidateSourceType.SavedSearch;
      const request: SearchCandidateSourcesRequest = { /* properties as needed */ };
      const mockResponse: SearchResults<CandidateSource> = {
        content: [{ id: 1 } as CandidateSource],
        number:1,
        size:10,
        totalElements:100,
        totalPages:10,
        first:false,
        last:false,
      };

      service.searchPaged(sourceType, request).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const apiUrl = savedSearchApiUrl;
      const req = httpMock.expectOne(`${apiUrl}/search-paged`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });

  describe('updateContextNote', () => {
    it('should make a PUT request to the correct URL for updating context notes', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const request: UpdateCandidateContextNoteRequest = {
        candidateId: 1,
        contextNote: 'testNote'
      };

      service.updateContextNote(source, request).subscribe();

      const apiUrl = savedListApiUrl;
      const req = httpMock.expectOne(`${apiUrl}/context/${source.id}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush({});
    });
  });

  describe('updateDescription', () => {
    it('should make a PUT request to the correct URL for updating description', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const request: UpdateCandidateSourceDescriptionRequest = {
        description: 'Test'
      };

      service.updateDescription(source, request).subscribe();

      const apiUrl = savedListApiUrl;
      const req = httpMock.expectOne(`${apiUrl}/description/${source.id}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush({});
    });
  });

  describe('updateDisplayedFieldPaths', () => {
    it('should make a PUT request to the correct URL for updating displayed field paths', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const request: UpdateDisplayedFieldPathsRequest = { displayedFieldsLong: [], displayedFieldsShort: [] };

      // Mock the behavior of isDefault
      candidateFieldService.isDefault.and.returnValue(true);

      service.updateDisplayedFieldPaths(source, request).subscribe();

      const apiUrl = savedListApiUrl;
      const req = httpMock.expectOne(`${apiUrl}/displayed-fields/${source.id}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush({});
    });
  });

});
