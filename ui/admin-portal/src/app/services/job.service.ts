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
import {Job, JobDocType, SearchJobRequest, UpdateJobRequest} from "../model/job";
import {SearchResults} from "../model/search-results";
import {UpdateLinkRequest} from "../components/util/input/input-link/input-link.component";
import {OpportunityService} from "../components/util/opportunity/OpportunityService";
import {JobChatUserInfo} from "../model/chat";
import {SkillName} from "../model/skill";

@Injectable({
  providedIn: 'root'
})
export class JobService implements OpportunityService<Job> {

  private apiUrl: string = environment.apiUrl + '/job';

  constructor(private http: HttpClient) { }


  create(request: UpdateJobRequest): Observable<Job> {
    return this.http.post<Job>(`${this.apiUrl}`, request);
  }

  createSuggestedSearch(id: number, suffix: string): Observable<Job> {
    return this.http.post<Job>(`${this.apiUrl}/${id}/create-search`, suffix);
  }

  get(id: number): Observable<Job> {
    return this.http.get<Job>(`${this.apiUrl}/${id}`);
  }

  getSkills(id: number): Observable<SkillName[]> {
    return this.http.get<SkillName[]>(`${this.apiUrl}/${id}/skills`);
  }

  publishJob(id: number): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/publish`, null);
  }

  removeSuggestedSearch(id: number, savedSearchId: number): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/remove-search`, savedSearchId);
  }

  checkUnreadChats(request: SearchJobRequest): Observable<JobChatUserInfo> {
    return this.http.post<JobChatUserInfo>(`${this.apiUrl}/check-unread-chats`, request);
  }

  searchPaged(request: SearchJobRequest): Observable<SearchResults<Job>> {
    return this.http.post<SearchResults<Job>>(`${this.apiUrl}/search-paged`, request);
  }

  search(request: SearchJobRequest): Observable<Job[]> {
    return this.http.post<Job[]>(`${this.apiUrl}/search`, request);
  }

  update(id: number, request: UpdateJobRequest): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}`, request);
  }

  updateIntakeData(id: number, formData: Object): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/intake`, formData);
  }

  updateJobLink(id: number, docType: JobDocType, updateLinkRequest: UpdateLinkRequest): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/${docType}-link`, updateLinkRequest);
  }

  updateStarred(id: number, starred: boolean): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/starred`, starred);
  }

  updateSummary(id: number, summary: string): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/summary`, summary);
  }

  uploadJobDoc(id: number, docType: JobDocType, formData: FormData): Observable<Job> {
    return this.http.post<Job>(
      `${this.apiUrl}/${id}/upload/${docType}`, formData);

  }
}
