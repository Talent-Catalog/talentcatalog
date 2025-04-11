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
import {
  CandidateSource,
  CandidateSourceType,
  IdsRequest,
  SearchCandidateSourcesRequest,
  UpdateCandidateContextNoteRequest,
  UpdateCandidateSourceDescriptionRequest,
  UpdateDisplayedFieldPathsRequest
} from "../model/base";
import {isSavedSearch} from "../model/saved-search";
import {map} from "rxjs/operators";
import {SavedSearchService} from "./saved-search.service";
import {CandidateFieldService} from "./candidate-field.service";
import {CopySourceContentsRequest} from "../model/saved-list";

@Injectable({providedIn: 'root'})
export class CandidateSourceService {

  private savedListApiUrl = environment.apiUrl + '/saved-list';
  private savedSearchApiUrl = environment.apiUrl + '/saved-search';

  constructor(private http: HttpClient,
              private candidateFieldService: CandidateFieldService
  ) {}

  copy(source: CandidateSource, selection: CopySourceContentsRequest): Observable<CandidateSource> {
    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.put<CandidateSource>(`${apiUrl}/copy/${source.id}`, selection);
  }

  delete(source: CandidateSource): Observable<boolean>  {
    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.delete<boolean>(`${apiUrl}/${source.id}`);
  }

  get(sourceType: CandidateSourceType, id: number): Observable<CandidateSource> {
    const apiUrl = sourceType === CandidateSourceType.SavedSearch ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.get<CandidateSource>(`${apiUrl}/${id}`);
  }

  starSourceForUser(source: CandidateSource, request: { userId: number }):
    Observable<CandidateSource> {

    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    //todo Need to replace this "shared by" idea with "starred by" in both API and Spring code.
    return this.http.put<CandidateSource>(`${apiUrl}/shared-add/${source.id}`, request)
    .pipe(
      map(result => this.processPostResult(result))
    );
  }

  unstarSourceForUser(source: CandidateSource, request: { userId: number }):
    Observable<CandidateSource> {

    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    //todo Need to replace this "shared by" idea with "starred by" in both API and Spring code.
    return this.http.put<CandidateSource>(`${apiUrl}/shared-remove/${source.id}`, request)
      .pipe(
        map(result => this.processPostResult(result))
      );
  }

  searchByIds(sourceType: CandidateSourceType, request: IdsRequest): Observable<CandidateSource[]> {
    const apiUrl = sourceType === CandidateSourceType.SavedSearch ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.post<CandidateSource[]>(`${apiUrl}/search-ids`, request);
  }

  searchPaged(sourceType: CandidateSourceType, request: SearchCandidateSourcesRequest):
    Observable<SearchResults<CandidateSource>> {

    let apiUrl;

    switch (sourceType) {
      case CandidateSourceType.SavedSearch:
        apiUrl = this.savedSearchApiUrl;
        break;
      case CandidateSourceType.SavedList:
        apiUrl = this.savedListApiUrl;
        break;
    }

    return this.http.post<SearchResults<CandidateSource>>(
      `${apiUrl}/search-paged`, request)
      .pipe(
        map(results => this.processPostResults(results))
      );
  }

  processPostResult(result: CandidateSource): CandidateSource {
    if (isSavedSearch(result)) {
      result = SavedSearchService.convertSavedSearchEnums(result);
    }
    return result;
  };

  processPostResults(results: SearchResults<CandidateSource>):
    SearchResults<CandidateSource> {
    for (const result of results.content) {
      this.processPostResult(result);
    }
    return results;
  };

  updateContextNote(source: CandidateSource,
                    request: UpdateCandidateContextNoteRequest): Observable<void> {
    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;
    return this.http.put<void>(`${apiUrl}/context/${source.id}`, request);

  }

  updateDescription(source: CandidateSource,
                    request: UpdateCandidateSourceDescriptionRequest): Observable<void> {
    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;
    return this.http.put<void>(`${apiUrl}/description/${source.id}`, request);

  }

  updateDisplayedFieldPaths(source: CandidateSource,
                    request: UpdateDisplayedFieldPathsRequest): Observable<void> {
    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    //Replace default requests with [].
    if (this.candidateFieldService.isDefault(request.displayedFieldsLong, true)) {
      request.displayedFieldsLong = [];
    }
    if (this.candidateFieldService.isDefault(request.displayedFieldsShort, false)) {
      request.displayedFieldsShort = [];
    }
    return this.http.put<void>(`${apiUrl}/displayed-fields/${source.id}`, request);

  }
}
