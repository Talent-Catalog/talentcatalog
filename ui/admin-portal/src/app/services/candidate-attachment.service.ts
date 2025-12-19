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
import {forkJoin, Observable, of, Subject, throwError} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from '../model/search-results';
import {catchError, map} from "rxjs/operators";
import {
  AttachmentType,
  CandidateAttachment,
  CandidateAttachmentRequest
} from '../model/candidate-attachment';
import {saveBlob} from "../util/file";
import {Candidate} from "../model/candidate";
import {CvText} from "../model/cv-text";

export interface UpdateCandidateAttachmentRequest {
  id?: number;
  name?: string;
  location?: string;
  cv?: boolean;
}

export interface SearchCandidateAttachmentsRequest {
  candidateId: number;
  cvOnly: boolean;
}

export interface ListByUploadTypeRequest {
  candidateId: number;
  uploadType: string;
}

@Injectable({providedIn: 'root'})
export class CandidateAttachmentService {

  private apiUrl = environment.apiUrl + '/candidate-attachment';

  constructor(private http: HttpClient) {}

  /**
   * Fetch the text of a candidate's CVs.
   * @param candidateId Id of candidate
   */
  getCandidateCvText(candidateId: number): Observable<CvText[]> {
    return this.http.get<CvText[]>(`${this.apiUrl}/cv-text/${candidateId}`);
  }

  search(request: SearchCandidateAttachmentsRequest): Observable<CandidateAttachment[]> {
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

  downloadAttachments(candidate: Candidate, ats: CandidateAttachment[]): Observable<string> {
    const downloadComplete = new Subject<string>();

    const downloads: Observable<any>[] = [];
    ats.forEach(cv => {
      if (cv.type === AttachmentType.googlefile) {
        downloads.push(this.downloadAttachment(cv.id, cv.name))
      } else {
        const newTab = window.open();
        if (newTab) {
          const url = cv.url;
          newTab.location.href = url;  // Open URL in new tab
        } else {
          console.error(`Failed to open new tab for ${cv.url}`)
        }
      }
    })

    //Attempt any downloads needed.
    //When all are done (with or without error) signal that fact using the
    //downloadComplete Observable (which is returned to the caller).
    //Note that the "complete" method is also called on downloadComplete.
    //This will automatically unsubscribe any subscribers, avoiding memory leaks.
    //See https://stackoverflow.com/questions/55893962/do-i-need-to-unsubscribe-from-observable-of
    if (downloads.length === 0) {
      return of('Complete'); // Nothing left to manage in this case
    } else {
      forkJoin(...downloads).subscribe(
        (results: CandidateAttachment[]) => {
          downloadComplete.next();
          downloadComplete.complete();
        },
        error => {
          downloadComplete.next(error);
          downloadComplete.complete();
        })
    }

    return downloadComplete.asObservable();
  }

  uploadAttachment(id: number, cv: boolean, formData: FormData): Observable<CandidateAttachment> {
    return this.http.post<CandidateAttachment>(
      `${this.apiUrl}/${id}/upload?cv=${cv}`, formData);
  }

  updateAttachment(id: number, request: UpdateCandidateAttachmentRequest): Observable<CandidateAttachment> {
    return this.http.put<CandidateAttachment>(`${this.apiUrl}/${id}`, request);
  }

  listByType(request: ListByUploadTypeRequest): Observable<CandidateAttachment[]> {
    return this.http.post<CandidateAttachment[]>(`${this.apiUrl}/list-by-type`, request);
  }

  getMaxUploadFileSize() {
    return 10 * (1<<20); //10 Mb
  }
}
