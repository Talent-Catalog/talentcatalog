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
  CandidateCertificationService,
  CreateCandidateCertificationRequest,
  UpdateCandidateCertificationRequest
} from './candidate-certification.service';
import {environment} from '../../environments/environment';
import {CandidateCertification} from '../model/candidate-certification';

describe('CandidateCertificationService', () => {
  let service: CandidateCertificationService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-certification';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateCertificationService]
    });
    service = TestBed.inject(CandidateCertificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should list certifications', () => {
    const mockCertifications: CandidateCertification[] = [{ id: 1, name: 'Certification 1', institution: 'Institution 1', dateCompleted: '2020-01-01' }];

    service.list(1).subscribe((certifications) => {
      expect(certifications.length).toBe(1);
      expect(certifications).toEqual(mockCertifications);
    });

    const req = httpMock.expectOne(`${apiUrl}/1/list`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCertifications);
  });

  it('should create a certification', () => {
    const request: CreateCandidateCertificationRequest = { candidateId: 1, name: 'Certification 1', institution: 'Institution 1', dateCompleted: '2020-01-01' };
    const mockCertification: CandidateCertification = { id: 1, name: 'Certification 1', institution: 'Institution 1', dateCompleted: '2020-01-01' };

    service.create(request).subscribe((certification) => {
      expect(certification).toEqual(mockCertification);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(mockCertification);
  });

  it('should update a certification', () => {
    const request: UpdateCandidateCertificationRequest = { id: 1, name: 'Updated Certification', institution: 'Updated Institution', dateCompleted: '2020-01-02' };
    const mockCertification: CandidateCertification = { id: 1, name: 'Updated Certification', institution: 'Updated Institution', dateCompleted: '2020-01-02' };

    service.update(request).subscribe((certification) => {
      expect(certification).toEqual(mockCertification);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('PUT');
    req.flush(mockCertification);
  });

  it('should delete a certification', () => {
    const mockCertification: CandidateCertification = { id: 1, name: 'Certification 1', institution: 'Institution 1', dateCompleted: '2020-01-01' };

    service.delete(1).subscribe((certification) => {
      expect(certification).toEqual(mockCertification);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockCertification);
  });
});
