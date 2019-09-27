import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateJobExperience} from "../model/candidate-job-experience";

@Injectable({
  providedIn: 'root'
})
export class CandidateJobExperienceService {

  private apiUrl: string = environment.apiUrl + '/job-experience';

  constructor(private http: HttpClient) { }

  createJobExperience(request): Observable<CandidateJobExperience> {
    return this.http.post<CandidateJobExperience>(`${this.apiUrl}`, request);
  }

  deleteJobExperience(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
