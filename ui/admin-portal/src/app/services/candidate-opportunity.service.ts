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
import {Injectable} from "@angular/core";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {CandidateOpportunity, SearchOpportunityRequest} from "../model/candidate-opportunity";
import {SearchResults} from "../model/search-results";
import {CandidateOpportunityParams} from "../model/candidate";
import {OpportunityService} from "../components/util/opportunity/OpportunityService";
import {JobChatUserInfo} from "../model/chat";

export interface UpdateRelocatingDependantIds {
  id?: number;
  relocatingDependantIds: number[];
}

@Injectable({
  providedIn: 'root'
})
export class CandidateOpportunityService implements OpportunityService<CandidateOpportunity> {

  private mocking = false;
  private mockOpp: CandidateOpportunity = {
    id: 234,
    name: 'Fred(12345)-Test opp',
    jobOpp: {
      id: 123,
      name: "Test job"
    },

    //Internally enums are stored by their keys. Override typing error. This is how Json maps
    //to Typescript enums
    // todo: I think this going to cause problems with getCandidateOpportunityStageName not always working
    //Maybe it needs to be smarter and be able to check whether it is being passed a string or an enum - or also looks for StringValues
    // @ts-ignore
    stage: "prospect",
    nextStep: "Come on get this guy hired",
    employerFeedback: "English is not great"
  }

  private mockOpps: CandidateOpportunity[] = [this.mockOpp];
  private mockSearchResults: SearchResults<CandidateOpportunity> = {
    number: 1,
    size: 1,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
    content: this.mockOpps
  }


  private apiUrl: string = environment.apiUrl + '/opp';

  constructor(private http: HttpClient) { }

  get(id: number): Observable<CandidateOpportunity> {
    let observable;
    if (this.mocking) {
      observable = of(this.mockOpp);
    } else {
      observable = this.http.get<CandidateOpportunity>(`${this.apiUrl}/${id}`);
    }
    return observable;
  }

  checkUnreadChats(request: SearchOpportunityRequest): Observable<JobChatUserInfo> {
    return this.http.post<JobChatUserInfo>(`${this.apiUrl}/check-unread-chats`, request);
  }

  searchPaged(request: SearchOpportunityRequest): Observable<SearchResults<CandidateOpportunity>> {
    let observable;
    if (this.mocking) {
      observable = of(this.mockSearchResults);
    } else {
      observable = this.http.post<SearchResults<CandidateOpportunity>>(`${this.apiUrl}/search-paged`, request);
    }
    return observable;
  }

  updateCandidateOpportunity(id: number, info: CandidateOpportunityParams): Observable <CandidateOpportunity> {
    let observable;
    if (this.mocking) {
      observable = of(this.mockOpp);
    } else {
      observable = this.http.put<CandidateOpportunity>(`${this.apiUrl}/${id}`, info);
    }
    return observable;
  }

  updateRelocatingDependants(id: number, request: UpdateRelocatingDependantIds): Observable <void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/relocating-dependants`, request);
  }

  uploadOffer(id: number, formData: FormData): Observable<CandidateOpportunity> {
    return this.http.post<CandidateOpportunity>(
      `${this.apiUrl}/${id}/upload-offer`, formData);

  }

  updateSfCaseRelocationInfo(id: number): Observable<void> {
    return this.http.put<void>(
      `${this.apiUrl}/${id}/update-sf-case-relocation-info`, null);
  }
}
