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
import {
  Candidate,
  CandidateIntakeData,
  CandidateOpportunityParams,
  UpdateCandidateListOppsRequest,
  UpdateCandidateMutedRequest,
  UpdateCandidateNotificationPreferenceRequest,
  UpdateCandidateOppsRequest,
  UpdateCandidateShareableDocsRequest,
  UpdateCandidateShareableNotesRequest,
  UpdateCandidateStatusRequest
} from '../model/candidate';
import {Observable, Subject} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from '../model/search-results';
import {map} from "rxjs/operators";
import {CandidateSource, FetchCandidatesWithChatRequest} from "../model/base";
import {IntakeService} from "../components/util/intake/IntakeService";
import {JobChatUserInfo} from "../model/chat";

export interface DownloadCVRequest {
  candidateId: number,
  showName: boolean,
  showContact: boolean
}

// If a completed date is provided, this intake is an external intake entered to the TC at a later date.
export interface IntakeAuditRequest {
  completedDate: Date,
  fullIntake: boolean
}

@Injectable({providedIn: 'root'})
export class CandidateService implements IntakeService {

  private candidateUpdatedSource = new Subject<Candidate>();

  private apiUrl = environment.apiUrl + '/candidate';

  constructor(private http: HttpClient) {}

  search(request): Observable<SearchResults<Candidate>> {
    return this.http.post<SearchResults<Candidate>>(`${this.apiUrl}/search`, request);
  }

  findByCandidateEmail(request): Observable<SearchResults<Candidate>> {
    return this.http.post<SearchResults<Candidate>>(`${this.apiUrl}/findbyemail`, request);
  }

  findByCandidateEmailPhoneOrWhatsapp(request): Observable<SearchResults<Candidate>> {
    return this.http.post<SearchResults<Candidate>>(`${this.apiUrl}/findbyemailphoneorwhatsapp`, request);
  }

  findByCandidateNumberOrName(request): Observable<SearchResults<Candidate>> {
    return this.http.post<SearchResults<Candidate>>(`${this.apiUrl}/findbynumberorname`, request);
  }

  findByExternalId(request): Observable<SearchResults<Candidate>> {
    return this.http.post<SearchResults<Candidate>>(`${this.apiUrl}/findbyexternalid`, request);
  }

