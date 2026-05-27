import {Injectable} from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {Router} from "@angular/router";
import {catchError} from "rxjs/operators";

@Injectable()
export class AuthExpiryInterceptor implements HttpInterceptor {

  constructor(private router: Router) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((err: unknown) => {
        if (err instanceof HttpErrorResponse && err.status === 401) {
          const currentUrl = this.router.url || '/';
          this.router.navigate(['/logout'],
            { queryParams: { reason: err } });
          // Avoid infinite loop by checking if we're already on the login page
          if (!currentUrl.startsWith('/login')) {
            this.router.navigate(['/login'], { queryParams: { returnUrl: currentUrl } });
          }
        }
        return throwError(err);
      })
    );
  }
}
