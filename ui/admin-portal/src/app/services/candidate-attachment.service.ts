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
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from '../model/search-results';
import {
  CandidateAttachment,
  CandidateAttachmentRequest
} from '../model/candidate-attachment';

@Injectable({providedIn: 'root'})
export class CandidateAttachmentService {

  private apiUrl = environment.apiUrl + '/candidate-attachment';

  constructor(private http: HttpClient) {}

  search(request): Observable<CandidateAttachment[]> {
    return this.http.post<CandidateAttachment[]>(`${this.apiUrl}/search`, request);
  }

  searchPaged(request): Observable<SearchResults<CandidateAttachment>> {
    return this.http.post<SearchResults<CandidateAttachment>>(`${this.apiUrl}/search-paged`, request);
  }

  createAttachment(details: CandidateAttachmentRequest): Observable<CandidateAttachment>  {
    return this.http.post<CandidateAttachment>(`${this.apiUrl}`, details);
  }

  deleteAttachment(id: number) {
    return this.http.delete<CandidateAttachment>(`${this.apiUrl}/${id}`);
  }

  downloadAttachment(id: number) {
    return this.http.get(`${this.apiUrl}/${id}/download`,
      { responseType: 'blob' });
  }

  uploadAttachment(id: number, cv: boolean, formData: FormData): Observable<CandidateAttachment> {
    return this.http.post<CandidateAttachment>(
      `${this.apiUrl}/${id}/upload?cv=${cv}`, formData);
  }

  updateAttachment(value: any): Observable<CandidateAttachment> {
    return this.http.put<CandidateAttachment>(`${this.apiUrl}`, value);
  }
}
