/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateVisaJob} from '../model/candidate';

export interface CreateCandidateVisaJobRequest {
  name: string;
  sfJobLink: string;
}

@Injectable({providedIn: 'root'})
export class CandidateVisaJobService {

  private apiUrl = environment.apiUrl + '/candidate-visa-check';

  constructor(private http: HttpClient) {}

  create(candidateId: number, candidateVisaJobRequest: CreateCandidateVisaJobRequest):
    Observable<CandidateVisaJob>  {
    return this.http.post<CandidateVisaJob>(
      `${this.apiUrl}/${candidateId}`, candidateVisaJobRequest);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }
}
