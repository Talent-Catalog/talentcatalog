import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateSource, Opportunity, UpdateEmployerOpportunityRequest} from "../model/base";

@Injectable({
  providedIn: 'root'
})
export class SalesforceService {
  private apiUrl: string = environment.apiUrl + '/sf';

  constructor(private http: HttpClient) { }

  /**
   * Converts a Salesforce opportunity id to a URL (link).
   * @param id Salesforce opportunity id
   * @return null if id is null, otherwise a url to the Salesforce opportunity record
   */
  sfOppToLink(id: string): string {
    const sfOpportunityLinkPrefix: string
      = "https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/";
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
