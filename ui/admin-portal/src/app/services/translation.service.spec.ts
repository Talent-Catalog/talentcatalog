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
import {TranslationService} from './translation.service';
import {environment} from '../../environments/environment';
import {Translation} from '../model/translation';
import {SearchResults} from '../model/search-results';
import {TranslatedObject} from '../model/translated-object';

describe('TranslationService', () => {
  let service: TranslationService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/translation';

  const mockTranslation: Translation = {
    id: 1,
    objectId: 1,
    objectType: 'hello',
    value: 'Hello'
  };

  const mockSearchResults: SearchResults<TranslatedObject> = {
    totalElements: 1,
    content: [{ id: 1, name: 'en', status:'active',translatedId: 1, translatedName: 'Hola' }]
  } as SearchResults<TranslatedObject>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TranslationService]
    });
    service = TestBed.inject(TranslationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should list translations via GET', () => {
    service.listTranslations().subscribe((translations) => {
      expect(translations.length).toBe(1);
      expect(translations).toEqual([mockTranslation]);
    });

    const req = httpMock.expectOne(`${apiUrl}`);
    expect(req.request.method).toBe('GET');
    req.flush([mockTranslation]);
  });

  it('should search translations via POST', () => {
    const type = 'type';
    const request = { query: 'test' };

    service.search(type, request).subscribe((searchResults) => {
      expect(searchResults).toEqual(mockSearchResults);
    });

    const req = httpMock.expectOne(`${apiUrl}/${type}`);
    expect(req.request.method).toBe('POST');
    req.flush(mockSearchResults);
  });

  it('should create a translation via POST', () => {
    const details = { language: 'en', key: 'greeting', value: 'Hello' };

    service.create(details).subscribe((translation) => {
      expect(translation).toEqual(mockTranslation);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(mockTranslation);
  });

  it('should update a translation via PUT', () => {
    const id = 1;
    const details = { language: 'en', key: 'farewell', value: 'Goodbye' };

    service.update(id, details).subscribe((translation) => {
      expect(translation).toEqual(mockTranslation);
    });

    const req = httpMock.expectOne(`${apiUrl}/${id}`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockTranslation);
  });

  it('should load translations file via GET', () => {
    const language = 'en';

    service.loadTranslationsFile(language).subscribe((response) => {
      expect(response).toEqual(mockTranslation);
    });

    const req = httpMock.expectOne(`${apiUrl}/file/${language}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTranslation);
  });

  it('should update translations file via PUT', () => {
    const language = 'en';
    const translations = { greeting: 'Hello' };

    service.updateTranslationFile(language, translations).subscribe((response) => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/file/${language}`);
    expect(req.request.method).toBe('PUT');
    req.flush(null);
  });

});
