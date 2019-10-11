import { Injectable } from '@angular/core';
import { Candidate } from '../model/candidate';
import { Observable } from 'rxjs/index';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { SearchResults } from '../model/search-results';
import {CandidateCertification} from "../model/candidate-certification";
import {Country} from "../model/country";

@Injectable({providedIn: 'root'})
export class CandidateCertificationService {

  private apiUrl = environment.apiUrl + '/candidate-certification';

  constructor(private http:HttpClient) {}

  list(id: number): Observable<CandidateCertification[]> {
    return this.http.get<CandidateCertification[]>(`${this.apiUrl}/${id}/list`);
  }

  create(id: number, details): Observable<CandidateCertification>  {
    return this.http.post<CandidateCertification>(`${this.apiUrl}/${id}`, details);
  }

  update(id: number, details): Observable<CandidateCertification>  {
    return this.http.put<CandidateCertification>(`${this.apiUrl}/${id}`, details);
  }

}
