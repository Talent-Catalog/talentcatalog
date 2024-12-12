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
import {Country} from "../model/country";
import {Observable, of, throwError} from "rxjs";
import {catchError, map, tap} from "rxjs/operators";
import {LanguageService} from "./language.service";

@Injectable({
  providedIn: 'root'
})
export class CountryService {

  private apiUrl: string = environment.apiUrl + '/country';

  private tcDestinations: Country[] = [];

  constructor(private http: HttpClient, private languageService: LanguageService) { }

  /**
   * Retrieve states of country with given country id.
   * <p/>
   * Current implementation just returns states when country is USA.
   * <p/>
   * Currently data is hard coded without making request to the server.
   * @param countryId
   * @return Observable of array of strings representing state names, or null if not available.
   */
  listStates(countryId: number): Observable<string[]> {
    let ret: Observable<string[]>;

    //US states
    if (countryId === 6178) {
      ret = of([
      "Alabama",
      "Alaska",
      "Arizona",
      "Arkansas",
      "California",
      "Colorado",
      "Connecticut",
      "Delaware",
      "District of Columbia",
      "Florida",
      "Georgia",
      "Guam",
      "Hawaii",
      "Idaho",
      "Illinois",
      "Indiana",
      "Iowa",
      "Kansas",
      "Kentucky",
      "Louisiana",
      "Maine",
      "Maryland",
      "Massachusetts",
      "Michigan",
      "Minnesota",
      "Mississippi",
      "Missouri",
      "Montana",
      "Nebraska",
      "Nevada",
      "New Hampshire",
      "New Jersey",
      "New Mexico",
      "New York",
      "North Carolina",
      "North Dakota",
      "Ohio",
      "Oklahoma",
      "Oregon",
      "Pennsylvania",
      "Rhode Island",
      "South Carolina",
      "South Dakota",
      "Tennessee",
      "Texas",
      "Utah",
      "Vermont",
      "Virginia",
      "Washington",
      "West Virginia",
      "Wisconsin",
      "Wyoming"
        ]
      )
    } else {
      ret = of(null);
    }
    return ret;
  }

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

  listTCDestinations(): Observable<Country[]> {
    //If we already have the data return it, otherwise get it.
    return this.tcDestinations.length > 0 ?
      //"of" turns the data into an Observable
      of(this.tcDestinations) :
      this.http.get<Country[]>(`${this.apiUrl}/destinations`)
      .pipe(
        //Save data the first time we fetch it
        tap(data => {this.tcDestinations = data})
      );
  }

}
