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
import {Observable, of} from "rxjs";
import {SearchResults} from "../model/search-results";
import {tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class NationalityService {

  private apiUrl: string = environment.apiUrl + '/nationality';

  private nationalities: Nationality[] = [];

  constructor(private http: HttpClient) { }

  listNationalities(): Observable<Nationality[]> {
    //If we already have the data return it, otherwise get it.
    return this.nationalities.length > 0 ?
      //"of" turns the data into an Observable
      of(this.nationalities) :
    this.http.get<Nationality[]>(`${this.apiUrl}`)
      .pipe(
        //Save data the first time we fetch it
        tap(data => {this.nationalities = data})
      );
  }

  search(request): Observable<SearchResults<Nationality>> {
    return this.http.post<SearchResults<Nationality>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<Nationality> {
    return this.http.get<Nationality>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Nationality>  {
    return this.http.post<Nationality>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Nationality>  {
    return this.http.put<Nationality>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
