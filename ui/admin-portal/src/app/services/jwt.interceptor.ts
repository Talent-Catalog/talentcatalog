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
import {EMPTY, from, Observable} from 'rxjs';
import {AuthenticationService} from "./authentication.service";
import {catchError, switchMap} from "rxjs/operators";
import {Router} from "@angular/router";

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authenticationService: AuthenticationService, private router: Router) {}


  // IMPORTANT NOTE: This should be derived from the list of public URL patterns in the backend.
  // See the PUBLIC_ENDPOINTS in Spring Server code: SecurityConfiguration.java
  private readonly publicUrlPatterns = [
    "/api/admin/branding",
    "/api/portal/branding",
    "/api/portal/language/system",
    "/api/portal/language/translations",
    "/api/admin/translate/translations",
    "/api/admin/terms-info",
    "/api/admin/user/check-token",
    "/api/portal/user/check-token",
    "/api/admin/user/reset-password",
    "/api/portal/user/reset-password",
    "/api/admin/user/reset-password-email",
    "/api/portal/user/reset-password-email",
    "/api/admin/user/verify-email",
    "/app",
    "/backend/jobseeker",
    "/error",
    "/files",
    "/published",
    "/status",
    "/topic",
    "/websocket",
  ];

  private isPublicRequest(url: string): boolean {
    return this.publicUrlPatterns.some(pattern => url.includes(pattern));
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.isPublicRequest(request.url)) {
      return next.handle(request);
    }

    return from(this.authenticationService.refreshToken()).pipe(
      catchError(() => this.handleRefreshFailure()),

      switchMap(() => {
        const token = this.authenticationService.getToken();
        if (!token) {
          return this.handleRefreshFailure();
        }

        const authRequest = request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        });

        return next.handle(authRequest);
      })
    );
  }

  private handleRefreshFailure(): Observable<never> {
    //Session has probably expired.
    //Save the url they were at before logout and pass that
    //in to the Login as a returnUrl so that they are returned to the same page after login
    //instead of the home page.
    const currentUrl = this.router.url || '/';

    //Note that we don't use the Logout Component here. The user does not need to see "session
    //expired" errors.
    //Logout to tidy things up, and they can login again.
    void this.authenticationService.logout();

    //Don't get into a loop if the returnUrl is "/login"
    if (!currentUrl.startsWith('/login')) {
      void this.router.navigate(['/login'], {queryParams: {returnUrl: currentUrl}});
    }

    return EMPTY;
  }
}
