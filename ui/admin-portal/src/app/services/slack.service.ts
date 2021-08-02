import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {PostJobToSlackRequest, UpdateEmployerOpportunityRequest} from "../model/base";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SlackService {
  private apiUrl: string = environment.apiUrl + '/slack';

  constructor(private http: HttpClient) { }

  //todo could return link to Slack post
  postJob(request: PostJobToSlackRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/post-job`, request);
  }

}
