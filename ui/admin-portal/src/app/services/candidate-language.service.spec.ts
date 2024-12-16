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
import {CandidateLanguageService, CreateCandidateLanguageRequest, UpdateCandidateLanguageRequest} from './candidate-language.service';
import {CandidateLanguage} from '../model/candidate-language';
import {environment} from '../../environments/environment';
import {MockCandidate} from "../MockData/MockCandidate";

describe('CandidateLanguageService', () => {
  let service: CandidateLanguageService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-language';
  const mockCandidate = new MockCandidate();
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateLanguageService]
    });
    service = TestBed.inject(CandidateLanguageService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve a list of candidate languages', () => {
    const mockLanguages: CandidateLanguage[] = [
      {
        id: 1,
        candidate: mockCandidate,
        language: { id: 1, name: 'English', status: 'active' },
        spokenLevel: { id: 1, name: 'Fluent', level: 5, status: 'active' },
        writtenLevel: { id: 1, name: 'Fluent', level: 5, status: 'active' },
        migrationLanguage: 'English'
      },
      {
        id: 2,
        candidate: mockCandidate,
        language: { id: 2, name: 'French', status: 'active' },
        spokenLevel: { id: 2, name: 'Intermediate', level: 3, status: 'active' },
        writtenLevel: { id: 2, name: 'Intermediate', level: 3, status: 'active' },
        migrationLanguage: 'French'
      }
    ];

    service.list(1).subscribe((languages) => {
      expect(languages.length).toBe(2);
      expect(languages).toEqual(mockLanguages);
    });

    const req = httpMock.expectOne(`${apiUrl}/1/list`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLanguages);
  });

  it('should create a new candidate language', () => {
    const newLanguage: CreateCandidateLanguageRequest = {
      candidateId: 1,
      languageId: 1,
      spokenLevelId: 3,
      writtenLevelId: 3,
      migrationLanguage: 'English'
    };

    const mockResponse: CandidateLanguage = {
      id: 3,
      candidate: mockCandidate,
      language: { id: 1, name: 'English', status: 'active' },
      spokenLevel: { id: 3, name: 'Fluent', level: 5, status: 'active' },
      writtenLevel: { id: 3, name: 'Fluent', level: 5, status: 'active' },
      migrationLanguage: 'English'
    };

    service.create(newLanguage).subscribe((language) => {
      expect(language).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newLanguage);
    req.flush(mockResponse);
  });

  it('should update an existing candidate language', () => {
    const updatedLanguage: UpdateCandidateLanguageRequest = {
      id: 1,
      languageId: 1,
      spokenLevelId: 4,
      writtenLevelId: 4,
      migrationLanguage: 'English'
    };

    const mockResponse: CandidateLanguage = {
      id: 1,
      candidate: mockCandidate,
      language: { id: 1, name: 'English', status: 'active' },
      spokenLevel: { id: 4, name: 'Advanced', level: 4, status: 'active' },
      writtenLevel: { id: 4, name: 'Advanced', level: 4, status: 'active' },
      migrationLanguage: 'English'
    };

    service.update(updatedLanguage).subscribe((language) => {
      expect(language).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedLanguage);
    req.flush(mockResponse);
  });

  it('should delete a candidate language', () => {
    const mockResponse: CandidateLanguage = {
      id: 1,
      candidate: mockCandidate,
      language: { id: 1, name: 'English', status: 'active' },
      spokenLevel: { id: 1, name: 'Fluent', level: 5, status: 'active' },
      writtenLevel: { id: 1, name: 'Fluent', level: 5, status: 'active' },
      migrationLanguage: 'English'
    };

    service.delete(1).subscribe((language) => {
      expect(language).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });
});
