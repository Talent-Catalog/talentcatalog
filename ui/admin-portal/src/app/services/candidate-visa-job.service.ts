/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateVisaJobCheck} from '../model/candidate';
// todo this was originally built for the Visa AU request, don't want to completely redo as don't want to mess up the current AU visa job checks.
// Should eventually just need the job id for the request, and be able to remove name and sf job link.

export interface CreateCandidateVisaJobRequest {
  jobOppId?: number;
}

@Injectable({providedIn: 'root'})
export class CandidateVisaJobService {

  private apiUrl = environment.apiUrl + '/candidate-visa-job';

  constructor(private http: HttpClient) {}

  get(id: number): Observable<CandidateVisaJobCheck> {
    return this.http.get<CandidateVisaJobCheck>(`${this.apiUrl}/${id}`);
  }

  create(visaId: number, candidateVisaJobRequest: CreateCandidateVisaJobRequest):
    Observable<CandidateVisaJobCheck>  {
    return this.http.post<CandidateVisaJobCheck>(
      `${this.apiUrl}/${visaId}`, candidateVisaJobRequest);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }
}
