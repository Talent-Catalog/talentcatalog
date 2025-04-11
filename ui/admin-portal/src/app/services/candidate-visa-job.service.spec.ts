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
import {CandidateVisaJobService, CreateCandidateVisaJobRequest} from './candidate-visa-job.service';
import {environment} from '../../environments/environment';
import {CandidateVisaJobCheck} from '../model/candidate';
import {MockJob} from "../MockData/MockJob";

describe('CandidateVisaJobService', () => {
  let service: CandidateVisaJobService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateVisaJobService]
    });

    service = TestBed.inject(CandidateVisaJobService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('get', () => {
    it('should return a CandidateVisaJobCheck object when called', () => {
      const mockJobCheck: CandidateVisaJobCheck = {
        id: 1,
        jobOpp: MockJob,
      };

      service.get(1).subscribe((jobCheck: CandidateVisaJobCheck) => {
        expect(jobCheck).toEqual(mockJobCheck);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate-visa-job/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockJobCheck);
    });
  });

  describe('create', () => {
    it('should return a CandidateVisaJobCheck object when created successfully', () => {
      const mockJobCheck: CandidateVisaJobCheck = {
        id: 1,
        jobOpp: MockJob,
      };
      const requestPayload: CreateCandidateVisaJobRequest = { jobOppId: 456 };

      service.create(1, requestPayload).subscribe((jobCheck: CandidateVisaJobCheck) => {
        expect(jobCheck).toEqual(mockJobCheck);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate-visa-job/1`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(requestPayload);
      req.flush(mockJobCheck);
    });
  });

  describe('delete', () => {
    it('should return true when delete is successful', () => {
      service.delete(1).subscribe((response: boolean) => {
        expect(response).toBeTrue();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate-visa-job/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(true);
    });
  });
});
