/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateDependant, FamilyRelations} from '../model/candidate';

export interface CreateCandidateDependantRequest {
  relation?: FamilyRelations;
  dob?: string;
  healthConcerns?: string;
}

@Injectable({providedIn: 'root'})
export class CandidateDependantService {

  private apiUrl = environment.apiUrl + '/candidate-dependant';

  constructor(private http: HttpClient) {}

  create(candidateId: number, request: CreateCandidateDependantRequest):
    Observable<CandidateDependant>  {
    return this.http.post<CandidateDependant>(
      `${this.apiUrl}/${candidateId}`, request);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }
}
