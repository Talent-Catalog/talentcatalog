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
import {CandidateDestinationService} from './candidate-destination.service';
import {environment} from '../../environments/environment';
import {CandidateDestination} from '../model/candidate-destination';
import {YesNoUnsure} from "../model/candidate";
import {MockJob} from "../MockData/MockJob";
import {MockCandidate} from "../MockData/MockCandidate";

describe('CandidateDestinationService', () => {
  let service: CandidateDestinationService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-destination';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateDestinationService]
    });
    service = TestBed.inject(CandidateDestinationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a candidate destination', () => {
    const candidateId = 1;
    const countryName = { name: 'USA' };
    const mockResponse: CandidateDestination = {
      id: 1,
      country: MockJob.country,
      candidate: new MockCandidate(),
      interest:YesNoUnsure.Yes,
      notes:'Notes'
    };

    service.create(candidateId, countryName).subscribe((destination) => {
      expect(destination).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${candidateId}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(countryName);
    req.flush(mockResponse);
  });
});
