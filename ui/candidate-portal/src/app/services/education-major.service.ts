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
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {EducationMajor} from "../model/education-major";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {LanguageService} from "./language.service";

@Injectable({
  providedIn: 'root'
})
export class EducationMajorService {

  private apiUrl: string = environment.apiUrl + '/education-major';

  constructor(private http: HttpClient,
              private languageService: LanguageService) { }

  listMajors(): Observable<EducationMajor[]> {
    const locale = this.languageService.getSelectedLanguage() || 'en';
    return this.http.get<EducationMajor[]>(`${this.apiUrl}`).pipe(
      map((items: EducationMajor[], index: number) => {
        return items.sort((a, b) => a.name.localeCompare(b.name, locale));
      }),
      catchError(e => throwError(e))
    );
  }

}
