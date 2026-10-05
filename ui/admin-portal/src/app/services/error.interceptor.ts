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

import {Injectable} from '@angular/core';
import {HttpContextToken, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthenticationService} from "./authentication.service";

/**
 * By default this interceptor normalizes every HTTP error into a plain message string (see
 * below), which discards the original status code. Some callers need to distinguish specific
 * statuses (e.g. 404 meaning "not yet created" rather than a real error) - setting this context
 * token to true on a request preserves the original HttpErrorResponse instead, for that request
 * only. All other requests are completely unaffected.
 */
export const PRESERVE_HTTP_ERROR_RESPONSE = new HttpContextToken<boolean>(() => false);

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private authenticationService: AuthenticationService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(catchError(err => {
      if (err.status === 401 || err.status === 403) {
        // auto logout if 401 or 403 responses returned from api
        //(unauthorised and forbidden)
        this.authenticationService.logout();
      }
      console.log(err);

      if (request.context.get(PRESERVE_HTTP_ERROR_RESPONSE)) {
        return throwError(err);
      }

      let error: string;
      if (err.error != null && err.error.message) {
        error = err.error.message;
      } else if (err.message != null) {
        error = err.message;
      } else {
        error = err.status + " " + err.statusText;
      }
      return throwError(error);
    }));
  }
}
