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
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl: string = environment.apiUrl + '/system';

  constructor(private http: HttpClient) { }

  // Currently only make GET or POST calls on the System Admin API
  call(apicall: string, method: string): Observable<string> {
    if (method == 'GET') {
      return this.http.get(`${this.apiUrl}/${apicall}`, {responseType: 'text'});
    } else {
      return this.http.post(`${this.apiUrl}/${apicall}`, {}, {responseType: 'text'});
    }
  }

}
