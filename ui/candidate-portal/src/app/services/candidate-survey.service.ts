import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateSurvey} from "../model/candidate-survey";

@Injectable({
  providedIn: 'root'
})
export class CandidateSurveyService {

  private apiUrl: string = environment.apiUrl + '/candidate-survey';

  constructor(private http: HttpClient) { }

  createCandidateSurvey(request): Observable<CandidateSurvey> {
    return this.http.post<CandidateSurvey>(`${this.apiUrl}`, request);
  }

  deleteCandidateSurvey(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
