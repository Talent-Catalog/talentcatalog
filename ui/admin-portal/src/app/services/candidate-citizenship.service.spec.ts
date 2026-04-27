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
import {CandidateCitizenshipService, CreateCandidateCitizenshipRequest} from './candidate-citizenship.service';
import {environment} from '../../environments/environment';
import {CandidateCitizenship, HasPassport} from '../model/candidate';

describe('CandidateCitizenshipService', () => {
  let service: CandidateCitizenshipService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-citizenship';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateCitizenshipService]
    });
    service = TestBed.inject(CandidateCitizenshipService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a candidate citizenship', () => {
    const candidateId = 1;
    const request: CreateCandidateCitizenshipRequest = {
      nationalityId: 2,
      hasPassport: HasPassport.ValidPassport,
      passportExp: '2025-12-31',
      notes: 'Valid passport'
    };
    const mockResponse: CandidateCitizenship = {
      id: 1,
      hasPassport: HasPassport.ValidPassport,
      passportExp: '2025-12-31',
      notes: 'Valid passport'
    };

    service.create(candidateId, request).subscribe((citizenship) => {
      expect(citizenship).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${candidateId}`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should delete a candidate citizenship', () => {
    const citizenshipId = 1;

    service.delete(citizenshipId).subscribe((response) => {
      expect(response).toBeTrue();
    });

    const req = httpMock.expectOne(`${apiUrl}/${citizenshipId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(true);
  });
});
