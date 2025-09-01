import { TestBed } from '@angular/core/testing';
import {
  HTTP_INTERCEPTORS,
  HttpClient,
  HttpErrorResponse
} from '@angular/common/http';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthExpiryInterceptor } from './auth-expiry.interceptor';
import { AuthenticationService } from './authentication.service';

describe('AuthExpiryInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  // simple spies
  let routerMock: any;
  let authMock: any;

  beforeEach(() => {
    routerMock = {
      url: '/admin-portal/list/6233',
      navigate: jasmine.createSpy('navigate')
    };
    authMock = {
      logout: jasmine.createSpy('logout')
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: routerMock },
        { provide: AuthenticationService, useValue: authMock },
        { provide: HTTP_INTERCEPTORS, useClass: AuthExpiryInterceptor, multi: true },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    routerMock.navigate.calls.reset();
    authMock.logout.calls.reset();
  });

  it('redirects to /login with returnUrl and logs out on 401', () => {
    routerMock.url = '/admin-portal/list/6233';

    http.get('/api/test').subscribe({
      next: () => fail('expected 401'),
      error: () => { /* ignore */ }
    });

    const req = httpMock.expectOne('/api/test');
    req.flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(authMock.logout).toHaveBeenCalledTimes(1);
    expect(routerMock.navigate).toHaveBeenCalledTimes(1);
    expect(routerMock.navigate).toHaveBeenCalledWith(
      ['/login'],
      { queryParams: { returnUrl: '/admin-portal/list/6233' } }
    );
  });

  it('does not redirect if already on login page (still logs out)', () => {
    routerMock.url = '/login/reset-password';

    http.get('/api/test').subscribe({
      next: () => fail('expected 401'),
      error: () => { /* ignore */ }
    });

    const req = httpMock.expectOne('/api/test');
    req.flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(authMock.logout).toHaveBeenCalledTimes(1);
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('does nothing on non-401 errors', (done) => {
    http.get('/api/test').subscribe({
      next: () => fail('expected error'),
      error: (err: HttpErrorResponse) => {
        expect(err.status).toBe(403);
        expect(authMock.logout).not.toHaveBeenCalled();
        expect(routerMock.navigate).not.toHaveBeenCalled();
        done();
      }
    });

    const req = httpMock.expectOne('/api/test');
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
  });
});
