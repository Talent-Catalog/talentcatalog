import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Job, UpdateJobRequest} from "../model/job";

@Injectable({
  providedIn: 'root'
})
export class JobService {

  private apiUrl: string = environment.apiUrl + '/job';

  constructor(private http: HttpClient) { }


  create(request: UpdateJobRequest): Observable<Job> {
    return this.http.post<Job>(`${this.apiUrl}`, request);
  }

}
