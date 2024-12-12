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
import {LanguageLevelService} from './language-level.service';
import {environment} from '../../environments/environment';
import {LanguageLevel} from '../model/language-level';
import {SystemLanguage} from '../model/language';
import {SearchResults} from '../model/search-results';

describe('LanguageLevelService', () => {
  let service: LanguageLevelService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LanguageLevelService]
    });

    service = TestBed.inject(LanguageLevelService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should add system language translations', () => {
    const langCode = 'es';
    const file = new File(['dummy content'], 'test.json', { type: 'application/json' });
    const mockResponse: SystemLanguage = { id: 1, label: 'es', language: 'Spanish', rtl:false };

    service.addSystemLanguageTranslations(langCode, file).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language-level/system/${langCode}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.get('file')).toBe(file);
    req.flush(mockResponse);
  });

  it('should list language levels', () => {
    const mockLanguageLevels: LanguageLevel[] = [{ id: 1, name:'Basic', level: 1, status: 'active'}];

    service.listLanguageLevels().subscribe(languageLevels => {
      expect(languageLevels).toEqual(mockLanguageLevels);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language-level`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLanguageLevels);
  });

  it('should search language levels', () => {
    const request = { query: 'A1' };
    const mockSearchResults: SearchResults<LanguageLevel> = { content: [{  id: 1, name:'Basic', level: 1, status: 'active' }], totalElements: 1 } as SearchResults<LanguageLevel>;

    service.search(request).subscribe(searchResults => {
      expect(searchResults).toEqual(mockSearchResults);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language-level/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockSearchResults);
  });

  it('should get a language level by id', () => {
    const mockLanguageLevel: LanguageLevel = { id: 1, name:'Basic', level: 1, status: 'active'}

    service.get(1).subscribe(languageLevel => {
      expect(languageLevel).toEqual(mockLanguageLevel);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language-level/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLanguageLevel);
  });

  it('should create a new language level', () => {
    const details = { level: 2, name: 'Intermediate', status: 'active' };
    const mockLanguageLevel: LanguageLevel = { id: 2, ...details };

    service.create(details).subscribe(languageLevel => {
      expect(languageLevel).toEqual(mockLanguageLevel);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language-level`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(details);
    req.flush(mockLanguageLevel);
  });

  it('should update a language level', () => {
    const id = 1;
    const details = { level: 3, name: 'Advance', status: 'active' };
    const mockLanguageLevel: LanguageLevel = { id: 1, ...details };

    service.update(id, details).subscribe(languageLevel => {
      expect(languageLevel).toEqual(mockLanguageLevel);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language-level/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(details);
    req.flush(mockLanguageLevel);
  });

  it('should delete a language level', () => {
    const id = 1;
    const mockResponse = true;

    service.delete(id).subscribe(response => {
      expect(response).toBe(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language-level/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });
});
