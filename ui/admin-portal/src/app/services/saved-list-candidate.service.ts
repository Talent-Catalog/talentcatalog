/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {IHasSetOfCandidates} from "../model/saved-list";

@Injectable({
  providedIn: 'root'
})
export class SavedListCandidateService {

  private apiUrl: string = environment.apiUrl + '/saved-list-candidate';

  constructor(private http: HttpClient) {
  }

  merge(id: number, request: IHasSetOfCandidates): Observable<boolean> {
    return this.http.put<boolean>(`${this.apiUrl}/${id}/merge`, request);
  }

  remove(id: number, request: IHasSetOfCandidates): Observable<boolean> {
    return this.http.put<boolean>(`${this.apiUrl}/${id}/remove`, request);
  }

  replace(id: number, request: IHasSetOfCandidates): Observable<boolean> {
    return this.http.put<boolean>(`${this.apiUrl}/${id}/replace`, request);
  }
}
