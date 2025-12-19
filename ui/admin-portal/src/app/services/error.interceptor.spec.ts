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
import {HttpHandler, HttpRequest, HttpErrorResponse} from '@angular/common/http';
import {throwError} from 'rxjs';
import {Router} from '@angular/router';
import {AuthenticationService} from './authentication.service';
import {ErrorInterceptor} from './error.interceptor';

describe('ErrorInterceptor', () => {
  let interceptor: ErrorInterceptor;
  let httpTestingController: HttpTestingController;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const spyAuth = jasmine.createSpyObj('AuthenticationService', ['logout']);
    const spyRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ErrorInterceptor,
        { provide: AuthenticationService, useValue: spyAuth },
        { provide: Router, useValue: spyRouter }
      ]
    });

    interceptor = TestBed.inject(ErrorInterceptor);
    httpTestingController = TestBed.inject(HttpTestingController);
    authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should call logout and handle 401 error', () => {
    const request = new HttpRequest<any>('GET', '/test');
    const error = new HttpErrorResponse({
      error: 'Unauthorized',
      status: 401,
      statusText: 'Unauthorized'
    });

    interceptor.intercept(request, { handle: () => throwError(error) } as HttpHandler).subscribe(
      () => fail('expected an error, not a response'),
      (errorMsg: string) => {
        expect(authenticationServiceSpy.logout).toHaveBeenCalled();
        expect(errorMsg).toBe('Http failure response for (unknown url): 401 Unauthorized');
      }
    );
  });

  it('should call logout and handle 403 error', () => {
    const request = new HttpRequest<any>('GET', '/test');
    const error = new HttpErrorResponse({
      error: 'Forbidden',
      status: 403,
      statusText: 'Forbidden'
    });

    interceptor.intercept(request, { handle: () => throwError(error) } as HttpHandler).subscribe(
      () => fail('expected an error, not a response'),
      (errorMsg: string) => {
        expect(authenticationServiceSpy.logout).toHaveBeenCalled();
        expect(errorMsg).toBe('Http failure response for (unknown url): 403 Forbidden');
      }
    );
  });

  it('should handle other errors correctly', () => {
    const request = new HttpRequest<any>('GET', '/test');
    const error = new HttpErrorResponse({
      error: null,
      status: 500,
      statusText: 'Server Error'
    });

    interceptor.intercept(request, { handle: () => throwError(error) } as HttpHandler).subscribe(
      () => fail('expected an error, not a response'),
      (errorMsg: string) => {
        expect(errorMsg).toBe('Http failure response for (unknown url): 500 Server Error');
      }
    );
  });

  it('should handle errors with custom messages', () => {
    const request = new HttpRequest<any>('GET', '/test');
    const error = new HttpErrorResponse({
      error: { message: 'Custom error message' },
      status: 400,
      statusText: 'Bad Request'
    });

    interceptor.intercept(request, { handle: () => throwError(error) } as HttpHandler).subscribe(
      () => fail('expected an error, not a response'),
      (errorMsg: string) => {
        expect(errorMsg).toBe('Custom error message');
      }
    );
  });
});
