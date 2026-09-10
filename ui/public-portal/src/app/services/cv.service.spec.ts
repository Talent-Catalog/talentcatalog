/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';

import {Candidate} from '../model/candidate';
import {CvService} from './cv.service';

describe('CvService', () => {
  let service: CvService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(CvService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should decode a CV token and return the candidate', () => {
    const token = 'valid-cv-token';
    const candidate = {
      id: 1,
      candidateNumber: '123456',
      publicId: 'candidate-public-id'
    } as Candidate;

    let result: Candidate;
    service.decodeCvRequest(token).subscribe(response => result = response);

    const request = httpTestingController.expectOne(`${service.apiUrl}/${token}`);
    expect(request.request.method).toBe('GET');

    request.flush(candidate);

    expect(result).toEqual(candidate);
  });

  it('should propagate an error when decoding the CV token fails', () => {
    const token = 'invalid-cv-token';
    let actualError: any;

    service.decodeCvRequest(token).subscribe({
      next: () => fail('Expected the request to fail'),
      error: error => actualError = error
    });

    const request = httpTestingController.expectOne(`${service.apiUrl}/${token}`);
    request.flush(
      {message: 'Invalid CV token'},
      {status: 404, statusText: 'Not Found'}
    );

    expect(actualError.status).toBe(404);
    expect(actualError.error).toEqual({message: 'Invalid CV token'});
  });
});
