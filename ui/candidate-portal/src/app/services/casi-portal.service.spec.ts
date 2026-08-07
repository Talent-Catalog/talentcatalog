/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';

import {environment} from '../../environments/environment';
import {ResourceStatus, ServiceProviderTermsInfo} from '../model/services';
import {CasiPortalService} from './casi-portal.service';

describe('CasiPortalService', () => {
  let service: CasiPortalService;
  let httpMock: HttpTestingController;

  const provider = 'REFERENCE';
  const serviceCode = 'VOUCHER';
  const apiUrl =
    `${environment.apiUrl}/services/${provider}/${serviceCode}`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CasiPortalService]
    });

    service = TestBed.inject(CasiPortalService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should check eligibility', () => {
    service.checkEligibility(provider, serviceCode)
    .subscribe((response) => {
      expect(response).toBeTrue();
    });

    const request = httpMock.expectOne(`${apiUrl}/eligibility`);

    expect(request.request.method).toBe('GET');

    request.flush(true);
  });

  it('should get the current assignment', () => {
    const assignment = {
      id: 1,
      provider,
      serviceCode
    };

    service.getAssignment(provider, serviceCode)
    .subscribe((response) => {
      expect(response).toEqual(assignment as any);
    });

    const request = httpMock.expectOne(`${apiUrl}/assignment`);

    expect(request.request.method).toBe('GET');

    request.flush(assignment);
  });

  it('should assign the service', () => {
    const assignment = {
      id: 1,
      provider,
      serviceCode
    };

    service.assign(provider, serviceCode).subscribe((response) => {
      expect(response).toEqual(assignment as any);
    });

    const request = httpMock.expectOne(`${apiUrl}/assign`);

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();

    request.flush(assignment);
  });

  it('should get provider terms', () => {
    const terms: ServiceProviderTermsInfo = {
      id: 'terms-1',
      content: 'Provider terms and conditions'
    };

    service.getProviderTerms(provider, serviceCode)
    .subscribe((response) => {
      expect(response).toEqual(terms);
    });

    const request = httpMock.expectOne(
      `${apiUrl}/agreement/terms`
    );

    expect(request.request.method).toBe('GET');

    request.flush(terms);
  });

  it('should return null when provider terms do not exist', () => {
    service.getProviderTerms(provider, serviceCode)
    .subscribe((response) => {
      expect(response).toBeNull();
    });

    const request = httpMock.expectOne(
      `${apiUrl}/agreement/terms`
    );

    expect(request.request.method).toBe('GET');

    request.flush(null);
  });

  it('should check whether provider terms need acceptance', () => {
    service.checkNeedsAgreement(provider, serviceCode)
    .subscribe((response) => {
      expect(response).toBeTrue();
    });

    const request = httpMock.expectOne(
      `${apiUrl}/agreement/needs-acceptance`
    );

    expect(request.request.method).toBe('GET');

    request.flush(true);
  });

  it('should accept provider terms', () => {
    service.acceptProviderTerms(provider, serviceCode).subscribe();

    const request = httpMock.expectOne(
      `${apiUrl}/agreement/accept`
    );

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();

    request.flush(null);
  });

  it('should get OPC DPA terms', () => {
    const terms: ServiceProviderTermsInfo = {
      id: 'opc-dpa-1',
      content: 'OPC data processing agreement'
    };

    service.getOpcDpaTerms(provider, serviceCode)
    .subscribe((response) => {
      expect(response).toEqual(terms);
    });

    const request = httpMock.expectOne(
      `${apiUrl}/agreement/opc-dpa/terms`
    );

    expect(request.request.method).toBe('GET');

    request.flush(terms);
  });

  it('should return null when OPC DPA terms do not exist', () => {
    service.getOpcDpaTerms(provider, serviceCode)
    .subscribe((response) => {
      expect(response).toBeNull();
    });

    const request = httpMock.expectOne(
      `${apiUrl}/agreement/opc-dpa/terms`
    );

    expect(request.request.method).toBe('GET');

    request.flush(null);
  });

  it('should check whether OPC DPA terms need acceptance', () => {
    service.checkNeedsOpcDpa(provider, serviceCode)
    .subscribe((response) => {
      expect(response).toBeFalse();
    });

    const request = httpMock.expectOne(
      `${apiUrl}/agreement/opc-dpa/needs-acceptance`
    );

    expect(request.request.method).toBe('GET');

    request.flush(false);
  });

  it('should accept OPC DPA terms', () => {
    service.acceptOpcDpa(provider, serviceCode).subscribe();

    const request = httpMock.expectOne(
      `${apiUrl}/agreement/opc-dpa/accept`
    );

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();

    request.flush(null);
  });

  it('should update the resource status', () => {
    const updateRequest = {
      resourceCode: 'REF-001',
      status: ResourceStatus.REDEEMED
    };

    service.updateResourceStatus(
      provider,
      serviceCode,
      updateRequest
    ).subscribe();

    const request = httpMock.expectOne(
      `${apiUrl}/resources/status`
    );

    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(updateRequest);

    request.flush(null);
  });
});
