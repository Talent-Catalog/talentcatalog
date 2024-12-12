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
/*
   MODEL - cache static data, "of", pipe, tap

   Shows how to cache static data (countries in this case) so that we don't have to repeatedly
   fetch it from server. See listCountries.

   Local attribute returned by "of", plus a pipe and tap saving to local attribute on initial fetch.
 */
import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Country} from '../model/country';
import {Observable, of} from 'rxjs';
import {SearchResults} from '../model/search-results';
import {tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CountryService {

  private apiUrl: string = environment.apiUrl + '/country';
  private countries: Country[] = [];
  private countriesRestricted: Country[] = [];
  private tcDestinations: Country[] = [];

  constructor(private http: HttpClient) { }

  isPalestine(country: Country): boolean {
    return country && country.name.startsWith("Palest");
  }

  listCountries(): Observable<Country[]> {
    //If we already have the data return it, otherwise get it.
    return this.countries.length > 0 ?
      //"of" turns the data into an Observable
      of(this.countries) :
      this.http.get<Country[]>(`${this.apiUrl}`)
        .pipe(
          //Save data the first time we fetch it
          tap(data => {this.countries = data})
        );
  }

  listCountriesRestricted(): Observable<Country[]> {
    //Get the restricted countries based on the users source countries
    return this.http.get<Country[]>(`${this.apiUrl}/restricted`);
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

  searchPaged(request): Observable<SearchResults<Country>> {
    return this.http.post<SearchResults<Country>>(`${this.apiUrl}/search-paged`, request);
  }

  get(id: number): Observable<Country> {
    return this.http.get<Country>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Country>  {
    return this.http.post<Country>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Country>  {
    return this.http.put<Country>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
