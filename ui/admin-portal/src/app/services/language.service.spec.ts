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
import {LanguageService} from './language.service';
import {environment} from '../../environments/environment';
import {Language, SystemLanguage} from '../model/language';
import {SearchResults} from '../model/search-results';

describe('LanguageService', () => {
  let service: LanguageService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LanguageService]
    });

    service = TestBed.inject(LanguageService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should list languages', () => {
    const mockLanguages: Language[] = [{ id: 1, name: 'English', status: 'active' }];

    service.listLanguages().subscribe(languages => {
      expect(languages).toEqual(mockLanguages);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLanguages);
  });

  it('should list system languages', () => {
    const mockSystemLanguages: SystemLanguage[] = [{ id: 1, label: 'en', language: 'English', rtl:false }];

    service.listSystemLanguages().subscribe(systemLanguages => {
      expect(systemLanguages).toEqual(mockSystemLanguages);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language/system`);
    expect(req.request.method).toBe('GET');
    req.flush(mockSystemLanguages);
  });

  it('should search languages', () => {
    const request = { query: 'English' };
    const mockSearchResults: SearchResults<Language> = { content: [{ id: 1, name: 'English', status: 'active' }], totalPages: 1 } as SearchResults<Language>;

    service.search(request).subscribe(searchResults => {
      expect(searchResults).toEqual(mockSearchResults);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockSearchResults);
  });

  it('should get a language by id', () => {
    const mockLanguage: Language = { id: 1, name: 'English', status: 'active' };

    service.get(1).subscribe(language => {
      expect(language).toEqual(mockLanguage);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLanguage);
  });

  it('should create a new language', () => {
    const details = { name: 'Spanish', status: 'active' };
    const mockLanguage: Language = { id: 2, ...details };

    service.create(details).subscribe(language => {
      expect(language).toEqual(mockLanguage);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(details);
    req.flush(mockLanguage);
  });

  it('should add a system language', () => {
    const langCode = 'es';
    const mockSystemLanguage: SystemLanguage = { id: 1, label: 'en', language: 'Spanish', rtl:false };

    service.addSystemLanguage(langCode).subscribe(systemLanguage => {
      expect(systemLanguage).toEqual(mockSystemLanguage);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language/system/${langCode}`);
    expect(req.request.method).toBe('POST');
    req.flush(mockSystemLanguage);
  });

  it('should update a language', () => {
    const id = 1;
    const details = { name: 'Spanish', status: 'active' };
    const mockLanguage: Language = { id: 1, ...details };

    service.update(id, details).subscribe(language => {
      expect(language).toEqual(mockLanguage);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(details);
    req.flush(mockLanguage);
  });

  it('should delete a language', () => {
    const id = 1;
    const mockResponse = true;

    service.delete(id).subscribe(response => {
      expect(response).toBe(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/language/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });
});
