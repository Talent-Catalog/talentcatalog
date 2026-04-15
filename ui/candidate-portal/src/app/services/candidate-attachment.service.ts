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
import {saveBlob} from "../util/file";
import {catchError, map} from "rxjs/operators";
import {AuthenticationService} from "./authentication.service";

export interface UpdateCandidateAttachmentRequest {
  id?: number;
  name?: string;
  location?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CandidateAttachmentService {

  private apiUrl: string = environment.apiUrl + '/candidate-attachment';

  constructor(
    private authenticationService: AuthenticationService,
    private http: HttpClient
  ) {}

  listCandidateAttachments(): Observable<CandidateAttachment[]> {
    return this.http.get<CandidateAttachment[]>(`${this.apiUrl}`);
  }

  deleteAttachment(id: number) {
    return this.http.delete<CandidateAttachment>(`${this.apiUrl}/${id}`);
  }

  downloadAttachment(id: number, name: string) {
    let observable: Observable<void>;
    if (this.authenticationService.isGrnInstance()) {
      //For GRN instances, we just want to view the file without downloading it.
      observable = this.http.get<void>(`${this.apiUrl}/${id}/view`);
    } else {
      observable = this.http.get(`${this.apiUrl}/${id}/download`,
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
    return observable;
  }

  uploadAttachment(cv: boolean, formData: FormData): Observable<CandidateAttachment> {
    return this.http.post<CandidateAttachment>(
      `${this.apiUrl}/upload?cv=${cv}`, formData);
  }

  updateAttachment(id: number, request: UpdateCandidateAttachmentRequest): Observable<CandidateAttachment> {
    return this.http.put<CandidateAttachment>(`${this.apiUrl}/${id}`, request);
  }

}
