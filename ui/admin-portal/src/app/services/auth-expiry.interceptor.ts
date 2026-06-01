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
          //Convert the incoming error to a simple string
          let error: string;
          if (err.error !== null) {
            error = err.error.message;
          } else if (err.message !== null) {
            error = err.message;
          } else {
            error = err.status + " " + err.statusText;
          }
          const returnUrl = this.router.url || '/';
          void this.router.navigate(['/logout'],
            { queryParams: { reason: error, returnUrl: returnUrl } });
        }
        return throwError(err);
      })
    );
  }
}
