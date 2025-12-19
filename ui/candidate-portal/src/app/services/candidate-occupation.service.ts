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
import {Observable} from "rxjs";
import {CandidateOccupation} from "../model/candidate-occupation";

@Injectable({
  providedIn: 'root'
})
export class CandidateOccupationService {

  private apiUrl: string = environment.apiUrl + '/candidate-occupation';

  constructor(private http: HttpClient) { }

  createCandidateOccupation(request): Observable<CandidateOccupation> {
    return this.http.post<CandidateOccupation>(`${this.apiUrl}`, request);
  }

  deleteCandidateOccupation(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  listMyOccupations(): Observable<CandidateOccupation[]> {
    return this.http.get<CandidateOccupation[]>(`${this.apiUrl}/list`);
  }

  updateCandidateOccupations(request): Observable<CandidateOccupation[]> {
    return this.http.post<CandidateOccupation[]>(`${this.apiUrl}/update`, request);
  }

}
