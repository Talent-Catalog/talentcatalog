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
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {
  PublishedDocImportReport,
  PublishListRequest,
  SavedList,
  SearchSavedListRequest,
  UpdateSavedListInfoRequest,
  UpdateShortNameRequest
} from "../model/saved-list";

@Injectable({
  providedIn: 'root'
})
export class SavedListService {

  private apiUrl: string = environment.apiUrl + '/saved-list';

  constructor(private http: HttpClient) {
  }

  create(request: UpdateSavedListInfoRequest): Observable<SavedList>  {
    return this.http.post<SavedList>(`${this.apiUrl}`, request);
  }

  createFolder(savedListId: number): Observable<SavedList> {
    return this.http.put<SavedList>(
      `${this.apiUrl}/${savedListId}/create-folder`, null);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  get(id: number): Observable<SavedList> {
    return this.http.get<SavedList>(`${this.apiUrl}/${id}`);
  }

  /**
   * Exports the whole list as a file containing candidate information that is suitable for
   * "publishing" - ie sharing externally, for example with prospective employers.
   * @param id ID of list to be published
   * @param request Request specifying the candidate data to be shared.
   */
  publish(id: number, request: PublishListRequest): Observable<SavedList> {
    return this.http.put<SavedList>(`${this.apiUrl}/${id}/publish`, request);
  }

  /**
   * Imports potential employer feedback from the currently published doc associated with a list.
   * <p/>
   * Does nothing if the list has not been published.
   * @param id ID of published list
   * @return PublishedDocImportReport containing details of the import
   */
  importEmployerFeedback(id: number): Observable<PublishedDocImportReport> {
    return this.http.put<PublishedDocImportReport>(`${this.apiUrl}/${id}/feedback`, null);
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

  updateShortName(request: UpdateShortNameRequest): Observable<SavedList>  {
    return this.http.put<SavedList>(`${this.apiUrl}/short-name`, request);
  }
}
