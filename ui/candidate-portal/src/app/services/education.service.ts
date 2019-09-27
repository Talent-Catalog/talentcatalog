import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateEducation} from "../model/candidate-education";

@Injectable({
  providedIn: 'root'
})
export class EducationService {

  private apiUrl: string = environment.apiUrl + '/education';

  constructor(private http: HttpClient) { }

  createEducation(request): Observable<CandidateEducation> {
    return this.http.post<CandidateEducation>(`${this.apiUrl}`, request);
  }

  updateEducation(request): Observable<CandidateEducation> {
     return this.http.post<CandidateEducation>(`${this.apiUrl}/update`, request);
  }

}
