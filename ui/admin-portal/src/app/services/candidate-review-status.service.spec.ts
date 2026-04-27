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
import {CandidateReviewStatusService} from './candidate-review-status.service';
import {environment} from '../../environments/environment';
import {CandidateReviewStatusItem} from "../model/candidate-review-status-item";
import {MockSavedSearch} from "../MockData/MockSavedSearch";
import {MockUser} from "../MockData/MockUser";

describe('CandidateReviewStatusService', () => {
  let service: CandidateReviewStatusService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-reviewstatus';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateReviewStatusService]
    });
    service = TestBed.inject(CandidateReviewStatusService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a candidate review status item by ID', () => {
    const mockItem: CandidateReviewStatusItem = {
      id: 1,
      savedSearch:  new MockSavedSearch(),
      reviewStatus: 'Approved',
      comment: 'Good candidate',
      createdBy: new MockUser(),
      createdDate: Date.now(),
      updatedBy: new MockUser(),
      updatedDate: Date.now()
    };

    service.get(1).subscribe((item) => {
      expect(item).toEqual(mockItem);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockItem);
  });

  it('should create a candidate review status item', () => {
    const newItem: CandidateReviewStatusItem = {
      id: 2,
      savedSearch: new MockSavedSearch(),
      reviewStatus: 'Pending',
      comment: 'Needs further review',
      createdBy: new MockUser(),
      createdDate: Date.now(),
      updatedBy: new MockUser(),
      updatedDate: Date.now()
    };

    service.create(newItem).subscribe((item) => {
      expect(item).toEqual(newItem);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(newItem);
  });

  it('should update a candidate review status item', () => {
    const updatedItem: CandidateReviewStatusItem = {
      id: 3,
      savedSearch: new MockSavedSearch(),
      reviewStatus: 'Rejected',
      comment: 'Not suitable',
      createdBy: new MockUser(),
      createdDate: Date.now(),
      updatedBy: new MockUser(),
      updatedDate: Date.now()
    };

    service.update(3, updatedItem).subscribe((item) => {
      expect(item).toEqual(updatedItem);
    });
    const req = httpMock.expectOne(`${apiUrl}/3`);
    expect(req.request.method).toBe('PUT');
    req.flush(updatedItem);
  });
});
