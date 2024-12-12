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
import {CountryService} from './country.service';
import {Country} from '../model/country';
import {environment} from '../../environments/environment';
import {SearchResults} from '../model/search-results';
import {MockJob} from "../MockData/MockJob";

describe('CountryService', () => {
  let service: CountryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CountryService]
    });
    service = TestBed.inject(CountryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#listCountries', () => {
    it('should return an Observable Country[] ', () => {
      const mockCountries: Country[] = [
        MockJob.country
      ];

      service.listCountries().subscribe(countries => {
        expect(countries.length).toBe(1);
        expect(countries).toEqual(mockCountries);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/country`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCountries);
    });
  });

  describe('#listCountriesRestricted', () => {
    it('should return an Observable Country[] ', () => {
      const mockCountries: Country[] = [
        MockJob.country
      ];

      service.listCountriesRestricted().subscribe(countries => {
        expect(countries.length).toBe(1);
        expect(countries).toEqual(mockCountries);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/country/restricted`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCountries);
    });
  });

  describe('#listTCDestinations', () => {
    it('should return an Observable Country[] ', () => {
      const mockCountries: Country[] = [
        MockJob.country
      ];

      service.listTCDestinations().subscribe(countries => {
        expect(countries.length).toBe(1);
        expect(countries).toEqual(mockCountries);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/country/destinations`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCountries);
    });
  });

  describe('#searchPaged', () => {
    it('should return an Observable SearchResults Country  ', () => {
      const mockSearchResults: SearchResults<Country> = {
        content: [
          MockJob.country
          ,
        ],
        totalPages: 2
      } as SearchResults<Country>;
      const request = { query: 'test' };

      service.searchPaged(request).subscribe(results => {
        expect(results.totalPages).toBe(2);
        expect(results.content.length).toBe(1);
        expect(results).toEqual(mockSearchResults);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/country/search-paged`);
      expect(req.request.method).toBe('POST');
      req.flush(mockSearchResults);
    });
  });

  describe('#get', () => {
    it('should return an Observable Country', () => {
      const mockCountry: Country = MockJob.country;

      service.get(1).subscribe(country => {
        expect(country).toEqual(mockCountry);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/country/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCountry);
    });
  });

  describe('#create', () => {
    it('should return an Observable Country ', () => {
      const mockCountry: Country = MockJob.country;
      const details = { name: 'USA' };

      service.create(details).subscribe(country => {
        expect(country).toEqual(mockCountry);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/country`);
      expect(req.request.method).toBe('POST');
      req.flush(mockCountry);
    });
  });

  describe('#update', () => {
    it('should return an Observable Country ', () => {
      const mockCountry: Country = MockJob.country;
      const details = { name: 'USA' };

      service.update(1, details).subscribe(country => {
        expect(country).toEqual(mockCountry);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/country/1`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockCountry);
    });
  });

  describe('#delete', () => {
    it('should return an Observable boolean ', () => {
      const mockResponse = true;

      service.delete(1).subscribe(response => {
        expect(response).toBe(true);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/country/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });
  });
});
