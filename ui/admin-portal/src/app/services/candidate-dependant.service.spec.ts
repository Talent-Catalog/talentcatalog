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
import {
  CandidateDependantService,
  CreateCandidateDependantRequest
} from './candidate-dependant.service';
import {environment} from '../../environments/environment';
import {CandidateDependant, DependantRelations, Registrations, YesNo} from '../model/candidate';

describe('CandidateDependantService', () => {
  let service: CandidateDependantService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-dependant';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateDependantService]
    });
    service = TestBed.inject(CandidateDependantService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a candidate dependant', () => {
    const candidateId = 1;
    const request: CreateCandidateDependantRequest = {
      relation: DependantRelations.Child,
      relationOther: 'Other Relation',
      dob: '2010-05-15',
      name: 'John Doe',
      registered: Registrations.UNHCR,
      registeredNumber: '12345',
      registeredNotes: 'Notes',
      healthConcern: YesNo.No,
      healthNotes: 'No health concerns'
    };
    const mockResponse: CandidateDependant = {
      id: 1,
      relation: DependantRelations.Child,
      relationOther: 'Other Relation',
      dob: '2010-05-15',
      name: 'John Doe',
      registered: Registrations.UNHCR,
      registeredNumber: '12345',
      registeredNotes: 'Notes',
      healthConcern: YesNo.Yes,
      healthNotes: 'No health concerns'
    };

    service.create(candidateId, request).subscribe((dependant) => {
      expect(dependant).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${candidateId}`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should delete a candidate dependant', () => {
    const dependantId = 1;

    service.delete(dependantId).subscribe((response) => {
      expect(response).toBeTrue();
    });

    const req = httpMock.expectOne(`${apiUrl}/${dependantId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(true);
  });
});
