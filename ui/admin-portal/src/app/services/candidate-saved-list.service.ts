/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SavedList, SearchSavedListRequest} from "../model/saved-list";

@Injectable({providedIn: 'root'})
export class CandidateSavedListService {

  private apiUrl = environment.apiUrl + '/candidate-saved-list';

  constructor(private http: HttpClient) {}

  search(id: number, request: SearchSavedListRequest): Observable<SavedList[]> {
    return this.http.post<SavedList[]>(`${this.apiUrl}/${id}/search`, request);
  }
}
