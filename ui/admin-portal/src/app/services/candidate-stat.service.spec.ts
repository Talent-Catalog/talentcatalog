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
import {CandidateStatService, CandidateStatsRequest} from './candidate-stat.service';
import {environment} from '../../environments/environment';
import {StatReport} from '../model/stat-report';

describe('CandidateStatService', () => {
  let service: CandidateStatService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateStatService]
    });

    service = TestBed.inject(CandidateStatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllStats', () => {
    it('should return an Observable StatReport[] when called', () => {
      const mockResponse: StatReport[] = [
        { name: 'Report 1', rows:[], chartType: 'bar' },
        { name: 'Report 2', rows:[], chartType: 'bar'  }
      ];

      const requestPayload: CandidateStatsRequest = {
        runOldStats: true,
        listId: 123,
        searchId: 456,
        dateFrom: '2021-01-01',
        dateTo: '2021-12-31'
      };

      service.getAllStats(requestPayload).subscribe((stats: StatReport[]) => {
        expect(stats.length).toBe(2);
        expect(stats).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate/stat/all`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(requestPayload);

      req.flush(mockResponse);
    });

    it('should handle empty response correctly', () => {
      const requestPayload: CandidateStatsRequest = {
        runOldStats: true,
        listId: 123,
        searchId: 456,
        dateFrom: '2021-01-01',
        dateTo: '2021-12-31'
      };

      service.getAllStats(requestPayload).subscribe((stats: StatReport[]) => {
        expect(stats.length).toBe(0);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/candidate/stat/all`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(requestPayload);

      req.flush([]);
    });
  });
});
