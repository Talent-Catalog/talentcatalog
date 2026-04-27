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
import {SalesforceService} from './salesforce.service';
import {EnvService} from './env.service';
import {environment } from '../../environments/environment';
import {CandidateSource, UpdateEmployerOpportunityRequest} from '../model/base';
import {Opportunity} from "../model/opportunity";

describe('SalesforceService', () => {
  let service: SalesforceService;
  let httpMock: HttpTestingController;
  let envServiceSpy: jasmine.SpyObj<EnvService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('EnvService', ['sfLightningUrl']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        SalesforceService,
        { provide: EnvService, useValue: spy }
      ]
    });

    service = TestBed.inject(SalesforceService);
    httpMock = TestBed.inject(HttpTestingController);
    envServiceSpy = TestBed.inject(EnvService) as jasmine.SpyObj<EnvService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#sfOppToLink', () => {
    it('should return null if id is null', () => {
      const result = service.sfOppToLink(null);
      expect(result).toBeNull();
    });

  });

  describe('#joblink', () => {
    it('should return null if candidateSource is null', () => {
      const result = service.joblink(null);
      expect(result).toBeNull();
    });

    it('should return null if sfJobOpp in candidateSource is null', () => {
      const candidateSource = { sfJobOpp: null } as CandidateSource;
      const result = service.joblink(candidateSource);
      expect(result).toBeNull();
    });

  });

  describe('#getOpportunity', () => {
    it('should return an Opportunity', () => {
      const mockOpportunity: Opportunity = {
        closed: false,
        name: 'Test Opportunity',
      } as Opportunity;

      service.getOpportunity('https://test.sf.com').subscribe(opportunity => {
        expect(opportunity).toEqual(mockOpportunity);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/sf/opportunity?url=https://test.sf.com`);
      expect(req.request.method).toBe('GET');
      req.flush(mockOpportunity);
    });
  });

  describe('#updateEmployerOpportunity', () => {
    it('should send a PUT request to update the employer opportunity', () => {
      const mockRequest: UpdateEmployerOpportunityRequest = {
        jobId:1
      } as UpdateEmployerOpportunityRequest;

      service.updateEmployerOpportunity(mockRequest).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/sf/update-emp-opp`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(mockRequest);
      req.flush({});
    });
  });
});
