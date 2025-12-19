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
import {environment} from "../../environments/environment";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {
  ClearSelectionRequest,
  SavedSearch,
  SavedSearchRequest,
  SavedSearchSubtype,
  SavedSearchType,
  SelectCandidateInSearchRequest
} from "../model/saved-search";
import {map} from "rxjs/operators";
import {CopySourceContentsRequest, SavedList} from "../model/saved-list";
import {UpdateCandidateStatusInfo} from "../model/candidate";
import {SearchCandidateRequest} from "../model/search-candidate-request";
import {DtoType} from "../model/base";

export interface CreateFromDefaultSavedSearchRequest {
  savedListId: number;
  name: string;
  jobId?: number;
}

export interface SavedSearchTypeInfo {
  savedSearchType?: SavedSearchType;
  title: string;
  categories?: SavedSearchTypeSubInfo[];
}

export interface SavedSearchTypeSubInfo {
  savedSearchSubtype?: SavedSearchSubtype;
  title: string;
}

@Injectable({
  providedIn: 'root'
})
export class SavedSearchService {

  private apiUrl: string = environment.apiUrl + '/saved-search';

  private readonly savedSearchTypeInfos: SavedSearchTypeInfo[] = [];

  constructor(private http: HttpClient) {
    const profCategories: SavedSearchTypeSubInfo[] = [
      {savedSearchSubtype: SavedSearchSubtype.business, title: 'Category 1 - Business / Finance'},
      {savedSearchSubtype: SavedSearchSubtype.agriculture, title: 'Category 2 - Agriculture & Livestock'},
      {savedSearchSubtype: SavedSearchSubtype.healthcare, title: 'Category 3 - Healthcare'},
      {savedSearchSubtype: SavedSearchSubtype.engineering, title: 'Category 4 - Engineering & Architecture'},
      {savedSearchSubtype: SavedSearchSubtype.food, title: 'Category 5 - Food Related'},
      {savedSearchSubtype: SavedSearchSubtype.education, title: 'Category 6 - Education'},
      {savedSearchSubtype: SavedSearchSubtype.labourer, title: 'Category 7 - Construction/laborers'},
      {savedSearchSubtype: SavedSearchSubtype.trade, title: 'Category 8 - Skilled Trades (construction related) '},
      {savedSearchSubtype: SavedSearchSubtype.arts, title: 'Category 9 - Arts / Design'},
      {savedSearchSubtype: SavedSearchSubtype.it, title: 'Category 10 - IT / Tech'},
      {savedSearchSubtype: SavedSearchSubtype.social, title: 'Category 11 - Social / Humanitarian Related'},
      {savedSearchSubtype: SavedSearchSubtype.science, title: 'Category 12 - Science Related'},
      {savedSearchSubtype: SavedSearchSubtype.law, title: 'Category 13 - Law'},
      {savedSearchSubtype: SavedSearchSubtype.other, title: 'Category 14 - Other'},
    ];

    //Maybe use these as categories in future
    const jobCategories: SavedSearchTypeSubInfo[] = [
      {savedSearchSubtype: SavedSearchSubtype.au, title: 'Australia'},
      {savedSearchSubtype: SavedSearchSubtype.ca, title: 'Canada'},
      {savedSearchSubtype: SavedSearchSubtype.uk, title: 'UK'},
    ];
    // todo I dont think we search by roles and other anymore - think this is zombie code that can be simplied.
    this.savedSearchTypeInfos[SavedSearchType.profession] =
      {savedSearchType: SavedSearchType.profession,
        title: 'Occupations',
        categories: profCategories
      };

    this.savedSearchTypeInfos[SavedSearchType.job] =
      {savedSearchType: SavedSearchType.job,
        title: 'Roles',
      };

    this.savedSearchTypeInfos[SavedSearchType.other] =
      {savedSearchType: SavedSearchType.other,
        title: 'Other'
      };
  }

  getSavedSearchTypeInfos(): SavedSearchTypeInfo[] {
    return this.savedSearchTypeInfos;
  }

  search(request): Observable<SavedSearch[]> {
    return this.http.post<SavedSearch[]>(`${this.apiUrl}/search`, request)
      .pipe(
        map(results => this.processPostResults(results))
      );
  }

