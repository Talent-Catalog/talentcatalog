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
import {VisaPathway, VisaPathwayService} from './visa-pathway.service';
import {environment} from '../../environments/environment';
import {Country} from "../model/country";

describe('VisaPathwayService', () => {
  let service: VisaPathwayService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [VisaPathwayService]
    });
    service = TestBed.inject(VisaPathwayService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should call getVisaPathwaysCountry and return an array of VisaPathways', () => {
    const mockVisaPathways: VisaPathway[] = [
      {
        name: 'Pathway 1',
        description: 'Description 1',
        country: {id: 1, name: 'Country 1'} as Country,
        age: '18-30',
        language: 'English',
        empCommitment: 'Yes',
        inclusions: 'Health Insurance',
        other: 'Other details',
        workExperience: '2 years',
        education: 'Bachelor',
        educationCredential: 'Degree'
      },
      {
        name: 'Pathway 2',
        description: 'Description 2',
        country: {id: 2, name: 'Country 2'} as Country,
        age: '21-35',
        language: 'Spanish',
        empCommitment: 'No',
        inclusions: 'Flight Tickets',
        other: 'Other details',
        workExperience: '3 years',
        education: 'Master',
        educationCredential: 'Diploma'
      }
    ];

    service.getVisaPathwaysCountry(1).subscribe(visaPathways => {
      expect(visaPathways.length).toBe(2);
      expect(visaPathways).toEqual(mockVisaPathways);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/visa-pathway/country/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockVisaPathways);
  });

  it('should return an empty array if no visa pathways are found', () => {
    service.getVisaPathwaysCountry(1).subscribe(visaPathways => {
      expect(visaPathways.length).toBe(0);
      expect(visaPathways).toEqual([]);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/visa-pathway/country/1`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should handle errors when the server returns a 500', () => {
    service.getVisaPathwaysCountry(1).subscribe(
      () => fail('Should have failed with a 500 error'),
      (error) => {
        expect(error.status).toBe(500);
      }
    );

    const req = httpMock.expectOne(`${environment.apiUrl}/visa-pathway/country/1`);
    expect(req.request.method).toBe('GET');
    req.flush('Something went wrong', {status: 500, statusText: 'Server Error'});
  });

});
