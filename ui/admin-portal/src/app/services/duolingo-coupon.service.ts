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

import {Observable} from "rxjs";
import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {DuolingoCouponResponse} from '../model/duolingo-coupon';

@Injectable({
  providedIn: 'root'
})
export class DuolingoCouponService {

  private apiUrl: string = environment.apiUrl + '/coupon';

  constructor(private http: HttpClient) { }

  create(canadidateId: number): Observable<DuolingoCouponResponse>  {
    return this.http.post<DuolingoCouponResponse>(`${this.apiUrl}/${canadidateId}/assign`, null);
  }
}
