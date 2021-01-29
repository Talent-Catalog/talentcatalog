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
import {Nationality} from "../model/nationality";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {LanguageService} from "./language.service";

@Injectable({
  providedIn: 'root'
})
export class NationalityService {

  private apiUrl: string = environment.apiUrl + '/nationality';

  constructor(private http: HttpClient,
              private languageService: LanguageService) { }

  listNationalities(): Observable<Nationality[]> {
    const locale = this.languageService.getSelectedLanguage() || 'en';
    return this.http.get<Nationality[]>(`${this.apiUrl}`).pipe(
      map((items: Nationality[], index: number) => {
        items.sort((a, b) => a.name.localeCompare(b.name, locale));
        //Bit of a hack, which only works in English, for putting some names
        //at top.
        if (locale === 'en') {
          const iraqi: Nationality = items.find(x => x.name === "Iraqi");
          if (iraqi) {
            items.splice(0, 0, iraqi);
          }
          const jordanian: Nationality = items.find(x => x.name === "Jordanian");
          if (jordanian) {
            items.splice(0, 0, jordanian);
          }
          const palestinian: Nationality = items.find(x => x.name === "Palestinian");
          if (palestinian) {
            items.splice(0, 0, palestinian);
          }
          const syrian: Nationality = items.find(x => x.name === "Syrian");
          if (syrian) {
            items.splice(0, 0, syrian);
          }
        }
        return items;
      }),
      catchError(e => throwError(e))
    );
  }

}
