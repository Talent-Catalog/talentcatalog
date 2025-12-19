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
import {EducationMajorService} from './education-major.service';
import {EducationMajor} from '../model/education-major';
import {environment} from '../../environments/environment';
import {SearchResults} from '../model/search-results';
import {SystemLanguage} from '../model/language';

describe('EducationMajorService', () => {
  let service: EducationMajorService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EducationMajorService]
    });
    service = TestBed.inject(EducationMajorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#listMajors', () => {
    it('should return an Observable EducationMajor[] ', () => {
      const mockMajors: EducationMajor[] = [
        { id: 1, name: 'Major1', status: 'Active' },
        { id: 2, name: 'Major2', status: 'Active' }
      ];

      service.listMajors().subscribe(majors => {
        expect(majors.length).toBe(2);
        expect(majors).toEqual(mockMajors);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-major`);
      expect(req.request.method).toBe('GET');
      req.flush(mockMajors);
    });
  });

  describe('#search', () => {
    it('should return an Observable SearchResults EducationMajor ', () => {
      const mockSearchResults: SearchResults<EducationMajor> = {
        content: [
          { id: 1, name: 'Major1', status: 'Active' },
          { id: 2, name: 'Major2', status: 'Active' }
        ],
        totalPages: 2
      } as SearchResults<EducationMajor>;
      const request = { query: 'test' };

      service.search(request).subscribe(results => {
        expect(results.totalPages).toBe(2);
        expect(results.content.length).toBe(2);
        expect(results).toEqual(mockSearchResults);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-major/search`);
      expect(req.request.method).toBe('POST');
      req.flush(mockSearchResults);
    });
  });

  describe('#get', () => {
    it('should return an Observable EducationMajor ', () => {
      const mockMajor: EducationMajor = { id: 1, name: 'Major1', status: 'Active' };

      service.get(1).subscribe(major => {
        expect(major).toEqual(mockMajor);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-major/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockMajor);
    });
  });

  describe('#create', () => {
    it('should return an Observable EducationMajor ', () => {
      const mockMajor: EducationMajor = { id: 1, name: 'Major1', status: 'Active' };

      service.create(mockMajor).subscribe(major => {
        expect(major).toEqual(mockMajor);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-major`);
      expect(req.request.method).toBe('POST');
      req.flush(mockMajor);
    });
  });

  describe('#update', () => {
    it('should return an Observable EducationMajor ', () => {
      const mockMajor: EducationMajor = { id: 1, name: 'Major1', status: 'Active' };

      service.update(1, mockMajor).subscribe(major => {
        expect(major).toEqual(mockMajor);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-major/1`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockMajor);
    });
  });

  describe('#delete', () => {
    it('should return an Observable boolean ', () => {
      const mockResponse = true;

      service.delete(1).subscribe(response => {
        expect(response).toBe(true);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-major/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });
  });

  describe('#addSystemLanguageTranslations', () => {
    it('should return an Observable SystemLanguage ', () => {
      const mockLanguage: SystemLanguage = { id: 1, label: 'en', language: 'English', rtl:false };
      const mockFile = new File([''], 'test.txt');

      service.addSystemLanguageTranslations('en', mockFile).subscribe(language => {
        expect(language).toEqual(mockLanguage);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-major/system/en`);
      expect(req.request.method).toBe('POST');
      req.flush(mockLanguage);
    });
  });
});
