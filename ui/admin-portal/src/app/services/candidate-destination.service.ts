/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateDestination} from '../model/candidate-destination';

@Injectable({providedIn: 'root'})
export class CandidateDestinationService {

  private apiUrl = environment.apiUrl + '/candidate-destination';

  constructor(private http: HttpClient) {}

  create(candidateId: number, countryName: {}):
    Observable<CandidateDestination>  {
    return this.http.post<CandidateDestination>(
      `${this.apiUrl}/${candidateId}`, countryName);
  }

}
