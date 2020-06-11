import {Injectable} from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthService} from './auth.service';
import {Router} from "@angular/router";


@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router ) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(catchError(err => {
      if (err.status === 401 || err.status === 403) {
        // auto logout if 401 or 403 responses returned from api
        //(unauthorised and forbidden)
        this.authService.logout();
        this.router.navigateByUrl('login');
      }
      console.log(err);
      let error: string;
      if (err.error !== null) {
        error = err.error.message;
      } else if (err.message !== null) {
        error = err.message;
      } else {
        error = err.status + " " + err.statusText;
      }
      return throwError(error);
    }));
  }
}
