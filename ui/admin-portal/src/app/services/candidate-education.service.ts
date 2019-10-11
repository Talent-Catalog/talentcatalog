import { Injectable } from '@angular/core';
import { Candidate } from '../model/candidate';
import { Observable } from 'rxjs/index';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { SearchResults } from '../model/search-results';
import {CandidateEducation} from "../model/candidate-education";
import {Country} from "../model/country";

@Injectable({providedIn: 'root'})
export class CandidateEducationService {

  private apiUrl = environment.apiUrl + '/candidate-education';

  constructor(private http:HttpClient) {}

  list(id: number): Observable<CandidateEducation[]> {
    return this.http.get<CandidateEducation[]>(`${this.apiUrl}/${id}/list`);
  }

  create(id: number, details): Observable<CandidateEducation>  {
    return this.http.post<CandidateEducation>(`${this.apiUrl}/${id}`, details);
  }

  update(id: number, details): Observable<CandidateEducation>  {
    return this.http.put<CandidateEducation>(`${this.apiUrl}/${id}`, details);
  }

}
