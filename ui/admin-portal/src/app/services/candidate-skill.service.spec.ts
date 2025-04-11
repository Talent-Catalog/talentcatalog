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
import {CandidateSkillService} from './candidate-skill.service';
import {environment} from '../../environments/environment';
import {SearchResults} from '../model/search-results';
import {CandidateSkill} from '../model/candidate-skill';

describe('CandidateSkillService', () => {
  let service: CandidateSkillService;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/candidate-skill`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateSkillService]
    });

    service = TestBed.inject(CandidateSkillService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('search', () => {
    it('should return search results of candidate skills', () => {
      let request = {
        candidateId: 1,
        pageNumber: 0,
        pageSize: 20
      };
      const mockResponse: SearchResults<CandidateSkill> = {
        number:1,
        size:10,
        first:true,
        last:false,
        totalPages:1,
        totalElements: 2,
        content: [
          { skill: 'JavaScript', timePeriod: 1 }, // Mocked CandidateSkill object
          { skill: 'TypeScript' , timePeriod: 3}  // Mocked CandidateSkill object
        ],
      };

      service.search(request).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${apiUrl}/search`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });
});
