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
import {SearchResults} from "../model/search-results";
import {HelpLink, SearchHelpLinkRequest, UpdateHelpLinkRequest} from "../model/help-link";

@Injectable({
  providedIn: 'root'
})
export class HelpLinkService {
  private apiUrl: string = environment.apiUrl + '/help-link';

  constructor(private http: HttpClient) { }

  fetch(request: SearchHelpLinkRequest) {
    return this.http.post<HelpLink[]>(`${this.apiUrl}/fetch`, request);
  }

  search(request: SearchHelpLinkRequest): Observable<HelpLink[]> {
    return this.http.post<HelpLink[]>(`${this.apiUrl}/search`, request);
  }

  searchPaged(request: SearchHelpLinkRequest): Observable<SearchResults<HelpLink>> {
    return this.http.post<SearchResults<HelpLink>>(`${this.apiUrl}/search-paged`, request);
  }

  create(request: UpdateHelpLinkRequest): Observable<HelpLink> {
    return this.http.post<HelpLink>(`${this.apiUrl}`, request);
  }

  update(id: number, request: UpdateHelpLinkRequest): Observable<HelpLink>  {
    return this.http.put<HelpLink>(`${this.apiUrl}/${id}`, request);
  }
}
