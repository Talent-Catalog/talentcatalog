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
import {CandidateSource, UpdateEmployerOpportunityRequest} from "../model/base";
import {Opportunity} from "../model/opportunity";
import {EnvService} from "./env.service";

@Injectable({
  providedIn: 'root'
})
export class SalesforceService {
  private apiUrl: string = environment.apiUrl + '/sf';

  constructor(private http: HttpClient, private envService: EnvService) { }

  /**
   * Converts a Salesforce opportunity id to a URL (link).
   * @param id Salesforce opportunity id
   * @return null if id is null, otherwise a url to the Salesforce opportunity record
   */
  sfOppToLink(id: string): string {
    const sfOpportunityLinkPrefix: string
      = this.envService.sfLightningUrl + "lightning/r/Opportunity/";
    const sfOpportunityLinkSuffix: string = "/view";

    return id == null ? null : sfOpportunityLinkPrefix + id + sfOpportunityLinkSuffix;

  }

  joblink(candidateSource: CandidateSource): string {
    const sfJobOpp = candidateSource == null ? null : candidateSource.sfJobOpp;
    return sfJobOpp == null ? null : this.sfOppToLink(sfJobOpp.sfId)
  }

  getOpportunity(sfUrl: string): Observable<Opportunity> {
    return this.http.get<Opportunity>(`${this.apiUrl}/opportunity`,
      {params: {"url": sfUrl}});
  }

  updateEmployerOpportunity(request: UpdateEmployerOpportunityRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/update-emp-opp`, request);
  }

}
