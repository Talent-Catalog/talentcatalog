import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateLanguage} from "../model/candidate-language";

@Injectable({providedIn: 'root'})
export class CandidateLanguageService {

  private apiUrl = environment.apiUrl + '/candidate-language';

  constructor(private http:HttpClient) {}

  list(id: number): Observable<CandidateLanguage[]> {
    return this.http.get<CandidateLanguage[]>(`${this.apiUrl}/${id}/list`);
  }

  update(id: number, details): Observable<CandidateLanguage>  {
    return this.http.put<CandidateLanguage>(`${this.apiUrl}/${id}`, details);
  }

}
