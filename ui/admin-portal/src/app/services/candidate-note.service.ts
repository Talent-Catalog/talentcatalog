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
import {Observable, Subject} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from '../model/search-results';
import {CandidateNote} from '../model/candidate-note';
import {tap} from 'rxjs/operators';

export interface CreateCandidateNoteRequest {
  candidateId: number;
  title: string;
  comment?: string;
}

@Injectable({providedIn: 'root'})
export class CandidateNoteService {

  private apiUrl = environment.apiUrl + '/candidate-note';

  private newNoteSource = new Subject();
  newNote$ = this.newNoteSource.asObservable();

  private updatedNoteSource = new Subject();
  updatedNote$ = this.updatedNoteSource.asObservable();

  constructor(private http: HttpClient) {}

  list(id: number): Observable<CandidateNote[]> {
    return this.http.get<CandidateNote[]>(`${this.apiUrl}/${id}/list`);
  }

  search(request): Observable<SearchResults<CandidateNote>> {
    return this.http.post<SearchResults<CandidateNote>>(`${this.apiUrl}/search`, request);
  }

  create(request: CreateCandidateNoteRequest): Observable<CandidateNote>  {
    return this.http.post<CandidateNote>(`${this.apiUrl}`, request).pipe(
      tap(() => {
        this.newNoteSource.next()
      })
    )
  }

  update(id: number, details): Observable<CandidateNote>  {
    return this.http.put<CandidateNote>(`${this.apiUrl}/${id}`, details).pipe(
      tap(() => {
        this.updatedNoteSource.next()
      })
    )
  }

}
