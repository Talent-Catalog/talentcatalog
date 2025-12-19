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
import {OccupationService} from './occupation.service';
import {Occupation} from '../model/occupation';
import {SearchResults} from '../model/search-results';
import {SystemLanguage} from '../model/language';
import {environment} from '../../environments/environment';

describe('OccupationService', () => {
  let service: OccupationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [OccupationService]
    });
    service = TestBed.inject(OccupationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should list occupations', () => {
    const dummyOccupations: Occupation[] = [
      { id: 1, name: 'Occupation1', status: 'active', isco08Code: 'code1' },
      { id: 2, name: 'Occupation2', status: 'inactive', isco08Code: 'code2' }
    ];

    service.listOccupations().subscribe((occupations) => {
      expect(occupations.length).toBe(2);
      expect(occupations).toEqual(dummyOccupations);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/occupation`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyOccupations);
  });

  it('should search occupations', () => {
    const searchRequest = { keyword: 'test' };
    const dummySearchResults: SearchResults<Occupation> = {
      content: [{ id: 1, name: 'Occupation1', status: 'active', isco08Code: 'code1' },
      ],
      totalElements: 1
    } as SearchResults<Occupation>;

    service.search(searchRequest).subscribe((searchResults) => {
      expect(searchResults.content.length).toBe(1);
      expect(searchResults).toEqual(dummySearchResults);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/occupation/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(searchRequest);
    req.flush(dummySearchResults);
  });

  it('should get an occupation by id', () => {
    const dummyOccupation: Occupation = { id: 1, name: 'Occupation1', status: 'active', isco08Code: 'code1' };

    service.get(1).subscribe((occupation) => {
      expect(occupation).toEqual(dummyOccupation);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/occupation/1`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyOccupation);
  });

  it('should create an occupation', () => {
    const newOccupation: Occupation = { id: 1, name: 'Occupation1', status: 'active', isco08Code: 'code1' };

    service.create(newOccupation).subscribe((occupation) => {
      expect(occupation).toEqual(newOccupation);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/occupation`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newOccupation);
    req.flush(newOccupation);
  });

  it('should update an occupation', () => {
    const updatedOccupation: Occupation = { id: 1, name: 'Occupation1', status: 'active', isco08Code: 'code1' };
    ;

    service.update(1, updatedOccupation).subscribe((occupation) => {
      expect(occupation).toEqual(updatedOccupation);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/occupation/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedOccupation);
    req.flush(updatedOccupation);
  });

  it('should delete an occupation', () => {
    service.delete(1).subscribe((response) => {
      expect(response).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/occupation/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(true);
  });

  it('should add system language translations', () => {
    const langCode = 'en';
    const dummyFile = new File(['dummy content'], 'dummy.txt', { type: 'text/plain' });
    const dummySystemLanguage: SystemLanguage = {id:1, label: 'en', language: 'English',rtl:false };

    service.addSystemLanguageTranslations(langCode, dummyFile).subscribe((systemLanguage) => {
      expect(systemLanguage).toEqual(dummySystemLanguage);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/occupation/system/${langCode}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush(dummySystemLanguage);
  });
});
