/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {SavedSearch} from "../model/saved-search";
import {
  CreateSavedListRequest,
  SavedList,
  SearchSavedListRequest,
  UpdateSavedListInfoRequest
} from "../model/saved-list";

@Injectable({
  providedIn: 'root'
})
export class SavedListService {

  private apiUrl: string = environment.apiUrl + '/saved-list';

  constructor(private http: HttpClient) {
  }

  create(request: CreateSavedListRequest): Observable<SavedList>  {
    return this.http.post<SavedList>(`${this.apiUrl}`, request);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  get(id: number): Observable<SavedSearch> {
    return this.http.get<SavedSearch>(`${this.apiUrl}/${id}`);
  }

  search(request: SearchSavedListRequest): Observable<SavedList[]> {
    return this.http.post<SavedList[]>(`${this.apiUrl}/search`, request);
  }

  searchPaged(request: SearchSavedListRequest): Observable<SearchResults<SavedList>> {
    return this.http.post<SearchResults<SavedList>>(`${this.apiUrl}/search-paged`, request);
  }

  update(id: number, request: UpdateSavedListInfoRequest): Observable<SavedList>  {
    return this.http.put<SavedList>(`${this.apiUrl}/${id}`, request);
  }

  //todo Sharing and watching
  //
  // addSharedUser(id: number, request: { userId: number }): Observable<SavedSearch> {
  //   return this.http.put<SavedSearch>(`${this.apiUrl}/shared-add/${id}`, request);
  // }
  //
  // removeSharedUser(id: number, request: { userId: number }): Observable<SavedSearch> {
  //   return this.http.put<SavedSearch>(`${this.apiUrl}/shared-remove/${id}`, request);
  // }
  //
  // addWatcher(id: number, request: { userId: number }): Observable<SavedSearch> {
  //   return this.http.put<SavedSearch>(`${this.apiUrl}/watcher-add/${id}`, request);
  // }
  //
  // removeWatcher(id: number, request: { userId: number }): Observable<SavedSearch> {
  //   return this.http.put<SavedSearch>(`${this.apiUrl}/watcher-remove/${id}`, request);
  // }
}
