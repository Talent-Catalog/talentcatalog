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
  CandidateVisaCheckService,
  CreateCandidateVisaCheckRequest
} from './candidate-visa-check.service';
import {environment} from '../../environments/environment';
import {CandidateVisa, YesNo} from '../model/candidate';
import {MockJob} from "../MockData/MockJob";

describe('CandidateVisaCheckService', () => {
  let service: CandidateVisaCheckService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateVisaCheckService]
    });

    service = TestBed.inject(CandidateVisaCheckService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('get', () => {
    it('should return a CandidateVisa object when called', () => {
      const mockVisa: CandidateVisa = { id: 1, country: MockJob.country, protection: YesNo.Yes } as CandidateVisa;
      service.get(1).subscribe((visa: CandidateVisa) => {
        expect(visa).toEqual(mockVisa);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate-visa-check/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockVisa);
    });
  });

  describe('list', () => {
    it('should return an array of CandidateVisa objects when called', () => {
      const mockVisas: CandidateVisa[] = [
        { id: 1, country: MockJob.country, protection: YesNo.Yes } as CandidateVisa,
        { id: 2, country: MockJob.country, protection: YesNo.No } as CandidateVisa
      ];

      service.list(1).subscribe((visas: CandidateVisa[]) => {
        expect(visas.length).toBe(2);
        expect(visas).toEqual(mockVisas);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate-visa-check/1/list`);
      expect(req.request.method).toBe('GET');
      req.flush(mockVisas);
    });
  });

  describe('create', () => {
    it('should return a CandidateVisa object when created successfully', () => {
      const mockVisa: CandidateVisa = { id: 1, country: MockJob.country, protection: YesNo.Yes } as CandidateVisa;
      const requestPayload: CreateCandidateVisaCheckRequest = { countryId: 123 };

      service.create(1, requestPayload).subscribe((visa: CandidateVisa) => {
        expect(visa).toEqual(mockVisa);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate-visa-check/1`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(requestPayload);
      req.flush(mockVisa);
    });
  });

  describe('delete', () => {
    it('should return true when delete is successful', () => {
      service.delete(1).subscribe((response: boolean) => {
        expect(response).toBeTrue();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate-visa-check/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(true);
    });
  });

  describe('updateIntakeData', () => {
    it('should complete without returning data', () => {
      const formData = { key: 'value' };

      service.updateIntakeData(1, formData).subscribe(() => {
        // No response data
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate-visa-check/1/intake`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(formData);
      req.flush({});
    });
  });
});
