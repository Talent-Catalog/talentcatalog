import {HttpErrorResponse, HttpHandler, HttpRequest} from '@angular/common/http';
import {Router} from '@angular/router';
import {throwError} from 'rxjs';
import {ErrorInterceptor} from './error.interceptor';

describe('ErrorInterceptor', () => {
  let interceptor: ErrorInterceptor;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    router = jasmine.createSpyObj<Router>(
      'Router',
      ['navigate'],
      { url: '/current-page' }
    );

    interceptor = new ErrorInterceptor(router);
  });

  function handlerReturningError(errorResponse: HttpErrorResponse): HttpHandler {
    return {
      handle: () => throwError(errorResponse)
    };
  }

  it('should navigate to logout and complete on 401 error', (done) => {
    const request = new HttpRequest('GET', '/api/test');

    const errorResponse = new HttpErrorResponse({
      status: 401,
      statusText: 'Unauthorized',
      error: { message: 'Access denied' }
    });

    interceptor.intercept(request, handlerReturningError(errorResponse)).subscribe({
      next: () => fail('Expected no emitted value'),
      error: () => fail('Expected EMPTY, not an error'),
      complete: () => {
        expect(router.navigate).toHaveBeenCalledWith(
          ['/logout'],
          {
            queryParams: {
              reason: 'Access denied',
              returnUrl: '/current-page'
            }
          }
        );
        done();
      }
    });
  });

  it('should navigate to logout and complete on 403 error', (done) => {
    const request = new HttpRequest('GET', '/api/test');

    const errorResponse = new HttpErrorResponse({
      status: 403,
      statusText: 'Forbidden',
      error: { message: 'Forbidden' }
    });

    interceptor.intercept(request, handlerReturningError(errorResponse)).subscribe({
      error: () => fail('Expected EMPTY, not an error'),
      complete: () => {
        expect(router.navigate).toHaveBeenCalledWith(
          ['/logout'],
          {
            queryParams: {
              reason: 'Forbidden',
              returnUrl: '/current-page'
            }
          }
        );
        done();
      }
    });
  });

  it('should throw a simple string error for non-auth errors', (done) => {
    const request = new HttpRequest('GET', '/api/test');

    const errorResponse = new HttpErrorResponse({
      status: 500,
      statusText: 'Server Error',
      error: { message: 'Something went wrong' }
    });

    interceptor.intercept(request, handlerReturningError(errorResponse)).subscribe({
      next: () => fail('Expected an error'),
      complete: () => fail('Expected an error'),
      error: (error) => {
        expect(error).toBe('Something went wrong');
        expect(router.navigate).not.toHaveBeenCalled();
        done();
      }
    });
  });

  it('should use / as returnUrl when router url is empty', (done) => {
    Object.defineProperty(router, 'url', {
      get: () => ''
    });

    const request = new HttpRequest('GET', '/api/test');

    const errorResponse = new HttpErrorResponse({
      status: 401,
      statusText: 'Unauthorized',
      error: { message: 'Access denied' }
    });

    interceptor.intercept(request, handlerReturningError(errorResponse)).subscribe({
      complete: () => {
        expect(router.navigate).toHaveBeenCalledWith(
          ['/logout'],
          {
            queryParams: {
              reason: 'Access denied',
              returnUrl: '/'
            }
          }
        );
        done();
      }
    });
  });
});
