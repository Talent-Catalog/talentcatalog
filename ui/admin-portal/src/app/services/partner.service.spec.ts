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
import {PartnerService} from './partner.service';
import {environment} from '../../environments/environment';
import {
  Partner,
  UpdatePartnerJobContactRequest,
  UpdatePartnerRequest
} from '../model/partner';
import {SearchResults} from '../model/search-results';
import {SearchPartnerRequest, Status} from '../model/base';
import {MockPartner} from "../MockData/MockPartner";
import {Job} from "../model/job";
import {MockJob} from "../MockData/MockJob";

describe('PartnerService', () => {
  let service: PartnerService;
  let httpMock: HttpTestingController;
  const mockPartner = new MockPartner();
  const dummyPartners: Partner[] = [
    mockPartner,
    mockPartner
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PartnerService]
    });
    service = TestBed.inject(PartnerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should list partners', () => {

    service.listPartners().subscribe((partners) => {
      expect(partners.length).toBe(2);
      expect(partners).toEqual(dummyPartners);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/partner`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyPartners);
  });

  it('should search partners', () => {
    const searchRequest: SearchPartnerRequest = { keyword: 'test', status: Status.active };
    const dummySearchResults: Partner[] = dummyPartners;

    service.search(searchRequest).subscribe((partners) => {
      expect(partners.length).toBe(2);
      expect(partners).toEqual(dummySearchResults);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/partner/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(searchRequest);
    req.flush(dummySearchResults);
  });

  it('should search partners with paging', () => {
    const searchRequest: SearchPartnerRequest = { keyword: 'test', status: Status.active };
    const dummySearchResults: SearchResults<Partner> = {
      content: [
        mockPartner
      ],
      totalElements: 1
    } as SearchResults<Partner>;

    service.searchPaged(searchRequest).subscribe((searchResults) => {
      expect(searchResults.content.length).toBe(1);
      expect(searchResults).toEqual(dummySearchResults);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/partner/search-paged`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(searchRequest);
    req.flush(dummySearchResults);
  });

  it('should create a partner', () => {
    const newPartner: UpdatePartnerRequest = { defaultContactId: 1, sourceCountryIds: [1,2] } as UpdatePartnerRequest;

    service.create(newPartner).subscribe((partner) => {
      expect(partner).toEqual(mockPartner);
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/partner`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newPartner);
    req.flush(mockPartner);
  });

  it('should list source partners', () => {
    const dummyJob: Job = MockJob;
    const expectedRequest: SearchPartnerRequest = {
      contextJobId: dummyJob.id,
      sourcePartner: true,
      status: Status.active,
      sortFields: ['name'],
      sortDirection: 'ASC'
    };

    service.listSourcePartners(dummyJob).subscribe((partners) => {
      expect(partners.length).toBe(2);
      expect(partners).toEqual(dummyPartners);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/partner/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(expectedRequest);
    req.flush(dummyPartners);
  });

  it('should update a partner', () => {
    const updateRequest: UpdatePartnerRequest = { defaultContactId: 1, sourceCountryIds: [1,2] } as UpdatePartnerRequest;

    service.update(1, updateRequest).subscribe((partner) => {
      expect(partner).toEqual(mockPartner);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/partner/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateRequest);
    req.flush(mockPartner);
  });

  it('should update job contact for a partner', () => {
    const updateJobContactRequest: UpdatePartnerJobContactRequest = { jobId: 1, userId: 1 };

    service.updateJobContact(1, updateJobContactRequest).subscribe((partner) => {
      expect(partner).toEqual(mockPartner);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/partner/1/update-job-contact`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateJobContactRequest);
    req.flush(mockPartner);
  });
});
