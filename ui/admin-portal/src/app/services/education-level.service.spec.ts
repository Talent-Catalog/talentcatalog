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
import {EducationLevelService} from './education-level.service';
import {EducationLevel} from '../model/education-level';
import {environment} from '../../environments/environment';
import {SearchResults} from '../model/search-results';
import {SystemLanguage} from '../model/language';

describe('EducationLevelService', () => {
  let service: EducationLevelService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EducationLevelService]
    });
    service = TestBed.inject(EducationLevelService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#listEducationLevels', () => {
    it('should return an Observable EducationLevel[] ', () => {
      const mockEducationLevels: EducationLevel[] = [
        { id: 1, name: 'Level1', status: 'Active', level: 1 },
        { id: 2, name: 'Level2', status: 'Active', level: 2 }
      ];

      service.listEducationLevels().subscribe(levels => {
        expect(levels.length).toBe(2);
        expect(levels).toEqual(mockEducationLevels);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-level`);
      expect(req.request.method).toBe('GET');
      req.flush(mockEducationLevels);
    });
  });

  describe('#search', () => {
    it('should return an Observable SearchResults EducationLevel ', () => {
      const mockSearchResults: SearchResults<EducationLevel> = {
        content: [
          { id: 1, name: 'Level1', status: 'Active', level: 1 },
          { id: 2, name: 'Level2', status: 'Active', level: 2 }
        ],
        totalElements: 2
      } as SearchResults<EducationLevel>;
      const request = { query: 'test' };

      service.search(request).subscribe(results => {
        expect(results.totalElements).toBe(2);
        expect(results.content.length).toBe(2);
        expect(results).toEqual(mockSearchResults);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-level/search`);
      expect(req.request.method).toBe('POST');
      req.flush(mockSearchResults);
    });
  });

  describe('#get', () => {
    it('should return an Observable EducationLevel ', () => {
      const mockEducationLevel: EducationLevel = { id: 1, name: 'Level1', status: 'Active', level: 1 };

      service.get(1).subscribe(level => {
        expect(level).toEqual(mockEducationLevel);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-level/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockEducationLevel);
    });
  });

  describe('#create', () => {
    it('should return an Observable EducationLevel ', () => {
      const mockEducationLevel: EducationLevel = { id: 1, name: 'Level1', status: 'Active', level: 1 };
      const details = { name: 'Level1', status: 'Active', level: 1 };

      service.create(details).subscribe(level => {
        expect(level).toEqual(mockEducationLevel);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-level`);
      expect(req.request.method).toBe('POST');
      req.flush(mockEducationLevel);
    });
  });

  describe('#update', () => {
    it('should return an Observable EducationLevel ', () => {
      const mockEducationLevel: EducationLevel = { id: 1, name: 'Level1', status: 'Active', level: 1 };
      const details = { name: 'Level1', status: 'Active', level: 1 };

      service.update(1, details).subscribe(level => {
        expect(level).toEqual(mockEducationLevel);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-level/1`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockEducationLevel);
    });
  });

  describe('#delete', () => {
    it('should return an Observable boolean ', () => {
      const mockResponse = true;

      service.delete(1).subscribe(response => {
        expect(response).toBe(true);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-level/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });
  });

  describe('#addSystemLanguageTranslations', () => {
    it('should return an Observable SystemLanguage ', () => {
      const mockSystemLanguage: SystemLanguage = { id:1, label: 'en', language: 'English',rtl:false };
      const file = new File(['content'], 'test.txt');

      service.addSystemLanguageTranslations('en', file).subscribe(language => {
        expect(language).toEqual(mockSystemLanguage);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/education-level/system/en`);
      expect(req.request.method).toBe('POST');
      req.flush(mockSystemLanguage);
    });
  });
});
