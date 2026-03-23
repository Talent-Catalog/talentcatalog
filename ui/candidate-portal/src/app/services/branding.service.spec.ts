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

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BrandingService, BrandingInfo } from './branding.service';
import { AuthenticationService } from './authentication.service';
import { environment } from '../../environments/environment';

describe('BrandingService', () => {
  let service: BrandingService;
  let httpMock: HttpTestingController;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  const apiUrl = environment.apiUrl + '/branding';

  beforeEach(() => {
    const authSpy = jasmine.createSpyObj('AuthenticationService', ['isRegistered']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        BrandingService,
        { provide: AuthenticationService, useValue: authSpy }
      ]
    });

    service = TestBed.inject(BrandingService);
    httpMock = TestBed.inject(HttpTestingController);
    authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getBrandingInfoFromApi', () => {
    it('should retrieve branding info from API successfully', () => {
      const mockBrandingInfo: BrandingInfo = {
        logo: 'test-logo.png',
        partnerName: 'Test Partner',
        websiteUrl: 'https://test.com'
      };

      service.getBrandingInfoFromApi().subscribe(brandingInfo => {
        expect(brandingInfo).toEqual(mockBrandingInfo);
        expect(brandingInfo.partnerName).toBe('Test Partner');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      req.flush(mockBrandingInfo);
    });

    it('should include partner abbreviation in query string when set', () => {
      const mockBrandingInfo: BrandingInfo = {
        logo: 'tbb-logo.png',
        partnerName: 'Talent Beyond Boundaries',
        websiteUrl: 'https://talentbeyondboundaries.org'
      };

      service.setPartnerAbbreviation('TBB');

      service.getBrandingInfoFromApi().subscribe(brandingInfo => {
        expect(brandingInfo).toEqual(mockBrandingInfo);
      });

      const req = httpMock.expectOne(`${apiUrl}?p=TBB`);
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('?p=TBB');
      req.flush(mockBrandingInfo);
    });

    it('should work without partner abbreviation', () => {
      const mockBrandingInfo: BrandingInfo = {
        logo: 'default-logo.png',
        partnerName: 'Default Partner',
        websiteUrl: 'https://default.com'
      };

      // Ensure no partner abbreviation is set
      service.partnerAbbreviation = null;

      service.getBrandingInfoFromApi().subscribe(brandingInfo => {
        expect(brandingInfo).toEqual(mockBrandingInfo);
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      expect(req.request.url).not.toContain('?p=');
      req.flush(mockBrandingInfo);
    });

    it('should handle different partner abbreviations correctly', () => {
      const mockBrandingInfo: BrandingInfo = {
        logo: 'partner-logo.png',
        partnerName: 'Partner XYZ',
        websiteUrl: 'https://partnerxyz.com'
      };

      service.setPartnerAbbreviation('XYZ');

      service.getBrandingInfoFromApi().subscribe(brandingInfo => {
        expect(brandingInfo.partnerName).toBe('Partner XYZ');
      });

      const req = httpMock.expectOne(`${apiUrl}?p=XYZ`);
      expect(req.request.method).toBe('GET');
      req.flush(mockBrandingInfo);
    });
  });

  describe('getBrandingInfo', () => {
    it('should return API branding info when user is registered', () => {
      authServiceSpy.isRegistered.and.returnValue(true);

      const mockBrandingInfo: BrandingInfo = {
        logo: 'registered-logo.png',
        partnerName: 'Registered Partner',
        websiteUrl: 'https://registered.com'
      };

      service.getBrandingInfo().subscribe(brandingInfo => {
        expect(brandingInfo).toEqual(mockBrandingInfo);
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      req.flush(mockBrandingInfo);
    });

    it('should return default branding info when user is not registered', () => {
      authServiceSpy.isRegistered.and.returnValue(false);

      service.getBrandingInfo().subscribe(brandingInfo => {
        expect(brandingInfo.logo).toBe('assets/images/tc-logo-2.png');
        expect(brandingInfo.partnerName).toBe('a Talent Catalog partner');
        expect(brandingInfo.websiteUrl).toBe('');
      });

      // No HTTP request should be made for unregistered users
      httpMock.expectNone(apiUrl);
    });
  });

  describe('setPartnerAbbreviation', () => {
    it('should set partner abbreviation', () => {
      service.setPartnerAbbreviation('TEST');
      expect(service.partnerAbbreviation).toBe('TEST');
    });

    it('should update partner abbreviation', () => {
      service.setPartnerAbbreviation('FIRST');
      expect(service.partnerAbbreviation).toBe('FIRST');

      service.setPartnerAbbreviation('SECOND');
      expect(service.partnerAbbreviation).toBe('SECOND');
    });
  });
});
