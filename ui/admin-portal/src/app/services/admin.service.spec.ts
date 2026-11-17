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
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {AdminService} from './admin.service';
import {environment} from '../../environments/environment';
import {of} from "rxjs";
import {HttpClient, provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {expect, vi} from 'vitest';

describe('AdminService', () => {
    let service: AdminService;
    let httpMock: HttpTestingController;
    let httpClient: HttpClient;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [AdminService, provideHttpClient(withXhr(), withInterceptorsFromDi()), provideHttpClientTesting()]
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
        const method = 'GET';
        const apiUrl = `${environment.apiUrl}/system/${apicall}`;

        // Mocking the return value of the HttpClient's get method
        const getSpy = vi.spyOn(httpClient, 'get').mockReturnValue(of(void 0));

        service.call(apicall, method).subscribe(response => {
            expect(response).toBeUndefined();
        });

        expect(getSpy).toHaveBeenCalledWith(apiUrl, { responseType: 'text' as any });
    });

    it('should make a POST request to the correct URL', () => {
        const apicall = 'test-call';
        const method = 'POST';
        const apiUrl = `${environment.apiUrl}/system/${apicall}`;

        // Mocking the return value of the HttpClient's get method
        const getSpy = vi.spyOn(httpClient, 'post').mockReturnValue(of(void 0));

        service.call(apicall, method).subscribe(response => {
            expect(response).toBeUndefined();
        });

        expect(getSpy).toHaveBeenCalledWith(apiUrl, {}, { responseType: 'text' as any });
    });
});
