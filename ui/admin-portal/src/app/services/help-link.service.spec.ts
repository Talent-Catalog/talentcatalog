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
import {HelpLinkService} from './help-link.service';
import {environment} from '../../environments/environment';
import {SearchHelpLinkRequest, UpdateHelpLinkRequest, HelpLink} from '../model/help-link';
import {SearchResults } from '../model/search-results';
import {MockJob} from "../MockData/MockJob";

describe('HelpLinkService', () => {
  let service: HelpLinkService;
  let httpTestingController: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/help-link`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HelpLinkService]
    });

    service = TestBed.inject(HelpLinkService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should fetch help links', () => {
    const request: SearchHelpLinkRequest = { countryId:1 };
    const mockResponse: HelpLink[] = [ {
      label:'Test',
      country: MockJob.country
    } as HelpLink ];

    service.fetch(request).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/fetch`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should search help links', () => {
    const request: SearchHelpLinkRequest = { countryId:1 };
    const mockResponse: HelpLink[] = [  {
      label:'Test',
      country: MockJob.country
    } as HelpLink
  ];

    service.search(request).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should search help links with paging', () => {
    const request: SearchHelpLinkRequest = { countryId:1 };
    const mockResponse: SearchResults<HelpLink> = {
      content:[],
      totalPages:0
    } as SearchResults<HelpLink>;

    service.searchPaged(request).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/search-paged`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should create a help link', () => {
    const request: UpdateHelpLinkRequest = {
      label:'Test',
      countryId:1
    } as UpdateHelpLinkRequest;
    const mockResponse: HelpLink = {
      label:'Test',
      country: MockJob.country
    } as HelpLink;

    service.create(request).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpTestingController.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should update a help link', () => {
    const id = 1;
    const request: UpdateHelpLinkRequest = {
      label:'Test',
      countryId:1
    } as UpdateHelpLinkRequest;

    const mockResponse: HelpLink = {
      label:'Test',
      country: MockJob.country
    } as HelpLink;

    service.update(id, request).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });
});
