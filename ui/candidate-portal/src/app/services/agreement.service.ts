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
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {Agreement} from '../model/agreement';

@Injectable({
  providedIn: 'root'
})
export class AgreementService {
  private apiUrl: string = environment.apiUrl + '/agreement';

  constructor(private http: HttpClient) { }

  /**
   * Fetches the list of agreements for the current user.
   */
  listMyAgreements(): Observable<Agreement[]> {
    return this.http.get<Agreement[]>(`${this.apiUrl}/list`);
  }
}
