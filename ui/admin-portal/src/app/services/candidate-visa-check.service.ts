/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateVisaCheck} from "../model/candidate";

export interface CreateCandidateVisaCheckRequest {
  countryId: number;
}

@Injectable({providedIn: 'root'})
export class CandidateVisaCheckService {

  private apiUrl = environment.apiUrl + '/candidate-visa-check';

  constructor(private http: HttpClient) {}

  create(candidateId: number, candidateVisaCheckRequest: CreateCandidateVisaCheckRequest):
    Observable<CandidateVisaCheck>  {
    return this.http.post<CandidateVisaCheck>(
      `${this.apiUrl}/${candidateId}`, candidateVisaCheckRequest);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }
}
