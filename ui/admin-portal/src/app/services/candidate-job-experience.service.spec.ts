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

import {CandidateJobExperienceService} from "./candidate-job-experience.service";
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {TestBed} from "@angular/core/testing";
import {CandidateJobExperience} from "../model/candidate-job-experience";
import {environment} from "../../environments/environment";
import {SearchResults} from "../model/search-results";

describe('CandidateJobExperienceService', () => {
  let service: CandidateJobExperienceService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-job-experience';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateJobExperienceService]
    });

    service = TestBed.inject(CandidateJobExperienceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should return an Observable of CandidateJobExperience[]', () => {
    const mockResponse: CandidateJobExperience[] = [
      {
        id: 1,
        country: { id: 1, name: 'Country A',status:'',translatedName:'' },
        companyName: 'Company A',
        role: 'Developer',
        startDate: '2022-01-01',
        endDate: '2022-12-31',
        fullTime: 'yes',
        paid: 'yes',
        description: 'Developed various features',
        expanded: true
      }
    ];

    service.list({ pageSize: 10, pageNumber: 1 }).subscribe((data) => {
      expect(data.length).toBe(1);
      expect(data[0].companyName).toBe('Company A');
      expect(data[0].role).toBe('Developer');
      expect(data[0].country.name).toBe('Country A');
    });

    const req = httpMock.expectOne(`${apiUrl}/list`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should return an Observable of CandidateJobExperience when created', () => {
    const mockResponse: CandidateJobExperience = {
      id: 1,
      country: { id: 1, name: 'Country A',status:'',translatedName:'' },
      companyName: 'Company A',
      role: 'Developer',
      startDate: '2022-01-01',
      endDate: '2022-12-31',
      fullTime: 'yes',
      paid: 'yes',
      description: 'Developed various features',
      expanded: true
    };
    const requestData = {
      country: { id: 1, name: 'Country A',status:'',translatedName:'' },
      companyName: 'Company A',
      role: 'Developer',
      startDate: '2022-01-01',
      endDate: '2022-12-31',
      fullTime: 'yes',
      paid: 'yes',
      description: 'Developed various features'
    };

    service.create(1, requestData).subscribe((data) => {
      expect(data.id).toBe(1);
      expect(data.companyName).toBe('Company A');
      expect(data.role).toBe('Developer');
      expect(data.country.name).toBe('Country A');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(requestData);
    req.flush(mockResponse);
  });

  it('should return an Observable of CandidateJobExperience when updated', () => {
    const mockResponse: CandidateJobExperience = {
      id: 1,
      country: { id: 1, name: 'Country A',status:'',translatedName:'' },
      companyName: 'Updated Company',
      role: 'Senior Developer',
      startDate: '2022-01-01',
      endDate: '2023-01-01',
      fullTime: 'yes',
      paid: 'yes',
      description: 'Updated job description',
      expanded: true
    };
    const requestData = {
      country: { id: 1, name: 'Country A',status:'',translatedName:'' },
      companyName: 'Updated Company',
      role: 'Senior Developer',
      startDate: '2022-01-01',
      endDate: '2023-01-01',
      fullTime: 'yes',
      paid: 'yes',
      description: 'Updated job description'
    };

    service.update(1, requestData).subscribe((data) => {
      expect(data.id).toBe(1);
      expect(data.companyName).toBe('Updated Company');
      expect(data.role).toBe('Senior Developer');
      expect(data.country.name).toBe('Country A');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(requestData);
    req.flush(mockResponse);
  });

  it('should return an Observable of SearchResults<CandidateJobExperience>', () => {
    const mockResponse: SearchResults<CandidateJobExperience> = {
      number: 0,
      size: 1,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      content: [
        {
          id: 1,
          country: { id: 1, name: 'Country A',status:'',translatedName:'' },
          companyName: 'Company A',
          role: 'Developer',
          startDate: '2022-01-01',
          endDate: '2022-12-31',
          fullTime: 'yes',
          paid: 'yes',
          description: 'Developed various features',
          expanded: true
        }
      ]
    };
    const requestData = { searchTerm: 'Developer' };

    service.search(requestData).subscribe((data) => {
      expect(data.content.length).toBe(1);
      expect(data.content[0].companyName).toBe('Company A');
      expect(data.content[0].role).toBe('Developer');
      expect(data.content[0].country.name).toBe('Country A');
      expect(data.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(requestData);
    req.flush(mockResponse);
  });

  it('should return an Observable of CandidateJobExperience when deleted', () => {
    const mockResponse: CandidateJobExperience = {
      id: 1,
      country: { id: 1, name: 'Country A',status:'',translatedName:'' },
      companyName: 'Company A',
      role: 'Developer',
      startDate: '2022-01-01',
      endDate: '2022-12-31',
      fullTime: 'yes',
      paid: 'yes',
      description: 'Developed various features',
      expanded: true
    };

    service.delete(1).subscribe((data) => {
      expect(data.id).toBe(1);
      expect(data.companyName).toBe('Company A');
      expect(data.role).toBe('Developer');
      expect(data.country.name).toBe('Country A');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });

});
