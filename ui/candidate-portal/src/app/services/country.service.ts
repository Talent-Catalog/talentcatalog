/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {Country} from "../model/country";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {LanguageService} from "./language.service";

@Injectable({
  providedIn: 'root'
})
export class CountryService {

  private apiUrl: string = environment.apiUrl + '/country';

  constructor(private http: HttpClient, private languageService: LanguageService) { }

  listCountries(): Observable<Country[]> {
    const locale = this.languageService.getSelectedLanguage() || 'en';
    return this.http.get<Country[]>(`${this.apiUrl}`).pipe(
      map((items: Country[], index: number) => {
         items.sort((a, b) => a.name.localeCompare(b.name, locale));
        //Bit of a hack, which only works in English, for putting some names
        //at top.
         if (locale === 'en') {
           const jordan: Country = items.find(x => x.name === "Jordan");
           if (jordan) {
             items.splice(items.indexOf(jordan), 1);
             items.unshift(jordan);
           }
           const lebanon: Country = items.find(x => x.name === "Lebanon");
           if (lebanon) {
             items.splice(items.indexOf(lebanon), 1);
             items.unshift(lebanon);
           }
         }
         return items;
      }),
      catchError(e => throwError(e))
    );
  }

}
