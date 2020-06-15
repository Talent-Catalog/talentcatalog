/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {
  IHasSetOfSavedLists,
  SavedList,
  SearchSavedListRequest
} from "../model/saved-list";

@Injectable({providedIn: 'root'})
export class CandidateSavedListService {

  private apiUrl = environment.apiUrl + '/candidate-saved-list';

  constructor(private http: HttpClient) {}

  replace(id: number, request: IHasSetOfSavedLists): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/replace`, request);
  }

  search(id: number, request: SearchSavedListRequest): Observable<SavedList[]> {
    return this.http.post<SavedList[]>(`${this.apiUrl}/${id}/search`, request);
  }
}
