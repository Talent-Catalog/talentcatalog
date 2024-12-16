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
import {CandidateEducationService} from './candidate-education.service';
import {environment} from '../../environments/environment';
import {CandidateEducation} from '../model/candidate-education';
import {CreateCandidateEducationRequest, UpdateCandidateEducationRequest} from './candidate-education.service';

describe('CandidateEducationService', () => {
  let service: CandidateEducationService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-education';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateEducationService]
    });
    service = TestBed.inject(CandidateEducationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should list candidate educations', () => {
    const candidateId = 1;
    const mockResponse: CandidateEducation[] = [
      { id: 1, educationType: 'Bachelor', country: { id: 1, name: 'CountryName', status: 'active', translatedName: 'CountryTranslated' }, educationMajor: { id: 1, name: 'MajorName', status: 'active' }, lengthOfCourseYears: 4, institution: 'University', courseName: 'Computer Science', yearCompleted: '2020', incomplete: false }
    ];

    service.list(candidateId).subscribe((educations) => {
      expect(educations).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${candidateId}/list`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should create a candidate education', () => {
    const request: CreateCandidateEducationRequest = {
      candidateId: 1,
      educationType: 'Bachelor',
      countryId: 1,
      educationMajorId: 1,
      lengthOfCourseYears: 4,
      institution: 'University',
      courseName: 'Computer Science',
      yearCompleted: '2020',
      incomplete: false
    };

    const mockResponse: CandidateEducation = {
      id: 1,
      educationType: 'Bachelor',
      country: { id: 1, name: 'CountryName', status: 'active', translatedName: 'CountryTranslated' },
      educationMajor: { id: 1, name: 'MajorName', status: 'active' },
      lengthOfCourseYears: 4,
      institution: 'University',
      courseName: 'Computer Science',
      yearCompleted: '2020',
      incomplete: false
    };

    service.create(request).subscribe((education) => {
      expect(education).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should update a candidate education', () => {
    const request: UpdateCandidateEducationRequest = {
      id: 1,
      educationType: 'Bachelor',
      countryId: 1,
      majorId: 1,
      lengthOfCourseYears: 4,
      institution: 'University',
      courseName: 'Computer Science',
      yearCompleted: '2020',
      incomplete: false
    };

    const mockResponse: CandidateEducation = {
      id: 1,
      educationType: 'Bachelor',
      country: { id: 1, name: 'CountryName', status: 'active', translatedName: 'CountryTranslated' },
      educationMajor: { id: 1, name: 'MajorName', status: 'active' },
      lengthOfCourseYears: 4,
      institution: 'University',
      courseName: 'Computer Science',
      yearCompleted: '2020',
      incomplete: false
    };

    service.update(request).subscribe((education) => {
      expect(education).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should delete a candidate education', () => {
    const educationId = 1;
    const mockResponse: CandidateEducation = {
      id: 1,
      educationType: 'Bachelor',
      country: { id: 1, name: 'CountryName', status: 'active', translatedName: 'CountryTranslated' },
      educationMajor: { id: 1, name: 'MajorName', status: 'active' },
      lengthOfCourseYears: 4,
      institution: 'University',
      courseName: 'Computer Science',
      yearCompleted: '2020',
      incomplete: false
    };

    service.delete(educationId).subscribe((result) => {
      expect(result).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${educationId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });
});
