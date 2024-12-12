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
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable, throwError} from "rxjs";
import {CandidateAttachment} from "../model/candidate-attachment";
import {SearchResults} from "../model/search-results";
import {saveBlob} from "../util/file";
import {catchError, map} from "rxjs/operators";

//todo use this for other requests
export interface UpdateCandidateAttachmentRequest {
  id?: number;
  name?: string;
  location?: string;
  cv?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CandidateAttachmentService {

  private apiUrl: string = environment.apiUrl + '/candidate-attachment';

  constructor(private http: HttpClient) { }

  listCandidateAttachments(): Observable<CandidateAttachment[]> {
    return this.http.get<CandidateAttachment[]>(`${this.apiUrl}`);
  }

  searchCandidateAttachments(request): Observable<SearchResults<CandidateAttachment>> {
    return this.http.post<SearchResults<CandidateAttachment>>(`${this.apiUrl}/search`, request);
  }

  createAttachment(request): Observable<CandidateAttachment> {
    return this.http.post<CandidateAttachment>(`${this.apiUrl}`, request);
  }

  deleteAttachment(id: number) {
    return this.http.delete<CandidateAttachment>(`${this.apiUrl}/${id}`);
  }

  downloadAttachment(id: number, name: string) {
    return this.http.get(`${this.apiUrl}/${id}/download`,
      { responseType: 'blob' }).pipe(
      map((resp: Blob) => {
          saveBlob(resp, name);
        }, catchError(e => {
            console.log('error', e);
            return throwError(e);
          }
        )
      )
    )
  }

  uploadAttachment(cv: boolean, formData: FormData): Observable<CandidateAttachment> {
    return this.http.post<CandidateAttachment>(
      `${this.apiUrl}/upload?cv=${cv}`, formData);
  }

  updateAttachment(id: number, request: UpdateCandidateAttachmentRequest): Observable<CandidateAttachment> {
    return this.http.put<CandidateAttachment>(`${this.apiUrl}/${id}`, request);
  }

}
