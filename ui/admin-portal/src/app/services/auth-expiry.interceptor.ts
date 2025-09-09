import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor, HttpErrorResponse
} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {Router} from "@angular/router";
import {AuthenticationService} from "./authentication.service";
import {catchError} from "rxjs/operators";

@Injectable()
export class AuthExpiryInterceptor implements HttpInterceptor {

  constructor(private router: Router, private auth: AuthenticationService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((err: unknown) => {
        if (err instanceof HttpErrorResponse && err.status === 401) {
          const currentUrl = this.router.url || '/';
          this.auth.logout();
          // Avoid infinite loop by checking if we're already on the login page
          if (!currentUrl.startsWith('/login')) {
            console.log('Navigating to login page with returnUrl:', currentUrl);
            this.router.navigate(['/login'], { queryParams: { returnUrl: currentUrl } });
          }
        }
        return throwError(() => err);
      })
    );
  }
}
