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
import {forkJoin, Observable, throwError} from 'rxjs';
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
import {Subject} from "rxjs/index";
import {Candidate} from "../model/candidate";

@Injectable({providedIn: 'root'})
export class CandidateAttachmentService {

  private apiUrl = environment.apiUrl + '/candidate-attachment';
  s3BucketUrl = environment.s3BucketUrl;

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

  getAttachmentUrl(candidate: Candidate, att: CandidateAttachment) {
    if (att.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + (att.migrated ? 'migrated' :
        candidate.candidateNumber) + '/' + att.location;
    }
    return att.location;
  }

  downloadAttachments(candidate: Candidate, ats: CandidateAttachment[]): Observable<string> {
    const downloadComplete = new Subject<string>();

    const downloads: Observable<any>[] = [];
    ats.forEach(cv => {
      if (cv.type === AttachmentType.googlefile) {
        downloads.push(this.downloadAttachment(cv.id, cv.name))
      } else {
        const newTab = window.open();
        const url = this.getAttachmentUrl(candidate, cv);
        newTab.location.href = url;
      }
    })

    if (downloads.length === 0) {
      downloadComplete.next();
      downloadComplete.complete();
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

  updateAttachment(value: any): Observable<CandidateAttachment> {
    return this.http.put<CandidateAttachment>(`${this.apiUrl}`, value);
  }
}
