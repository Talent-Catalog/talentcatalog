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
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {DtoType, SearchPartnerRequest, Status} from "../model/base";
import {Partner, UpdatePartnerJobContactRequest, UpdatePartnerRequest} from "../model/partner";
import {Job} from "../model/job";

@Injectable({
  providedIn: 'root'
})
export class PartnerService {

  private apiUrl: string = environment.apiUrl + '/partner';

  constructor(private http: HttpClient) { }

  listPartners(): Observable<Partner[]> {
    return this.http.get<Partner[]>(`${this.apiUrl}`);
  }

  search(request: SearchPartnerRequest): Observable<Partner[]> {
    return this.http.post<Partner[]>(`${this.apiUrl}/search`, request);
  }

  searchPaged(request: SearchPartnerRequest): Observable<SearchResults<Partner>> {
    return this.http.post<SearchResults<Partner>>(`${this.apiUrl}/search-paged`, request);
  }

  create(request: UpdatePartnerRequest): Observable<Partner> {
    return this.http.post<Partner>(`${this.apiUrl}`, request);
  }

  listSourcePartners(jobContext?: Job): Observable<Partner[]> {
    const request: SearchPartnerRequest = {
      contextJobId: jobContext?.id,
      sourcePartner: true,
      status: Status.active,
      sortFields: ["name"],
      sortDirection: "ASC"
    }
    return this.search(request);
 }

  update(id: number, request: UpdatePartnerRequest): Observable<Partner>  {
    return this.http.put<Partner>(`${this.apiUrl}/${id}`, request);
  }

  updateJobContact(id: number, request: UpdatePartnerJobContactRequest): Observable<Partner>  {
    return this.http.put<Partner>(`${this.apiUrl}/${id}/update-job-contact`, request);
  }

  getPartner(id: number, dtoType: DtoType): Observable<Partner> {
    return this.http.get<Partner>(`${this.apiUrl}/${id}?dtoType=${dtoType}`);
  }
  
  updateAcceptedDpa(dpaId: string):
    Observable<Partner>  {
    return this.http.put<Partner>(`${this.apiUrl}/${dpaId}/accept-dpa`, null);
  }

  setFirstDpaSeen(): Observable<Partner>  {
    return this.http.put<Partner>(`${this.apiUrl}/dpa-seen`,null);
  }

  requiresDpaAcceptance(): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/requires-dpa`);
  }
}
