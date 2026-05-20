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
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {Router} from "@angular/router";


@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private router: Router) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(catchError(err => {
      console.log(err);
      //Convert the incoming error to a simple string
      let error: string;
      if (err.error !== null) {
        error = err.error.message;
      } else if (err.message !== null) {
        error = err.message;
      } else {
        error = err.status + " " + err.statusText;
      }

      if (err.status === 401 || err.status === 403) {
        // auto logout if Access Denied errors (eg 401) are returned from api
        this.router.navigate(['/logout'],
          { queryParams: { reason: error } });
      } else {
        //otherwise but throw an error for the code to handle
        return throwError(error);
      }
    }));
  }
}
