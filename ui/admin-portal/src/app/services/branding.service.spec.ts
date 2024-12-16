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
import {BrandingInfo, BrandingService} from './branding.service';
import {environment} from '../../environments/environment';

describe('BrandingService', () => {
  let service: BrandingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BrandingService]
    });

    service = TestBed.inject(BrandingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return branding info', () => {
    const dummyBrandingInfo: BrandingInfo = { logo: 'dummyLogo', websiteUrl: 'dummyUrl' };

    service.getBrandingInfo().subscribe((brandingInfo) => {
      expect(brandingInfo).toEqual(dummyBrandingInfo);
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/branding`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyBrandingInfo);
  });
});