  searchPaged(request): Observable<SearchResults<SavedSearch>> {
    return this.http.post<SearchResults<SavedSearch>>(`${this.apiUrl}/search-paged`, request)
      .pipe(
        map(results => {
          results.content = this.processPostResults(results.content);
          return results;
        })
      );
  }

  processPostResults(content: SavedSearch[]): SavedSearch[] {
    for (let savedSearch of content) {
      savedSearch = SavedSearchService.convertSavedSearchEnums(savedSearch);
    }
    return content;
  };

  load(id: number): Observable<SearchCandidateRequest> {
    return this.http.get<SearchCandidateRequest>(`${this.apiUrl}/${id}/load`);
  }

  get(id: number): Observable<SavedSearch>;
  get(id: number, dtoType: DtoType): Observable<SavedSearch>;
  get(id: number, dtoType?: DtoType): Observable<SavedSearch> {
    const params = dtoType ? new HttpParams().set('dtoType', dtoType) : new HttpParams();
    return this.http.get<SavedSearch>(`${this.apiUrl}/${id}`, { params })
      .pipe(
        map(savedSearch => SavedSearchService.convertSavedSearchEnums(savedSearch))
      );
  }

  getDefault(): Observable<SavedSearch> {
    return this.http.get<SavedSearch>(`${this.apiUrl}/default`)
      .pipe(
        map(savedSearch => SavedSearchService.convertSavedSearchEnums(savedSearch))
      );
  }

  create(savedSearchRequest: SavedSearchRequest): Observable<SavedSearch>  {
    return this.http.post<SavedSearch>(`${this.apiUrl}`, savedSearchRequest)
      .pipe(
        map(savedSearch => SavedSearchService.convertSavedSearchEnums(savedSearch))
      );
  }

  createFromDefaultSearch(request: CreateFromDefaultSavedSearchRequest): Observable<SavedSearch>  {
    return this.http.post<SavedSearch>(`${this.apiUrl}/create-from-default`, request)
      .pipe(
        map(savedSearch => SavedSearchService.convertSavedSearchEnums(savedSearch))
      );
  }

  update(savedSearchRequest: SavedSearchRequest): Observable<SavedSearch>  {
    return this.http.put<SavedSearch>(`${this.apiUrl}/${savedSearchRequest.id}`, savedSearchRequest);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  public static convertSavedSearchEnums(savedSearch: any): SavedSearch {
    if (typeof savedSearch.savedSearchType === "string") {
      savedSearch.savedSearchType = SavedSearchType[savedSearch.savedSearchType];
    }
    if (typeof savedSearch.savedSearchSubtype === "string") {
      savedSearch.savedSearchSubtype = SavedSearchSubtype[savedSearch.savedSearchSubtype];
    }
    return savedSearch;
  }

  selectCandidate(id: number, request: SelectCandidateInSearchRequest):
    Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/select-candidate/${id}`, request);
  }

  addWatcher(id: number, request: { userId: number }): Observable<SavedSearch> {
    return this.http.put<SavedSearch>(`${this.apiUrl}/watcher-add/${id}`, request)
      .pipe(
        map(savedSearch => SavedSearchService.convertSavedSearchEnums(savedSearch))
      );
  }

  removeWatcher(id: number, request: { userId: number }): Observable<SavedSearch> {
    return this.http.put<SavedSearch>(`${this.apiUrl}/watcher-remove/${id}`, request)
      .pipe(
        map(savedSearch => SavedSearchService.convertSavedSearchEnums(savedSearch))
      );
  }

  clearSelection(id: number, request: ClearSelectionRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/clear-selection/${id}`, request);
  }

  saveSelection(id: number, request: CopySourceContentsRequest): Observable<SavedList> {
    return this.http.put<SavedList>(`${this.apiUrl}/save-selection/${id}`, request);
  }

  updateSelectedStatuses(id: number, request: UpdateCandidateStatusInfo): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/update-selected-statuses/${id}`, request);
  }

  getSelectionCount(id: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/get-selection-count/${id}`);
  }
}