  getByNumber(number: string): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/number/${number}`);
  }

  get(id: number): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/${id}`);
  }

  getIntakeData(id: number): Observable<CandidateIntakeData> {
    return this.http.get<CandidateIntakeData>(`${this.apiUrl}/${id}/intake`);
  }

  updateLinks(id: number, details): Observable<Candidate>  {
    return this.http.put<Candidate>(`${this.apiUrl}/${id}/links`, details);
  }

  updateNotificationPreference(id: number, request: UpdateCandidateNotificationPreferenceRequest):
    Observable<void>  {
    return this.http.put<void>(`${this.apiUrl}/${id}/notification`, request);
  }

  updateShareableNotes(
    id: number, request: UpdateCandidateShareableNotesRequest): Observable<Candidate> {
    return this.http.put<Candidate>(`${this.apiUrl}/${id}/shareable-notes`, request);
  }

  updateShareableDocs(
    id: number, request: UpdateCandidateShareableDocsRequest): Observable<Candidate> {
    return this.http.put<Candidate>(`${this.apiUrl}/${id}/shareable-docs`, request);
  }

  updateStatus(details: UpdateCandidateStatusRequest): Observable<void>  {
    return this.http.put<void>(`${this.apiUrl}/status`, details);
  }

  updateMuted(id: number, request: UpdateCandidateMutedRequest): Observable<void>  {
    return this.http.put<void>(`${this.apiUrl}/${id}/muted`, request);
  }

  updateInfo(id: number, details): Observable<Candidate>  {
    return this.http.put<Candidate>(`${this.apiUrl}/${id}/info`, details);
  }

  updateSurvey(id: number, details): Observable<Candidate>  {
    return this.http.put<Candidate>(`${this.apiUrl}/${id}/survey`, details);
  }

  updateMedia(id: number, details): Observable<Candidate>  {
    return this.http.put<Candidate>(`${this.apiUrl}/${id}/media`, details);
  }

  update(id: number, details): Observable<Candidate>  {
    return this.http.put<Candidate>(`${this.apiUrl}/${id}`, details);
  }

  updateMaxEducationLevel(id: number, details): Observable<Candidate>  {
    return this.http.put<Candidate>(`${this.apiUrl}/${id}/education`, details);
  }

  updateRegistration(id: number, details): Observable<Candidate>  {
    return this.http.put<Candidate>(`${this.apiUrl}/${id}/registration`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  export(request) {
    return this.http.post(`${this.apiUrl}/export/csv`, request, {responseType: 'blob'});
  }

  downloadCv(request: DownloadCVRequest) {
    return this.http.post(
      `${this.apiUrl}/${request.candidateId}/cv.pdf`, request, {responseType: 'blob'})
      .pipe(
        map(res => {
          return new Blob([res], { type: 'application/pdf', });
        })
      );
  }

  createCandidateFolder(candidateId: number): Observable<Candidate> {
    return this.http.put<Candidate>(
      `${this.apiUrl}/${candidateId}/create-folder`, null);
  }

  createUpdateLiveCandidate(candidateId: number): Observable<Candidate> {
    return this.http.put<Candidate>(
      `${this.apiUrl}/${candidateId}/update-live`, null);
  }

  createUpdateOppsFromCandidateList(source: CandidateSource,
                                    candidateOppParams: CandidateOpportunityParams): Observable<void> {

    const request: UpdateCandidateListOppsRequest = {
      savedListId: source.id,
      candidateOppParams: candidateOppParams
    }
    return this.http.put<void>(`${this.apiUrl}/update-opps-by-list`, request);
  }

  createUpdateOppsFromCandidates(
    candidateIds: number[], sfJobOpp: string, candidateOppParams: CandidateOpportunityParams): Observable<void> {

    const request: UpdateCandidateOppsRequest = {
      candidateIds: candidateIds,
      sfJobOppId: sfJobOpp,
      candidateOppParams: candidateOppParams
    }

    return this.http.put<void>(`${this.apiUrl}/update-opps`, request);
  }

  /**
   * Note that the sent data, formData, is not typed.
   * The data is copied across using the name of the form fields.
   * Those names must match field names in CandidateIntakeDataUpdate.java.
   * @param candidateId ID of candidate
   * @param formData form.value of an intake data form.
   */
  updateIntakeData(candidateId: number, formData: Object): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${candidateId}/intake`, formData);
  }

  completeIntake(candidateId: number, request: IntakeAuditRequest): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/${candidateId}/intake`, request);
  }

  resolveOutstandingTasks(details): Observable<void>  {
    return this.http.put<void>(`${this.apiUrl}/resolve-tasks`, details);
  }

  // As we are just returning the token as a string (not an object) we have to set the response type to text.
  // See explained here: https://angular.io/guide/http#requesting-non-json-data
  generateToken(cn: string, restrictCandidateOccupations: boolean, candidateOccupationIds: number[]) {
    return this.http.get(`${this.apiUrl}/token/${cn}`,
      {
        params:
        {
          restrictCandidateOccupations: restrictCandidateOccupations,
          candidateOccupationIds: candidateOccupationIds
        },
        responseType: 'text'
      });
  }

  checkUnreadChats(): Observable<JobChatUserInfo> {
    return this.http.post<JobChatUserInfo>(`${this.apiUrl}/check-unread-chats`, null);
  }

  fetchCandidatesWithChat(request: FetchCandidatesWithChatRequest):
    Observable<SearchResults<Candidate>> {
    return this.http.post<SearchResults<Candidate>>(
      `${this.apiUrl}/fetch-candidates-with-chat`, request
    );
  }

  // In the candidate-search-card we pass in the updated candidate object to merge with the extended candidate DTO,
  // this reduces an additional API call to fetch the updated extended candidate object. But in candidate profile we fetch
  // the updated object so we don't need the updated object, just need to refetch the current candidate.
  updateCandidate(candidate?: Candidate) {
    candidate ? this.candidateUpdatedSource.next(candidate) : this.candidateUpdatedSource.next();
  }

  candidateUpdated() {
    return this.candidateUpdatedSource.asObservable();
  }

  fetchPotentialDuplicates(id: number): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(
      `${this.apiUrl}/${id}/fetch-potential-duplicates-of-given-candidate`
    );
  }

}
