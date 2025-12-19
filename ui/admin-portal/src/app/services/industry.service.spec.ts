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
import {IndustryService} from './industry.service';
import {Industry} from '../model/industry';
import {SearchResults} from '../model/search-results';
import {environment} from "../../environments/environment";

describe('IndustryService', () => {
  let service: IndustryService;
  let httpTestingController: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/industry`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [IndustryService]
    });

    service = TestBed.inject(IndustryService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should list industries', () => {
    const mockIndustries: Industry[] = [
      { id: 1, name: 'Industry 1', status: 'Active' },
      { id: 2, name: 'Industry 2', status: 'Inactive' }
    ];

    service.listIndustries().subscribe((response) => {
      expect(response).toEqual(mockIndustries);
    });

    const req = httpTestingController.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockIndustries);
  });

  it('should search industries', () => {
    const request = { /* search request parameters */ };
    const mockResponse: SearchResults<Industry> = {
      content: [
        { id: 1, name: 'Industry 1', status: 'Active' },
        { id: 2, name: 'Industry 2', status: 'Inactive' }
      ],
      totalPages: 2
    } as SearchResults<Industry>;

    service.search(request).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should get industry by ID', () => {
    const id = 1;
    const mockIndustry: Industry = { id: 1, name: 'Industry 1', status: 'Active' };

    service.get(id).subscribe((response) => {
      expect(response).toEqual(mockIndustry);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockIndustry);
  });

  it('should create a new industry', () => {
    const details: Industry = { id: 3, name: 'New Industry', status: 'Active' };
    const mockIndustry: Industry = { id: 3, name: 'New Industry', status: 'Active' };

    service.create(details).subscribe((response) => {
      expect(response).toEqual(mockIndustry);
    });

    const req = httpTestingController.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(details);
    req.flush(mockIndustry);
  });

  it('should update an industry', () => {
    const id = 1;
    const details: Industry = { id: 1, name: 'Updated Industry', status: 'Inactive' };
    const mockIndustry: Industry = { id: 1, name: 'Updated Industry', status: 'Inactive' };

    service.update(id, details).subscribe((response) => {
      expect(response).toEqual(mockIndustry);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(details);
    req.flush(mockIndustry);
  });

  it('should delete an industry', () => {
    const id = 1;
    const mockResponse = true;  // Assuming the delete operation returns a boolean

    service.delete(id).subscribe((response) => {
      expect(response).toBe(mockResponse);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });
});
