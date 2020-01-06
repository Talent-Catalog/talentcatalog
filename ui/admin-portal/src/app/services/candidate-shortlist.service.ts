import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/index';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import {CandidateShortlistItem} from "../model/candidate-shortlist-item";

@Injectable({providedIn: 'root'})
export class CandidateShortlistService {

  private apiUrl = environment.apiUrl + '/candidate-shortlist';

  constructor(private http:HttpClient) {}

  get(id): Observable<CandidateShortlistItem>  {
    return this.http.get<CandidateShortlistItem>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<CandidateShortlistItem>  {
    return this.http.post<CandidateShortlistItem>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<CandidateShortlistItem>  {
    return this.http.put<CandidateShortlistItem>(`${this.apiUrl}/${id}`, details);
  }

}
