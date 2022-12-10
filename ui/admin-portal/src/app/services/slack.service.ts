import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {PostJobToSlackRequest, PostJobToSlackResponse} from "../model/base";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SlackService {
  private apiUrl: string = environment.apiUrl + '/slack';

  constructor(private http: HttpClient) { }

  postJob(request: PostJobToSlackRequest): Observable<PostJobToSlackResponse> {
    return this.http.post<PostJobToSlackResponse>(`${this.apiUrl}/post-job`, request);
  }

  postJobFromId(id: number): Observable<PostJobToSlackResponse> {
    return this.http.post<PostJobToSlackResponse>(`${this.apiUrl}/${id}/post-job`, null);
  }
}
