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

import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';

import {environment} from '../../environments/environment';
import {Agreement} from '../model/agreement';
import {AgreementService} from './agreement.service';

describe('AgreementService', () => {
  let service: AgreementService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(AgreementService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch the current user agreements', () => {
    const mockAgreements: Agreement[] = [
      {
        id: 1,
        start: '2026-01-01',
        end: null,
        termsInfoId: 'terms-1',
        counterparty: {
          id: 10,
          type: 'PARTNER',
          displayName: 'Test Partner'
        },
        termsInfo: {
          id: 'terms-1',
          type: 'PRIVACY_POLICY',
          pathToContent: '/terms/terms-1',
          createdDate: '2026-01-01',
          content: 'Agreement content'
        }
      }
    ];

    service.listMyAgreements().subscribe((agreements) => {
      expect(agreements).toEqual(mockAgreements);
    });

    const request = httpMock.expectOne(
      `${environment.apiUrl}/agreement/list`
    );

    expect(request.request.method).toBe('GET');

    request.flush(mockAgreements);
  });

  it('should return an empty agreement list from the API', () => {
    service.listMyAgreements().subscribe((agreements) => {
      expect(agreements).toEqual([]);
    });

    const request = httpMock.expectOne(
      `${environment.apiUrl}/agreement/list`
    );

    expect(request.request.method).toBe('GET');

    request.flush([]);
  });
});
