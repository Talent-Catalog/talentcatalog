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

import {Injectable} from "@angular/core";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateTodo} from "../model/candidate-todo";

export interface CreateCandidateTodoRequest {
  type: string,
  name: string,
  completed: boolean
}

@Injectable({providedIn: 'root'})
export class CandidateTodoService {

  private apiUrl = environment.apiUrl + '/candidate-todo';

  constructor(private http: HttpClient) {}

  get(candidateId: number): Observable<CandidateTodo[]> {
    return this.http.get<CandidateTodo[]>(`${this.apiUrl}/${candidateId}/list`);
  }

  create(candidateId: number, details): Observable<CandidateTodo>  {
    return this.http.post<CandidateTodo>(`${this.apiUrl}/${candidateId}`, details);
  }

  update(id: number, details): Observable<CandidateTodo>  {
    return this.http.put<CandidateTodo>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<CandidateTodo>  {
    return this.http.delete<CandidateTodo>(`${this.apiUrl}/${id}`);
  }
}
