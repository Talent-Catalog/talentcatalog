import { Injectable } from '@angular/core';
import { Candidate } from '../model/candidate';
import { Observable } from 'rxjs/index';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { SearchResults } from '../model/search-results';
import {CandidateLanguage} from "../model/candidate-language";
import {Country} from "../model/country";

@Injectable({providedIn: 'root'})
export class CandidateLanguageService {

  private apiUrl = environment.apiUrl + '/candidate-language';

  constructor(private http:HttpClient) {}

  list(id: number): Observable<CandidateLanguage[]> {
    return this.http.get<CandidateLanguage[]>(`${this.apiUrl}/${id}/list`);
  }


}
