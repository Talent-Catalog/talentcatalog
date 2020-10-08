/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateCitizenship} from "../model/candidate";

@Injectable({providedIn: 'root'})
export class CandidateCitizenshipService {

  private apiUrl = environment.apiUrl + '/candidate-citizenship';

  constructor(private http: HttpClient) {}

  create(candidateId: number, candidateCitizenship: CandidateCitizenship):
    Observable<CandidateCitizenship>  {
    return this.http.post<CandidateCitizenship>(
      `${this.apiUrl}/${candidateId}`, candidateCitizenship);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }
}
