import {HttpHandler, HttpRequest} from '@angular/common/http';
import {Router} from '@angular/router';
import {of} from 'rxjs';

import {JwtInterceptor} from './jwt.interceptor';
import {AuthenticationService} from './authentication.service';

describe('JwtInterceptor', () => {
  let interceptor: JwtInterceptor;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authenticationService = jasmine.createSpyObj<AuthenticationService>(
      'AuthenticationService',
      ['refreshToken', 'getToken', 'logout']
    );

    router = jasmine.createSpyObj<Router>(
      'Router',
      ['navigate'],
      {
        url: '/current-page'
      }
    );

    interceptor = new JwtInterceptor(authenticationService, router);
  });

  function handlerExpectingAuthorization(expectedToken: string): HttpHandler {
    return {
      handle: request => {
        expect(request.headers.get('Authorization')).toBe(`Bearer ${expectedToken}`);
        return of({} as any);
      }
    };
  }

  function handlerExpectingNoAuthorization(): HttpHandler {
    return {
      handle: request => {
        expect(request.headers.has('Authorization')).toBeFalse();
        return of({} as any);
      }
    };
  }

  it('should not add Authorization header for public requests', (done) => {
    const request = new HttpRequest('GET', '/api/portal/branding');

    interceptor.intercept(request, handlerExpectingNoAuthorization()).subscribe({
      complete: () => {
        expect(authenticationService.refreshToken).not.toHaveBeenCalled();
        expect(authenticationService.getToken).not.toHaveBeenCalled();
        done();
      }
    });
  });

  it('should refresh token and add Authorization header for private requests', (done) => {
    const request = new HttpRequest('GET', '/api/admin/private-data');

    authenticationService.refreshToken.and.returnValue(Promise.resolve());
    authenticationService.getToken.and.returnValue('abc123');

    interceptor.intercept(request, handlerExpectingAuthorization('abc123')).subscribe({
      complete: () => {
        expect(authenticationService.refreshToken).toHaveBeenCalled();
        expect(authenticationService.getToken).toHaveBeenCalled();
        expect(authenticationService.logout).not.toHaveBeenCalled();
        expect(router.navigate).not.toHaveBeenCalled();
        done();
      }
    });
  });

  it('should logout and navigate to login when refreshToken fails', (done) => {
    const request = new HttpRequest('GET', '/api/admin/private-data');

    authenticationService.refreshToken.and.returnValue(Promise.reject('refresh failed'));

    interceptor.intercept(request, handlerExpectingNoAuthorization()).subscribe({
      complete: () => {
        expect(authenticationService.logout).toHaveBeenCalled();

        expect(router.navigate).toHaveBeenCalledWith(
          ['/login'],
          {
            queryParams: {
              returnUrl: '/current-page'
            }
          }
        );

        done();
      }
    });
  });

  it('should logout and navigate to login when token is missing after refresh', (done) => {
    const request = new HttpRequest('GET', '/api/admin/private-data');

    authenticationService.refreshToken.and.returnValue(Promise.resolve());
    authenticationService.getToken.and.returnValue(null);

    interceptor.intercept(request, handlerExpectingNoAuthorization()).subscribe({
      complete: () => {
        expect(authenticationService.logout).toHaveBeenCalled();

        expect(router.navigate).toHaveBeenCalledWith(
          ['/login'],
          {
            queryParams: {
              returnUrl: '/current-page'
            }
          }
        );

        done();
      }
    });
  });

  it('should not navigate to login when already on login page', (done) => {
    Object.defineProperty(router, 'url', {
      get: () => '/login'
    });

    const request = new HttpRequest('GET', '/api/admin/private-data');

    authenticationService.refreshToken.and.returnValue(Promise.reject('refresh failed'));

    interceptor.intercept(request, handlerExpectingNoAuthorization()).subscribe({
      complete: () => {
        expect(authenticationService.logout).toHaveBeenCalled();
        expect(router.navigate).not.toHaveBeenCalled();
        done();
      }
    });
  });

  it('should use / as returnUrl when router url is empty', (done) => {
    Object.defineProperty(router, 'url', {
      get: () => ''
    });

    const request = new HttpRequest('GET', '/api/admin/private-data');

    authenticationService.refreshToken.and.returnValue(Promise.reject('refresh failed'));

    interceptor.intercept(request, handlerExpectingNoAuthorization()).subscribe({
      complete: () => {
        expect(authenticationService.logout).toHaveBeenCalled();

        expect(router.navigate).toHaveBeenCalledWith(
          ['/login'],
          {
            queryParams: {
              returnUrl: '/'
            }
          }
        );

        done();
      }
    });
  });
});
