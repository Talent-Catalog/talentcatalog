/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from "../model/search-results";
import {Candidate} from "../model/candidate";
import {CandidateSource, PagedSearchRequest, SearchCandidateSourcesRequest} from "../model/base";
import {isSavedSearch} from "../model/saved-search";

@Injectable({providedIn: 'root'})
export class CandidateSourceCandidateService {

  private savedListApiUrl = environment.apiUrl + '/saved-list-candidate';
  private savedSearchApiUrl = environment.apiUrl + '/saved-search-candidate';

  constructor(private http: HttpClient) {}

  list(source: CandidateSource): Observable<Candidate[]> {
    const apiUrl = isSavedSearch(source) ? this.savedSearchApiUrl : this.savedListApiUrl;
    return this.http.get<Candidate[]>(`${apiUrl}/${source.id}/list`);
  }

  isEmpty(source: CandidateSource): Observable<boolean> {
    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;
    return this.http.get<boolean>(`${apiUrl}/${source.id}/is-empty`);
  }

  search(source: CandidateSource, request: SearchCandidateSourcesRequest):
    Observable<Candidate[]> {

    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;
    return this.http.post<Candidate[]>(
      `${apiUrl}/${source.id}/search`, request)
  }

  searchPaged(source: CandidateSource, request: SearchCandidateSourcesRequest):
    Observable<SearchResults<Candidate>> {

    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;
    return this.http.post<SearchResults<Candidate>>(
      `${apiUrl}/${source.id}/search-paged`, request)
  }

  export(source: CandidateSource, request: PagedSearchRequest) {
    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;
    return this.http.post(
      `${apiUrl}/${source.id}/export/csv`, request, {responseType: 'blob'});
  }
}
