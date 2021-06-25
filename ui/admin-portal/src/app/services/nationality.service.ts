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

@Injectable({
  providedIn: 'root'
})
export class NationalityService {

  // private apiUrl: string = environment.apiUrl + '/nationality';
  //
  // private nationalities: Country[] = [];
  //
  // constructor(private http: HttpClient) { }
  //
  // listNationalities(): Observable<Country[]> {
  //   //If we already have the data return it, otherwise get it.
  //   return this.nationalities.length > 0 ?
  //     //"of" turns the data into an Observable
  //     of(this.nationalities) :
  //   this.http.get<Country[]>(`${this.apiUrl}`)
  //     .pipe(
  //       //Save data the first time we fetch it
  //       tap(data => {this.nationalities = data})
  //     );
  // }
  //
  // search(request): Observable<SearchResults<Country>> {
  //   return this.http.post<SearchResults<Country>>(`${this.apiUrl}/search`, request);
  // }
  //
  // get(id: number): Observable<Country> {
  //   return this.http.get<Country>(`${this.apiUrl}/${id}`);
  // }
  //
  // create(details): Observable<Country>  {
  //   return this.http.post<Country>(`${this.apiUrl}`, details);
  // }
  //
  // update(id: number, details): Observable<Nationality>  {
  //   return this.http.put<Nationality>(`${this.apiUrl}/${id}`, details);
  // }
  //
  // delete(id: number): Observable<boolean>  {
  //   return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  // }

}
