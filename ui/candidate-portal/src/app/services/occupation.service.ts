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
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Occupation} from '../model/occupation';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class OccupationService {

  private apiUrl: string = environment.apiUrl + '/occupation';

  constructor(private http: HttpClient) { }

  listOccupations(): Observable<Occupation[]> {
    return this.http.get<Occupation[]>(`${this.apiUrl}`).pipe(
      map((items: Occupation[], index: number) => {
        const unknown: Occupation = items.find(x => x.id === 0);
        const i: number = items.indexOf(unknown);
        if (unknown){
          items.splice(i, 1);
          items.push(unknown);
        }
        return items;
      }
    ))
  }

}
