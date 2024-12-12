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
import {CandidateExamService, CreateCandidateExamRequest} from './candidate-exam.service';
import {environment} from '../../environments/environment';
import {CandidateExam, Exam} from '../model/candidate';

describe('CandidateExamService', () => {
  let service: CandidateExamService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-exam';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateExamService]
    });
    service = TestBed.inject(CandidateExamService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a candidate exam', () => {
    const candidateId = 1;
    const request: CreateCandidateExamRequest = {
      exam: Exam.IELTSGen,
      otherExam: 'Other Exam',
      score: '8.0',
      year: 2022,
      notes: 'Passed with high score'
    };

    const mockResponse: CandidateExam = {
      id: 1,
      ...request
    };

    service.create(candidateId, request).subscribe((exam) => {
      expect(exam).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${candidateId}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should delete a candidate exam', () => {
    const examId = 1;
    const mockResponse = true;

    service.delete(examId).subscribe((result) => {
      expect(result).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${examId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });
});
