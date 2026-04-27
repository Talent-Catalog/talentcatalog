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
import {SurveyTypeService} from './survey-type.service';
import {SurveyType} from '../model/survey-type';
import {environment} from '../../environments/environment';

describe('SurveyTypeService', () => {
  let service: SurveyTypeService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/survey-type';

  // Sample data to test the service
  const mockSurveyTypes: SurveyType[] = [
    { id: 1, name: 'Survey Type 1' },
    { id: 2, name: 'Survey Type 2' }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SurveyTypeService]
    });

    service = TestBed.inject(SurveyTypeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve a list of survey types from the API via GET', () => {
    service.listSurveyTypes().subscribe((surveyTypes) => {
      expect(surveyTypes.length).toBe(2);
      expect(surveyTypes).toEqual(mockSurveyTypes);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockSurveyTypes);
  });

  it('should handle empty response gracefully', () => {
    service.listSurveyTypes().subscribe((surveyTypes) => {
      expect(surveyTypes.length).toBe(0);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
