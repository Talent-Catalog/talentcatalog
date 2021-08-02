import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Candidate} from "../model/candidate";
import {Opportunity} from "../model/base";

@Injectable({
  providedIn: 'root'
})
export class SalesforceService {
  private apiUrl: string = environment.apiUrl + '/sf';

  constructor(private http: HttpClient) { }

  getOpportunity(sfUrl: string): Observable<Opportunity> {
    return this.http.get<Opportunity>(`${this.apiUrl}/opportunity`,
      {params: {"url": sfUrl}});
  }

}
