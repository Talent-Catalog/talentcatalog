/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */
import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Opportunity} from '../model/base';

@Injectable({
  providedIn: 'root'
})
export class SalesforceService {
  private apiUrl: string = environment.apiUrl + '/system/sfjobname';

  constructor(private http: HttpClient) {
  }

  findSfJobName(sfUrl: string): Observable<Opportunity> {
    return this.http.get<Opportunity>(`${this.apiUrl}`,
      {params: {"url": sfUrl}});
  }
}

