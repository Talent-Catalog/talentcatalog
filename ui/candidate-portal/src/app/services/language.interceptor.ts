/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Injectable} from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LanguageService} from "./language.service";

/**
 * This inserts the user's currently selected language into the X-Language
 * header of any Http requests.
 * <p/>
 * See LanguageFilter.java on the Server side which updates the user's
 * selected language according to the X-Language header value.
 */
@Injectable()
export class LanguageInterceptor implements HttpInterceptor {

  constructor(private languageService: LanguageService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // add selected language header
    const selectedLanguage = this.languageService.getSelectedLanguage();
    if (selectedLanguage) {
      request = request.clone({
        setHeaders: {
          'X-Language': selectedLanguage
        }
      });
    }

    return next.handle(request);
  }
}
