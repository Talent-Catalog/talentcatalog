import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateEducation} from "../model/candidate-education";

@Injectable({
  providedIn: 'root'
})
export class CandidateEducationService {

  private apiUrl: string = environment.apiUrl + '/candidate-education';

  constructor(private http: HttpClient) { }

  createCandidateEducation(request): Observable<CandidateEducation> {
    return this.http.post<CandidateEducation>(`${this.apiUrl}`, request);
  }

  updateCandidateEducation(request): Observable<CandidateEducation> {
     return this.http.post<CandidateEducation>(`${this.apiUrl}/update`, request);
  }

}
