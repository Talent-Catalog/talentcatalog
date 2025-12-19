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
import {SavedListCandidateService} from './saved-list-candidate.service';
import {environment} from "../../environments/environment";
import {UpdateExplicitSavedListContentsRequest, SavedList} from '../model/saved-list';
import {MockSavedList} from "../MockData/MockSavedList";

describe('SavedListCandidateService', () => {
  let service: SavedListCandidateService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/saved-list-candidate';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SavedListCandidateService]
    });
    service = TestBed.inject(SavedListCandidateService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create a new saved list', () => {
    const requestPayload: UpdateExplicitSavedListContentsRequest = { candidateIds: [1, 2, 3] };
    const responsePayload: SavedList = MockSavedList;

    service.create(requestPayload).subscribe((response) => {
      expect(response).toEqual(responsePayload);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(requestPayload);
    req.flush(responsePayload);
  });

  it('should merge from file', () => {
    const id = 1;
    const formData = new FormData();

    service.mergeFromFile(id, formData).subscribe((response) => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/${id}/merge-from-file`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(formData);
    req.flush(null);
  });

  it('should merge saved list contents', () => {
    const id = 1;
    const requestPayload: UpdateExplicitSavedListContentsRequest = { candidateIds: [1, 2, 3] };

    service.merge(id, requestPayload).subscribe((response) => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/${id}/merge`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(requestPayload);
    req.flush(null);
  });

  it('should remove saved list contents', () => {
    const id = 1;
    const requestPayload: UpdateExplicitSavedListContentsRequest = { candidateIds: [1, 2, 3] };

    service.remove(id, requestPayload).subscribe((response) => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/${id}/remove`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(requestPayload);
    req.flush(null);
  });

  it('should save selection', () => {
    const id = 1;
    const requestPayload: UpdateExplicitSavedListContentsRequest = { candidateIds: [1, 2, 3] };

    service.saveSelection(id, requestPayload).subscribe((response) => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/${id}/save-selection`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(requestPayload);
    req.flush(null);
  });
});
