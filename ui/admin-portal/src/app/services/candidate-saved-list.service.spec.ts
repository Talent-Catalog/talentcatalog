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
import {CandidateSavedListService} from './candidate-saved-list.service';
import {environment} from '../../environments/environment';
import {IHasSetOfSavedLists, SavedList, SearchSavedListRequest} from '../model/saved-list';

describe('CandidateSavedListService', () => {
  let service: CandidateSavedListService;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/candidate-saved-list`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateSavedListService]
    });

    service = TestBed.inject(CandidateSavedListService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('replace', () => {
    it('should replace the saved list and return void', () => {
      const id = 1;
      const requestPayload: IHasSetOfSavedLists = {
        savedListIds:[1,2,3]
      };

      service.replace(id, requestPayload).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${apiUrl}/${id}/replace`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(requestPayload);
      req.flush(null); // Flush with no response body
    });
  });

  describe('search', () => {
    it('should search and return a list of saved lists', () => {
      const id = 1;
      const requestPayload: SearchSavedListRequest = {
        // Add properties as needed
      };
      const mockSavedLists: SavedList[] = [
        // Mock saved list objects
      ];

      service.search(id, requestPayload).subscribe(savedLists => {
        expect(savedLists).toEqual(mockSavedLists);
      });

      const req = httpMock.expectOne(`${apiUrl}/${id}/search`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(requestPayload);
      req.flush(mockSavedLists); // Provide mock response
    });
  });
});
