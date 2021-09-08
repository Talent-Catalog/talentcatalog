/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {
  PublishListRequest,
  SavedList,
  UpdateExplicitSavedListContentsRequest
} from "../model/saved-list";

@Injectable({
  providedIn: 'root'
})
export class SavedListCandidateService {

  private apiUrl: string = environment.apiUrl + '/saved-list-candidate';

  constructor(private http: HttpClient) {
  }

  create(request: UpdateExplicitSavedListContentsRequest): Observable<SavedList>  {
    return this.http.post<SavedList>(`${this.apiUrl}`, request);
  }

  mergeFromFile(id: number, formData: FormData): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/merge-from-file`, formData);
  }

  merge(id: number, request: UpdateExplicitSavedListContentsRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/merge`, request);
  }

  //todo Should return link to file
  /**
   * Exports the whole list as a file containing candidate information that is suitable for
   * "publishing" - ie sharing externally, for example with prospective employers.
   * @param id ID of list to be published
   * @param request Request specifying the candidate data to be shared.
   */
  publish(id: number, request: PublishListRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/publish`, request);
  }

  remove(id: number, request: UpdateExplicitSavedListContentsRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/remove`, request);
  }

  saveSelection(id: number, request: UpdateExplicitSavedListContentsRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/save-selection`, request);
  }
}
