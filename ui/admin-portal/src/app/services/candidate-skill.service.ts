import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/index';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { SearchResults } from '../model/search-results';
import {CandidateSkill} from "../model/candidate-skill";

@Injectable({providedIn: 'root'})
export class CandidateSkillService {

  private apiUrl = environment.apiUrl + '/candidate-skill';

  constructor(private http:HttpClient) {}

  search(request): Observable<SearchResults<CandidateSkill>> {
    return this.http.post<SearchResults<CandidateSkill>>(`${this.apiUrl}/search`, request);
  }

}
