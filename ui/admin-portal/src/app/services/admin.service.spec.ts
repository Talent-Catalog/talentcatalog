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
import {AdminService} from './admin.service';
import {environment} from '../../environments/environment';
import {of} from "rxjs";
import {HttpClient} from "@angular/common/http";

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminService]
    });

    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should make a GET request to the correct URL', () => {
    const apicall = 'test-call';
    const apiUrl = `${environment.apiUrl}/system/${apicall}`;

    // Mocking the return value of the HttpClient's get method
    const getSpy = spyOn(httpClient, 'get').and.returnValue(of(void 0));

    service.call(apicall).subscribe(response => {
      expect(response).toBeUndefined();
    });

    expect(getSpy).toHaveBeenCalledWith(apiUrl);
  });
});
