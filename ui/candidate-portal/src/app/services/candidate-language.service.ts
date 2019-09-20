import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateLanguage} from "../model/candidate-language";

@Injectable({
  providedIn: 'root'
})
export class CandidateLanguageService {

  private apiUrl: string = environment.apiUrl + '/candidate-language';

  constructor(private http: HttpClient) { }

  createCandidateLanguage(request): Observable<CandidateLanguage> {
    return this.http.post<CandidateLanguage>(`${this.apiUrl}`, request);
  }

  deleteCandidateLanguage(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
