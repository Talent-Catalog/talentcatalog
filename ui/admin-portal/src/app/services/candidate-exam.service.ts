/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateExam} from '../model/candidate';

@Injectable({providedIn: 'root'})
export class CandidateExamService {

  private apiUrl = environment.apiUrl + '/candidate-exam';

  constructor(private http: HttpClient) {}

  create(candidateId: number, candidateExam: CandidateExam):
    Observable<CandidateExam>  {
    return this.http.post<CandidateExam>(
      `${this.apiUrl}/${candidateId}`, candidateExam);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }
}
